/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.Events;
import nars.core.Memory;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;

/**
 * Immediate processing of a new task, in constant time Local processing, in
 * one concept only
 */
public class ImmediateProcess extends NAL {


    public ImmediateProcess(Memory mem) {
        this(mem, null);
    }

    public ImmediateProcess(Memory mem, Task task) {
        super(mem, task);

    }
    
    

    @Override
    public String toString() {
        return "ImmediateProcess[" + getCurrentTask().toString() + "]";
    }



    @Override
    public void reason() {
        Concept c = memory.conceptualize(currentTask.budget, getCurrentTask().getTerm());
        if (c == null) return;

        boolean processed = c.directProcess(this, currentTask);
        if (!processed) return;

        c.link(currentTask);

        emit(Events.TaskImmediateProcessed.class, getCurrentTask(), this, c);

        memory.logic.TASK_IMMEDIATE_PROCESS.hit();
    }


}