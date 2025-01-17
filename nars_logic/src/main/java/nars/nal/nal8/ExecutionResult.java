package nars.nal.nal8;

import nars.Memory;
import nars.task.Task;

import java.io.Serializable;

/**
 * Created by me on 5/16/15.
 */
public class ExecutionResult implements Serializable {

    public final Task<Operation> operation;
    public final Object feedback;
    transient private final Memory memory;

    public ExecutionResult(Task<Operation> op, Object feedback, Memory memory) {
        this.operation = op;
        this.feedback = feedback;
        this.memory = memory;
    }

    public Task getTask() {
        return operation;
    }

    public Operation getOperation() {
        return operation.getTerm();
    }

    @Override
    public String toString() {
        Task t = getTask();
        //if (t == null) return "";

        /*if (operation instanceof ImmediateOperation) {
            return operation.toString();
        } else */{
            //Term[] args = operation.argArray();
            //Term operator = operation.getOperator();
            StringBuilder sb = new StringBuilder();

            t.appendTo(sb, memory);

//                Budget b = getTask();
//                if (b!=null)
//                    sb.append(b.toStringExternal()).append(' ');

            //sb.append(operator).append('(');

            /*
            if (args.length > 0) {
                String argString = Arrays.toString(args);
                sb.append(argString.substring(1, argString.length()-1)); //remove '[' and ']'
            }
            */

            //sb.append(')');

            if (feedback != null)
                sb.append("  ").append(feedback);

            return sb.toString();
        }
    }


}
