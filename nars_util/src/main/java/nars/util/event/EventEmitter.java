
package nars.util.event;

import nars.util.data.list.FasterList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * TODO separate this into a single-thread and multithread implementation
 */
abstract public class EventEmitter<K,V>  {


    public interface EventRegistration {
        public void off();
    }

//    /** more sophisticated event emitter which uses reactor.io */
//    public static class ReactorEventEmitter<E> extends EventEmitter<E> {
//
//
//        public final Reactor r;
//
//        public static class ReactorEventRegistration implements EventRegistration {
//
//            private final Registration registration;
//
//            ReactorEventRegistration(Registration r) {
//                this.registration = r;
//            }
//
//            @Override
//            public void off() {
//                registration.cancel();
//            }
//        }
//
//        public ReactorEventEmitter() {
//            this(Reactors.reactor().synchronousDispatcher().broadcastEventRouting().get());
//        }
//
//        public ReactorEventEmitter(Reactor r) {
//            this.r = r;
//
//            if (Global.DEBUG_TRACE_EVENTS) {
//            /*
//            System.out.println(r);
//            System.out.println(r.getDispatcher());
//            System.out.println(r.getRouter());
//            */
//
//                r.on(Selectors.matchAll(), x -> {
//                    System.out.println(x + " --> " +
//                            r.getConsumerRegistry().select(x.getKey()).size());
//                });
//            }
//        /*
//        r.on(T(Throwable.class), t -> {
//            Throwable e = (Throwable)t.getData();
//            //if (Parameters.DEBUG) {
//                if (e.getCause()!=null)
//                    e.getCause().printStackTrace();
//                else
//                    e.printStackTrace();
//
//                //throw new RuntimeException(e);
//            //}
//
//            //else {
//            //    System.err.println(e);
//            }///
//
//            //throw new RuntimeException(e.getCause());
//
//        });*/
//
//        }
//
//
//
//    /*public static Eventer newWorkQueue() {
//        return new Eventer(Reactors.reactor(new Environment(), Environment.WORK_QUEUE));
//    }*/
//
//        @Override
//        public void cycle() {
//        }
//
//
//
//        /** new Eventer with a dispatcher mode that runs in the same thread */
//        public static ReactorEventEmitter newSynchronous() {
//            return new ReactorEventEmitter(Reactors.reactor().env(new Environment()).synchronousDispatcher().firstEventRouting().get());
//        }
//
//        public void synch() {
//            r.getDispatcher().awaitAndShutdown();
//        }
//
//        public void shutdown() {
//            r.getDispatcher().shutdown();
//        }
//
//        public <X extends Event<?>> Registration on(Selector s, Consumer<X> c) {
//            return r.on(s, c);
//        }
//
//        @Override
//        public void notify(final Class channel, final Object[] arg) {
//            r.notify(channel, Event.wrap(arg));
//        }
//
//        public void notify(Object channel) {
//            r.notify(channel);
//        }
//
//        public void fire(Object event) {
//            r.notify(Event.wrap(event));
//        }
//
//        public void fire(Object channel, Event event) {
//            r.notify(channel, event);
//        }
//
//
//        @Override
//        public final boolean isActive(final Class event) {
//            return !r.getConsumerRegistry().select(event).isEmpty();
//        }
//
//        @Override
//        public EventRegistration on(Class<?> channel, Reaction obs) {
//
//            return new ReactorEventRegistration(on(Selectors.T(channel), obs));
//        }
//
//        Registration on(Selector s, Reaction obs) {
//            return on(s,  new Consumer<Event>() {
//                @Override public void accept(Event event) {
//                    try {
//                        Class channel = (Class) (event.getKey());
//                        Object o = event.getData();
//                        obs.event(channel, (Object[]) o);
//                    }
//                    catch (Throwable t) {
//                        if (Global.DEBUG) {
//                            t.printStackTrace();
//                        }
//                        emit(Events.ERR.class, t);
//                    }
//                }
//            });
//        }
//    }

 //   abstract public void forEachReaction(Consumer<Reaction> c);


    /** single-thread synchronous (in-thread) event emitter with direct array access
     * NOT WORKING YET
     * */
    public static class DefaultEventEmitter<K,V> extends EventEmitter<K,V> {

        final Map<K,ArraySharingList<Reaction<K,V>>> reactions = new HashMap(64);

        final Function<K, ArraySharingList<Reaction<K,V>>> getNewChannel = k -> { return newChannelList(); };


//        @Override
//        public void forEachReaction(Consumer<Reaction> c) {
//            for (List<Reaction<K, V>> reactionList : reactions.values()) {
//                reactionList.forEach(c);
//            }
//        }

        @Override
        public String toString() {
            return reactions.toString();
        }


        public class DefaultEventRegistration implements EventRegistration {

            final K key;
            final Reaction<K,V> reaction;

            DefaultEventRegistration(K key, Reaction<K,V> o) {
                this.key= key;
                this.reaction = o;
            }

            @Override
            public void off() {
                ArraySharingList<Reaction<K, V>> r = reactions.get(key);
                if (r!=null) {
                    r.remove(reaction);
                    if (r.isEmpty())
                        reactions.remove(key);
                }


            }
        }



        @Override
        final public int emit(final K channel, final V arg) {
            ArraySharingList<Reaction<K, V>> r = reactions.get(channel);
            if (r == null) return 0;
            final Reaction<K, V>[] c = r.getCachedNullTerminatedArray();
            if (c == null) return 0;
            int i;
            for (i = 0; ; i++) {
                final Reaction<K, V> cc = c[i];
                if (cc == null) break;
                cc.event(channel, arg);
            }
            return i;
        }

        @Override
        public EventRegistration on(K channel, Reaction<K,V> o) {
            DefaultEventRegistration d = new DefaultEventRegistration(channel, o);

            ArraySharingList<Reaction<K,V>> cl = reactions.computeIfAbsent(channel, getNewChannel);
            cl.add(o);

            return d;
        }

        protected ArraySharingList<Reaction<K,V>> newChannelList() {
            return new ArraySharingList<Reaction<K,V>>(
                    r -> new Reaction[r]
            );
        }

        @Override
        public void delete() {
            reactions.clear();
        }

        @Override
        public boolean isActive(K event) {
            return reactions.containsKey(event);
        }
    }

//    /** uses DirectCopyOnWriteArrayList for direct access to its array, for
//     * fast non-Lambda, non-Itrator iteration
//     * NOT WORKING YET
//     * */
//    public static class FastDefaultEventEmitter<K> extends DefaultEventEmitter<K,DirectCopyOnWriteArrayList<Reaction<K>>> {
//
//
//        static final private Reaction[] nullreactionlist = null;
//
//        @Override
//        public DirectCopyOnWriteArrayList<Reaction<K>> all(K c) {
//            return reactions.get(c);
//        }
//
//        @Override
//        public void notify(final K channel, final Object... arg) {
//
//            final DirectCopyOnWriteArrayList<Reaction<K>> c = all(channel);
//            if (c != null) {
//                for (final Reaction<K> x : c.getArray()) {
//                    x.event(channel, arg);
//                }
//            }
//
//        }
//
//        @Override
//        protected List<Reaction<K>> newChannelList() {
//            return new DirectCopyOnWriteArrayList<Reaction<K>>(Reaction.class);
//        }
//    }
//
    public abstract int emit(K channel, V arg);
//
    abstract public EventRegistration on(K k, Reaction<K,V> o);
//
    abstract public boolean isActive(final K event);
//
//
//    /** for enabling many events at the same time */
    @Deprecated public void set(final Reaction<K,V> o, final boolean enable, final K... events) {

        for (final K c : events) {
            if (enable)
                on(c, o);
            else
                off(c, o);
        }
    }


    public static class Registrations extends FasterList<EventRegistration> {

        Registrations(int length) {
            super(length);
        }

//        public void resume() {
//            for (Registration r : this)
//                r.resume();
//        }
//        public void pause() {
//            for (Registration r : this)
//                r.pause();
//        }
//        public void cancelAfterUse() {
//            for (Registration r : this)
//                r.cancelAfterUse();
//        }

        public synchronized void off() {
            for (int i = 0; i < this.size(); i++) {
                this.get(i).off();
            }
            clear();
        }
        
    }


    
    public Registrations on(final Reaction<K,V> o, final K... events) {
        Registrations r = new Registrations(events.length);
    
        for (final K c : events)
            r.add( on(c, o) );
        
        return r;
    }

//
//    @Override
//    @Deprecated public void emit(Class channel, Object arg) {
//
//        if (!(arg instanceof Object[]))
//            super.emit(channel, new Object[] { arg });
//        else
//            super.emit(channel, arg);
//    }


    public void emit(final K channel) {
        emit(channel, null);
    }



    
            
 
    /**
     * @param event
     * @param o
     * @return  whether it was removed
     */
    @Deprecated public void off(final K event, final Reaction<K,V> o) {
        throw new RuntimeException("off() not supported; use the returned Registration object to .cancel()");
    }

    public void delete() {

    }


}