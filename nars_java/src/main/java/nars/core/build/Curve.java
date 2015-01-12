package nars.core.build;

import nars.core.Core;
import nars.core.Memory;
import nars.core.control.DefaultCore;
import nars.entity.*;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.CurveBag;
import nars.storage.CurveBag.FairPriorityProbabilityCurve;


public class Curve extends Default {
    public final boolean randomRemoval;
    public final CurveBag.BagCurve curve;

    public Curve() {
        this(true);
    }
    
    public Curve(boolean randomRemoval) {
        this(new FairPriorityProbabilityCurve(), randomRemoval);        
    }
    
    public Curve(CurveBag.BagCurve curve, boolean randomRemoval) {
        super();
        this.randomRemoval = randomRemoval;
        this.curve = curve;
    }
    

    @Override
    public Bag<Task<Term>,Sentence<Term>> newNovelTaskBag() {
        return new CurveBag<Task<Term>,Sentence<Term>>(getNovelTaskBagSize(), curve, randomRemoval);
    }

    @Override
    public Bag<Concept,Term> newConceptBag() {
        return new CurveBag<>(getConceptBagSize(), curve, randomRemoval);
        //return new AdaptiveContinuousBag<>(getConceptBagSize());
    }

    

    @Override
    public Core newAttention() {
        //return new BalancedSequentialMemoryCycle(newConceptBag(p), c);
        return new DefaultCore(newConceptBag(), newSubconceptBag(), getConceptBuilder());
    }
    
    @Override
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {
        
        Bag<TaskLink,Task> taskLinks = new CurveBag<>(getConceptTaskLinks(), curve, randomRemoval);
        Bag<TermLink,TermLink> termLinks = new CurveBag<>(getConceptTermLinks(), curve, randomRemoval);
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }
    
}
