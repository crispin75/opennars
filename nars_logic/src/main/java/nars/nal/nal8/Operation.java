/*
 * Inheritance.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal8;

import nars.Memory;
import nars.Op;
import nars.Symbols;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetExt1;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.Product;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.Arrays;

import static nars.Symbols.COMPOUND_TERM_CLOSER;
import static nars.Symbols.COMPOUND_TERM_OPENER;

/**
 * An operation is interpreted as an Inheritance relation with an operator.
 *
 * TODO generalize it so that Prduct<A> is moved to the generic part, allowing non-products
 * to form Operations
 */
public class Operation<A extends Term> extends Inheritance<SetExt1<Product<A>>, Operator> {



    public Operation(String operatorName, Object... args) {
        this(Operator.the(operatorName), Product.termizedProduct(args));
    }

    public Operation(Operator operator, SetExt1<Product<A>> args) {
        super(args, operator);
    }

    /**
     * Constructor with partial values, called by make
     */
    public Operation(Operator operator, Product<A> argProduct) {
        this(operator, new SetExt1<>(argProduct));
    }


    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Operation<A> clone() {
        return clone(arg());
    }

    public Operation clone(Product args) {
        Operation x = new Operation(getPredicate(), args);
        return x;
    }


    /**
     * gets the term wrapped by the Operator predicate
     */
    public final Term getOperatorTerm() {
        return getOperator().the();
    }
    public final Operator getOperator() {
        return getPredicate();
    }

    public final Product<A> arg() {
        return getSubject().the();
    }

    public final A arg(int i) {
        return arg().term(i);
    }


    public static Task newSubTask(Task parent, Memory m, Compound content, char punctuation, Truth truth, long occ, Budget budget) {
        return newSubTask(parent, m, content, punctuation, truth, occ, budget.getPriority(), budget.getDurability(), budget.getQuality());
    }

    public static Task newSubTask(Task parent, Memory m, Compound content, char punctuation, Truth truth, long occ, float p, float d, float q) {
        return TaskSeed.make(m, content)
                .punctuation(punctuation)
                .truth(truth)
                .budget(p, d, q)
                .parent(parent)
                .occurr(occ);
    }


//    public Term[] arg(Memory memory) {
//        return arg(memory, false);
//    }
//
//    public Term[] arg(Memory memory, boolean evaluate) {
//        return arg(memory, evaluate, true);
//    }
//
//    public Term[] arg(Memory memory, boolean evaluate, boolean removeSelf) {
//        final Term[] rawArgs = args();
//        int numInputs = rawArgs.length;
//
//        if (removeSelf) {
//            if (numInputs > 0) {
//                if (rawArgs[numInputs - 1].equals(memory.self()))
//                    numInputs--;
//            }
//        }
//
//        if (numInputs > 0) {
//            if (rawArgs[numInputs - 1] instanceof Variable)
//                numInputs--;
//        }
//
//
//        Term[] x;
//
//        if (evaluate) {
//
//            x = new Term[numInputs];
//            for (int i = 0; i < numInputs; i++) {
//                x[i] = eval.eval(rawArgs[i], memory);
//            }
//        } else {
//            x = Arrays.copyOfRange(rawArgs, 0, numInputs);
//        }
//
//        return x;
//    }


//    /**
//     * produces a cloned instance with the replaced args + additional terms in a new argument product
//     */
//    public Operation cloneWithArguments(Term[] args, Term... additional) {
//        return (Operation) cloneReplacingSubterm(0, Product.make(args, additional));
//    }

    /**
     * returns a reference to the raw arguments as contained by the Product subject of this operation
     * avoid using this because it may involve creation of unnecessary array
     * if Product1.terms() is called
     */
    public A[] args() {
        return arg().terms();
    }

    public Concept getConcept(Memory m) {
        if (m == null) return null;
        return m.concept(getTerm());
    }

    public Truth getConceptDesire(Memory m) {
        Concept c = getConcept(m);
        if (c == null) return null;
        return c.getDesire();
    }

    public float getConceptExpectation(Memory m) {
        Truth tv = getConceptDesire(m);
        if (tv == null) return 0;
        return tv.getExpectation();
    }



//    /**
//     * if any of the arguments are 'eval' operations, replace its result
//     * in that position in a cloned Operation instance
//     *
//     * @return
//     */
//    public Operation inline(Memory memory, boolean removeSelf) {
//        //TODO avoid clone if it does not involve any eval()
//        //if (!hasEval()) return this;
//        return clone(Product.make(arg(memory, true, removeSelf /* keep SELF term at this point */)));
//    }

//    protected boolean hasEval() {
//        for (Term x : arg().term) {
//            if (x instanceof Operation) {
//                Operation o = (Operation)x;
//                if (o.getOperator().equals(eval.term)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


    public int numArgs() {
        return arg().length();
    }

    public static boolean isA(Term x, Term someOperatorTerm) {
        if (x instanceof Operation) {
            Operation o = (Operation) x;
            if (o.getOperatorTerm().equals(someOperatorTerm))
                return true;
        }
        return false;
    }



    @Override
    public byte[] bytes() {

        byte[] op = getOperatorTerm().bytes();
        //Term[] arg = argArray();

        int len = op.length + 1 + 1;
        int n = 0;

        final Term[] xt = arg().terms();
        for (final Term t : xt) {
            len += t.bytes().length;
            n++;
        }
        if (n > 1) len += n - 1;


        final ByteBuf b = ByteBuf.create(len);
        b.append(op); //add the operator name without leading '^'
        b.append((byte) COMPOUND_TERM_OPENER);


        n = 0;
        for (final Term t : xt) {
            /*if(n==arg.length-1) {
                break;
            }*/
            if (n != 0)
                b.add((byte) Symbols.ARGUMENT_SEPARATOR);

            b.add(t.bytes());

            n++;
        }

        b.append((byte) COMPOUND_TERM_CLOSER);

        return b.toBytes();
    }

    @Override
    public void append(Appendable p, boolean pretty) throws IOException {

        Term predTerm = getOperatorTerm();

        if ((predTerm.volume() != 1) || (predTerm.hasVar())) {
            //if the predicate (operator) of this operation (inheritance) is not an atom, use Inheritance's append format
            super.append(p, pretty);
            return;
        }


        final Term[] xt = arg().terms();

        predTerm.append(p, pretty); //add the operator name without leading '^'
        p.append(COMPOUND_TERM_OPENER);


        int n = 0;
        for (final Term t : xt) {
            if (n != 0) {
                p.append(Symbols.ARGUMENT_SEPARATOR);
                if (pretty)
                    p.append(' ');
            }

            t.append(p, pretty);


            n++;
        }

        p.append(COMPOUND_TERM_CLOSER);

    }


    final static int ProductInSetExtPattern =
            Op.bitStructure(
                    Op.SET_EXT,
                    Op.PRODUCT);

    public static Product getArgumentProduct(Compound c) {
        /*if (!c.impossibleStructure(ProductInSetExtPattern)) {
            if (c instanceof SetExt) {*/
                SetExt1 sc = ((SetExt1) c);
                //if (sc.length() == 1) {
                    final Term scp = sc.the(); //term(0);
                    //if (scp instanceof Product) {
                        return (Product) scp;
                    //}
                //}
            /*}
        }*/
        //return null;
    }


    public String argString() {
        return Arrays.toString(args());
    }

    /**
     * creates a result term in the conventional format
     */
    public static Inheritance result(Term op, Product x, Term y) {
        return Inheritance.make(
                SetExt.make(y),
                ImageExt.make(x, op, (short) (x.length() - 1) /* position of the variable */)
        );
    }

}
