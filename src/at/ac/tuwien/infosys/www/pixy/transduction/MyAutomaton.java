package at.ac.tuwien.infosys.www.pixy.transduction;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class MyAutomaton {

    // STATE INFO ***********************************

    // counter for state labels
    protected int counter;

    // The set of all states of this automaton.
    protected Set<MyState> states;

    protected MyState initial;

    //the set of terminal states
    protected Set<MyState> terminals;

    // TRANSITION INFO *************************************************

    // state -> set of transitions that have this state as start node
    protected Map<MyState, Set<MyTransition>> state2trans;

    // state -> set of transitions that have this state as end node
    protected Map<MyState, Set<MyTransition>> reverseState2trans;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ********************************************************************************

    // constructs a new, empty automaton
    public MyAutomaton() {
        this.counter = 0;
        this.states = new HashSet<MyState>();
        this.initial = null;
        this.terminals = new HashSet<MyState>();
        this.state2trans = new HashMap<MyState, Set<MyTransition>>();
        this.reverseState2trans = new HashMap<MyState, Set<MyTransition>>();
    }

//  ********************************************************************************

    // clones the given automaton
    public MyAutomaton(MyAutomaton orig) {

        this();

        // map from orig state to cloned state
        Map<MyState, MyState> map = new HashMap<MyState, MyState>();

        // copy states and fill auxiliary map along the way
        for (MyState origState : orig.states) {
            map.put(origState, this.addState(origState.isInitial(), origState.isTerminal()));
        }

        // copy transitions
        for (Set<MyTransition> origTransSet : orig.state2trans.values()) {
            for (MyTransition t : origTransSet) {
                this.addTransition(new MyTransition(
                    map.get(t.getStart()),
                    t.getLabel(),
                    map.get(t.getEnd())));
            }
        }
    }

//  ********************************************************************************

    // conversion from rationals.Automaton
    public MyAutomaton(rationals.Automaton a) {

        this();

        // auxiliary map from foreign state to my cloned state
        Map<rationals.State, MyState> map = new HashMap<rationals.State, MyState>();

        // copy states, updating auxiliary map on the way
        for (Iterator iter = a.states().iterator(); iter.hasNext(); ) {
            rationals.State e = (rationals.State) iter.next();
            map.put(e, this.addState(e.isInitial(), e.isTerminal()));
        }

        // copy transitions
        for (Iterator iter = a.delta().iterator(); iter.hasNext(); ) {
            rationals.Transition t = (rationals.Transition) iter.next();
            this.addTransition(new MyTransition(
                map.get(t.start()),
                t.label(),
                map.get(t.end())));
        }
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

//  ********************************************************************************

    // returns the transition between the given states;
    // if there is more than one such transition, an exception is raised;
    // can also return null if there is no such transition
    public MyTransition getTransition(MyState from, MyState to) {
        Set<MyTransition> transitions = this.getTransitions(from, to);
        if (transitions.size() > 1) {
            throw new RuntimeException("SNH");
        } else if (transitions.isEmpty()) {
            return null;
        } else {
            return transitions.iterator().next();
        }
    }

//  ********************************************************************************

    // returns all transitions that lead into the given state, except loops
    Set<MyTransition> getIncomingNoLoop(MyState into) {
        Set<MyTransition> ret = new HashSet<MyTransition>();
        Set<MyTransition> toSet = this.reverseState2trans.get(into);
        if (toSet != null) {
            for (MyTransition trans : toSet) {
                if (!into.equals(trans.getStart())) {
                    ret.add(trans);
                }
            }
        }
        return ret;
    }

//  ********************************************************************************

    // returns all transitions that start at the given state, except loops
    Set<MyTransition> getOutgoingNoLoop(MyState from) {
        Set<MyTransition> ret = new HashSet<MyTransition>();
        Set<MyTransition> fromSet = this.state2trans.get(from);
        if (fromSet != null) {
            for (MyTransition trans : fromSet) {
                if (!from.equals(trans.getEnd())) {
                    ret.add(trans);
                }
            }
        }
        return ret;
    }

//  ********************************************************************************

    // returns all transitions that start at the given state (including loops)
    Set<MyTransition> getOutgoingTransitions(MyState from) {
        Set<MyTransition> outgoing = this.state2trans.get(from);
        if (outgoing == null) {
            return new HashSet<MyTransition>();
        } else {
            return new HashSet<MyTransition>(outgoing);
        }
    }

//  ********************************************************************************

    // returns all states
    public Set<MyState> getStates() {
        return new HashSet<MyState>(this.states);
    }

//  ********************************************************************************

    // returns the initial state
    public MyState getInitial() {
        return this.initial;
    }

//  ********************************************************************************

    // returns the terminal states
    public Set<MyState> getTerminals() {
        return new HashSet<MyState>(this.terminals);
    }

//  ********************************************************************************

    // returns all transitions
    public Set<MyTransition> getDelta() {
        Set<MyTransition> s = new HashSet<MyTransition>();
        for (Set<MyTransition> transSet : this.state2trans.values()) {
            s.addAll(transSet);
        }
        return s;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  ********************************************************************************

    // creates a unique start and a unique end node
    public void addUniqueStartEnd() {

        // *** INITIAL STATE ***

        // make old initial state uninitial
        MyState oldInitial = this.initial;
        oldInitial.setInitial(false);

        // update bookkeeping
        this.initial = null;

        // add a new initial state
        this.initial = this.addState(true, false);

        // create an epsilon-transition from new to old initial state
        this.addTransition(new MyTransition(this.initial, null, oldInitial));

        // *** TERMINAL STATE ***

        List<MyState> oldTerminals = new LinkedList<MyState>(this.terminals);

        // create a new terminal state
        MyState newTerminal = this.addState(false, true);

        // update old terminals and create transitions
        for (MyState oldTerminal : oldTerminals) {
            oldTerminal.setTerminal(false);
            this.addTransition(new MyTransition(oldTerminal, null, newTerminal));
        }
        // update bookkeeping
        this.terminals.clear();
        this.terminals.add(newTerminal);
    }

//  ********************************************************************************

    // adds the given state
    public MyState addState(boolean initial, boolean terminal) {

        MyState state = new MyState(counter++, initial, terminal);
        if (initial) {
            if (this.initial == null) {
                this.initial = state;
            } else {
                throw new RuntimeException("SNH");
            }
        }
        if (terminal)
            terminals.add(state);
        states.add(state);
        return state;
    }
//  ********************************************************************************

    // returns the transitions between the given states
    public Set<MyTransition> getTransitions(MyState from, MyState to) {
        Set<MyTransition> retme = new HashSet<MyTransition>();
        Set<MyTransition> fromSet = this.state2trans.get(from);
        if (fromSet != null) {
            for (MyTransition t : fromSet) {
                if (to.equals(t.getEnd())) {
                    retme.add(t);
                }
            }
        }
        return retme;
    }

//  ********************************************************************************

    // adds the given transition
    public void addTransition(MyTransition transition) {

        if (!states.contains(transition.getStart()))
            throw new RuntimeException("SNH: start " + transition.getStart());

        if (!states.contains(transition.getEnd())) {
            System.out.println("states: " + this.states);
            throw new RuntimeException("SNH: end " + transition.getEnd());
        }

        MyState start = transition.getStart();
        MyState end = transition.getEnd();

        // add to state2trans
        Set<MyTransition> transSet = state2trans.get(start);
        if (transSet == null) {
            transSet = new HashSet<MyTransition>();
            transSet.add(transition);
            state2trans.put(start, transSet);
        } else {
            transSet.add(transition);
        }

        // add to reverseState2trans
        Set<MyTransition> reverseTransSet = reverseState2trans.get(end);
        if (reverseTransSet == null) {
            reverseTransSet = new HashSet<MyTransition>();
            reverseTransSet.add(transition);
            reverseState2trans.put(end, reverseTransSet);
        } else {
            reverseTransSet.add(transition);
        }
    }

//  ********************************************************************************

    // removes all transitions between the given states
    public void removeTransitions(MyState from, MyState to) {

        // remove matching transitions from the out-map
        Set<MyTransition> fromSet = this.state2trans.get(from);
        for (Iterator iter = fromSet.iterator(); iter.hasNext(); ) {
            MyTransition trans = (MyTransition) iter.next();
            if (to.equals(trans.getEnd())) {
                iter.remove();
            }
        }

        // remove matching transitions from the in-map
        Set<MyTransition> toSet = this.reverseState2trans.get(to);
        for (Iterator iter = toSet.iterator(); iter.hasNext(); ) {
            MyTransition trans = (MyTransition) iter.next();
            if (from.equals(trans.getStart())) {
                iter.remove();
            }
        }
    }

//  ********************************************************************************

    // removes the given transition
    public void removeTransition(MyTransition t) {

        // remove the transition from the out-map
        Set<MyTransition> fromSet = this.state2trans.get(t.getStart());
        fromSet.remove(t);

        // remove the transition from the in-map
        Set<MyTransition> toSet = this.reverseState2trans.get(t.getEnd());
        toSet.remove(t);
    }

//  ********************************************************************************

    // removes the given state, together with all touching transitions;
    // not allowed for the initial state
    public void removeState(MyState state) {

        if (this.initial.equals(state)) {
            throw new RuntimeException("SNH");
        }

        // remove touching transitions:
        // outgoing:
        Set<MyTransition> fromTransSet = new HashSet<MyTransition>(this.state2trans.get(state));
        for (MyTransition t : fromTransSet) {
            this.removeTransitions(t.getStart(), t.getEnd());
        }
        // incoming:
        Set<MyTransition> toTransSet = new HashSet<MyTransition>(this.reverseState2trans.get(state));
        for (MyTransition t : toTransSet) {
            this.removeTransitions(t.getStart(), t.getEnd());
        }

        // remove state
        this.states.remove(state);
        this.terminals.remove(state);
    }

//  ********************************************************************************

    // for each set of transitions that have identical start and
    // end states: merges these transitions into one, labelled with
    // a corresponding regular expression
    public void mergeTransitions() {

        // list view of our original transitions
        LinkedList<MyTransition> origList = new LinkedList<MyTransition>(this.getDelta());

        // list view of our desired merged transitions
        LinkedList<MyTransition> newList = new LinkedList<MyTransition>();

        // we will shift transitions from orig to new, merging along the way
        while (!origList.isEmpty()) {

            // intermediary list holding "competing" transitions
            // (i.e., transitions with the same start and end state)
            List<MyTransition> competing = new LinkedList<MyTransition>();

            // shift the first transition from orig to competing,
            // and also all other transitions from orig that "compete"
            Iterator<MyTransition> iter = origList.iterator();
            MyTransition trans1 = iter.next();
            competing.add(trans1);
            iter.remove();
            // here we search for those competing transitions
            while (iter.hasNext()) {
                MyTransition trans_next = iter.next();
                if (trans1.getStart().equals(trans_next.getStart()) &&
                    trans1.getEnd().equals(trans_next.getEnd())) {
                    competing.add(trans_next);
                    iter.remove();
                }
            }

            // compute merged regex
            StringBuilder regex_merged = new StringBuilder();
            regex_merged.append("(");
            for (MyTransition ctrans : competing) {
                String label = (String) ctrans.getLabel();
                if (label == null) {
                    label = "(1)";      // epsilon transition
                }
                regex_merged.append(label);
                regex_merged.append("+");
            }
            regex_merged.deleteCharAt(regex_merged.length() - 1);
            regex_merged.append(")");

            // add appropriate merged transition to the new list
            newList.add(new MyTransition(trans1.getStart(), regex_merged.toString(), trans1.getEnd()));
        }

        // now we have a complete list of the resulting transitions in newList;
        // first, we have to remove all old transitions, and then, we add all the new ones
        // replace delta with the computed new list
        this.clearTransitions();
        for (MyTransition t : newList) {
            this.addTransition(t);
        }
    }

//  ********************************************************************************

    // removes all transitions
    public void clearTransitions() {
        this.state2trans.clear();
        this.reverseState2trans.clear();
    }

//  ********************************************************************************

    // makes the given state terminal;
    // LATER: find a way how to disallow others to call setTerminal on the
    // state directly; maybe: remove these booleans from State, such that
    // everyone has to query the automaton in order to find out whether a
    // state is final or not
    public void setTerminal(MyState state, boolean terminal) {
        state.setTerminal(terminal);
        if (terminal) {
            this.terminals.add(state);
        } else {
            this.terminals.remove(state);
        }
    }

//  ********************************************************************************

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Q = ").append(this.states.toString()).append("\n");
        sb.append("I = ").append(this.initial.toString()).append("\n");
        sb.append("T = ").append(this.terminals.toString()).append("\n");
        sb.append("delta = [\n");
        for (MyTransition t : this.getDelta()) {
            sb.append(t.toString()).append("\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

//  ********************************************************************************

    // taken from DotCodec
    public void toDot(String filename) {

        OutputStream stream = null;
        try {
            stream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(stream));

        // header
        String name = "jauto";
        pw.println("digraph " + name + " {");

        // initial state
        MyState initial = this.getInitial();
        if (initial.isTerminal()) {
            pw.println("node [shape=doublecircle, color=black, fontcolor=white];");
        } else {
            pw.println("node [shape=circle, color=black, fontcolor=white];");
        }
        pw.println("s" + initial + ";");

        // terminal states
        pw.println("node [shape=doublecircle,color=white, fontcolor=black];");
        for (Iterator i = this.getTerminals().iterator(); i.hasNext(); ) {
            MyState st = (MyState) i.next();
            if (st.isInitial())
                continue;
            pw.println("s" + st + ";");
        }

        // normal states
        pw.println("node [shape=circle,color=white, fontcolor=black];");
        for (Iterator i = this.getStates().iterator(); i.hasNext(); ) {
            MyState st = (MyState) i.next();
            if (st.isInitial() || st.isTerminal())
                continue;
            pw.println("s" + st + ";");
        }

        // transitions
        for (Iterator i = this.getDelta().iterator(); i.hasNext(); ) {
            MyTransition tr = (MyTransition) i.next();
            pw.println("s" + tr.getStart() + " -> " + "s" + tr.getEnd() + " [ label=\"" + tr.getLabel() + "\" ];");
        }
        pw.println("}");
        pw.flush();
        pw.close();
    }
}