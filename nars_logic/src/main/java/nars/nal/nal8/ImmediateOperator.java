package nars.nal.nal8;


import nars.$;
import nars.Symbols;
import nars.nal.nal4.Product;
import nars.nal.nal8.operator.NullOperator;
import nars.task.DefaultTask;
import nars.task.Task;

import java.util.List;
import java.util.function.Consumer;

/** an operation that executes immediately, and without logical consequences;
 *  used for system control functions  */
abstract public class ImmediateOperator extends NullOperator implements Consumer<Task<Operation>> {

    public final Operator op;

    public ImmediateOperator() {
        super();
        op = Operator.the(getOperatorTerm());
    }

    public Operation newOperation(Object...args) {
        return newOperation(Product.termizedProduct(args));
    }
//    public Operation newOperation(Term...args) {
//        return newOperation(Product.make(args));
//    }
    public Operation newOperation(Product args) {
        return $.opr(args, op);
    }

    /** create a new task that wraps this operation */
    public Task<Operation> newTask(Operation o) {
        return new DefaultTask(newOperation(o.args()), Symbols.COMMAND,
                null, 0, 0, 0).normalized();
    }

    @Override
    public List<Task> apply(Task<Operation> o) {
        accept(o);
        return null;
    }

    public static Task<Operation> command(Class<? extends ImmediateOperator> opClass, Object... args) {
        return Task.command(operation(opClass, args));
    }

    public static Operation operation(Class<? extends ImmediateOperator> opClass, Object... args) {
        return new Operation( opClass.getSimpleName(), args);
    }


    //    //TODO make Task an interface so this is lightweight
//    public static class ImmediateTask extends DefaultTask<Compound> {
//
//        public final ImmediateOperator operation;
//
//        ImmediateTask(ImmediateOperator o) {
//            super(null,null,null,null);
//            this.operation = o;
//        }
//
//
//        public ImmediateOperator immediateOperation() {
//            return operation;
//        }
//
//        @Override
//        public CharSequence stampAsStringBuilder() {
//            return "";
//        }
//
//        /**
//         * @return true if it was immediate
//         */
//        @Override
//        public boolean executeIfImmediate(Memory memory) {
////            final Term taskTerm = get();
////            if (taskTerm instanceof Operation) {
////                Operation o = (Operation) taskTerm;
////                o.setTask((Task<Operation>) this);
////
////
////                if (o instanceof ImmediateOperation) {
//                    if (getPunctuation()!= Symbols.GOAL)
//                        throw new RuntimeException("ImmediateOperation " + immediateOperation() + " was not specified with goal punctuation");
//
//                    immediateOperation().execute(memory);
//
//                    return true;
//                //}
////            else if (o.getOperator().isImmediate()) {
////                if (sentence!=null && getPunctuation()!= Symbols.GOAL)
////                    throw new RuntimeException("ImmediateOperator call " + o + " was not specified with goal punctuation");
////
////                o.getOperator().execute(o, memory);
////                return true;
////            }
//            //}
//
////            return false;
//        }
//
//
//        @Override
//        public String toString() {
//            return operation.toString();
//        }
//    }



}
