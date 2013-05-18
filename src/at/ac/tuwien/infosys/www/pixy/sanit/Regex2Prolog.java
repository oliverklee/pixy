package at.ac.tuwien.infosys.www.pixy.sanit;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Responsible for converting a PHP regex into a prolog (FSA Utils) regex.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Regex2Prolog {

//  ********************************************************************************

    // converts a PHP regex into a Prolog (FSA-Utils) regexp;
    // throws UnsupportedRegexException for unsupported regexes;
    // preg: true if this regex is perl-compatible, false if it is posix (ereg)
    static String convertPhpRegex(List<String> phpRegexOrig, boolean preg) {

        // we don't like empty regexes
        if (phpRegexOrig.isEmpty()) {
            throw new RuntimeException("Empty regex");
        }

        // make a copy for the following work
        List<String> phpRegex = new LinkedList<>(phpRegexOrig);

        if (preg) {
            // if the preg regex is not delimited...
            if (!phpRegex.get(0).equals(FSAAutomaton.slash) || !phpRegex.get(phpRegex.size() - 1).equals(FSAAutomaton.slash)) {
                throw new UnsupportedRegexException();
            }
            // peel off delimiter
            phpRegex = phpRegex.subList(1, phpRegex.size() - 1);
        }

        StringBuilder prologRegex = new StringBuilder();

        // parse subsequence
        prologRegex.append(parseSub(phpRegex.listIterator()));

        return prologRegex.toString();
    }

//  ********************************************************************************

    // parses a subsequence: ... -> [...,...,...]
    private static StringBuilder parseSub(ListIterator<String> iter) {

        StringBuilder prologRegex = new StringBuilder();

        String seq = "";
        while (iter.hasNext()) {

            // the current symbol
            String sym = iter.next();

            // lookahead
            String look = null;
            if (iter.hasNext()) {
                look = iter.next();
                iter.previous();
            }

            // this will be set to true if lookahead detects
            // a meta-character and handles it; in this case,
            // the current symbol must not be treated again below
            boolean done = false;

            if (look != null) {

                // if we are not at the end

                // making a look ahead to see if we have
                // to do something unusual for the current symbol

            }

            if (!done) {
                if (sym.equals(FSAAutomaton.star)) {
                    prologRegex.append('*');
                    seq = ",";
                } else if (sym.equals(FSAAutomaton.plus)) {
                    prologRegex.append('+');
                    seq = ",";
                } else if (sym.equals(FSAAutomaton.obra)) {
                    // start of subpattern
                    prologRegex.append(seq);
                    prologRegex.append(parseSub(iter));
                    seq = ",";
                } else if (sym.equals(FSAAutomaton.cbra)) {
                    // end of subpattern
                    break;
                } else if (sym.equals(FSAAutomaton.osqbra)) {
                    // start of character class
                    prologRegex.append(seq);
                    prologRegex.append(parseCharClass(iter));
                    seq = ",";
                } else if (sym.equals(FSAAutomaton.union)) {
                    // <from_start_of_sub> | <to_end_of_sub>
                    // ->
                    // {[<from_start_of_sub>],[<to_end_of_sub>]}
                    StringBuilder toEnd = parseSub(iter);
                    prologRegex.insert(0, "{[");
                    prologRegex.append("],");
                    prologRegex.append(toEnd);
                    prologRegex.append('}');
                } else if (sym.equals(FSAAutomaton.point)) {
                    // any character
                    prologRegex.append(seq);
                    prologRegex.append('?');
                    seq = ",";
                } else if (sym.equals(FSAAutomaton.backslash)) {
                    // an escape
                    String escaped = escape(iter);
                    prologRegex.append(seq);
                    prologRegex.append(escaped);
                    seq = ",";
                } else if (sym.equals(FSAAutomaton.ocurly)) {
                    // repetition;
                    // automaton could become quite large;
                    // here is how it would work:
                    // - determine the number of repetitions
                    // - determine the regex that is to be repeated
                    //   (easy: from start of the current subsequence to here)
                    // - if the number of repetitions is a constant {x}:
                    //   - concat the regex this number of times
                    // - else if the number of repetitions is {x,} (i.e., unbounded):
                    //   - repeat x times, and add a star-repetition
                    // - else if the number of repetitions is a range a{2,4}
                    //   - [a,a,a^,a^] (where ^ is option, i.e, ? in usual regex syntax)
                    throw new UnsupportedRegexException();
                } else if (sym.equals(FSAAutomaton.circum)) {
                    // "start of line"
                    throw new UnsupportedRegexException();
                } else if (sym.equals(FSAAutomaton.dollar)) {
                    // "end of line"
                    throw new UnsupportedRegexException();
                } else {
                    // not a meta-character
                    prologRegex.append(seq);
                    prologRegex.append(sym);
                    seq = ",";
                }
            }
        }

        // enclose in sequence and return
        prologRegex.insert(0, '[');
        prologRegex.append(']');
        return prologRegex;
    }

//  ********************************************************************************

    // parses a character class: [...]
    private static StringBuilder parseCharClass(ListIterator<String> iter) {

        StringBuilder prologRegex = new StringBuilder();

        String first = iter.next();

        // check whether the first symbol in the character class is a ^;
        // in this case, we have a negated character class
        if (!first.equals(FSAAutomaton.circum)) {
            // push back
            iter.previous();
        }

        String seq = "";
        while (iter.hasNext()) {

            // the current symbol
            String sym = iter.next();

            // lookahaed
            String look = null;
            if (iter.hasNext()) {
                look = iter.next();
                iter.previous();
            }

            // this will be set to true if lookahead detects
            // a meta-character and handles it; in this case,
            // the current symbol must not be treated again below
            boolean done = false;

            if (look != null) {

                // if we are not at the end

                // making a look ahead to see if we have
                // to do something unusual for the current symbol

                if (look.equals(FSAAutomaton.minus) && !sym.equals(FSAAutomaton.backslash)) {
                    // character range lying ahead (but only if the minus was not escaped)

                    // also check if the minus is the last character in this
                    // character class; if so, it does not denote a character range
                    boolean charRange = true;
                    iter.next();
                    if (iter.hasNext()) {
                        String symAfterMinus = iter.next();
                        iter.previous();
                        iter.previous();
                        if (symAfterMinus.equals(FSAAutomaton.csqbra)) {
                            // end of character class
                            charRange = false;
                        }
                    } else {
                        charRange = false;
                        iter.previous();
                    }

                    if (charRange) {
                        String rangeStart = sym;
                        iter.next();
                        String rangeEnd = iter.next();
                        StringBuilder range = makeRange(rangeStart, rangeEnd);

                        prologRegex.append(seq);
                        prologRegex.append(range);
                        seq = ",";

                        done = true;
                    }
                }
            }

            if (!done) {
                if (sym.equals(FSAAutomaton.csqbra)) {
                    // end of character class
                    break;
                } else if (sym.equals(FSAAutomaton.backslash)) {
                    // an escape

                    String escaped = escape(iter);
                    prologRegex.append(seq);
                    prologRegex.append(escaped);
                    seq = ",";
                } else {
                    // not a meta-character
                    prologRegex.append(seq);
                    prologRegex.append(sym);
                    seq = ",";
                }
            }
        }

        // enclose in union
        prologRegex.insert(0, '{');
        prologRegex.append('}');

        // check whether the first symbol in the character class is a ^;
        // in this case, we have a negated character class
        if (first.equals(FSAAutomaton.circum)) {
            prologRegex.insert(0, "term_complement(");
            prologRegex.append(')');
        }

        return prologRegex;
    }

//  ********************************************************************************

    // converts a character range: a-z => {a,b,c,...,z}
    private static StringBuilder makeRange(String startX, String endX) {

        char start = FSAAutomaton.decode(startX);
        char end = FSAAutomaton.decode(endX);

        if (start >= end) {
            throw new RuntimeException("faulty regex: " + start + "-" + end);
        }

        StringBuilder b = new StringBuilder();
        b.append('{');
        while (start <= end) {
            b.append(FSAAutomaton.encode(start));
            b.append(',');
            start++;
        }
        if (b.charAt(b.length() - 1) == ',') {
            b.deleteCharAt(b.length() - 1);
        }
        b.append('}');
        return b;
    }

//  ********************************************************************************

    // handles backslash escaping;
    // expects the iterator to be right after the backslash
    private static String escape(ListIterator<String> iter) {

        // safety check
        if (!iter.previous().equals(FSAAutomaton.backslash)) {
            throw new RuntimeException("SNH");
        }
        iter.next();

        String escaped = iter.next();
        String retMe;
        char escapedOrig = FSAAutomaton.decode(escaped);

        if (Character.isLetterOrDigit(escapedOrig)) {
            // has a special meaning, not supported yet
            throw new UnsupportedRegexException();
        } else {
            // a simple escape of a metacharacter
            retMe = escaped;
        }

        return retMe;
    }
}