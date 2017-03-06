package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import java.util.*;

public class EncodedCallStrings {

	List<CallString> callStrings;

	public EncodedCallStrings() {
		this.callStrings = new LinkedList<CallString>();
	}

	public EncodedCallStrings(CallString firstCallString) {
		this.callStrings = new LinkedList<CallString>();
		this.callStrings.add(firstCallString);
	}

	public int getPosition(CallString findMe) {
		int index = 0;
		for (Iterator<CallString> iter = this.callStrings.iterator(); iter.hasNext();) {
			CallString callString = (CallString) iter.next();
			if (callString.equals(findMe)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public CallString getCallString(int position) {
		if (position >= this.callStrings.size()) {
			return null;
		}
		return (CallString) this.callStrings.get(position);
	}

	public List<CallString> getCallStrings() {
		return this.callStrings;
	}

	public int append(CallString appendMe) {
		int newIndex = this.callStrings.size();
		this.callStrings.add(appendMe);
		return newIndex;
	}

	public String toString() {
		if (this.callStrings.isEmpty()) {
			return "<empty>";
		}

		StringBuilder myString = new StringBuilder();
		for (Iterator<CallString> iter = this.callStrings.iterator(); iter.hasNext();) {
			CallString callString = (CallString) iter.next();
			myString.append(callString);
			myString.append(", ");
		}
		return myString.substring(0, myString.length() - 2);
	}

	public String dump() {
		if (this.callStrings.isEmpty()) {
			return "<empty>\n";
		}

		StringBuilder b = new StringBuilder();
		for (Iterator<CallString> iter = this.callStrings.iterator(); iter.hasNext();) {
			CallString callString = (CallString) iter.next();
			b.append(callString.dump());
		}
		return b.toString();
	}

	public boolean isEmpty() {
		return this.callStrings.isEmpty();
	}

	public int size() {
		return this.callStrings.size();
	}
}
