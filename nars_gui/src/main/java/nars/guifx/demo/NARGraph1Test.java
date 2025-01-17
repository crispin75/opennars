package nars.guifx.demo;

import nars.Global;
import nars.NAR;
import nars.clock.FrameClock;
import nars.guifx.IOPane;
import nars.guifx.NARide;
import nars.guifx.graph2.ConceptsSource;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.guifx.graph2.scene.DefaultVis;
import nars.guifx.graph2.source.DefaultNARGraph;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.guifx.util.TabX;
import nars.nal.DerivationRules;
import nars.nar.Default;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    static {
        DerivationRules.maxVarArgsToMatch = 3;
    }

    public static SpaceGrapher newGraph(NAR n) {
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.8f;


        n.memory.conceptForgetDurations.set(8);
        n.memory.termLinkForgetDurations.set(12);
        n.memory.taskLinkForgetDurations.set(12);

        //n.input(new File("/tmp/h.nal"));
        n.input("<hydochloric --> acid>.");
        n.input("<#x-->base>. %0.65%");
        n.input("<neutralization --> (acid,base)>. %0.75;0.90%");
        //n.input("<(&&, <#x --> hydochloric>, eat:#x) --> nice>. %0.75;0.90%");
        //n.input("<(&&,a,b,ca)-->#x>?");

        //n.frame(5);


        SpaceGrapher<?,?> g = new DefaultNARGraph(

                new ConceptsSource(n),


                128,

                new DefaultVis(),


                new CanvasEdgeRenderer() {
                    @Override
                    protected final void clear(double w, double h) {
                        clearFade(w,h);
                    }
                });




        return g;
    }

    public static void main(String[] args)  {


        NAR n = new Default(512, 3,3,3, new FrameClock());

        NARide.show(n.loop(), ide -> {

            ide.content.getTabs().setAll(new TabX("Graph", newGraph(n)));
            ide.addView(new IOPane(n));


            //n.frame(5);

        });

//        NARfx.run((a,b)-> {
//            b.setScene(
//                new Scene(newGraph(n), 600, 600)
//            );
//            b.show();
//
//            n.spawnThread(250, x -> {
//
//            });
//        });





//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }

}
