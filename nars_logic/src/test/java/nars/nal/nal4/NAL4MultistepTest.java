package nars.nal.nal4;

import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL4MultistepTest extends AbstractNALTest {


    public NAL4MultistepTest(Supplier<NAR> b) {
        super(b);
    }

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Supplier<NAR>> configurations() {
        return AbstractNALTest.nars(4, true);
    }

    //this test only works because the confidence matches, but the related task has insufficient budget
    @Test
    public void nal4_everyday_reasonoing() throws InvalidInputException {
        int time = 1250;

        //Global.DEBUG = true;

        TestNAR tester = test();
        tester.believe("<{sky} --> [blue]>",1.0f,0.9f); //en("the sky is blue");
        tester.believe("<{tom} --> cat>",1.0f,0.9f); //en("tom is a cat");
        tester.believe("<({tom},{sky}) --> likes>",1.0f,0.9f); //en("tom likes the sky");
        //tester.runUntil(1000);

        tester.mustBelieve(time, "<(cat,[blue]) --> likes>", 1.0f, 0.42f); //en("A base is something that has a reaction with an acid.");

        test().ask("<(cat,[blue]) --> likes>"); //cats like blue?

        tester.run();
    }

    //like seen when changing the expected confidence in mustBelief, or also in the similar list here we have such a ghost task where I expect better budget:

    @Test
    public void multistep_budget_ok() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<{sky} --> [blue]>",1.0f,0.9f); //en("the sky is blue");
        tester.believe("<{tom} --> cat>",1.0f,0.9f); //en("tom is a cat");
        tester.believe("<(*,{tom},{sky}) --> likes>",1.0f,0.9f); //en("tom likes the sky");
        test().ask("<(*,cat,[blue]) --> likes>"); //cats like blue?
        tester.mustBelieve(2500, "<(*,cat,[blue]) --> likes>", 1.0f, 0.42f); //en("A base is something that has a reaction with an acid.");
        tester.run();
    }

}
