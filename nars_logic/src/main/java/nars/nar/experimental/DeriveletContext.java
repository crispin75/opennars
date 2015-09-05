package nars.nar.experimental;

import nars.Memory;
import nars.concept.Concept;
import nars.term.Term;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by me on 9/5/15.
 */
abstract public class DeriveletContext implements Consumer<Memory> {

    /** random # generator local to this thread */
    public final Random rng;

    /** current concept, next concept */
    public final Supplier<Concept> conceptSupply;
    private final Memory memory;
    private float forgetCycles;

    public DeriveletContext(Memory memory, Random rng, Supplier<Concept> conceptSupply) {
        this.memory = memory;
        this.rng = rng;
        this.conceptSupply = conceptSupply;
        memory.eventCycleStart.on(this);
    }

    @Override
    public void accept(final Memory memory) {
        //each cycle
        forgetCycles = memory.duration() * memory.param.conceptForgetDurations.floatValue();
    }

    public float nextFloat() {
        return rng.nextFloat();
    }


    public abstract Concept concept(Term term);

    public float getForgetCycles() {
        return forgetCycles;
    }
}
