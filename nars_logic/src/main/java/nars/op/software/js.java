package nars.op.software;

import nars.NAR;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.operator.TermFunction;
import nars.op.mental.Mental;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Term;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.HashMap;
import java.util.List;

/**
 * Executes a Javascript expression
 */
public class js extends TermFunction implements Mental {

    private static final ThreadLocal<ScriptEngine> js = new ThreadLocal<ScriptEngine>() {
        @Override
        protected ScriptEngine initialValue() {
            ScriptEngineManager factory = new ScriptEngineManager();
            js.set(factory.getEngineByName("JavaScript"));
            return js.get();
        }
    };

    final HashMap global = new HashMap();

    class DynamicFunction extends TermFunction {

        private final String function;
        private Object fnCompiled;

        public DynamicFunction(String name, String function) {
            super(name);
            this.function = function;

            try {
                this.fnCompiled = js.get().eval(function);
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        @Override public Object function(Operation o) {
            Term[] args = o.args();
            Bindings bindings = newBindings(args);
            bindings.put("_o", fnCompiled);
            String input = "_o.apply(this,arg)";

            Object result;
            try {
                result = js.get().eval(input, bindings);
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            return result;
        }


    }

    /** create dynamic javascript functions */
    //TODO make this an ImmediateOperator that will not conceptualize its subterms
    public class jsop extends NullOperator {

        @Override
        public List<Task> apply(Task<Operation> op) {
            Term[] x = op.getTerm().args();
            String funcName = Atom.unquote(x[0]);
            String functionCode = Atom.unquote(x[1]);
            //nar.input( echo.newTask("JS Operator Bind: " + funcName + " = " + functionCode));
            DynamicFunction d = new DynamicFunction(funcName, functionCode);
            nar.on(d);

            //op.stop();

            return null;
        }



    }

//    public class JSBelievedConceptBuilder extends ConstantConceptBuilder {
//
//        private Object fnCompiled;
//
//        public JSBelievedConceptBuilder(String fnsource) {
//
//            ensureJSLoaded();
//
//            try {
//                this.fnCompiled = js.eval(fnsource);
//            }
//            catch (Throwable ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//        @Override
//        protected Truth truth(Term t, Memory m) {
//
//            Bindings bindings = new SimpleBindings();
//            bindings.put("t", t);
//            bindings.put("_o", fnCompiled);
//            String input = "_o.apply(this,[t])";
//
//            Object result;
//            try {
//                result = js.eval(input, bindings);
//            } catch (Throwable ex) {
//                ex.printStackTrace();
//                throw new RuntimeException(ex);
//            }
//
//            if (result instanceof Number) {
//                return new DefaultTruth(((Number)result).floatValue(), 0.99f);
//            }
//            if (result instanceof Object[]) {
//                if (((Object[])result).length > 1) {
//                    Object a = ((Object[])result)[0];
//                    Object b = ((Object[])result)[1];
//                    if ((a instanceof Number) && (b instanceof Number)) {
//                        return new DefaultTruth(((Number) a).floatValue(), ((Number) b).floatValue());
//                    }
//                }
//            }
//
//            return null;
//        }
//    }


//    /** create dynamic javascript functions */
//    public class jsbelief extends NullOperator {
//
//
//        @Override
//        public List<Task> apply(Operation op) {
//            Term[] x = op.args();
//
//            String functionCode = Atom.unquote(x[0]);
//
//            nar.on(new JSBelievedConceptBuilder(functionCode));
//
//            op.stop();
//
//            return null;
//        }
//
//    }


    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        //this is a plugin which attches additional plugins. kind of messy, this will change
        boolean x = super.setEnabled(n, enabled);
        if (enabled) {
            n.on(new jsop());
            //n.on(new jsbelief());
        }
        return x;
    }


    public Bindings newBindings(Term[] args) {

        Bindings bindings = new SimpleBindings();
        bindings.put("global", global);
        bindings.put("js", this);
        bindings.put("arg", args);
        bindings.put("memory", nar());
        bindings.put("nar", nar);

        return bindings;
    }


    @Override public Object function(Operation o) {
        Term[] args = o.args();
        if (args.length < 1) {
            return null;
        }

        // copy over all arguments
        Term[] scriptArguments;
        scriptArguments = new Term[args.length-1];
        System.arraycopy(args, 1, scriptArguments, 0, args.length-1);

        Bindings bindings = newBindings(scriptArguments);


        
        String input = args[0].toString();
        if (input.charAt(0) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        Object result;
        try {

            result = js.get().eval(input, bindings);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

}
