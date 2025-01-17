//package nars.bag;
//
//import nars.Global;
//import nars.io.in.LibraryInput;
//import nars.event.AnswerReaction;
//import nars.meter.TestNAR;
//import nars.nar.experimental.Solid;
//import nars.task.Sentence;
//import nars.task.Task;
//import nars.term.Term;
//import org.junit.Test;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static org.junit.Assert.assertTrue;
//
//
//public class SolidTest {
//
//    @Test
//    public void testDetective() throws Exception {
//
//        int time = 256; //should solve the example in few cycles
//
//
//        Global.DEBUG = false;
//
//        final int numConcepts = 96;
//        Solid s = new Solid(1, numConcepts, 1, 1, 1, 3) {
//
//        };
//
//
//        //s.setMaxTasksPerCycle(numConcepts);
//
//        TestNAR n = new TestNAR(s);
//        n.memory.reset(1);
//
//        n.memory.nal(6);
//        n.param.conceptActivationFactor.set(0.15f);
//
//        //TextOutput.out(n).setOutputPriorityMin(0f);
//
//        Set<Term> solutionTerms = new HashSet();
//        Set<Sentence> solutions = new HashSet();
//
//
//        new AnswerReaction(n) {
//
//            @Override
//            public void onSolution(Task belief) {
//                solutions.add(belief);
//                solutionTerms.add(belief.getTerm());
//                if ((solutionTerms.size() >= 2) && (solutions.size() >= 2)) {
//                    n.stop();
//                }
//            }
//
//        };
//
//        n.input(LibraryInput.get(n, "app/detective.nal"));
//
//        for (int i = 0; i < time;  i++) {
//            n.frame(1);
//            if (solutionTerms.size() >= 2)
//                break;
//            //System.out.println("time=" + n.time() + " " + solutionTerms.size());
//            if (solutionTerms.size() >= 2)
//                break;
//        }
//
//        //n.memory.concepts.forEach(x -> System.out.println(x.getPriority() + " " + x));
//
//
//        //System.out.println(solutions);
//        assertTrue("at least 2 unique solutions: " + solutions.toString(), 2 <= solutions.size());
//        assertTrue("at least 2 unique terms: " + solutionTerms.toString(), 2 <= solutionTerms.size());
//
//    }
//
//
//        /*
//        n.input("<a --> b>. %1.00;0.90%\n" +
//                "<b --> c>. %1.00;0.90%\n"+
//                "<c --> d>. %1.00;0.90%\n" +
//                "<a --> d>?");*/
//        //''outputMustContain('<a --> d>. %1.00;0.27%')
//
//
//}
