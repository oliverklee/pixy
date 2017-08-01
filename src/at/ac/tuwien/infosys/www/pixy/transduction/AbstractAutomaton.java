package at.ac.tuwien.infosys.www.pixy.transduction;

import java.io.*;
import java.util.*;

public abstract class AbstractAutomaton {

	protected int counter;

	protected Set<MyState> states;

	protected MyState initial;

	protected Set<MyState> terminals;

	protected Map<MyState, Set<MyTransition>> state2trans;

	protected Map<MyState, Set<MyTransition>> reverseState2trans;

	public AbstractAutomaton() {
		this.counter = 0;
		this.states = new HashSet<MyState>();
		this.initial = null;
		this.terminals = new HashSet<MyState>();
		this.state2trans = new HashMap<MyState, Set<MyTransition>>();
		this.reverseState2trans = new HashMap<MyState, Set<MyTransition>>();
	}

	public AbstractAutomaton(AbstractAutomaton orig) {

		this();

		Map<MyState, MyState> map = new HashMap<MyState, MyState>();

		for (MyState origState : orig.states) {
			map.put(origState, this.addState(origState.isInitial(), origState.isTerminal()));
		}

		for (Set<MyTransition> origTransSet : orig.state2trans.values()) {
			for (MyTransition t : origTransSet) {
				this.addTransition(new MyTransition(map.get(t.getStart()), t.getLabel(), map.get(t.getEnd())));
			}
		}
	}

	public AbstractAutomaton(rationals.Automaton a) {

		this();

		Map<rationals.State, MyState> map = new HashMap<rationals.State, MyState>();

		for (Iterator<?> iter = a.states().iterator(); iter.hasNext();) {
			rationals.State e = (rationals.State) iter.next();
			map.put(e, this.addState(e.isInitial(), e.isTerminal()));
		}

		for (Iterator<?> iter = a.delta().iterator(); iter.hasNext();) {
			rationals.Transition t = (rationals.Transition) iter.next();
			this.addTransition(new MyTransition(map.get(t.start()), t.label(), map.get(t.end())));
		}
	}

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

	Set<MyTransition> getOutgoingTransitions(MyState from) {
		Set<MyTransition> outgoing = this.state2trans.get(from);
		if (outgoing == null) {
			return new HashSet<MyTransition>();
		} else {
			return new HashSet<MyTransition>(outgoing);
		}
	}

	public Set<MyState> getStates() {
		return new HashSet<MyState>(this.states);
	}

	public MyState getInitial() {
		return this.initial;
	}

	public Set<MyState> getTerminals() {
		return new HashSet<MyState>(this.terminals);
	}

	public Set<MyTransition> getDelta() {
		Set<MyTransition> s = new HashSet<MyTransition>();
		for (Set<MyTransition> transSet : this.state2trans.values()) {
			s.addAll(transSet);
		}
		return s;
	}

	public void addUniqueStartEnd() {

		MyState oldInitial = this.initial;
		oldInitial.setInitial(false);

		this.initial = null;

		this.initial = this.addState(true, false);

		this.addTransition(new MyTransition(this.initial, null, oldInitial));

		List<MyState> oldTerminals = new LinkedList<MyState>(this.terminals);

		MyState newTerminal = this.addState(false, true);

		for (MyState oldTerminal : oldTerminals) {
			oldTerminal.setTerminal(false);
			this.addTransition(new MyTransition(oldTerminal, null, newTerminal));
		}
		this.terminals.clear();
		this.terminals.add(newTerminal);
	}

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

	public void addTransition(MyTransition transition) {

		if (!states.contains(transition.getStart()))
			throw new RuntimeException("SNH: start " + transition.getStart());

		if (!states.contains(transition.getEnd())) {
			System.out.println("states: " + this.states);
			throw new RuntimeException("SNH: end " + transition.getEnd());
		}

		MyState start = transition.getStart();
		MyState end = transition.getEnd();

		Set<MyTransition> transSet = state2trans.get(start);
		if (transSet == null) {
			transSet = new HashSet<MyTransition>();
			transSet.add(transition);
			state2trans.put(start, transSet);
		} else {
			transSet.add(transition);
		}

		Set<MyTransition> reverseTransSet = reverseState2trans.get(end);
		if (reverseTransSet == null) {
			reverseTransSet = new HashSet<MyTransition>();
			reverseTransSet.add(transition);
			reverseState2trans.put(end, reverseTransSet);
		} else {
			reverseTransSet.add(transition);
		}
	}

	public void removeTransitions(MyState from, MyState to) {

		Set<MyTransition> fromSet = this.state2trans.get(from);
		for (Iterator<MyTransition> iter = fromSet.iterator(); iter.hasNext();) {
			MyTransition trans = (MyTransition) iter.next();
			if (to.equals(trans.getEnd())) {
				iter.remove();
			}
		}

		Set<MyTransition> toSet = this.reverseState2trans.get(to);
		for (Iterator<MyTransition> iter = toSet.iterator(); iter.hasNext();) {
			MyTransition trans = (MyTransition) iter.next();
			if (from.equals(trans.getStart())) {
				iter.remove();
			}
		}
	}

	public void removeTransition(MyTransition t) {

		Set<MyTransition> fromSet = this.state2trans.get(t.getStart());
		fromSet.remove(t);

		Set<MyTransition> toSet = this.reverseState2trans.get(t.getEnd());
		toSet.remove(t);
	}

	public void removeState(MyState state) {

		if (this.initial.equals(state)) {
			throw new RuntimeException("SNH");
		}

		Set<MyTransition> fromTransSet = new HashSet<MyTransition>(this.state2trans.get(state));
		for (MyTransition t : fromTransSet) {
			this.removeTransitions(t.getStart(), t.getEnd());
		}
		Set<MyTransition> toTransSet = new HashSet<MyTransition>(this.reverseState2trans.get(state));
		for (MyTransition t : toTransSet) {
			this.removeTransitions(t.getStart(), t.getEnd());
		}

		this.states.remove(state);
		this.terminals.remove(state);
	}

	public void mergeTransitions() {

		LinkedList<MyTransition> origList = new LinkedList<MyTransition>(this.getDelta());

		LinkedList<MyTransition> newList = new LinkedList<MyTransition>();

		while (!origList.isEmpty()) {

			List<MyTransition> competing = new LinkedList<MyTransition>();

			Iterator<MyTransition> iter = origList.iterator();
			MyTransition trans1 = iter.next();
			competing.add(trans1);
			iter.remove();
			while (iter.hasNext()) {
				MyTransition trans_next = iter.next();
				if (trans1.getStart().equals(trans_next.getStart()) && trans1.getEnd().equals(trans_next.getEnd())) {
					competing.add(trans_next);
					iter.remove();
				}
			}

			StringBuilder regex_merged = new StringBuilder();
			regex_merged.append("(");
			for (MyTransition ctrans : competing) {
				String label = (String) ctrans.getLabel();
				if (label == null) {
					label = "(1)";
				}
				regex_merged.append(label);
				regex_merged.append("+");
			}
			regex_merged.deleteCharAt(regex_merged.length() - 1);
			regex_merged.append(")");

			newList.add(new MyTransition(trans1.getStart(), regex_merged.toString(), trans1.getEnd()));
		}

		this.clearTransitions();
		for (MyTransition t : newList) {
			this.addTransition(t);
		}
	}

	public void clearTransitions() {
		this.state2trans.clear();
		this.reverseState2trans.clear();
	}

	public void setTerminal(MyState state, boolean terminal) {
		state.setTerminal(terminal);
		if (terminal) {
			this.terminals.add(state);
		} else {
			this.terminals.remove(state);
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

	public void toDot(String filename) {

		OutputStream stream = null;
		try {
			stream = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(stream));

		String name = "jauto";
		pw.println("digraph " + name + " {");

		MyState initial = this.getInitial();
		if (initial.isTerminal()) {
			pw.println("node [shape=doublecircle, color=black, fontcolor=white];");
		} else {
			pw.println("node [shape=circle, color=black, fontcolor=white];");
		}
		pw.println("s" + initial + ";");

		pw.println("node [shape=doublecircle,color=white, fontcolor=black];");
		for (Iterator<?> i = this.getTerminals().iterator(); i.hasNext();) {
			MyState st = (MyState) i.next();
			if (st.isInitial())
				continue;
			pw.println("s" + st + ";");
		}

		pw.println("node [shape=circle,color=white, fontcolor=black];");
		for (Iterator<?> i = this.getStates().iterator(); i.hasNext();) {
			MyState st = (MyState) i.next();
			if (st.isInitial() || st.isTerminal())
				continue;
			pw.println("s" + st + ";");
		}

		for (Iterator<?> i = this.getDelta().iterator(); i.hasNext();) {
			MyTransition tr = (MyTransition) i.next();
			pw.println("s" + tr.getStart() + " -> " + "s" + tr.getEnd() + " [ label=\"" + tr.getLabel() + "\" ];");
		}
		pw.println("}");
		pw.flush();
		pw.close();
	}
}