package nars.util;

import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.Events.ConceptNew;
import nars.core.Events.CycleEnd;
import nars.core.Events.InferenceEvent;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.inference.MemoryObserver;
import nars.io.meter.SignalData;
import nars.io.meter.TemporalMetrics;
import nars.io.meter.func.BasicStatistics;
import nars.io.meter.func.FirstOrderDifference;
import nars.io.narsese.Narsese;
import nars.language.Term;

import java.awt.*;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.List;


/**
 * Records all sensors, output, and trace events in an indexed data structure for runtime or subsequent analysis of a NAR's execution telemetry.
 */
public class NARTrace extends MemoryObserver implements Serializable {

    /**
     * utility method for diagnosing stack overflow errors caused by unbounded
     * recursion or other phenomena
     */
    public static boolean guardStack(int alertDepth, String methodname, Object... args) {
        StackTraceElement[] st = new Exception().getStackTrace();
        if (st.length < 1 + alertDepth) {
            return false;
        }
        for (int i = 1; i < alertDepth; i++) {
            //look for a series of equal (TODO: or cyclic) method names
            if (!st[i].getMethodName().contains(methodname)) {
                return false;
            }
        }
        return true;
    }

    
    public final Map<Concept, List<InferenceEvent>> concept = new HashMap();
    public final TreeMap<Long, List<InferenceEvent>> time = new TreeMap();
        
    public final TemporalMetrics<Object> metrics;
    
    

    private long t;
    public final NAR nar;

    public SignalData[] getCharts(String... names) {
        List<SignalData> l = new ArrayList(names.length);
        for (String n : names) {
            SignalData t = metrics.newSignalData(n);
            if (t!=null)
                l.add(t);
        }
        return l.toArray(new SignalData[l.size()]);
    }
    
    public List<SignalData> getCharts() {
        return metrics.getSignalDatas();
    }    



    public static class OutputEvent extends InferenceEvent {

        public final Class channel;
        public final Object[] signal;

        public OutputEvent(long when, Class channel, Object... signal) {
            super(when);
            this.channel = channel;
            this.signal = signal;
        }

        @Override
        public String toString() {
            return channel.getSimpleName() + ": " +
                    (signal.length > 1 ? Arrays.toString(signal) : signal[0]);
        }
        
        public Class getType() {
            return channel;
        }
        

    }

    public static interface HasLabel {
        public String toLabel();
    }
    
    public static enum AddOrRemove {
        Add, Remove
    }
    
    public static class TaskEvent extends InferenceEvent implements HasLabel {

        public final Task task;
        public final AddOrRemove type;
        public final String reason;
        public final float priority;

        public TaskEvent(Task t, long when, AddOrRemove type, String reason) {
            super(when);
            this.task = t;
            this.type = type;
            this.reason = reason;
            this.priority = t.getPriority();
        }

        @Override
        public String toString() {
            return "Task " + type + " (" + reason + "): " + task.toStringExternal();
        }
        @Override
        public String toLabel() {
            return "Task " + type + " (" + reason + "):\n" + task.name();
        }
    }

    
    public NARTrace(NAR n) {
        this(n, 64);
    }
    
    public NARTrace(NAR n, int metricsHistoryLength) {
        super(n, true);
        this.nar = n;
    
        metrics = new TemporalMetrics(metricsHistoryLength);
        
        metrics.addMeters(n.memory.emotion);
        
        metrics.addMeters(n.memory.resource);
        

        metrics.addMeter(new BasicStatistics(metrics, n.memory.resource.CYCLE_DURATION.id(), 16));
        metrics.addMeter(new FirstOrderDifference(metrics, n.memory.resource.CYCLE_RAM_USED.id()));
     
        metrics.addMeters(n.memory.logic);
        
    }
    
    

    public void addEvent(InferenceEvent e) {
        List<InferenceEvent> timeslot = time.get(t);
        if (timeslot == null) {
            timeslot = new ArrayList();
            time.put(t, timeslot);
        }
        timeslot.add(e);
    }

  

    public void reset() {
        time.clear();
        concept.clear();
    }
    
    @Override
    public void event(final Class event, final Object[] arguments) {
        if (event == Events.TaskAdd.class) {
            onTaskAdd((Task)arguments[0], (String)arguments[1]);
        }
        else if (event == Events.TaskRemove.class) {
            onTaskRemove((Task)arguments[0], (String)arguments[1]);
        }
        else
            super.event(event, arguments);

    }
    
    @Override
    public void onConceptAdd(Concept concept) {
        ConceptNew cc = new ConceptNew(concept, t);
        addEvent(cc);

        List<InferenceEvent> lc = new ArrayList(1);
        lc.add(cc);
                
        this.concept.put(concept, lc);
    }

    public TemporalMetrics getMetrics() {
        return metrics;
    }

    
    @Override
    public void onCycleStart(long clock) {
        this.t = clock;
    }

    @Override
    public void onCycleEnd(long time) {
        metrics.update((double)time);    
    }

    @Override
    public void onTaskAdd(Task task, String reason) {
        TaskEvent ta = new TaskEvent(task, t, AddOrRemove.Add, reason);
        addEvent(ta);
    }

    @Override
    public void onTaskRemove(Task task, String reason) {
        TaskEvent tr = new TaskEvent(task, t, AddOrRemove.Remove, reason);
        addEvent(tr);
    }

    @Override
    public void output(Class channel, Object... signal) {
        addEvent(new OutputEvent(t, channel, signal));
    }
    
    public void printTime() {
        printTime(System.out);
    }

    public void printTime(PrintStream out) {
        for (Long w : time.keySet()) {
            List<InferenceEvent> events = time.get(w);
            if (events.isEmpty()) {
                continue;
            }

            out.println(w + " ---------\\");
            for (InferenceEvent e : events) {
                System.out.println("  " + e);
            }
            out.println(w + " ---------/\n");

        }
    }

    abstract public static class CycleTreeMLData extends TreeMLData implements EventObserver {

        private final NAR nar;

        public CycleTreeMLData(NAR n, String theName, int historySize) {
            super(theName, Color.WHITE  /*Video.getColor(theName, 0.9f, 1f)*/, historySize);
            this.nar = n;
            n.on(CycleEnd.class, this);
        }

        public CycleTreeMLData(NAR n, String theName, float min, float max, int historySize) {
            this(n, theName, historySize);
            setRange(min, max);
        }

        @Override
        public void event(Class event, Object[] arguments) {
            long time = nar.time();
            setData((int)nar.time(), next(time, nar));
        }

        public abstract float next(long time, NAR nar);

    }

    public static class ConceptBagTreeMLData extends CycleTreeMLData {

        public final Mode mode;
        private final Iterable<Concept> concepts;

        public static enum Mode {

            ConceptPriorityTotal, TaskLinkPriorityMean, TermLinkPriorityMean /* add others */ }

        public ConceptBagTreeMLData(NAR n, Iterable<Concept> concepts, int historySize, Mode mode) {
            super(n, "Concepts: " + mode, historySize);
            this.mode = mode;
            this.concepts = concepts;

        }

        @Override
        public float next(long time, NAR nar) {
            float r = 0;
            int numConcepts = 0;
            for (Concept c : concepts) {
                switch (mode) {
                    case ConceptPriorityTotal:
                        r += c.getPriority();
                        break;
                    case TermLinkPriorityMean:
                        r += c.termLinks.getTotalPriority();
                        break;
                    case TaskLinkPriorityMean:
                        r += c.taskLinks.getTotalPriority();
                        break;
                }
                numConcepts++;
            }
            
            switch (mode) {
                case TermLinkPriorityMean:
                case TaskLinkPriorityMean:
                    if (numConcepts > 0) r /= numConcepts;
                    break;
            }
            
            return r;
        }

    }

    public static class ConceptTreeMLData extends CycleTreeMLData {

        public final Mode mode;
        private final String conceptString;
        private final Term conceptTerm;
        private Concept concept;

        public static enum Mode {

            Priority, Duration, BeliefConfidenceMax /* add others */ }

        public ConceptTreeMLData(NAR n, String concept, int historySize, Mode mode) throws Narsese.InvalidInputException {
            super(n, concept + ": " + mode, 0, 1, historySize);
            this.mode = mode;
            this.conceptString = concept;
            this.conceptTerm = new Narsese(n).parseTerm(conceptString);

            this.concept = null;
        }

        @Override
        public float next(final long time, final NAR nar) {
            if (concept == null) {
                concept = nar.memory.concept(conceptTerm);
                if (concept == null) {
                    return 0;
                }
            }
            switch (mode) {
                case Priority:
                    return concept.getPriority();
                case Duration:
                    return concept.getDurability();
                case BeliefConfidenceMax:
                    if (concept.beliefs.size() > 0) {
                        return concept.beliefs.get(0).truth.getConfidence();
                    }
                    return 0;
            }
            return 0f;
        }

    }
    
}
