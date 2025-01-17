package nars.meter;

import nars.Global;
import nars.Memory;
import nars.event.CycleReaction;
import nars.nal.nal3.SetInt;
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.util.meter.event.DoubleMeter;

import java.io.Serializable;

/**
 * emotional value; self-felt internal mental states; variables used to record emotional values
 */
public class EmotionMeter extends CycleReaction implements Serializable {

    public static final Compound BUSYness = SetInt.make(Atom.the("busy"));
    private final Memory memory;


    /**
     * busy = total priority accumulated in this cycle
     */
    private float busy;

    /** happy = total happiness accumulated in this cycle */
    private float happy;

    //private final float happinessFade = 0.95f;

    public float lasthappy = -1;
    public float lastbusy = -1;


    public final DoubleMeter happyMeter = new DoubleMeter("happy");
    public final DoubleMeter busyMeter = new DoubleMeter("busy");

    public static final Atom satisfied = Atom.the("satisfied");
    final static Compound satisfiedSetInt = SetInt.make(satisfied);

    public EmotionMeter(Memory memory) {
        super(memory);

        this.memory = memory;

        commit();
    }

    @Override
    public void onCycle() {
        commit();
    }

    public float happy() {
        return (float)happyMeter.get();
    }

    public float busy() {
        return (float)busyMeter.get();
    }


    public void happy(final float delta) {
        this.happy += delta;
    }

    public void happy(final float solution, final Task task, @Deprecated final Premise p) {
        this.happy += ( task.getBudget().summary() * solution );
    }

    protected void commitHappy() {


        if (lasthappy != -1) {
            //float frequency = changeSignificance(lasthappy, happy, Global.HAPPY_EVENT_CHANGE_THRESHOLD);
//            if (happy > Global.HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy <= Global.HAPPY_EVENT_HIGHER_THRESHOLD) {
//                frequency = 1.0f;
//            }
//            if (happy < Global.HAPPY_EVENT_LOWER_THRESHOLD && lasthappy >= Global.HAPPY_EVENT_LOWER_THRESHOLD) {
//                frequency = 0.0f;
//            }

//            if ((frequency != -1) && (memory.nal(7))) { //ok lets add an event now
//
//                Inheritance inh = Inheritance.make(memory.self(), satisfiedSetInt);
//
//                memory.input(
//                        TaskSeed.make(memory, inh).judgment()
//                                .truth(frequency, Global.DEFAULT_JUDGMENT_CONFIDENCE)
//                                .occurrNow()
//                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
//                                .reason("Happy Metabelief")
//                );
//
//                if (Global.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now
//
//                    //TODO convert to fluent format
//
//                    memory.input(
//                            TaskSeed.make(memory, inh).goal()
//                                    .truth(frequency, Global.DEFAULT_GOAL_CONFIDENCE)
//                                    .occurrNow()
//                                    .budget(Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY)
//                                    .reason("Happy Metagoal")
//                    );
//
//                    //this is a good candidate for innate belief for consider and remind:
//
//                    if (InternalExperience.enabled && Global.CONSIDER_REMIND) {
//                        Operation op_consider = Operation.op(Product.only(inh), consider.consider);
//                        Operation op_remind = Operation.op(Product.only(inh), remind.remind);
//
//                        //order important because usually reminding something
//                        //means it has good chance to be considered after
//                        for (Operation o : new Operation[]{op_remind, op_consider}) {
//
//                            memory.input(
//                                    TaskSeed.make(memory, o).judgment()
//                                            .occurrNow()
//                                            .truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
//                                            .budget(Global.DEFAULT_JUDGMENT_PRIORITY * InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL,
//                                                    Global.DEFAULT_JUDGMENT_DURABILITY * InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL)
//                                            .reason("Happy Remind/Consider")
//                            );
//                        }
//                    }
//                }
//            }
//        }
        }

        happyMeter.set(happy);

        /*if (happy > 0)
            happy *= happinessFade;*/
    }

    /** @return -1 if no significant change, 0 if decreased, 1 if increased */
    private float changeSignificance(float prev, float current, float proportionChangeThreshold) {
        float range = Math.max(prev, current);
        if (range == 0) return -1;
        if (prev - current > range * proportionChangeThreshold)
            return -1;
        else if (current - prev > range * proportionChangeThreshold)
            return 1;

        return -1;
    }

    public void busy(ConceptProcess nal) {
        busy(nal.getTask(), nal);
    }

    public void busy(Premise nal) {
        busy(nal.getTask(), nal);
    }

    public void busy(Task cause, Premise p) {
        this.busy += cause.getPriority();
    }


    protected void commitBusy() {

        if (lastbusy != -1) {
            //float frequency = -1;
            float frequency = changeSignificance(lastbusy, busy, Global.BUSY_EVENT_CHANGE_THRESHOLD);
            //            if (busy > Global.BUSY_EVENT_HIGHER_THRESHOLD && lastbusy <= Global.BUSY_EVENT_HIGHER_THRESHOLD) {
//                frequency = 1.0f;
//            }
//            if (busy < Global.BUSY_EVENT_LOWER_THRESHOLD && lastbusy >= Global.BUSY_EVENT_LOWER_THRESHOLD) {
//                frequency = 0.0f;
//            }


            /*
            if (Global.REFLECT_META_BUSY_BELIEF && (frequency != -1) && (memory.nal(7))) { //ok lets add an event now
                final Inheritance busyTerm = Inheritance.make(memory.self(), BUSYness);

                memory.input(
                        TaskSeed.make(memory, busyTerm).judgment()
                                .truth(frequency, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                                .occurrNow()
                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                                .reason("Busy")
                );
            }
            */
        }

        busyMeter.set(lastbusy = this.busy);

        this.busy = 0;


    }

    public void commit() {
        commitHappy();

        commitBusy();
    }

    public void clear() {
        busy = 0;
        happy = 0;
    }
}
