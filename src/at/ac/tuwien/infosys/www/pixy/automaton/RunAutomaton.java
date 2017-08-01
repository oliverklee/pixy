package at.ac.tuwien.infosys.www.pixy.automaton;

import java.util.*;
import java.io.*;
import java.net.*;

public class RunAutomaton implements Serializable {

	static final long serialVersionUID = 20001;

	int size;
	boolean[] accept;
	int initial;
	int[] transitions;
	char[] points;
	int[] classmap;

	void setAlphabet() {
		classmap = new int[Character.MAX_VALUE - Character.MIN_VALUE + 1];
		int i = 0;
		for (int j = 0; j <= Character.MAX_VALUE - Character.MIN_VALUE; j++) {
			if (i + 1 < points.length && j == points[i + 1])
				i++;
			classmap[j] = i;
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("initial state: ").append(initial).append("\n");
		for (int i = 0; i < size; i++) {
			b.append("state " + i);
			if (accept[i])
				b.append(" [accept]:\n");
			else
				b.append(" [reject]:\n");
			for (int j = 0; j < points.length; j++) {
				int k = transitions[i * points.length + j];
				if (k != -1) {
					char min = points[j];
					char max;
					if (j + 1 < points.length)
						max = (char) (points[j + 1] - 1);
					else
						max = Character.MAX_VALUE;
					b.append(" ");
					Transition.appendCharString(min, b);
					if (min != max) {
						b.append("-");
						Transition.appendCharString(max, b);
					}
					b.append(" -> ").append(k).append("\n");
				}
			}
		}
		return b.toString();
	}

	public int getSize() {
		return size;
	}

	public boolean isAccept(int state) {
		return accept[state];
	}

	public int getInitialState() {
		return initial;
	}

	public char[] getCharIntervals() {
		return points.clone();
	}

	int getCharClass(char c) {
		return Automaton.findIndex(c, points);
	}

	@SuppressWarnings("unused")
	private RunAutomaton() {
	}

	public RunAutomaton(Automaton a) {
		this(a, true);
	}

	public static RunAutomaton load(URL url) throws IOException, OptionalDataException, ClassCastException,
			ClassNotFoundException, InvalidClassException {
		return load(url.openStream());
	}

	public static RunAutomaton load(InputStream stream) throws IOException, OptionalDataException, ClassCastException,
			ClassNotFoundException, InvalidClassException {
		ObjectInputStream s = new ObjectInputStream(stream);
		return (RunAutomaton) s.readObject();
	}

	public void store(OutputStream stream) throws IOException {
		ObjectOutputStream s = new ObjectOutputStream(stream);
		s.writeObject(this);
		s.flush();
	}

	public RunAutomaton(Automaton a, boolean tableize) {
		a.determinize();
		points = a.getStartPoints();
		Set<State> states = a.getStates();
		Automaton.setStateNumbers(states);
		initial = a.initial.number;
		size = states.size();
		accept = new boolean[size];
		transitions = new int[size * points.length];
		for (int n = 0; n < size * points.length; n++)
			transitions[n] = -1;
		for (State s : states) {
			int n = s.number;
			accept[n] = s.accept;
			for (int c = 0; c < points.length; c++) {
				State q = s.step(points[c]);
				if (q != null)
					transitions[n * points.length + c] = q.number;
			}
		}
		if (tableize)
			setAlphabet();
	}

	public int step(int state, char c) {
		if (classmap == null)
			return transitions[state * points.length + getCharClass(c)];
		else
			return transitions[state * points.length + classmap[c - Character.MIN_VALUE]];
	}

	public boolean run(String s) {
		int p = initial;
		int l = s.length();
		for (int i = 0; i < l; i++) {
			p = step(p, s.charAt(i));
			if (p == -1)
				return false;
		}
		return accept[p];
	}

	public int run(String s, int offset) {
		int p = initial;
		int l = s.length();
		int max = -1;
		for (int r = 0; offset <= l; offset++, r++) {
			if (accept[p])
				max = r;
			if (offset == l)
				break;
			p = step(p, s.charAt(offset));
			if (p == -1)
				break;
		}
		return max;
	}
}
