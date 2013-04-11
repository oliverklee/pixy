/*
 * dk.brics.automaton
 *
 * Copyright (c) 2001-2006 Anders Moeller
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.ac.tuwien.infosys.www.pixy.automaton;

import java.util.*;
import java.io.*;

/**
 * <tt>Automaton</tt> state.
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class State
implements Serializable, Comparable {

    static final long serialVersionUID = 30001;

	boolean accept;
	HashSet<Transition> transitions;

	int number;

	int id;
	static int next_id;

	/**
	 * Constructs new state. Initially, the new state is a reject state.
	 */
	public State() {
		resetTransitions();
		id = next_id++;
	}

	/**
	 * Resets transition set.
	 */
	void resetTransitions() {
		transitions = new HashSet<Transition>();
	}

	/**
	 * Returns set of outgoing transitions.
	 * Subsequent changes are reflected in the automaton.
	 * @return transition set
	 */
	public Set<Transition> getTransitions()	{
		return transitions;
	}

	/**
	 * Adds outgoing transition.
	 * @param t transition
	 */
	public void addTransition(Transition t)	{
		transitions.add(t);
	}

	/**
	 * Sets acceptance for this state.
	 * @param accept if true, this state is an accept state
	 */
	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	/**
	 * Returns acceptance status.
	 * @return true is this is an accept state
	 */
	public boolean isAccept() {
		return accept;
	}

	/**
	 * Performs lookup in transitions.
	 * @param c character to look up
	 * @return destination state, null if no matching outgoing transition
	 */
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

	/** Returns transitions sorted by (min, reverse max, to) or (to, min, reverse max) */
	Transition[] getSortedTransitionArray(boolean to_first) {
		Transition[] e = transitions.toArray(new Transition[0]);
		Arrays.sort(e, new TransitionComparator(to_first));
		return e;
	}

	List<Transition> getSortedTransitions(boolean to_first)	{
		return Arrays.asList(getSortedTransitionArray(to_first));
	}

	/**
	 * Returns string describing this state. Normally invoked via
	 * {@link Automaton#toString()}.
	 */
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

	/**
	 * Compares this object with the specified object for order.
	 * States are ordered by the time of construction.
	 */
	public int compareTo(Object o) {
		return ((State)o).id - id;
	}
}