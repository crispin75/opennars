package nars.model.cycle;

import nars.Events;
import nars.Memory;
import nars.Global;
import nars.budget.BudgetFunctions;
import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import nars.bag.impl.CacheBag;
import nars.bag.tx.BagActivator;

/**
* Created by me on 3/15/15.
*/
abstract public class ConceptActivator extends BagActivator<Term,Concept> {

    final float relativeThreshold = Global.FORGET_QUALITY_RELATIVE;

    private boolean createIfMissing;
    private long now;

    public ConceptActivator() {
    }

    abstract public Memory getMemory();

    @Override
    public Concept updateItem(Concept c) {

        long cyclesSinceLastForgotten = now - c.getLastForgetTime();
        getMemory().forget(c, cyclesSinceLastForgotten, relativeThreshold);

        if (budget!=null) {
            Budget cb = c;

            final float activationFactor = getMemory().param.conceptActivationFactor.floatValue();
            BudgetFunctions.activate(cb, getBudgetRef(), BudgetFunctions.Activating.TaskLink, activationFactor);
        }

        return c;
    }

    public ConceptActivator set(Term t, Budget b, boolean createIfMissing, long now) {
        setKey(t);
        setBudget(b);
        this.createIfMissing = createIfMissing;
        this.now = now;
        return this;
    }

    abstract public CacheBag<Term,Concept> getSubConcepts();

    @Override
    public Concept newItem() {

        //try remembering from subconscious
        if (getSubConcepts()!=null) {
            Concept concept = getSubConcepts().take(getKey());
            if (concept!=null) {

                getMemory().emit(Events.ConceptRemember.class, concept);

                return concept;
            }
        }

        //create new concept, with the applied budget
        if (createIfMissing) {
            Concept concept = getMemory().newConcept(budget, getKey());

            if ( concept == null) {
                throw new RuntimeException("No ConceptBuilder will build: " + getKey() + " " + budget + ", builders=" + getMemory().getConceptBuilders());
            }


            if (getMemory().logic!=null)
                getMemory().logic.CONCEPT_NEW.hit();

            getMemory().emit(Events.ConceptNew.class, concept);

            return concept;
        }

        return null;
    }

    @Override
    public void overflow(Concept overflow) {
        getMemory().concepts.conceptRemoved(overflow);
    }
}