package nars.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.extensions.PerMessageDeflateHandshake;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import nars.Memory;
import nars.NAR;
import nars.bag.impl.InfiniCacheBag;
import nars.clock.RealtimeMSClock;
import nars.nar.Default;
import nars.util.NARLoop;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.db.InfiniPeer;
import nars.util.io.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;


public class NARServer extends PathHandler {


    public final NAR nar;

    public final Undertow server;
    public NARLoop loop;

    long idleFPS = 7 /* low alpha brainwaves */;


    public class WebSocketCore extends AbstractReceiveListener implements WebSocketCallback<Void>, WebSocketConnectionCallback {


        public WebSocketCore() {
            super();
        }


        public HttpHandler get() {
            return websocket(this).addExtension(new PerMessageDeflateHandshake());
        }


        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel socket) {

            /*if (log.isInfoEnabled())
                log.info(socket.getPeerAddress() + " connected websocket");*/

            socket.getReceiveSetter().set(this);
            socket.resumeReceives();

            /*Topic.all(nar.memory(), (k, v) -> {
                send(socket, k + ":" + v);
            });*/



            nar.memory.eventInput.on(t -> send(socket,
                    " IN: " + t));
            nar.memory.eventDerived.on(t -> send(socket,
                    "DER: " + t));
            nar.memory.eventAnswer.on(t -> send(socket,
                    "ANS: " + t));
            nar.memory.eventExecute.on(t -> send(socket,
                    "EXE: " + t));
            nar.memory.eventError.on(t -> send(socket,
                    "ERR: " + t));

//            textOutput = new TextOutput(nar) {
//
//
//                @Override
//                protected boolean output(final Channel channel, final Class event, final Object... args) {
//
//                    final String prefix = channel.getLinePrefix(event, args);
//                    final CharSequence s = channel.get(event, args);
//
//                    if (s != null) {
//                        return output(prefix, s);
//                    }
//
//
//                    return false;
//                }
//
//                @Override
//                protected boolean output(String prefix, CharSequence s) {
//                    send(socket, prefix + ": " + s);
//                    return true;
//                }
//            };

        }

        @Override
        protected void onClose(WebSocketChannel socket, StreamSourceFrameChannel channel) throws IOException {

//            if (textOutput!=null) {
//                textOutput.stop();
//                textOutput=null;
//            }

            /*if (log.isInfoEnabled())
                log.info(socket.getPeerAddress() + " disconnected websocket");*/
        }


        @Override
        protected void onFullTextMessage(WebSocketChannel socket, BufferedTextMessage message) throws IOException {

            nar.input(message.getData());

//            if (attemptJSONParseOfText) {
//                try {
//                    System.out.println(socket + " recv txt: " + message.getData());
//                    //JsonNode j = Core.json.readValue(message.getData(), JsonNode.class);
//                    //onJSONMessage(socket, j);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }


        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {

            //System.out.println(channel + " recv bin: " + message.getData());
        }


        public void send(WebSocketChannel socket, Object object) {
            try {

                ByteBuffer data = ByteBuffer.wrap(JSON.omDeep.writeValueAsBytes(object));

                WebSockets.sendText(data, socket, this);


            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
        }

//    @Override
//    public void complete(WebSocketChannel wsc, Void t) {
//        //System.out.println("Sent: " + wsc);
//    }

        @Override
        public void onError(WebSocketChannel wsc, Void t, Throwable thrwbl) {
            //System.out.println("Error: " + thrwbl);
        }

        @Override
        public void complete(WebSocketChannel channel, Void context) {
            //log.info("Complete: " + channel);
        }
    }

    public NARServer(NAR nar, int httpPort) throws IOException {
        super();

        this.nar = nar;

        //websockets = new NARSWebSocketServer(new InetSocketAddress(webSocketsPort));
        //websockets.start();

        //TODO use resource path
        String clientPath = "./nars_web/src/main/web";
        File c = new File(clientPath);

        //https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/sessionhandling/SessionServer.java
        addPrefixPath("/", resource(
                new FileResourceManager(c, 100)).
                setDirectoryListingEnabled(false));
        addPrefixPath("/ws", new WebSocketCore().get());


        server = Undertow.builder()
                .addHttpListener(httpPort, "localhost")
                .setIoThreads(2)
                .setHandler(this)
                .build();


    }


    public void start() {

        synchronized (server) {
            if (loop == null) {
                System.out.println("starting");
                //narThread.start();
                //TextOutput.out(nar).setShowInput(false);
                server.start();

                loop = nar.loop(idleFPS);
            }
        }

    }

    public void stop() {
        synchronized (server) {
            try {
                loop.waitForTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            server.stop();
        }
    }

    public static void main(String[] args) throws Exception {


        NAR nar = new Default(
                new Memory(
                        new RealtimeMSClock(),
                        new XorShift1024StarRandom(1),
                        new InfiniCacheBag(
                                InfiniPeer.tmp().the("default")
                        )),
                1024,
                1, 2, 3
        );


        nar.memory.concepts.forEach(c -> System.out.println(c));

        int httpPort;

        if (args.length < 1) {
            //System.out.println("Usage: NARServer <httpPort>");
            httpPort = 8080;
        } else {
            httpPort = Integer.parseInt(args[0]);


        }

        NARServer s = new NARServer(nar, httpPort);

        System.out.println("NARS Web Server ready. port: " + httpPort);
        /*if (nlp!=null) {
            System.out.println("  NLP enabled, using: " + nlpHost + ":" + nlpPort);
        }*/



        s.start();
    }


    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    /*public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }*/


}
