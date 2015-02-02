package nars.util;

import nars.core.NAR;
import nars.build.Default;
import nars.util.graph.DefaultGraphizer;
import nars.util.graph.NARGraph;
import org.junit.Test;

import static nars.util.graph.NARGraph.IncludeEverything;



public class TestNARGraph {
    
    @Test
    public void testGraph() {

        NAR n = new NAR(new Default());
        
        n.addInput("<a --> b>.");
        
        n.run(2);
        
        //System.out.println(n);

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new DefaultGraphizer(true,true,true,true,0,true,true));
        
        //System.out.println(g);
        
        assert(g.vertexSet().size() > 0);
        assert(g.edgeSet().size() > 0);
    }
}