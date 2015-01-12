/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.NARPrologMirror;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.ExampleFileInput;
import nars.language.Term;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class NARPrologMirrorTest {
    
    boolean prologAnswered = false;
    
    public static void main(String[] args) throws Exception {
        new NARPrologMirrorTest().testMultistep();
    }
    
    @Test
    public void testMultistep() throws Exception {
        boolean prolog = true;
        //boolean showOutput = false;
        Parameters.DEBUG = true;

        NAR nar = new NAR( new Default().setInternalExperience(null) );

        NARPrologMirror p = new NARPrologMirror(nar, 0.1f, true, true, true) {

            @Override
            protected void onQuestion(Sentence s) {
                super.onQuestion(s);
                System.err.println("QUESTION: " + s);
            }
            
            
            
            @Override
            public Term answer(Task question, Term t, nars.prolog.Term pt) {
                Term r = super.answer(question, t, pt);

                //look for <a --> d> answer
                //if (t.equals(aInhd))
                prologAnswered = true;
                assertTrue(true);
                
                return r;
            }
            
            
        };        
        
        
        //nal1.multistep.nal
        NALPerformance nts = new NALPerformance(nar, ExampleFileInput.get("../nal/test/nars_multistep_1.nal").getSource(), 3500) {
//            
//            
//            @Override
//            public NAR newNAR() {
//
//                Term aInhd;
//                try {
//                    aInhd = new Narsese(nar).parseTerm("<a --> d>");
//                } catch (Narsese.InvalidInputException ex) {
//                    assertTrue(false);
//                    return null;
//                }
//                
//                if (prolog) {
//
//                }
//                
//                return nar;
//            }
//          
            
        };

        
        
        nts.run();
        
        assertTrue(prologAnswered);
        
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-MultiStep-edited.txt")));
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-NAL1-edited.txt")));
//        nar.addInput(new TextInput(new File("nal/test/nal1.multistep.nal")));
//        nar.finish(10);
        
        
    }    
    
}
