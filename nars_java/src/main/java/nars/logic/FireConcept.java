/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.Events;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.nal1.Negation;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.logic.nal5.SyllogisticRules;
import nars.logic.nal7.TemporalRules;
import nars.logic.rule.concept.ConceptFireRule;
import nars.util.bag.select.ForgetNext;

import java.util.Set;

import static nars.io.Symbols.VAR_INDEPENDENT;

/** Concept reasoning context - a concept is "fired" or activated by applying the reasoner */
abstract public class FireConcept extends NAL {

    protected final Term currentTerm;
    protected final TaskLink currentTaskLink;
    protected final Concept currentConcept;

    private Concept beliefConcept;
    private int termLinkCount;

//    public FireConcept(Memory mem, Concept concept, int numTaskLinks) {
//        this(mem, concept, numTaskLinks, mem.param.termLinkMaxReasoned.get());
//    }


    public FireConcept(Concept concept, TaskLink taskLink) {
        this(concept, taskLink, concept.memory.param.termLinkMaxReasoned.get());
    }

    public FireConcept(Concept concept, TaskLink taskLink, int termLinkCount) {
        super(concept.memory, taskLink.getTask());
        this.currentTaskLink = taskLink;
        this.currentConcept = concept;
        this.currentTerm = concept.getTerm();

        this.termLinkCount = termLinkCount;
    }



    /**
     * @return the currentConcept
     */
    public Concept getCurrentConcept() {
        return currentConcept;
    }


    abstract protected void beforeFinish();

    @Override
    protected void onFinished() {
        beforeFinish();

        currentConcept.termLinks.processNext(
                memory.param.termLinkForgetDurations,
                Parameters.TERMLINK_FORGETTING_ACCURACY,
                memory);

        currentConcept.taskLinks.processNext(
                memory.param.taskLinkForgetDurations,
                Parameters.TASKLINK_FORGETTING_ACCURACY,
                memory);

        /*
        System.err.println(this);
        for (Task t : tasksAdded) {
            System.err.println("  " + t);
        }
        System.err.println();
        */


        memory.addNewTasks(this);
    }


    @Override
    protected void reason() {

        reset();

        if (currentTaskLink.type == TermLink.TRANSFORM) {

            RuleTables.transformTask(currentTaskLink, this); // to turn this into structural logic as below?

            emit(Events.TermLinkTransform.class, currentTaskLink, currentConcept, this);
            memory.logic.TERM_LINK_TRANSFORM.hit();

        } else {

            reason(currentTaskLink);

            int noveltyHorizon = Parameters.NOVELTY_HORIZON;

            int termLinkSelectionAttempts = termLinkCount;

            //TODO early termination condition of this loop when (# of termlinks) - (# of non-novel) <= 0
            //int numTermLinks = getCurrentConcept().termLinks.size();

            while (termLinkSelectionAttempts > 0)  {


                final TermLink beliefLink = currentConcept.selectTermLink(currentTaskLink, memory.time(), noveltyHorizon);

                termLinkSelectionAttempts--;

                //try again, because it may have selected a non-novel tlink
                if (beliefLink == null)
                    continue;
                

                int numAddedTasksBefore = produced.size();

                reason(currentTaskLink, beliefLink);

                currentConcept.returnTermLink(beliefLink);

                int numAddedTasksAfter = produced.size();

                emit(Events.TermLinkSelected.class, beliefLink, getCurrentTaskLink(), getCurrentConcept(), this, numAddedTasksBefore, numAddedTasksAfter);
                memory.logic.TERM_LINK_SELECT.hit();


            }
        }
                
        emit(Events.ConceptFired.class, this);
        memory.logic.TASKLINK_FIRE.hit();

    }

    /** reasoning processes involving only the task itself */
    protected void reason(Sentence.Sentenceable taskLink) {

        final Sentence taskSentence = taskLink.getSentence();

        final Term taskTerm = taskSentence.term;         // cloning for substitution

        if ((taskTerm instanceof Implication) && taskSentence.isJudgment()) {
            //there would only be one concept which has a term equal to another term... so samplingis totally unnecessary

            //Concept d=memory.concepts.sampleNextConcept();
            //if(d!=null && d.term.equals(taskSentence.term)) {

            double n=taskTerm.getComplexity(); //don't let this rule apply every time, make it dependent on complexity
            double w=1.0/((n*(n-1))/2.0); //let's assume hierachical tuple (triangle numbers) amount for this
            if(Memory.randomNumber.nextDouble() < w) { //so that NARS memory will not be spammed with contrapositions

                StructuralRules.contraposition((Statement) taskTerm, taskSentence, this);
                //}
            }
        }

    }


    /**
     * Entry point of the logic engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     */
    protected void reason(final TaskLink tLink, final TermLink bLink) {

        reset();
        setCurrentBeliefLink(bLink);

        Sentence belief = getCurrentBelief();

        reasoner.add(this);

        if (belief!=null) {
            emit(Events.BeliefReason.class, belief, tLink.getTarget(), this);
        }



//        final Task task = tLink.getTarget(); //== getCurrentTask();
//        final Sentence taskSentence = task.sentence;
//
//        final Term taskTerm = taskSentence.term;         // cloning for substitution
//        final Term beliefTerm = bLink.target;       // cloning for substitution


//        if (equalSubTermsInRespectToImageAndProduct(taskTerm, beliefTerm)) {
//            throw new RuntimeException("shoulld have been suppressed by the new rule impl");
//        }
//
//        if ((belief != null) && (LocalRules.match(task, belief, this))) {
//            throw new RuntimeException("shoulld have been suppressed by the new rule impl");
//        }




//        //current belief and task may have changed, so set again:
//        setCurrentBelief(belief);
//        setCurrentTask(task);




    }

    /**
     * @return the currentBeliefLink
     */
    public TermLink getCurrentBeliefLink() {
        return currentBeliefLink;
    }

    /**
     * @param currentBeliefLink the currentBeliefLink to set
     */
    public void setCurrentBeliefLink(TermLink currentBeliefLink) {

        if (this.currentBeliefLink == currentBeliefLink)
            throw new RuntimeException("Setting the same current belief link");

        this.currentBeliefLink = currentBeliefLink;

        Term beliefTerm = currentBeliefLink.getTerm();

        this.beliefConcept = memory.concept(beliefTerm);

        this.currentBelief = (beliefConcept != null) ? beliefConcept.getBelief(this, getCurrentTask()) : null;
    }

    public void reset() {
        this.currentBelief = null;
        this.currentBeliefLink = null;
    }


    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentTerm;
    }

    /**
     * @return the currentTaskLink
     */
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }





    /** the current belief concept */
    public Concept getBeliefConcept() {
        return beliefConcept;
    }



    @Override
    public String toString() {
        return "FireConcept[" + currentConcept + "," + currentTaskLink + "]";
    }


}