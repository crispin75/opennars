package nars.io;

import nars.LocalMemory;
import nars.Memory;
import nars.Op;
import nars.Symbols;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal3.IntersectionExt;
import nars.nal.nal7.Temporal;
import nars.nal.nal7.Tense;
import nars.narsese.InvalidInputException;
import nars.narsese.NarseseParser;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import org.junit.Test;

import static nars.io.NarseseParserTest.task;
import static nars.io.NarseseParserTest.term;
import static nars.nal.nal7.Tense.*;
import static org.junit.Assert.*;

/**
 * Proposed syntax extensions, not implemented yet
 */
public class NarseseParserExtendedTest  {



    void eternal(Task t) {
        tensed(t, true, null);
    }
    void tensed(Task t, Tense w) {
        tensed(t, false, w);
    }
    void tensed(Task t, boolean eternal, Tense w) {
        assertEquals(eternal, Temporal.isEternal(t.getOccurrenceTime()));
        if (!eternal) {
            switch (w) {
                case Past: assertTrue(t.getOccurrenceTime() < 0); break;
                case Future: assertTrue(t.getOccurrenceTime() > 0); break;
                case Present: assertTrue(t.getOccurrenceTime() == 0); break;
            }
        }
    }

    @Test public void testOriginalTruth() {
        //singular form, normal, to test that it still works
        eternal(task("(a & b). %1.0%"));

        //normal, to test that it still works
        tensed(task("(a & b). :|: %1.0%"), Present);
    }

    /** compact representation combining truth and tense */
    @Test public void testTruthTense() {


        tensed(task("(a & b). %1.0|0.7%"), Present);

        tensed(task("(a & b). %1.0/0.7%"), Future);
        tensed(task("(a & b). %1.0\\0.7%"), Past);
        eternal(task("(a & b). %1.0;0.7%"));

        /*tensed(task("(a & b). %1.0|"), Present);
        tensed(task("(a & b). %1.0/"), Future);
        tensed(task("(a & b). %1.0\\"), Past);*/
        eternal(task("(a & b). %1.0%"));





    }

    @Test public void testQuestionTenseOneCharacter() {
        //TODO one character tense for questions/quests since they dont have truth values
    }

    @Test
    public void testColonReverseInheritance() {
        Inheritance t = term("namespace:named");
        assertEquals(t.op(), Op.INHERITANCE);
        assertEquals("namespace", t.getPredicate().toString());
        assertEquals("named", t.getSubject().toString());


        Compound u = term("<a:b --> c:d>");
        assertEquals("<<b --> a> --> <d --> c>>", u.toString());

        Task ut = task("<a:b --> c:d>.");
        assertNotNull(ut);
        assertEquals(ut.getTerm(), u);

    }
//    @Test
//    public void testBacktickReverseInstance() {
//        Inheritance t = term("namespace`named");
//        assertEquals(t.op(), Op.INHERITANCE);
//        assertEquals("namespace", t.getPredicate().toString());
//        assertEquals("{named}", t.getSubject().toString());
//    }


    static void eqTerm(String shorter, String expected) {
        NarseseParser p = NarseseParser.the();

        Term a = p.term(shorter);
        assertNotNull(a);
        assertEquals(expected, a.toString());

        eqTask(shorter, expected);
    }

    final static Memory m = new LocalMemory();

    static void eqTask(String x, String b) {
        NarseseParser p = NarseseParser.the();

        Task a = p.task(x + ".", m);
        assertNotNull(a);
        assertEquals(b, a.getTerm().toString());
    }

    @Test
    public void testNamespaceTerms2() {


        eqTerm("a:b", "<b --> a>");
        eqTerm("a : b", "<b --> a>");


        eqTerm("d:{a,b}:c", "<<c --> {a,b}> --> d>");

        //this one is important that we fix. i think the presence of the -- operator gets confused with negation and the > with end of statement
        eqTerm("{a,b}:c", "<c --> {a,b}>");

        eqTerm("(a,b):c", "<c --> (a,b)>");


        eqTerm("c:{a,b}", "<{a,b} --> c>");
        eqTerm("a:b:c",   "<<a --> b> --> c>");
        eqTerm("a :b :c",   "<<a --> b> --> c>");
    }

    @Test
    public void testNamespaceLikeJSON() {
        NarseseParser p = NarseseParser.the();
        Term a = p.term("{ a:x, b:{x,y} }");
        assertNotNull(a);
        assertEquals("{<{x, y} --> b>, <x --> a>}", a.toString());

    }

    @Test
    public void testNegation2() throws InvalidInputException {


        for (String s : new String[]{"--negated!", "-- negated!"}) {
            Task t = task(s);

            //System.out.println(t);
            /*
            (--,(negated))! %1.00;0.90% {?: 1}
            (--,(negated))! %1.00;0.90% {?: 2}
            */

            Term tt = t.getTerm();
            assertTrue(tt instanceof Negation);
            assertTrue(((Negation) tt).the().toString().equals("negated"));
            assertTrue(t.getPunctuation() == Symbols.GOAL);
        }
    }

    @Test
    public void testNegation3() {
        //without comma
        assertEquals( "(--,x)", term("--x").toStringCompact() );
        assertEquals( "(--,x)", term("-- x").toStringCompact() );

        assertEquals( "(--,(&&,x,y))", term("-- (x && y)").toStringCompact() );


        Negation nab = term("--(a & b)");
        assertTrue(nab instanceof Negation);
        IntersectionExt ab = (IntersectionExt) nab.the();
        assertTrue(ab instanceof IntersectionExt);

//        try {
//            task("(-- negated illegal_extra_term)!");
//            assertTrue(false);
//        } catch (Exception e) {
//        }
    }
}
