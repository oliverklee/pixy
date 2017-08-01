package at.ac.tuwien.infosys.www.pixy.automaton;

import java.util.*;
import java.io.*;

@SuppressWarnings("rawtypes")
public class State implements Serializable, Comparable {

	static final long serialVersionUID = 30001;

	boolean accept;
	HashSet<Transition> transitions;

	int number;

	int id;
	static int next_id;

	public State() {
		resetTransitions();
		id = next_id++;
	}

	void resetTransitions() {
		transitions = new HashSet<Transition>();
	}

	public Set<Transition> getTransitions() {
		return transitions;
	}

	public void addTransition(Transition t) {
		transitions.add(t);
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	public boolean isAccept() {
		return accept;
	}

	public State step(char c) {
		for (Transition t : transitions)
			if (t.min <= c && c <= t.max)
				return t.to;
		return null;
	}

	void addEpsilon(State to) {
		if (to.accept)
			accept = true;
		for (Transition t : to.transitions)
			transitions.add(t);
	}

	Transition[] getSortedTransitionArray(boolean to_first) {
		Transition[] e = transitions.toArray(new Transition[0]);
		Arrays.sort(e, new TransitionComparator(to_first));
		return e;
	}

	List<Transition> getSortedTransitions(boolean to_first) {
		return Arrays.asList(getSortedTransitionArray(to_first));
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("state ").append(number);
		if (accept)
			b.append(" [accept]");
		else
			b.append(" [reject]");
		b.append(":\n");
		for (Transition t : transitions)
			b.append("  ").append(t.toString()).append("\n");
		return b.toString();
	}

	public int compareTo(Object o) {
		return ((State) o).id - id;
	}
}
