package nars.io.meter;

import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ValueMeter;

/**
 * Logic/reasoning sensors
 * TODO convert all sensors so that what is in commit(m) becomes the commit() method of each sensor
 */
public class LogicMeter {
    
    //public final Sensor CONCEPT_FIRE;
    
    public final HitMeter TASK_IMMEDIATE_PROCESS = new HitMeter("task.immediate_process");
    
    public final ValueMeter TASKLINK_FIRE = new ValueMeter("tasklink.fire");
    
    /** triggered at beginning of StructuralRules.reason(), entry point of inference. counts invocation and records priority of termlink parameter.
     */
    public final ValueMeter REASON = new ValueMeter("reason.tasktermlinks"); 
    
    /**
       triggered for each StructuralRules.contraposition().
       counts invocation and records complexity of statement parameter      
     */
    public final ValueMeter CONTRAPOSITION = new ValueMeter("reason.contraposition"); 
    
    
    public final HitMeter TASK_ADD_NEW = new HitMeter("task.new.add");
    public final HitMeter TASK_DERIVED = new HitMeter("task.derived");
    public final HitMeter TASK_EXECUTED = new HitMeter("task.executed");
    public final HitMeter TASK_ADD_NOVEL = new HitMeter("task.novel.add");
    
    public final HitMeter CONCEPT_NEW = new HitMeter("concept.new");
    
    public final HitMeter JUDGMENT_PROCESS = new HitMeter("judgment.process");
    public final HitMeter GOAL_PROCESS = new HitMeter("goal.process");
    public final HitMeter QUESTION_PROCESS = new HitMeter("question.process");
    public final HitMeter LINK_TO_TASK = new HitMeter("task.link_to");
    
    //public final ThreadBlockTimeTracker CYCLE_BLOCK_TIME;
    private long conceptNum;
    private double conceptPriorityMean;
    //private Object conceptPrioritySum;
    private long conceptBeliefsSum;
    private long conceptQuestionsSum;               
    
    public final HitMeter BELIEF_REVISION = new HitMeter("reason.belief.revised");
    public final HitMeter DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS = new HitMeter("reason.ded2ndunifterms");
    public final HitMeter DED_SECOND_LAYER_VARIABLE_UNIFICATION = new HitMeter("reason.ded2ndunif");
    public final HitMeter DED_CONJUNCTION_BY_QUESTION = new HitMeter("reason.dedconjbyquestion");
    public final HitMeter ANALOGY = new HitMeter("reason.analogy");
    public final ValueMeter IO_INPUTS_BUFFERED = new ValueMeter("io.inputs.buffered");
    
    public final HitMeter SHORT_TERM_MEMORY_UPDATE = new HitMeter("exec.short_term_memory.update");
    public final ValueMeter DERIVATION_LATENCY = new ValueMeter("reason.derivation.latency");
    public final ValueMeter SOLUTION_BEST = new ValueMeter("task.solution.best");
    
    public final ValueMeter PLAN_GRAPH_IN_DELAY_MAGNITUDE = new ValueMeter("plan.graph.add#delay_magnitude");
    public final ValueMeter PLAN_GRAPH_IN_OPERATION = new ValueMeter("plan.graph.add#operation");
    public final ValueMeter PLAN_GRAPH_IN_OTHER = new ValueMeter("plan.graph.add#other");
    public final ValueMeter PLAN_GRAPH_EDGE = new ValueMeter("plan.graph.edge");
    public final ValueMeter PLAN_GRAPH_VERTEX = new ValueMeter("plan.graph.vertex");
    public final ValueMeter PLAN_TASK_PLANNED = new ValueMeter("plan.task.planned");
    public final ValueMeter PLAN_TASK_EXECUTABLE = new ValueMeter("plan.task.executable");

    private double conceptVariance;
    private double[] conceptHistogram;
   
//    @Override
//    public void commit(Memory memory) {
//        super.commit(memory);
//        
//        put("concept.count", conceptNum);
//        
//        put("concept.pri.mean", conceptPriorityMean);
//        put("concept.pri.variance", conceptVariance);
//        
//        //in order; 0= top 25%, 1 = 50%..75%.., etc
//        for (int n = 0; n < conceptHistogram.length; n++)
//            put("concept.pri.histo#" + n, conceptHistogram[n]);
//        
//        put("concept.belief.mean", conceptNum > 0 ? ((double)conceptBeliefsSum)/conceptNum : 0);
//        put("concept.question.mean", conceptNum > 0 ? ((double)conceptQuestionsSum)/conceptNum : 0);
//        
//        put("task.novel.total", memory.novelTasks.size());
//        //put("memory.newtasks.total", memory.newTasks.size()); //redundant with output.tasks below
//
//        //TODO move to EmotionState
//        put("emotion.happy", memory.emotion.happy());
//        put("emotion.busy", memory.emotion.busy());
//
//        
//        {
//            //DataSet reason = TASKLINK_REASON.get();
//            put("reason.fire.tasklink.pri.mean", TASKLINK_FIRE.mean());
//            put("reason.fire.tasklinks", TASKLINK_FIRE.getHits());
//            
//            putHits(REASON);
//            
//            //only makes commit as a mean, since it occurs multiple times during a cycle
//            put("reason.tasktermlink.pri.mean", REASON.mean());                        
//        }
//        {
//            putValue(IO_INPUTS_BUFFERED);
//        }
//        {            
//            putHits(CONTRAPOSITION);
//            
//            //put("reason.contrapositions.complexity.mean", CONTRAPOSITION.get().mean());
//            
//            putHits(BELIEF_REVISION);
//            put("reason.ded_2nd_layer_variable_unification_terms", DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS.getHits());
//            put("reason.ded_2nd_layer_variable_unification", DED_SECOND_LAYER_VARIABLE_UNIFICATION.getHits());
//            put("reason.ded_conjunction_by_question", DED_CONJUNCTION_BY_QUESTION.getHits());
//            
//            putHits(ANALOGY);
//        }
//        {
//            DataSet d = DERIVATION_LATENCY.get();
//            double min = d.min();
//            if (!Double.isFinite(min)) min = 0;
//            double max = d.max();
//            if (!Double.isFinite(max)) max = 0;
//            
//            put(DERIVATION_LATENCY.name() + ".min", min);
//            put(DERIVATION_LATENCY.name() + ".max", max);
//            put(DERIVATION_LATENCY.name() + ".mean", d.mean());
//        }
//        {
//            putHits(TASK_ADD_NEW);
//            putHits(TASK_ADD_NOVEL);            
//            put("task.derived", TASK_DERIVED.getHits());
//            
//            put("task.pri.mean#added", TASK_ADD_NEW.getReset().mean());
//            put("task.pri.mean#derived", TASK_DERIVED.getReset().mean());
//            put("task.pri.mean#executed", TASK_EXECUTED.getReset().mean());
//            
//            put("task.executed", TASK_EXECUTED.getHits());
//            
//            put("task.immediate.process", TASK_IMMEDIATE_PROCESS.getHits());
//            //put("task.immediate_processed.pri.mean", TASK_IMMEDIATE_PROCESS.get().mean());
//        }
//        {
//            put("task.link_to", LINK_TO_TASK.getHits());
//            put("task.process#goal", GOAL_PROCESS.getHits());
//            put("task.process#judgment", JUDGMENT_PROCESS.getHits());
//            put("task.process#question", QUESTION_PROCESS.getHits());            
//        }
//        
//        
//        putHits(SHORT_TERM_MEMORY_UPDATE);
//        
//        {
//            putHits(SOLUTION_BEST);
//            put("task.solved.best.pri.mean", SOLUTION_BEST.get().mean());
//        }
//        
//        
//        {
//            
//            put("plan.graph#edge", PLAN_GRAPH_EDGE.getValue());
//            put("plan.graph#vertex", PLAN_GRAPH_VERTEX.getValue());
//            
//            put("plan.graph.add#other", PLAN_GRAPH_IN_OTHER.getHits());
//            put("plan.graph.add#operation", PLAN_GRAPH_IN_OPERATION.getHits());
//            put("plan.graph.add#interval", PLAN_GRAPH_IN_DELAY_MAGNITUDE.getHits());
//            put("plan.graph.in.delay_magnitude.mean", PLAN_GRAPH_IN_DELAY_MAGNITUDE.getReset().mean());
//
//            put("plan.task#executable", PLAN_TASK_EXECUTABLE.getReset().sum());
//            put("plan.task#planned", PLAN_TASK_PLANNED.getReset().sum());
//
//        }
//    }
//    
//    public void putValue(final ValueMeter s) {
//        put(s.getName(), s.getValue());
//    }
//    public void putHits(final ValueMeter s) {
//        put(s.getName(), s.getHits());
//    }
//    public void putMean(final ValueMeter s) {
//        put(s.getName(), s.get().mean());
//    }

    public void setConceptBeliefsSum(long conceptBeliefsSum) {
        this.conceptBeliefsSum = conceptBeliefsSum;
    }

    public void setConceptNum(long conceptNum) {
        this.conceptNum = conceptNum;
    }

    public void setConceptPriorityMean(double conceptPriorityMean) {
        this.conceptPriorityMean = conceptPriorityMean;
    }

//    public void setConceptPrioritySum(double conceptPrioritySum) {
//        this.conceptPrioritySum = conceptPrioritySum;
//    }

    public void setConceptQuestionsSum(long conceptQuestionsSum) {
        this.conceptQuestionsSum = conceptQuestionsSum;
    }

    public void setConceptPriorityVariance(double variance) {
        this.conceptVariance = variance;
    }

    public void setConceptPriorityHistogram(double[] histogram) {
        this.conceptHistogram = histogram;
    }

    
    
}
