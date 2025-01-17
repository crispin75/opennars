package nars.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.Item;
import nars.nal.nal8.Operation;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.TermMetadata;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.data.Util;
import nars.util.data.array.LongArrays;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static nars.Global.dereference;
import static nars.Global.reference;

/**
 * Mutable
 */
@JsonSerialize(using = ToStringSerializer.class)
public class DefaultTask<T extends Compound> extends Item<Sentence<T>> implements Task<T>, Serializable, JsonSerializable {

    /**
     * The punctuation also indicates the type of the Sentence:
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    private char punctuation;
    /**
     * Task from which the Task is derived, or null if input
     */
    transient private Reference<Task> parentTask; //should this be transient? we may want a Special kind of Reference that includes at least the parent's Term
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    transient private Reference<Task> parentBelief;

    public Truth truth;
    protected T term;
    transient private int hash;
    private long[] evidentialSet = LongArrays.EMPTY_ARRAY;
    long creationTime = Stamp.TIMELESS;
    long occurrenceTime = Stamp.ETERNAL;
    int duration = 0;


    /**
     * TODO move to SolutionTask subclass
     * For Question and Goal: best solution found so far
     */
    transient private Reference<Task> bestSolution;

    /**
     * TODO move to DesiredTask subclass
     * causal factor if executed; an instance of Operation
     */
    private Operation cause;

    private List<String> log = null;

    /**
     * indicates this Task can be used in Temporal induction
     */
    private boolean temporallyInductable = true;

    public DefaultTask(T term, final char punctuation, final Truth truth, final Budget bv, final Task parentTask, final Task parentBelief, final Task solution) {
        this(term, punctuation, truth,
                bv.getPriority(),
                bv.getDurability(),
                bv.getQuality(),
                parentTask, parentBelief,
                solution);
    }

    public DefaultTask(T term, final char punc, final Truth truth, final float p, final float d, final float q) {
        this(term, punc, truth, p, d, q, (Task) null, null, null);
    }

    public DefaultTask(T term, final char punc, final Truth truth, final float p, final float d, final float q, final Task parentTask, final Task parentBelief, final Task solution) {
        this(term, punc, truth,
                p, d, q,
                Global.reference(parentTask),
                reference(parentBelief),
                reference(solution)
        );
    }

    /** clone constructor */
    public DefaultTask(Task<T> task) {
        this(task.getTerm(), task.getPunctuation(), task.getTruth(),
                task.getPriority(), task.getDuration(), task.getQuality(),
                task.getParentTaskRef(), task.getParentBeliefRef(), task.getBestSolutionRef());

    }


    protected void setTerm(T t) {
        //if (Global.DEBUG) {
        if (Sentence.invalidSentenceTerm(t)) {
            throw new RuntimeException("Invalid sentence content term: " + t);
        }
        //}

        term = t;
    }


    public DefaultTask(T term, final char punctuation, final Truth truth, final float p, final float d, final float q, final Reference<Task> parentTask, final Reference<Task> parentBelief, final Reference<Task> solution) {
        super(p, d, q);
        this.truth = truth;
        this.punctuation = punctuation;
        this.term = term;
        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
        this.bestSolution = solution;
    }



    protected final void setPunctuation(char punctuation) {
        this.punctuation = punctuation;
    }

    /** includes: evidentialset, occurrencetime, truth, term, punctuation */
    private final int rehash() {

        final int h = Util.hash(
                Arrays.hashCode(getEvidence()),
                (int)getOccurrenceTime(),
                getTerm().hashCode(),
                getPunctuation(),
                (getTruth() != null) ? getTruth().hashCode() : 0
        );

        if (h == 0) return 1; //reserve 0 for non-hashed

        return h;
    }

    @Override
    public void setTermShared(final T equivalentInstance) {

        //intermval generally contains unique information that should not be replaced
        if (this.term instanceof TermMetadata)
            return;

        //if debug, check that they are equal..

        this.term = equivalentInstance;
    }

    @Override
    public T getTerm() {
        return term;
    }

    @Override
    public Truth getTruth() {
        return truth;
    }

    @Override
    public void setTruth(Truth t) {
        if (!Objects.equals(this.truth, t)) {
            this.truth = t;
            invalidate();
        }
    }



    @Override
    public Task<T> setEvidence(final long... evidentialSet) {
        this.evidentialSet = evidentialSet;
        invalidate();
        return this;
    }

    @Override
    final public boolean isDouble() {
        return getParentBelief() != null && getParentTask() != null;
    }
    @Override
    final public boolean isSingle() {
        return getParentBelief()==null && getParentTask()!=null ;
    }

    @Override
    public Task log(List<String> historyToCopy) {
        if (!Global.DEBUG_TASK_LOG)
            return this;

        if (historyToCopy != null) {
            if (this.log == null) this.log = Global.newArrayList(historyToCopy.size());
            log.addAll(historyToCopy);
        }
        return this;
    }

    @Override
    public final char getPunctuation() {
        return punctuation;
    }

    @Override
    public final long[] getEvidence() {
        return evidentialSet;
    }

    @Override
    public final long getCreationTime() {
        return creationTime;
    }

    @Override
    public final long getOccurrenceTime() {
        return occurrenceTime;
    }

    @Override
    public final int getDuration() {
        return duration;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;

        Task t = (Task)o;
        int tc;
        tc = term.compareTo(t.getTerm());
        if (tc != 0) return tc;
        tc = Character.compare(punctuation, t.getPunctuation());
        if (tc != 0) return tc;

        if (truth!=null) {
            tc = truth.toString().compareTo( t.getTruth().toString());
            if (tc != 0) return tc;
        }

        tc = Long.compare( getOccurrenceTime(), t.getOccurrenceTime() );
        if (tc!=0) return tc;

        long[] e1 = getEvidence();
        long[] e2 = t.getEvidence();
        tc = Integer.compare(e1.length,e2.length);
        if (tc!=0) return tc;

        for (int i = 0; i < e1.length; i++) {
            tc = Long.compare(e1[i],e2[i]);
            if (tc!=0) return tc;
        }

        //TODO merge contents for instance sharing?

        return 0;
    }

    @Override
    public final Sentence<T> setCreationTime(final long creationTime) {
        if ((this.creationTime <= Stamp.TIMELESS) && (this.occurrenceTime > Stamp.TIMELESS)) {
            //use the occurrence time as the delta, now that this has a "finite" creationTime
            setOccurrenceTime(this.occurrenceTime + creationTime);
        }
        //if (this.creationTime != creationTime) {
        this.creationTime = creationTime;
            //does not need invalidated since creation time is not part of hash
        //}
        return this;
    }


    @Override
    public final boolean isNormalized() {
        return this.hash != 0;
    }

    /**
     * call if the task was changed; re-hashes it at the end.
     * if the task was removed then this returns null
     */
    @Override
    public final Task normalized() {

        //dont recompute if hash isnt invalid (==0)
        if (isNormalized())
            return this;

        if (isDeleted())
            return null;

        return normalizeThis();
    }

    /** actual normalization process */
    protected Task normalizeThis() {

        final char punc = getPunctuation();
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if ((truth == null) && (isJudgmentOrGoal())) {
            truth = new DefaultTruth(punc);
        }


        Compound sentenceTerm = getTerm();
        if (sentenceTerm == null)
            return null;


        updateEvidence();




        /*Task t = new DefaultTask(sentenceTerm, punc,
                (truth != null) ? new DefaultTruth(truth) : null, //clone the truth so that this class can be re-used multiple times with different values to create different tasks
                getBudget(),
                getParentTask(),
                getParentBelief(),
                solutionBelief);*/


        if (Float.isNaN(getQuality())) {
            applyDefaultBudget();
        }

        //if (this.cause != null) t.setCause(cause);
        //if (this.reason != null) t.log(reason);

        this.hash = rehash();

        return this;
    }

    protected boolean applyDefaultBudget() {
        //if (getBudget().isBudgetValid()) return true;
        if (getTruth() == null) return false;

        final char punc = getPunctuation();
        setPriority(Budget.newDefaultPriority(punc));
        setDurability(Budget.newDefaultDurability(punc));

        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(quality)) {
            setQuality(BudgetFunctions.truthToQuality(truth));
        }

        return true;
    }

    final void updateEvidence() {
        //supplying no evidence will be assigned a new serial
        //but this should only happen for input tasks (with no parent)

        if (isDouble()) {
            long[] as = getParentTask().getEvidence();
            long[] bs = getParentBelief().getEvidence();

            //temporary
            if (as == null)
                throw new RuntimeException("parentTask " + getParentTask() + " has no evidentialSet");
            if (bs == null)
                throw new RuntimeException("parentBelief " + getParentBelief() + " has no evidentialSet");

            final long[] zipped = Stamp.zip(as, bs);
            final long[] uniques = Stamp.toSetArray(zipped);

            setEvidence(uniques);

                /*if (getParentTask().isInput() || getParentBelief().isInput()) {
                    setCyclic(false);
                } else {*/
                    /*
                    <patham9> since evidental overlap is not checked on deduction, a derivation can be cyclic
                    <patham9> its on revision when it finally matters, but not whether the two parents are cyclic, but whether the combination of both evidental bases of both parents would be cyclic/have an overlap
                    <patham9> else deductive conclusions could not lead to revisions altough the overlap is only local to the parent (the deductive conclusion)
                    <patham9> revision is allowed here because the two premises to revise dont have an overlapping evidental base element
                    */

//            setCyclic(
//                    //boolean bothParentsCyclic =
//                    ((getParentTask().isCyclic() && getParentBelief().isCyclic())
//                            ||
//                            //boolean overlapBetweenParents = if the sum of the two parents length is greater than the result then there was some overlap
//                            (zipped.length > uniques.length))
//            );

            //}

        } else if (isSingle()) {
            //Single premise
            setEvidence(getParentTask().getEvidence());

            //setCyclic(true); //p.isCyclic());
        }
        else {
            //setCyclic(false);
        }

    }


    public final void invalidate() {
        /*if (term!=null)
            term.invalidate();*/
        hash = 0;
    }

    @Override
    public Sentence setOccurrenceTime(final long o) {
        if (o != occurrenceTime) {
            this.occurrenceTime = o;
            invalidate();
        }
        return this;
    }

    @Override
    public DefaultTask<T> setEternal() {
        setOccurrenceTime(Stamp.ETERNAL);
        return this;
    }


    @Override
    public final Sentence setDuration(int d) {
        this.duration = d;
        return this;
    }

    @Override
    public final int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            //throw new RuntimeException(this + " not normalized");
            hash = this.hash = rehash();
        }
        return hash;
    }

    /**
     * To check whether two sentences are equal
     * Must be consistent with the values calculated in getHash()
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */
    @Override
    public final boolean equals(final Object that) {
        if (this == that) return true;
        if (that instanceof Sentence) {

            if (hashCode() != that.hashCode()) return false;

            return equivalentTo((Sentence) that, true, true, true, true, false);
        }
        return false;
    }

    @Override
    public final boolean equivalentTo(final Sentence that, final boolean punctuation, final boolean term, final boolean truth, final boolean stamp, final boolean creationTime) {

        if (this == that) return true;

        final char thisPunc = this.getPunctuation();

        if (term) {
            if (!equalTerms(that)) return false;
        }

        if (punctuation) {
            if (thisPunc != that.getPunctuation()) return false;
        }

        if (truth) {
            Truth thisTruth = this.getTruth();
            if (thisTruth == null) {
                //equal punctuation will ensure thatTruth is also null
            } else {
                if (!thisTruth.equals(that.getTruth())) return false;
            }
        }


        if (stamp) {
            //uniqueness includes every aspect of stamp except creation time
            //<patham9> if they are only different in creation time, then they are the same
            if (!this.equalStamp(that, true, creationTime, true))
                return false;
        }

        return true;
    }

    /**
     * Check if two stamps contains the same types of content
     * <p>
     * NOTE: hashcode will include within it the creationTime & occurrenceTime, so if those are not to be compared then avoid comparing hash
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    public final boolean equalStamp(final Stamp s, final boolean evidentialSet, final boolean creationTime, final boolean occurrenceTime) {
        if (this == s) return true;

        /*if (hash && (!occurrenceTime || !evidentialSet))
            throw new RuntimeException("Hash equality test must be followed by occurenceTime and evidentialSet equality since hash incorporates them");

        if (hash)
            if (hashCode() != s.hashCode()) return false;*/
        if (creationTime)
            if (getCreationTime() != s.getCreationTime()) return false;
        if (occurrenceTime)
            if (getOccurrenceTime() != s.getOccurrenceTime()) return false;
        if (evidentialSet) {
            return Arrays.equals(getEvidence(), s.getEvidence());
        }


        return true;
    }


    @Override
    public Reference<Task> getParentTaskRef() {
        return parentTask;
    }

    @Override
    public Reference<Task> getParentBeliefRef() {
        return parentBelief;
    }

    @Override
    public Reference<Task> getBestSolutionRef() {
        return bestSolution;
    }

    /**
     * Get the best-so-far solution for a Question or Goal
     *
     * @return The stored Sentence or null
     */
    @Override
    public Task getBestSolution() {
        return dereference(bestSolution);
    }

    /**
     * Set the best-so-far solution for a Question or Goal, and report answer
     * for input question
     *
     * @param judg The solution to be remembered
     */
    @Override
    public final void setBestSolution(final Task judg, final Memory memory) {
        bestSolution = reference(judg);
        //InternalExperience.experienceFromBelief(memory, this, judg);
    }

    /**
     * flag to indicate whether this Event Task participates in tempporal induction
     */
    @Override
    public final Task setTemporalInducting(boolean b) {
        this.temporallyInductable = b;
        return this;
    }

    @Override
    public final boolean isTemporalInductable() {
        return temporallyInductable;
    }

    /**
     * add to this task's log history
     * useful for debugging but can also be applied to meta-analysis
     */
    @Override
    public void log(final String reason) {
        if (!Global.DEBUG_TASK_LOG)
            return;

        //TODO parameter for max history length, although task history should not grow after they are crystallized with a concept
        if (this.log == null)
            this.log = Global.newArrayList(1);

        this.log.add(reason);
    }

    @Override
    public final List<String> getLog() {
        return log;
    }


    /*
    @Override
    public void delete() {
        super.delete();
//        this.parentBelief = this.parentTask = this.bestSolution = null;
//        this.cause = null;
//        log.clear();
//        this.term = null;
//        this.truth = null;
//        this.hash = 0;
    }*/

    public final void setParentTask(Task parentTask) {
        this.parentTask = reference(parentTask);
        invalidate();
    }

    public final void setParentBelief(Task parentBelief) {
        this.parentBelief = reference(parentBelief);
        invalidate();
    }

    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    @Override
    final public Task getParentBelief() {
        return dereference(parentBelief);
    }

    /**
     * Get the parent task of a task
     *
     * @return The task from which the task is derived
     */
    @Override
    final public Task getParentTask() {
        return dereference(parentTask);
    }

    @Override
    final public Sentence<T> name() {
        return this;
    }

    @Override
    @Deprecated
    public String toString() {
        return appendTo(null, null).toString();
    }

    /**
     * the causing Operation, or null if not applicable.
     */
    @Override
    public final Operation getCause() {
        return cause;
    }

    @Override
    public final Task setCause(final Operation op) {
        this.cause = op;
        return this;
    }

    @Override
    public void discountConfidence() {
        setTruth(getTruth().discountConfidence());
    }


    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(toString());
    }

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        serialize(jgen, provider);
    }


}
