package at.ac.tuwien.infosys.www.pixy.transduction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class MyAcceptor extends MyAutomaton {
    public MyAcceptor() {
        super();
    }

    public MyAcceptor(MyAutomaton orig) {
        super(orig);
    }

    public MyAcceptor(rationals.Automaton orig) {
        super(orig);
    }

    // converts the given automaton into a regular expression
    public String toRegEx() {

        MyAcceptor aut = this;

        aut.addUniqueStartEnd();

        // automaton to work on
        MyAcceptor my = aut;

        my.mergeTransitions();
        my.toDot("aut1.dot");

        // for all states except the initial/terminal state...
        for (MyState state : my.getStates()) {
            if (state.isTerminal() || state.isInitial()) {
                continue;
            }

            // determine loop regex
            MyTransition loopTrans = my.getTransition(state, state);
            String regex_s = null;
            if (loopTrans == null) {
                // there is no loop
                regex_s = "(1)";
            } else {
                regex_s = (String) loopTrans.getLabel();
            }

            // for all incoming edges (except loops)
            Set<MyTransition> incoming = my.getIncomingNoLoop(state);
            for (MyTransition in : incoming) {

                // an incoming state
                MyState state_qi = in.getStart();
                String regex_qi = (String) in.getLabel();

                // for all outgoing edges (except loops)
                Set<MyTransition> outgoing = my.getOutgoingNoLoop(state);
                for (MyTransition out : outgoing) {

                    // an outgoing state
                    MyState state_pj = out.getEnd();
                    String regex_pj = (String) out.getLabel();

                    // transition from incoming to outgoing state
                    String regex_r = null;
                    MyTransition trans_r = my.getTransition(state_qi, state_pj);
                    if (trans_r == null) {
                        regex_r = "(0)";    // hope that works...
                    } else {
                        // there was already such a transition
                        regex_r = (String) trans_r.getLabel();
                        my.removeTransitions(state_qi, state_pj);
                    }

                    String regex_total = "(" + regex_r + "+" + regex_qi + regex_s + "*" + regex_pj + ")";
                    System.out.println("Regex_total for " + state + " / " + state_qi + "->" + state_pj + ": " + regex_total);

                    // install appropriate transition
                    my.addTransition(new MyTransition(state_qi, regex_total, state_pj));
                }
            }

            // we have now processed all incoming and outgoing edges,
            // so we can remove the current state
            my.removeState(state);
        }

        // the resulting, final regex
        Set<MyTransition> delta = my.getDelta();
        if (delta.size() != 1) {
            throw new RuntimeException("can't determine final regex! delta size: " + delta.size());
        }
        MyTransition trans = delta.iterator().next();
        String final_regex = (String) trans.getLabel();

        return final_regex;
    }

    // label2Int: input/output label -> Integer
    public void toFsmFile(String filename, Map<Object, Integer> label2Int)
        throws IOException {

        Writer writer = new FileWriter(filename + ".txt");

        int i = 1 + label2Int.size();
        Set<MyTransition> delta = this.getDelta();
        for (MyTransition t : delta) {
            Object label = t.getLabel();
            if (label2Int.get(label) == null) {
                label2Int.put(label, i++);
            }
        }

        // print symbols file
        Writer symWriter = new FileWriter(filename + ".sym");
        symWriter.write("EPS 0\n");
        for (Map.Entry<Object, Integer> entry : label2Int.entrySet()) {
            symWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        symWriter.close();

        // print transitions starting from the initial state
        for (MyTransition t : this.getOutgoingTransitions(this.getInitial())) {
            writer.write(t.getStart() + " " + t.getEnd() + " " + t.getLabel() + "\n");
        }

        // print remaining transitions
        for (MyTransition t : delta) {
            if (t.getStart().isInitial()) {
                continue;
            }
            writer.write(t.getStart() + " " + t.getEnd() + " " + t.getLabel() + "\n");
        }

        // print final states
        for (MyState state : this.getTerminals()) {
            writer.write(state + "\n");
        }

        writer.close();
    }
}