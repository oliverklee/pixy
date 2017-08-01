package at.ac.tuwien.infosys.www.pixy.sanitation;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Regex2Prolog {

	static String convertPhpRegex(List<String> phpRegexOrig, boolean preg) {

		if (phpRegexOrig.isEmpty()) {
			throw new RuntimeException("Empty regex");
		}

		List<String> phpRegex = new LinkedList<String>(phpRegexOrig);

		if (preg) {
			if (!phpRegex.get(0).equals(FSAAutomaton.slash)
					|| !phpRegex.get(phpRegex.size() - 1).equals(FSAAutomaton.slash)) {
				throw new UnsupportedRegexException();
			}
			phpRegex = phpRegex.subList(1, phpRegex.size() - 1);
		}

		StringBuilder prologRegex = new StringBuilder();

		prologRegex.append(parseSub(phpRegex.listIterator()));

		return prologRegex.toString();
	}

	private static StringBuilder parseSub(ListIterator<String> iter) {

		StringBuilder prologRegex = new StringBuilder();

		String seq = "";
		while (iter.hasNext()) {

			String sym = iter.next();

			String look = null;
			if (iter.hasNext()) {
				look = iter.next();
				iter.previous();
			}

			boolean done = false;

			if (look != null) {
			}

			if (!done) {
				if (sym.equals(FSAAutomaton.star)) {
					prologRegex.append('*');
					seq = ",";
				} else if (sym.equals(FSAAutomaton.plus)) {
					prologRegex.append('+');
					seq = ",";
				} else if (sym.equals(FSAAutomaton.obra)) {
					prologRegex.append(seq);
					prologRegex.append(parseSub(iter));
					seq = ",";
				} else if (sym.equals(FSAAutomaton.cbra)) {
					break;
				} else if (sym.equals(FSAAutomaton.osqbra)) {
					prologRegex.append(seq);
					prologRegex.append(parseCharClass(iter));
					seq = ",";
				} else if (sym.equals(FSAAutomaton.union)) {
					StringBuilder toEnd = parseSub(iter);
					prologRegex.insert(0, "{[");
					prologRegex.append("],");
					prologRegex.append(toEnd);
					prologRegex.append('}');
				} else if (sym.equals(FSAAutomaton.point)) {
					prologRegex.append(seq);
					prologRegex.append('?');
					seq = ",";
				} else if (sym.equals(FSAAutomaton.backslash)) {
					String escaped = escape(iter);
					prologRegex.append(seq);
					prologRegex.append(escaped);
					seq = ",";
				} else if (sym.equals(FSAAutomaton.ocurly)) {
					throw new UnsupportedRegexException();
				} else if (sym.equals(FSAAutomaton.circum)) {
					throw new UnsupportedRegexException();
				} else if (sym.equals(FSAAutomaton.dollar)) {
					throw new UnsupportedRegexException();
				} else {
					prologRegex.append(seq);
					prologRegex.append(sym);
					seq = ",";
				}
			}
		}
		prologRegex.insert(0, '[');
		prologRegex.append(']');
		return prologRegex;

	}

	private static StringBuilder parseCharClass(ListIterator<String> iter) {

		StringBuilder prologRegex = new StringBuilder();

		String first = iter.next();

		if (!first.equals(FSAAutomaton.circum)) {
			iter.previous();
		}

		String seq = "";
		while (iter.hasNext()) {
			String sym = iter.next();
			String look = null;
			if (iter.hasNext()) {
				look = iter.next();
				iter.previous();
			}

			boolean done = false;

			if (look != null) {

				if (look.equals(FSAAutomaton.minus) && !sym.equals(FSAAutomaton.backslash)) {
					boolean charRange = true;
					iter.next();
					if (iter.hasNext()) {
						String symAfterMinus = iter.next();
						iter.previous();
						iter.previous();
						if (symAfterMinus.equals(FSAAutomaton.csqbra)) {
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
					break;

				} else if (sym.equals(FSAAutomaton.backslash)) {

					String escaped = escape(iter);
					prologRegex.append(seq);
					prologRegex.append(escaped);
					seq = ",";

				} else {
					prologRegex.append(seq);
					prologRegex.append(sym);
					seq = ",";
				}
			}
		}

		prologRegex.insert(0, '{');
		prologRegex.append('}');

		if (first.equals(FSAAutomaton.circum)) {
			prologRegex.insert(0, "term_complement(");
			prologRegex.append(')');
		}

		return prologRegex;

	}

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

	private static String escape(ListIterator<String> iter) {

		if (!iter.previous().equals(FSAAutomaton.backslash)) {
			throw new RuntimeException("SNH");
		}
		iter.next();

		String escaped = iter.next();
		String retMe;
		char escapedOrig = FSAAutomaton.decode(escaped);

		if (Character.isLetterOrDigit(escapedOrig)) {
			throw new UnsupportedRegexException();
		} else {
			retMe = escaped;
		}
		return retMe;
	}
}