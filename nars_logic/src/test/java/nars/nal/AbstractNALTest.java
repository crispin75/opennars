package nars.nal;

import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nar.Default;
import nars.nar.SingleStepNAR;
import org.junit.Ignore;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTest {

    @Deprecated public static final List<Supplier<NAR>> core1 = Lists.newArrayList(
            () -> new Default().nal(2),
            () -> new SingleStepNAR().nal(2)
    );
    public static final List<Supplier<NAR>> core2 = Lists.newArrayList(
            /** for some reason, NAL2 tests require nal(3) level */
            () -> new Default().nal(3),
            () -> new SingleStepNAR().nal(3)
    );
    public static final List<Supplier<NAR>> core3 = Lists.newArrayList(
            () -> new Default().nal(4),
            () -> new SingleStepNAR().nal(4)
    );
    @Deprecated public static final List<Supplier<NAR>> core4 = Lists.newArrayList(
            () -> new Default().nal(4),
            () -> new SingleStepNAR().nal(4)
    );
    public static final List<Supplier<NAR>> core5 = Lists.newArrayList(
            () -> new Default().nal(5),
            () -> new SingleStepNAR().nal(5)
    );
    public static final List<Supplier<NAR>> core6 = Lists.newArrayList(
            //() -> new Default().nal(6),
            () -> new SingleStepNAR().nal(6)
    );
    public static final List<Supplier<NAR>> core8 = Lists.newArrayList(
            () -> new SingleStepNAR().nal(8)
            //() -> new Default().nal(8)
    );
//    public static final List<Supplier<NAR>> core =Lists.newArrayList(
//            () -> new Default().nal(9)
//    );
//    @Deprecated public static final List<Supplier<NAR>> singleStep = core6; /*Lists.newArrayList(
//            () -> new SingleStepNAR().nal(9)
//    );*/

  /*  public static final List<Supplier<NAR>> fullDeclarativeTest =Lists.newArrayList(
            //() -> new Default().nal(1),
            //() -> new Default().nal(2),
            () -> new Default().nal(6),
            () -> new SingleStepNAR().nal(6)
            //() -> new DefaultAlann(48)
    );*/

    //final ThreadLocal<NAR> nars;
    //private final Supplier<NAR> nar;
    private final NAR the;

    protected AbstractNALTest(NAR nar) {
        this.the = nar;
    }

    protected AbstractNALTest(Supplier<NAR> nar) {
        //this.nar = nar;
        this.the = nar.get();
    }


    public final TestNAR test() {
        return new TestNAR(nar());
    }


    public final NAR nar() {
        return the;
    }

    public static Iterable<Supplier<NAR>> nars(int level, boolean multistep) {

        //HACK why are these levels not accurate:
        {
            switch (level) {
                case 1: level = 2; break;
            }
        }

        List<Supplier<NAR>> l = Global.newArrayList();

        final int finalLevel = level;
        l.add( () -> new Default().nal(finalLevel) );

        if (!multistep)
            l.add( () -> new SingleStepNAR().nal(finalLevel) );

        return l;
    }
}
