package at.ac.tuwien.infosys.www.pixy.transduction;

import rationals.State;
import rationals.Transition;

import java.io.*;
import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractAutomaton {
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
    public AbstractAutomaton() {
        this.counter = 0;
        this.states = new HashSet<>();
        this.initial = null;
        this.terminals = new HashSet<>();
        this.state2trans = new HashMap<>();
        this.reverseState2trans = new HashMap<>();
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // returns all transitions that start at the given state (including loops)
    Set<MyTransition> getOutgoingTransitions(MyState from) {
        Set<MyTransition> outgoing = this.state2trans.get(from);
        if (outgoing == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(outgoing);
        }
    }

//  ********************************************************************************

    // returns all states
    public Set<MyState> getStates() {
        return new HashSet<>(this.states);
    }

//  ********************************************************************************

    // returns the initial state
    public MyState getInitial() {
        return this.initial;
    }

//  ********************************************************************************

    // returns the terminal states
    public Set<MyState> getTerminals() {
        return new HashSet<>(this.terminals);
    }

//  ********************************************************************************

    // returns all transitions
    public Set<MyTransition> getDelta() {
        Set<MyTransition> s = new HashSet<>();
        for (Set<MyTransition> transSet : this.state2trans.values()) {
            s.addAll(transSet);
        }
        return s;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
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
        Set<MyTransition> retme = new HashSet<>();
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
            transSet = new HashSet<>();
            transSet.add(transition);
            state2trans.put(start, transSet);
        } else {
            transSet.add(transition);
        }

        // add to reverseState2trans
        Set<MyTransition> reverseTransSet = reverseState2trans.get(end);
        if (reverseTransSet == null) {
            reverseTransSet = new HashSet<>();
            reverseTransSet.add(transition);
            reverseState2trans.put(end, reverseTransSet);
        } else {
            reverseTransSet.add(transition);
        }
    }

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
}