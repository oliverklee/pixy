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

import java.io.Serializable;
import java.util.Comparator;

/**
 * <tt>Automaton</tt> transition.
 * <p/>
 * A transition, which belongs to a source state, consists of a Unicode character interval
 * and a destination state.
 *
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class Transition implements Serializable, Cloneable {

    // Indirectly: tainted through sanitization function;
    // forms a lattice: Untainted -> Indirectly -> Directly
    public static enum Taint {
        Untainted, Indirectly, Directly
    }

    static final long serialVersionUID = 40001;

    Transition.Taint taint;

	/*
     * CLASS INVARIANT: min<=max
	 */

    char min;
    char max;

    State to;

    /**
     * Constructs new singleton interval transition.
     *
     * @param c  transition character
     * @param to destination state
     */
    public Transition(char c, State to) {
        this(c, to, Transition.Taint.Untainted);
    }

    public Transition(char c, State to, Transition.Taint taint) {
        min = max = c;
        this.to = to;
        this.taint = taint;
    }

    /**
     * Constructs new transition.
     * Both end points are included in the interval.
     *
     * @param min transition interval minimum
     * @param max transition interval maximum
     * @param to  destination state
     */
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

    /**
     * Returns minimum of this transition interval.
     */
    public char getMin() {
        return min;
    }

    /**
     * Returns maximum of this transition interval.
     */
    public char getMax() {
        return max;
    }

    /**
     * Returns destination of this transition.
     */
    public State getDest() {
        return to;
    }

    /**
     * Checks for equality.
     *
     * @param obj object to compare with
     *
     * @return true if <tt>obj</tt> is a transition with same
     *         character interval and destination state as this transition.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transition) {
            Transition t = (Transition) obj;
            return t.min == min && t.max == max && t.to == to;
        } else
            return false;
    }

    /**
     * Returns hash code.
     * The hash code is based on the character interval (not the destination state).
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return min * 2 + max * 3;
    }

    /**
     * Clones this transition.
     *
     * @return clone with same character interval and destination state
     */
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

    // reverses the transformation that is performed in appendCharString()
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

    /**
     * Returns string describing this state. Normally invoked via
     * {@link Automaton#toString()}.
     */
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

    // returns true if this transition represents a ".*" transition (@)
    boolean isDotStar() {
        if (min == Character.MIN_VALUE && max == Character.MAX_VALUE) {
            return true;
        } else {
            return false;
        }
    }
}

/**
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
class TransitionComparator implements Comparator<Transition> {

    boolean to_first;

    TransitionComparator(boolean to_first) {
        this.to_first = to_first;
    }

    /**
     * Compares by (min, reverse max, to) or (to, min, reverse max).
     */
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