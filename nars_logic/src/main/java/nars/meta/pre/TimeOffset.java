package nars.meta.pre;

import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.meta.RuleMatch;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Temporal;
import nars.premise.Premise;
import nars.term.Atom;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class TimeOffset extends PreCondition1 {

    final boolean positive;
    final int direction;

    public TimeOffset(Term arg1, Term operator, boolean positive) {
        super(arg1);

        if ((
            this.direction = relationDirection
                    .getIfAbsent(operator, Integer.MIN_VALUE)
        ) == Integer.MIN_VALUE)
            throw new RuntimeException("unrecognized TimeOffset parameter: " + operator);

        this.positive = positive;
    }

    @Override public boolean test(final RuleMatch m) {
        return super.test(m);
    }

    @Override
    public boolean test(RuleMatch m, Term arg) {

        if (arg == null) return false;
        if (arg.getTemporalOrder() != Temporal.ORDER_NONE) { //this is the expected case, namely that there is a order
        } else {
            return true; //and this means its no-order so dont shift
        }

        long s = positive ? +1 : -1;
        m.occurenceAdd( s * timeOffsetForward(arg, m.premise) ); //shift since it has an order..

        return true;
    }


    static final ObjectIntHashMap<Atom> relationDirection = new ObjectIntHashMap<>(16);
    static {
        Procedure2<String,Integer> r = (k, v) -> relationDirection.put(Atom.the('"' + k + '"'), v);

        r.value("=/>", +1);
        r.value("=\\>", -1);
        r.value("=|>", 0);
        r.value("==>", 0);

        r.value("<|>", 0);
        r.value("</>", +1);
        r.value("<=>", 0);
    }


    long timeOffsetForward(final Term arg, final Premise nal) {

        //NOTE intervals should not appear this point;
        // add here a general purpose 'interval span' method
        // that any term will report its total interval.
        // a sequence will return the sum of its intermvals, for example
        //
//        if (arg instanceof AbstractInterval) {
//            return ((AbstractInterval) arg).cycles(nal.memory());
//        }
        if (direction == 0) return 0;

        int dur = nal.duration();

        long cycles;
        if (arg instanceof Sequence) {
            cycles = dur/2 + ((Sequence)arg).intervalLength();
        }
        else  {
            //default: assume duration or half-duration # of cycles for the term
            cycles = dur/2;
        }

        return direction * cycles;
    }

    @Override
    public String toString() {
        return (positive ? "Pos" : "Neg") + super.toString() + '[' + direction + ']';
    }
}
