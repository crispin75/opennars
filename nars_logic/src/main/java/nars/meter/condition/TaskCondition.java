package nars.meter.condition;


import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.io.Texts;
import nars.nal.nal7.Temporal;
import nars.narsese.InvalidInputException;
import nars.task.DefaultTask;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TaskCondition extends DefaultTask implements Serializable, Predicate<Task>, Consumer<Task> {

    //@Expose

    //@JsonSerialize(using= JSONOutput.TermSerializer.class)
    //public  Term term;

    //@Expose
    //public  char punc;

    //@Expose
    public  float freqMin;
    //@Expose
    public  float freqMax;
    //@Expose
    public  float confMin;
    //@Expose
    public  float confMax;
    //@Expose
    public  long cycleStart; //-1 for not compared
    //@Expose
    public  long cycleEnd;  //-1 for not compared

    /*float tenseCost = 0.35f;
    float temporalityCost = 0.75f;*/


    private final NAR nar;
    //private final Observed.DefaultObserved.DefaultObservableRegistration taskRemoved;

    //@Expose
    //protected long creationTime;


    //@Expose
    //public Tense tense = Tense.Eternal;


    public List<Task> valid;


    transient int maxSimilars = 16;

    protected TreeMap<Float,Task> similar;

//    public static class StringDistance extends Item<String> {
//
//        private final String text;
//
//        public StringDistance(String s, String other) {
//            super();
//            this.text = s;
//            int dist = Texts.levenshteinDistance(s, other);
//            setPriority(1.0f / (1.0f + dist));
//        }
//
//        @Override
//        public String name() {
//            return text;
//        }
//    }
//
//    CurveBag<String,StringDistance> similar2 = new CurveBag(16);

    public TaskCondition(NAR n, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        super(n.task(sentenceTerm + punc));

        this.nar = n;

        if (freqMax < freqMin) throw new RuntimeException("freqMax < freqMin");
        if (confMax < confMin) throw new RuntimeException("confMax < confMin");

        if (cycleEnd - cycleStart < 1) throw new RuntimeException("cycleEnd must be after cycleStart by at least 1 cycle");


        setCreationTime(n.time());
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
        setEternal();
        this.freqMax = Math.min(1.0f, freqMax);
        this.freqMin = Math.max(0.0f, freqMin);
        this.confMax = Math.min(1.0f, confMax);
        this.confMin = Math.max(0.0f, confMin);
        setPunctuation(punc);
        setTerm(n.term(sentenceTerm));
    }




    public double getAcceptableDistanceThreshold() {
        return 0.01;
    }

    //how many multiples of the range it is away from the acceptable time interval
    public static double rangeError(double value, double min, double max, boolean squash) {
        double dt;
        if (value < min)
            dt = min - value;
        else if (value > max)
            dt = value - max;
        else
            return 0;

        double result = dt/(max-min);

        if (squash)
            return Math.tanh(result);
        else
            return result;
    }

    //time distance function
    public double getTimeDistance(long now) {
        return rangeError(now, cycleStart, cycleEnd, true);
    }

    //truth distance function
    public double getTruthDistance(Truth t) {
        //manhattan distance:
        return rangeError(t.getFrequency(), freqMin, freqMax, true) +
                rangeError(t.getConfidence(), confMin, confMax, true);

        //we could also calculate geometric/cartesian vector distance
    }

////    public void setRelativeOccurrenceTime(Tense t, int duration) {
////        setRelativeOccurrenceTime(Stamp.getOccurrenceTime(t, duration), duration);
////    }
//    /** task's tense is compared by its occurence time delta to the current time when processing */
//    public void setRelativeOccurrenceTime(long ocRelative, int duration) {
//        //may be more accurate if duration/2
//
//        Tense tense;
//        final float ocRel = ocRelative; //cast to float for this compare
//        if (ocRel > duration/2f) tense = Tense.Future;
//        if (ocRel < -duration/2f) tense = Tense.Past;
//        else tense = Tense.Present;
//
//        setRelativeOccurrenceTime(tense, nar.memory.duration());
//
//    }


//
//    public void setRelativeOccurrenceTime(long creationTime, int ocRelative, int duration) {
//        setCreationTime(creationTime);
//        setRelativeOccurrenceTime(ocRelative, duration);
//    }



    public boolean matches(Task task) {
        if (task == null) {
            return false;
        }
        if (task.getPunctuation() != getPunctuation())
            return false;
        //long now = nar.time();

        Term tterm = task.getTerm();

        //require exact term
        return tterm.equals(this.term);

    }





    @Override
    public boolean test(Task task) {

        /** how many errors accumulated while testing it  */
        double distance = 0;

        if (!matches(task))
            distance = 1;

        long now = nar.time();



        if ( ((cycleStart!=-1) && (now < cycleStart)) ||
                ((cycleEnd!=-1) && (now > cycleEnd)))  {
            distance += 1; //getTimeDistance(now);
        }



        //require right kind of tense
        if (isEternal()) {
            if (!Temporal.isEternal(task.getOccurrenceTime())) {
                distance += 1; //temporalityCost;
            }
        }
        else {
            if (Temporal.isEternal(task.getOccurrenceTime())) {
                distance += 1;// temporalityCost - 0.01;
            }
            else {

                    final long oc = task.getOccurrenceTime();

                    final int durationWindow = task.getDuration();

                    final int durationWindowNear = durationWindow / 2;
                    final int durationWindowFar = true ? durationWindowNear : durationWindow;


//                    long at = relativeToCondition ? getCreationTime() : task.getCreationTime();
                    final boolean tmatch = false;
//                    switch () {
//                        //TODO write time matching
////                        case Past: tmatch = oc <= (-durationWindowNear + at); break;
////                        case Present: tmatch = oc >= (-durationWindowFar + at) && (oc <= +durationWindowFar + at); break;
////                        case Future: tmatch = oc > (+durationWindowNear + at); break;
//                        default:
                            throw new RuntimeException("Invalid tense for non-eternal TaskCondition: " + this);
//                    }
//                    if (!tmatch) {
//                        //beyond tense boundaries
//                        //distance += rangeError(oc, -halfDur, halfDur, true) * tenseCost;
//                        distance += 1; //tenseCost + rangeError(oc, creationTime, creationTime, true); //error distance proportional to occurence time distance
//                        match = false;
//                    }
//                    else {
//                        //System.out.println("matched time");
//                    }

            }

        }


        //TODO use range of acceptable occurrenceTime's for non-eternal tests


        char punc = getPunctuation();
        if ((punc == '.') || (punc == '!')) {
            if(task.getTruth() == null) {
                return false;
            }
            float fr = task.getFrequency();
            float co = task.getConfidence();

            if ((co > confMax) || (co < confMin) || (fr > freqMax) || (fr < freqMin)) {
                distance += getTruthDistance(task.getTruth());
            }
        }

        boolean match = (distance == 0);

        if (match) {
            //TODO record a different score for fine-tune optimization?
            ensureExact();
            valid.add(task);
            /*if (exact==null || (exact.size() < maxExact)) {
                ensureExact();
                exact.add(task);
                return true;
            }*/
        }
        else {

            final float textDifference = Texts.levenshteinDistance(
                    task.toString().split("\\{")[0], //HACK to avoi comparing stamps
                    toString().split("\\{")[0]);

            if (textDifference > 0) {
                ensureSimilar();

                //TODO more efficient way than this
                if (!similar.values().contains(task))
                    similar.put(textDifference, task);

                if (similar.size() > maxSimilars) {
                    similar.remove(similar.lastEntry().getKey());
                }
            }

        }
        return match;
    }

    private void ensureExact() {
        if (valid == null) valid = Global.newArrayList(1);
    }
    private void ensureSimilar() {
        if (similar == null) similar = new TreeMap();
    }


//    public String getFalseReason() {
//        String x = "Unmatched; ";
//
//        if (similar!=null) {
//            x += "Similar:\n";
//            for (Map.Entry<Double,Task> et : similar.entrySet()) {
//                Task tt = et.getValue();
//                x += Texts.n4(et.getKey().floatValue()) + ' ' + tt.toString() + ' ' + tt.getLog() + '\n';
//            }
//        }
//        else {
//            x += "No similar: " + term;
//        }
//        return x;
//    }

    public Truth getTruthMean() {
        return new DefaultTruth(0.5f * (freqMax + freqMin), 0.5f * (confMax + confMin));
    }


//    public List<Task> getTrueReasons() {
//        return valid;
//        //if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
//        /*return Lists.newArrayList("match at: " +
//
//                Iterables.transform(trueAt, new Function<Task, String>() {
//                    @Override
//                    public String apply(Task task) {
//                        return task.toString() + " @ " + task.sentence.getCreationTime();
//                    }
//                }));
//                */
//        //return exact;
//    }

//    @Override
//    public String toString() {
//        return succeeded  +": "  + JSONOutput.stringFromFields(this);
//    }


    public Memory getMemory() {
        return nar.memory;
    }

    boolean succeeded = false;

    long successTime = Stamp.TIMELESS;

    @Override
    public void accept(Task task) {
        if (!succeeded && test(task)) {
            succeeded = true;
            successTime = nar.time();
        }
    }

    public long getSuccessTime() {
        return successTime;
    }

    /** calculates the "cost" of an execution according to certain evaluated condtions
     *  this is the soonest time at which all output conditions were successful.
     *  if any conditions were not successful, the cost is infinity
     * */
    public static double cost(Iterable<TaskCondition> conditions) {
        long lastSuccess = Stamp.TIMELESS;
        for (TaskCondition e : conditions) {
            long est = e.getSuccessTime();
            if (est != Stamp.TIMELESS) {
                if (lastSuccess < est) {
                    lastSuccess = est;
                }
            }
        }
        if (lastSuccess != Stamp.TIMELESS) {
            //score = 1.0 + 1.0 / (1+lastSuccess);
            return lastSuccess;
        }

        return Double.POSITIVE_INFINITY;
    }

    /** returns a function of the cost characterizing the optimality of the conditions
     *  monotonically increasing from -1..+1 (-1 if there were errors,
     *  0..1.0 if all successful.  limit 0 = takes forever, limit 1.0 = instantaneous
     */
    public static double score(List<TaskCondition> requirements) {
        double cost = cost(requirements);
        if (Double.isFinite(cost))
            return 1.0 / (1.0 + cost);
        else
            return -1;

    }

    public final boolean isTrue() {
        return succeeded;
    }

    public String toConditionString() {
        return  "  freq in(" + freqMin + "," + freqMax +
                "), conf in(" + confMin + "," + confMax +
                "), cycle in(" + cycleStart + "," + cycleEnd + ")";
    }

    public void toString(PrintStream out) {
        out.println(isTrue() ? " OK" : "ERR" + "\t" + toString() + " " + toConditionString());

        BiConsumer<String,Task> printer = (label,s) -> {
            out.print("\t" + label + " ");
            out.println(s.getExplanation().replace("\n", "\n\t\t"));
        };

        if (valid!=null) {
            valid.forEach(s -> printer.accept("VALID", s));
        }
        if (similar!=null) {
            similar.values().forEach(s -> printer.accept("SIMILAR", s));
        }
    }
}
