package at.ac.tuwien.infosys.www.pixy.transduction;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.automaton.Transition;

public class MyTransducer
extends MyAutomaton {

    // constructs a transducer from the given automaton by duplicating
    // the input labels into output labels.
    public MyTransducer(MyAcceptor orig) {

        // start by cloning
        super(orig);

        // transform transition labels
        Set<MyTransition> origDelta = this.getDelta();
        this.clearTransitions();
        for (MyTransition t : origDelta) {
            MyTransducerRelation tr = new MyTransducerRelation(t.getLabel(), t.getLabel());
            this.addTransition(new MyTransition(t.getStart(), tr, t.getEnd()));
        }
    }

    // clones the given transducer
    public MyTransducer(MyTransducer orig) {
        super(orig);
    }

    public MyTransducer(rationals.transductions.TransducerNivat a) {

        super();

        // auxiliary map from foreign state to my cloned state
        Map<rationals.State,MyState> map = new HashMap<rationals.State,MyState>();

        // copy states, updating auxiliary map on the way
        for (Iterator iter = a.states().iterator(); iter.hasNext(); ) {
            rationals.State e = (rationals.State) iter.next();
            map.put(e, this.addState(e.isInitial(), e.isTerminal()));
        }

        // copy transitions (and transform TransducerRelations)
        for (Iterator iter = a.delta().iterator(); iter.hasNext(); ) {
            rationals.Transition t = (rationals.Transition) iter.next();
            rationals.transductions.TransducerRelation origtr = (rationals.transductions.TransducerRelation) t.label();
            this.addTransition(new MyTransition(
                    map.get(t.start()),
                    new MyTransducerRelation(origtr.getIn(), origtr.getOut()),
                    map.get(t.end())));
        }

    }

    /* getPoint needs an update
    public static MyTransducer type1Marker(String beta) {

        // initialize alphabet (necessary for "." symbol)
        MyAlphabet sigma = new MyAlphabet();

        // construct an automaton for alpha = sigma*beta
        String sigmaStarBeta = sigma.getPoint() + "*" + beta;
        rationals.Automaton ralpha = null;
        try {
            ralpha = (new rationals.converters.analyzers.Parser(sigmaStarBeta)).analyze();
        } catch (rationals.converters.ConverterException e) {
            throw new RuntimeException("SNH");
        }
        MyAcceptor alpha = new MyAcceptor(ralpha);

        // LATER: alpha has to be deterministic

        // construct a transducer from this automaton: chi = Id(alpha)
        MyTransducer chi = new MyTransducer(alpha);

        // construct tau from chi: start with clone
        MyTransducer tau = new MyTransducer(chi);

        // memorize final states of tau
        Set<MyState> oldTerminals = new HashSet<MyState>(tau.terminals);

        // make all states of tau terminal
        for (MyState state : tau.states) {
            tau.setTerminal(state, true);
        }

        // perform the "copy-translation" for each previously terminal state of tau
        for (MyState q : oldTerminals) {

            // make state copy
            MyState q2 = tau.addState(false, true);  // LATER: could the new state be initial?

            // turn all transitions leaving from q into outgoing transitions of q2,
            // and remove them
            // LATER: what about loops in q?
            Set<MyTransition> q_out = tau.getOutgoingNoLoop(q);
            for (MyTransition t : q_out) {
                tau.addTransition(new MyTransition(q2, t.getLabel(), t.getEnd()));
                tau.removeTransition(t);
            }

            // connect q with q2
            tau.addTransition(new MyTransition(q, new MyTransducerRelation(null, "#"),q2));

            // make q non-terminal
            tau.setTerminal(q, false);
        }

        return tau;
    }
    */

    // label2Int: input/output label -> Integer
    public void toFsmFile(String filename, Map<Object,Integer> label2Int)
    throws IOException {

        Writer writer = new FileWriter(filename + ".txt");

        int i = 1 + label2Int.size();
        Set<MyTransition> delta = this.getDelta();
        for (MyTransition t : delta) {
            MyTransducerRelation rel = (MyTransducerRelation) t.getLabel();
            Object inLabel = rel.getIn();
            Object outLabel = rel.getOut();
            if (inLabel != null) {
                if (label2Int.get(inLabel) == null) {
                    label2Int.put(inLabel, i++);
                }
            }
            if (outLabel != null) {
                if (label2Int.get(outLabel) == null) {
                    label2Int.put(outLabel, i++);
                }
            }
        }

        String eps = "EPS";

        // print symbols file
        Writer symWriter = new FileWriter(filename + ".sym");
        symWriter.write(eps + " 0\n");
        for (Map.Entry<Object,Integer> entry : label2Int.entrySet()) {
            symWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        symWriter.close();

        // print transitions starting from the initial state
        for (MyTransition t : this.getOutgoingTransitions(this.getInitial())) {
            writeTransition(t, writer, eps);
        }

        // print remaining transitions
        for (MyTransition t : delta) {
            if (t.getStart().isInitial()) {
                continue;
            }
            writeTransition(t, writer, eps);
        }

        // print final states
        for (MyState state : this.getTerminals()) {
            writer.write(state + "\n");
        }

        writer.close();
    }

    // you can use this if you already have the symbols
    public void toFsmFile(String filename)
    throws IOException {

        Writer writer = new FileWriter(filename + ".txt");

        Set<MyTransition> delta = this.getDelta();

        String eps = "EPS";

        // print transitions starting from the initial state
        for (MyTransition t : this.getOutgoingTransitions(this.getInitial())) {
            writeTransition(t, writer, eps);
        }

        // print remaining transitions
        for (MyTransition t : delta) {
            if (t.getStart().isInitial()) {
                continue;
            }
            writeTransition(t, writer, eps);
        }

        // print final states
        for (MyState state : this.getTerminals()) {
            writer.write(state + "\n");
        }

        writer.close();
    }

    private void writeTransition(MyTransition t, Writer writer, String eps)
    throws IOException {

        MyTransducerRelation rel = (MyTransducerRelation) t.getLabel();
        Object inLabel = rel.getIn();
        String inString;
        if (inLabel == null) {
            // epsilon transition
            inString = eps;
        } else {
            inString = inLabel.toString();
            StringBuilder temp = new StringBuilder();
            Transition.appendCharString(inString.charAt(0), temp);
            inString = temp.toString();
        }

        Object outLabel = rel.getOut();
        String outString;
        if (outLabel == null) {
            // epsilon transition
            outString = eps;
        } else {
            outString = outLabel.toString();
            StringBuilder temp = new StringBuilder();
            Transition.appendCharString(outString.charAt(0), temp);
            outString = temp.toString();
        }

        writer.write(
                t.getStart() + " " +
                t.getEnd() + " " +
                inString + " " +
                outString + " " +
                "\n");
    }
}