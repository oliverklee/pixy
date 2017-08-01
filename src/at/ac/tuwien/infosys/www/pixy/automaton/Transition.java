package at.ac.tuwien.infosys.www.pixy.automaton;

import java.util.*;
import java.io.*;

public class Transition implements Serializable, Cloneable {

	public static enum Taint {
		Untainted, Indirectly, Directly
	}

	static final long serialVersionUID = 40001;

	Transition.Taint taint;

	char min;
	char max;

	State to;

	public Transition(char c, State to) {
		this(c, to, Transition.Taint.Untainted);
	}

	public Transition(char c, State to, Transition.Taint taint) {
		min = max = c;
		this.to = to;
		this.taint = taint;
	}

	public Transition(char min, char max, State to) {
		this(min, max, to, Transition.Taint.Untainted);
	}

	public Transition(char min, char max, State to, Transition.Taint taint) {
		if (max < min) {
			char t = max;
			max = min;
			min = t;
		}
		this.min = min;
		this.max = max;
		this.to = to;
		this.taint = taint;
	}

	public char getMin() {
		return min;
	}

	public char getMax() {
		return max;
	}

	public State getDest() {
		return to;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transition) {
			Transition t = (Transition) obj;
			return t.min == min && t.max == max && t.to == to;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return min * 2 + max * 3;
	}

	@Override
	public Transition clone() {
		try {
			return (Transition) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void appendCharString(char c, StringBuilder b) {
		if (c >= 0x21 && c <= 0x7e && c != '\\' && c != '"')
			b.append(c);
		else {
			b.append("\\u");
			String s = Integer.toHexString(c);
			if (c < 0x10)
				b.append("000").append(s);
			else if (c < 0x100)
				b.append("00").append(s);
			else if (c < 0x1000)
				b.append("0").append(s);
			else
				b.append(s);
		}
	}

	public static char reverseCharString(String s) {
		if (s.length() == 1) {
			return s.charAt(0);
		}
		if (!s.startsWith("\\u")) {
			throw new RuntimeException("SNH");
		}
		if (!(s.length() == 6)) {
			throw new RuntimeException("SNH");
		}
		String hexString = s.substring(2, 6);
		String dec = new java.math.BigInteger(hexString, 16).toString();
		return (char) Integer.valueOf(dec).intValue();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		appendCharString(min, b);
		if (min != max) {
			b.append("-");
			appendCharString(max, b);
		}
		b.append(" -> ").append(to.number);
		return b.toString();
	}

	void appendDot(StringBuilder b) {
		b.append(" -> ").append(to.number).append(" [label=\"");
		if (this.isDotStar()) {
			b.append("@");
		} else {
			appendCharString(min, b);
			if (min != max) {
				b.append("-");
				appendCharString(max, b);
			}
		}
		String color = "";
		if (taint == Transition.Taint.Directly) {
			color = ",color=red";
		} else if (taint == Transition.Taint.Indirectly) {
			color = ",color=green2";
		}
		b.append("\"" + color + "]\n");
	}

	boolean isDotStar() {
		if (min == Character.MIN_VALUE && max == Character.MAX_VALUE) {
			return true;
		} else {
			return false;
		}
	}
}

class TransitionComparator implements Comparator<Transition> {

	boolean to_first;

	TransitionComparator(boolean to_first) {
		this.to_first = to_first;
	}

	public int compare(Transition t1, Transition t2) {
		if (to_first) {
			if (t1.to != t2.to) {
				if (t1.to == null)
					return -1;
				else if (t2.to == null)
					return 1;
				else if (t1.to.number < t2.to.number)
					return -1;
				else if (t1.to.number > t2.to.number)
					return 1;
			}
		}
		if (t1.min < t2.min)
			return -1;
		if (t1.min > t2.min)
			return 1;
		if (t1.max > t2.max)
			return -1;
		if (t1.max < t2.max)
			return 1;
		if (!to_first) {
			if (t1.to != t2.to) {
				if (t1.to == null)
					return -1;
				else if (t2.to == null)
					return 1;
				else if (t1.to.number < t2.to.number)
					return -1;
				else if (t1.to.number > t2.to.number)
					return 1;
			}
		}
		return 0;
	}
}
