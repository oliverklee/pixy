package at.ac.tuwien.infosys.www.pixy.automaton;

public class StatePair {
	State s;
	State s1;
	State s2;

	StatePair(State s, State s1, State s2) {
		this.s = s;
		this.s1 = s1;
		this.s2 = s2;
	}

	public StatePair(State s1, State s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	public State getFirstState() {
		return s1;
	}

	public State getSecondState() {
		return s2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StatePair) {
			StatePair p = (StatePair) obj;
			return p.s1 == s1 && p.s2 == s2;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return s1.hashCode() + s2.hashCode();
	}
}
