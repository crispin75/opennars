package nars.concept;

import com.gs.collections.api.block.procedure.Procedure2;
import infinispan.com.google.common.collect.Iterators;
import javolution.util.function.Equality;
import nars.Memory;
import nars.budget.Budget;
import nars.nal.nal7.Temporal;
import nars.premise.Premise;
import nars.task.Task;
import nars.truth.Truth;
import nars.truth.TruthWave;
import nars.truth.Truthed;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;

import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal7.Temporal.solutionQuality;
import static nars.nal.nal7.Temporal.solutionQualityMatchingOrder;

/**
 * A model storing, ranking, and projecting beliefs or goals (tasks with TruthValue).
 * It should iterate in top-down order (highest ranking first)
 */
public interface BeliefTable extends TaskTable {

    /** main method */
    Task add(final Task input, BeliefTable.Ranker ranking, Concept c, Premise nal);

    Ranker BeliefConfidenceOrOriginality = (belief, bestToBeat) -> {
        final float confidence = belief.getTruth().getConfidence();
        final float originality = belief.getOriginality();
        return or(confidence, originality);
    };

    BeliefTable EMPTY = new BeliefTable() {

        @Override
        final public Iterator<Task> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public int getCapacity() {
            return 0;
        }

        @Override
        public void setCapacity(int newCapacity) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean isEmpty() {
            return true;
        }


        @Override
        public Task add(Task t, Equality<Task> equality, Procedure2<Budget, Budget> duplicateMerge, Memory m) {
            return null;
        }

        @Override
        public Task add(Task input, Ranker ranking, Concept c, Premise nal) {
            return null;
        }

        @Override
        public boolean tryAdd(Task input, Ranker r, Memory memory) {
            return false;
        }

        @Override
        public boolean add(Task t) {
            return false;
        }




        @Override
        public Task top(boolean eternal, boolean temporal) {
            return null;
        }
    };

    boolean tryAdd(Task input, BeliefTable.Ranker r, Memory memory);

    /** add a task to the table */
    boolean add(Task t);




//    /**
//     * projects to a new task at a given time
//     * was: getTask(q, now, getBeliefs()).  Does not affect the table itself */
//    public Task project(Task t, long now);

    /*default public Task project(final Task t) {
        return project(t, Stamp.TIMELESS);
    }*/


    /**
     * get a random belief, weighted by their sentences confidences
     */
    default Task getBeliefRandomByConfidence(boolean eternal, Random rng) {

        if (isEmpty()) return null;

        float totalConfidence = getConfidenceSum();
        float r = rng.nextFloat() * totalConfidence;


        for (Task x : this) {
            r -= x.getTruth().getConfidence();
            if (r < 0)
                return x;
        }

        return null;
    }


    default float getConfidenceSum() {
        return getConfidenceSum(this);
    }

    static float getConfidenceSum(Iterable<? extends Truthed> beliefs) {
        float t = 0;
        for (final Truthed s : beliefs)
            t += s.getTruth().getConfidence();
        return t;
    }

    static float getMeanFrequency(Collection<? extends Truthed> beliefs) {
        if (beliefs.isEmpty()) return 0.5f;

        float t = 0;
        for (final Truthed s : beliefs)
            t += s.getTruth().getFrequency();
        return t / beliefs.size();
    }

    default Task top(final Task query, final long now) {

        final Task top = top();
        if (top == null) return null;

        if (!Temporal.matchingOrder(query, top.getTerm())) {
            return null;
        }

        if (size() == 1)
            return top;

        return top(new SolutionQualityMatchingOrderRanker(query, now));
    }

    default float getConfidenceMax(float minFreq, float maxFreq) {
        float max = Float.NEGATIVE_INFINITY;

        for (Task t : this) {
            float f = t.getTruth().getFrequency();

            if ((f >= minFreq) && (f <= maxFreq)) {
                float c = t.getTruth().getConfidence();
                if (c > max)
                    max = c;
            }
        }

        if (max == -1) return Float.NaN;
        return max;
    }

    final class SolutionQualityMatchingOrderRanker implements Ranker {

        private final Task query;
        private final long now;
        final boolean hasQueryVar; //cache hasQueryVar

        public SolutionQualityMatchingOrderRanker(Task query, long now) {
            this.query = query;
            this.now = now;
            this.hasQueryVar = query.hasQueryVar();
        }

        @Override
        public final float rank(final Task t, final float bestToBeat) {
            if (t.equals(query)) return Float.NaN; //dont compare to self

            //TODO use bestToBeat to avoid extra work
            return solutionQualityMatchingOrder(query, t, now, hasQueryVar);
        }
    }

    default Task top(boolean hasQueryVar, final long now, long occTime, Truth truth) {

        if (isEmpty()) return null;

        return top((t, bestToBeat) -> solutionQuality(hasQueryVar, occTime, t, truth, now));
    }

//    /**
//     * Select a belief value or desire value for a given query
//     *
//     * @param query The query to be processed
//     * @param list  The list of beliefs or goals to be used
//     * @return The best candidate selected
//     */
//    public static Task getTask(final Sentence query, long now, final List<Task>... lists) {
//        float currentBest = 0;
//        float beliefQuality;
//        Task candidate = null;
//
//        for (List<Task> list : lists) {
//            if (list.isEmpty()) continue;
//
//            int lsv = list.size();
//            for (int i = 0; i < lsv; i++) {
//                Task judg = list.get(i);
//                beliefQuality = solutionQuality(query, judg.sentence, now);
//                if (beliefQuality > currentBest) {
//                    currentBest = beliefQuality;
//                    candidate = judg;
//                }
//            }
//        }
//
//        return candidate;
//    }



    /** get the top-ranking belief/goal, selecting either eternal or temporal beliefs, or both  */
    Task top(boolean eternal, boolean temporal);

    /** get the top-ranking belief/goal */
    default Task top() {
        return top(true, true);
    }

    /** the truth v alue of the topmost element, or null if there is none */
    default Truth topTruth() {
        if (isEmpty()) return null;
        return top().getTruth();
    }

    default void print(PrintStream out) {
        for (Task t : this) {
            System.out.println(t + " " + t.getLog());
        }
    }

    default TruthWave getWave() {
        return new TruthWave(this);
    }


    /** computes the truth/desire as an aggregate of projections of all
     * beliefs to current time
     */
    default float getMeanProjectedExpectation(final long time) {
        final int size = size();
        if (size == 0) return 0;

        final float[] d = {0};
        this.forEach(t -> d[0] += t.projectionTruthQuality(time, time, false) * t.getExpectation());

        final float dd = d[0];

        if (dd == 0) return 0;

        return dd / size;

    }

    @FunctionalInterface
    interface Ranker extends Function<Task,Float>, Serializable {
        /** returns a number producing a score or relevancy number for a given Task
         * @param bestToBeat current best score, which the ranking can use to decide to terminate early
         * @return a score value, or Float.MIN_VALUE to exclude that result
         * */
        float rank(Task t, float bestToBeat);


        default float rank(Task t) {
            return rank(t, Float.MIN_VALUE);
        }

        @Override default Float apply(Task t) {
            return rank(t);
        }

    }



    /** allowed to return null. must evaluate all items in case the final one is the
     *  only item that does not have disqualifying rank (MIN_VALUE)
     * */
    default Task top(Ranker r) {

        float s = Float.MIN_VALUE;
        Task b = null;

        for (Task t : this) {
            float x = r.rank(t, s);
            if (x > s) {
                s = x;
                b = t;
            }
        }

        return b;
    }


    /** TODO experimental and untested */
    class BeliefConfidenceAndCurrentTime implements Ranker {

        private final Concept concept;

        /** controls dropoff rate, measured in durations */
        float relevanceWindow = 0.9f;
        float temporalityFactor = 1f;



        public BeliefConfidenceAndCurrentTime(Concept c) {
            this.concept = c;
        }

        /** if returns c itself, this is a 1:1 linear mapping of confidence to starting
         * score before penalties applied. this could also be a curve to increase
         * or decrease the apparent relevance of certain confidence amounts.
         * @return value >=0, <=1
         */
        public float confidenceScore(final float c) {
            return c;
        }

        @Override
        public float rank(Task t, float bestToBeat) {
            float r = confidenceScore(t.getTruth().getConfidence());

            if (!Temporal.isEternal(t.getOccurrenceTime())) {

                final long now = concept.getMemory().time();
                float dur = t.getDuration();
                float durationsToNow = Math.abs(t.getOccurrenceTime() - now) / dur;


                //float agePenalty = (1f - 1f / (1f + (durationsToNow / relevanceWindow))) * temporalityFactor;
                float agePenalty = (durationsToNow / relevanceWindow) * temporalityFactor;
                r -= agePenalty; // * temporalityFactor;
            }

            float unoriginalityPenalty = 1f - t.getOriginality();
            r -= unoriginalityPenalty * 1;

            return r;
        }

    }


//    default public Task top(boolean eternal, boolean nonEternal) {
//
//    }




//    /** temporary until goal is separated into goalEternal, goalTemporal */
//    @Deprecated default public Task getStrongestTask(final List<Task> table, final boolean eternal, final boolean temporal) {
//        for (Task t : table) {
//            boolean e = t.isEternal();
//            if (e && eternal) return t;
//            if (!e && temporal) return t;
//        }
//        return null;
//    }
//
//    public static Sentence getStrongestSentence(List<Task> table) {
//        Task t = getStrongestTask(table);
//        if (t!=null) return t.sentence;
//        return null;
//    }
//
//    public static Task getStrongestTask(List<Task> table) {
//        if (table == null) return null;
//        if (table.isEmpty()) return null;
//        return table.get(0);
//    }

//    /**
//     * Determine the rank of a judgment by its quality and originality (stamp
//     * baseLength), called from Concept
//     *
//     * @param s The judgment to be ranked
//     * @return The rank of the judgment, according to truth value only
//     */
    /*public float rank(final Task s, final long now) {
        return rankBeliefConfidenceTime(s, now);
    }*/


//    public Sentence getSentence(final Sentence query, long now, final List<Task>... lists) {
//        Task t = getTask(query, now, lists);
//        if (t == null) return null;
//        return t.sentence;
//    }
}
