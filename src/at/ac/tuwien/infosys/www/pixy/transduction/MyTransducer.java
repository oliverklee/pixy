package at.ac.tuwien.infosys.www.pixy.transduction;

import at.ac.tuwien.infosys.www.pixy.automaton.Transition;
import rationals.State;
import rationals.transductions.TransducerRelation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class MyTransducer extends AbstractAutomaton {
    public MyTransducer(rationals.transductions.TransducerNivat a) {
        super();

        // auxiliary map from foreign state to my cloned state
        Map<rationals.State, MyState> map = new HashMap<>();

        // copy states, updating auxiliary map on the way
        Set<State> states = a.states();
        for (State state : states) {
            map.put(state, this.addState(state.isInitial(), state.isTerminal()));
        }

        // copy transitions (and transform TransducerRelations)
        Set<rationals.Transition> delta = a.delta();
        for (rationals.Transition t : delta) {
            TransducerRelation origtr = (TransducerRelation) t.label();
            this.addTransition(new MyTransition(
                map.get(t.start()),
                new MyTransducerRelation(origtr.getIn(), origtr.getOut()),
                map.get(t.end())));
        }
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