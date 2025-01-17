package nars.nal.nal3;


import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL3Test extends AbstractNALTest {

    public static final int cycles = 64;

    public NAL3Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{index}:{0}")
    public static Collection configurations() {
        return AbstractNALTest.core3;
    }


    @Test
    public void compound_composition_two_premises() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> swimmer>",0.9f,0.9f); //.en("Swan is a type of swimmer.");
        tester.believe("<swan --> bird>", 0.8f, 0.9f); //.en("Swan is a type of bird.");
        tester.mustBelieve(cycles, "<swan --> (|,bird,swimmer)>", 0.98f, 0.81f); //.en("Swan is a type of bird or a type of swimmer.");
        tester.mustBelieve(cycles, "<swan --> (&,bird,swimmer)>",0.72f,0.81f); //.en("Swan is a type of swimming bird.");
        tester.run();
    }

    @Test
    public void compound_composition_two_premises2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<sport --> competition>",0.9f,0.9f); //.en("Sport is a type of competition.");
        tester.believe("<chess --> competition>", 0.8f, 0.9f); //.en("Chess is a type of competition.");
        tester.mustBelieve(cycles, "<(|,chess,sport) --> competition>", 0.72f ,0.81f); //.en("If something is either chess or sport, then it is a competition.");
        tester.mustBelieve(cycles, "<(&,chess,sport) --> competition>", 0.98f, 0.81f); //.en("If something is both chess and sport, then it is a competition.");
        tester.run();
    }

    @Test
    public void compound_decomposition_two_premises() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin --> (|,bird,swimmer)>",1.0f,0.9f); //.en("Robin is a type of bird or a type of swimmer.");
        tester.believe("<robin --> swimmer>", 0.0f, 0.9f); //.en("Robin is not a type of swimmer.");
        tester.mustBelieve(cycles, "<robin --> bird>", 1.0f ,0.81f); //.en("Robin is a type of bird.");
        tester.run();
    }

    @Test //works, just control related issue (DecomposeNegativeNegativeNegative)
    public void compound_decomposition_two_premises2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin --> swimmer>",0.0f,0.9f); //.en("Robin is not a type of swimmer.");
        tester.believe("<robin --> (-,mammal,swimmer)>", 0.0f, 0.9f); //.en("Robin is not a nonswimming mammal.");
        tester.mustBelieve(cycles, "<robin --> mammal>", 0.0f ,0.81f); //.en("Robin is not a type of mammal.");
        tester.run();
    }

    @Test
    public void set_operations() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<planetX --> {Mars,Pluto,Venus}>",0.9f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
        tester.believe("<planetX --> {Pluto,Saturn}>", 0.7f,0.9f); //.en("PlanetX is probably Pluto or Saturn.");
        tester.mustBelieve(cycles, "<planetX --> {Mars,Pluto,Saturn,Venus}>", 0.97f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
        tester.mustBelieve(cycles, "<planetX --> {Pluto}>", 0.63f ,0.81f); //.en("PlanetX is probably Pluto.");
        tester.run();
    }

    @Test
    public void set_operations2() throws InvalidInputException {
        Global.DEBUG = true;
        TestNAR tester = test();
        tester.believe("<planetX --> {Mars,Pluto,Venus}>",0.9f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
        tester.believe("<planetX --> {Pluto,Saturn}>", 0.1f, 0.9f); //.en("PlanetX is probably neither Pluto nor Saturn.");
        tester.mustBelieve(cycles, "<planetX --> {Mars,Pluto,Saturn,Venus}>", 0.91f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
        tester.mustBelieve(cycles, "<planetX --> {Mars,Venus}>", 0.81f ,0.81f); //.en("PlanetX is either Mars or Venus.");
        tester.run();
    }

    @Test
    public void set_operations3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<planetX --> [marsy,earthly,venusy]>",1.0f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
        tester.believe("<planetX --> [earthly,saturny]>", 0.1f, 0.9f); //.en("PlanetX is probably neither Pluto nor Saturn.");
        tester.mustBelieve(cycles, "<planetX --> [marsy,earthly,saturny,venusy]>", 0.1f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
        tester.mustBelieve(cycles, "<planetX --> [marsy,venusy]>", 0.90f ,0.81f); //.en("PlanetX is either Mars or Venus.");
        tester.run();
    }

    @Test
    public void set_operations4() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<[marsy,earthly,venusy] --> planetX>",1.0f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
        tester.believe("<[earthly,saturny] --> planetX>", 0.1f, 0.9f); //.en("PlanetX is probably neither Pluto nor Saturn.");
        tester.mustBelieve(cycles, "<[marsy,earthly,saturny,venusy] --> planetX>", 1.0f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
        tester.mustBelieve(cycles, "<[marsy,venusy] --> planetX>", 0.90f ,0.81f); //.en("PlanetX is either Mars or Venus.");
        tester.run();
    }

    @Test
    public void set_operations5() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<{Mars,Pluto,Venus} --> planetX>",1.0f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
        tester.believe("<{Pluto,Saturn} --> planetX>", 0.1f, 0.9f); //.en("PlanetX is probably neither Pluto nor Saturn.");
        tester.mustBelieve(cycles, "<{Mars,Pluto,Saturn,Venus} --> planetX>", 0.1f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
        tester.mustBelieve(cycles, "<{Mars,Venus} --> planetX>", 0.90f ,0.81f); //.en("PlanetX is either Mars or Venus.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",0.9f,0.9f); //.en("Bird is a type of animal.");
        tester.ask("<(&,bird,swimmer) --> (&,animal,swimmer)>"); //.en("Is a swimming bird a type of swimming animal?");
        tester.mustBelieve(cycles, "<(&,bird,swimmer) --> (&,animal,swimmer)>", 0.90f ,0.73f); //.en("A swimming bird is probably a type of swimming animal.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement_2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",0.9f,0.9f); //.en("Bird is a type of animal.");
        tester.ask("<(|,bird,swimmer) --> (|,animal,swimmer)>"); //.en("Is a swimming bird a type of swimming animal?");
        tester.mustBelieve(cycles, "<(|,bird,swimmer) --> (|,animal,swimmer)>", 0.90f ,0.73f); //.en("A swimming bird is probably a type of swimming animal.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",0.9f,0.9f); //.en("Bird is a type of animal.");
        tester.ask("<(-,swimmer,animal) --> (-,swimmer,bird)>"); //.en("Is a nonanimal swimmer a type of a nonbird swimmer?");
        tester.mustBelieve(cycles, "<(-,swimmer,animal) --> (-,swimmer,bird)>", 0.90f ,0.73f); //.en("A nonanimal swimmer is probably a type of nonbird swimmer.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement2_2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",0.9f,0.9f); //.en("Bird is a type of animal.");
        tester.ask("<(~,swimmer,animal) --> (~,swimmer,bird)>"); //.en("Is a nonanimal swimmer a type of a nonbird swimmer?");
        tester.mustBelieve(cycles, "<(~,swimmer,animal) --> (~,swimmer,bird)>", 0.90f ,0.73f); //.en("A nonanimal swimmer is probably a type of nonbird swimmer.");
        tester.run();
    }

    @Test
     public void compound_composition_one_premise() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>",0.9f,0.9f); //.en("Swan is a type of bird.");
        tester.ask("<swan --> (|,bird,swimmer)>"); //.en("Is a swan a type of bird or swimmer?");
        tester.mustBelieve(cycles, "<swan --> (|,bird,swimmer)>", 0.90f ,0.73f); //.en("A swan is probably a type of bird or swimmer.");
        tester.run();
    }

    @Test
     public void compound_composition_one_premise2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>",0.9f,0.9f); //.en("Swan is a type of bird.");
        tester.ask("<(&,swan,swimmer) --> bird>"); //.en("Is swimming swan a type of bird?");
        tester.mustBelieve(cycles, "<(&,swan,swimmer) --> bird>", 0.90f ,0.73f); //.en("Swimming swan is a type of bird.");
        tester.run();
    }

    @Test
    public void compound_composition_one_premise3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>",0.9f,0.9f); //.en("Swan is a type of bird.");
        tester.ask("<swan --> (-,swimmer,bird)>"); //.en("Is swan a type of nonbird swimmer?");
        tester.mustBelieve(cycles, "<swan --> (-,swimmer,bird)>", 0.10f ,0.73f); //.en("A swan is not a type of nonbird swimmer.");
        tester.run();
    }

    @Test
    public void compound_composition_one_premise4() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>",0.9f,0.9f); //.en("Swan is a type of bird.");
        tester.ask("<(~,swimmer, swan) --> bird>"); //.en("Is being bird what differ swimmer from swan?");
        tester.mustBelieve(cycles, "<(~,swimmer, swan) --> bird>", 0.10f, 0.73f); //.en("What differs swimmer from swan is not being bird.");
        tester.run();
    }

    @Test
    public void compound_decomposition_one_premise() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin --> (-,bird,swimmer)>", 0.9f, 0.9f); //.en("Robin is a type of nonswimming bird.");
        tester.mustBelieve(cycles, "<robin --> bird>", 0.90f ,0.73f); //.en("Robin is a type of bird.");
        tester.run();
    }

    @Test
    public void compound_decomposition_one_premise2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(|, boy, girl) --> youth>", 0.9f, 0.9f); //.en("Boys and gials are youth.");
        tester.mustBelieve(cycles, "<boy --> youth>", 0.90f ,0.73f); //.en("Boys are youth.");
        tester.run();
    }

    @Test
    public void compound_decomposition_one_premise3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(~, boy, girl) --> [strong]>", 0.9f, 0.9f); //.en("What differs boys from girls are being strong.");
        tester.mustBelieve(cycles, "<boy --> [strong]>", 0.90f ,0.73f); //.en("Boys are strong.");
        tester.run();
    }
}

