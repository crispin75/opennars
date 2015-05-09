package nars.prolog;

import nars.NAR;
import nars.model.impl.Default;
import nars.tuprolog.InvalidTheoryException;
import nars.tuprolog.MalformedGoalException;
import nars.tuprolog.NoMoreSolutionException;
import nars.tuprolog.NoSolutionException;

import java.util.List;

/**
 * Created by me on 5/9/15.
 */
public class NARPrologStructuralInference {

    NAR n = new NAR(new Default());
    NARPrologMirror pl = new NARPrologMirror(n, 0.90f, true, true, false) {
        @Override
        public List<String> initAxioms() {
            List<String> l = super.initAxioms();
            l.add("connected(A,B) :- product(A, B).");
            l.add("connected(A,B) :- similarity(A, B).");
            l.add("connected(A,B) :- equivalence(A, B).");
            l.add("connected(A,B) :- conjunction(A, B).");
            l.add("connected(A,B) :- setint(A, B).");
            l.add("connected(A,B) :- setext(A, B).");
            l.add("connected(B,A) :- connected(A,B).");
            //l.add("connected(X,Y) :- member(X,L), member(Y,L).");
            l.add("[A] :- product(A).");
            l.add("[A,B] :- product(A,B).");
            l.add("[A,B,C] :- product(A,B,C).");

            l.add("subject(S) :- inheritance(S,P).");
            l.add("predicate(P) :- inheritance(S,P).");
            l.add("rdf(S,P,O) :- inheritance(product(S,O),P).");


            //https://www.cpp.edu/~jrfisher/www/prolog_tutorial/2_15.html
            l.add("path(A,B,Path) :- travel(A,B,[A],Q),reverse(Q,Path).");
            l.add("travel(A,B,P,[B|P]) :- connected(A,B).");
            l.add("travel(A,B,Visited,Path) :- connected(A,C),C \\== B,\\+member(C,Visited),travel(C,B,[C|Visited],Path).");
            return l;
        }
    };

    public static void main(String[] args) throws MalformedGoalException, NoSolutionException, NoMoreSolutionException, InvalidTheoryException {
        NARPrologStructuralInference p = new NARPrologStructuralInference();
        p.solve("subject(X)");
        p.solve("[A,B]");
        p.solve("rdf(S,P,O)");
        p.solve("connected(A,B)");
        p.solve("path(A,B,C)");
        p.solve("path(a,d,C)");

        p.solve("rdf(a,b,c)");
        p.solve("not(X)");
    }

    public NARPrologStructuralInference() {

        pl.setReportAnswers(true);
        pl.setReportAssumptions(true);


        n.input("<a --> b>.");
        n.run(1);
        n.input("<x <-> y>.");
        n.run(1);


        n.input("<a --> b>?");
        n.run(1);

        n.input("(a,b).");
        n.run(1);

        n.input("(b, a).");
        n.run(1);

        n.input("(b,c).");
        n.run(1);
        n.input("(c,d).");
        n.run(1);
        n.input("(x1,x2,x3,x4,c).");
        n.run(1);

        n.input("(--,<this --> true>).");
        n.run(1);

        n.input("<(*,subj,obj) --> predicate>.");
        n.run(1);

    }


    public void solve(String s) {


        try {
            boolean x = pl.solve(s, 0.05f, t -> {
                System.out.println(s + " ==== " + t);
            });
            if (!x) {
                System.out.println(s + " ==== NO SOLUTION");
            }
        } catch (InvalidTheoryException e) {
            e.printStackTrace();
        }


    }

}
