package nars.task.filter;

import nars.concept.Concept;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.Truth;

import java.util.Arrays;

/**
 * Prevent a duplicate belief from entering the system again
 */
public class FilterDuplicateExistingBelief { //implements DerivationFilter {

//    public final static String DUPLICATE = "DuplicateExistingBelief";
//
//
//
//    @Override public final String reject(final Premise nal, final Task task, final boolean solution, final boolean revised) {
//
//
//        //only process non-solution judgments
//        if (solution || !task.isJudgment())
//            return VALID;
//
//        return isUniqueBelief(nal, task) ? VALID : DUPLICATE;
//    }

    public static boolean isUniqueBelief(Premise nal, Task t) {
        return isUniqueBelief(nal, t.getTerm(), t.getTruth(), t.getOccurrenceTime(), t.getEvidence());
    }

    public static boolean isUniqueBelief(Premise nal, Compound taskTerm, Truth taskTruth, long taskOccurrrence, long[] taskEvidence) {


        //equality:
        //  1. term (given because it is looking up in concept)
        //  2. truth
        //  3. occurrence time
        //  4. evidential set

        final Concept c = nal.concept(taskTerm);
        if ((c == null) || //concept doesnt even exist so this is not a duplciate of anything
                (!c.hasBeliefs())) //no beliefs exist at this concept
            return true;

        for (Task t : c.getBeliefs()) {

            final Truth tt = t.getTruth();

            if (

                    //different truth value
                    (!tt.equals(taskTruth))
                            ||

                    //differnt occurence time
                    (t.getOccurrenceTime()!=taskOccurrrence)
                            ||

                    //differnt evidence
                    (!Arrays.equals(t.getEvidence(), taskEvidence))

                )
                return true;
        }

        return false;
    }

}
