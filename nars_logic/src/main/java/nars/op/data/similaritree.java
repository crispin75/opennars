package nars.op.data;

import nars.io.Texts;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.TermFunction;
import nars.term.Term;

/**
 * Uses the levenshtein distance of two term's string represents to
 * compute a similarity metric
 */
public class similaritree extends TermFunction<Float> {

    @Override
    public Float function(Operation o) {
        final Term[] x = o.args();
        if (x.length!=2) return Float.NaN;

        String a = x[0].toString();
        String b = x[1].toString();

        float d = Texts.levenshteinDistance(a, b);
        return 1.0f - (d / (Math.max(a.length(), b.length())));
    }

}
