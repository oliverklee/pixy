package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringContext;

public class ConnectorFunction {

	private Map<CallStringContext, CallStringContext> pos2pos;

	private Map<CallStringContext, Set<CallStringContext>> reverse;

	public ConnectorFunction() {
		this.pos2pos = new HashMap<CallStringContext, CallStringContext>();
		this.reverse = new HashMap<CallStringContext, Set<CallStringContext>>();
	}

	public void add(int from, int to) {

		CallStringContext fromInt = new CallStringContext(from);
		CallStringContext toInt = new CallStringContext(to);

		this.pos2pos.put(fromInt, toInt);

		Set<CallStringContext> reverseSet = this.reverse.get(toInt);
		if (reverseSet == null) {
			reverseSet = new HashSet<CallStringContext>();
			reverseSet.add(fromInt);
			this.reverse.put(toInt, reverseSet);
		} else {
			reverseSet.add(fromInt);
		}
	}

	public CallStringContext apply(int input) {
		CallStringContext output = (CallStringContext) this.pos2pos.get(new CallStringContext(input));
		return output;
	}

	public Set<CallStringContext> reverseApply(int output) {
		return this.reverse.get(new CallStringContext(output));
	}

	@SuppressWarnings("rawtypes")
	public String toString() {
		if (this.pos2pos.isEmpty()) {
			return "<empty>";
		}
		StringBuilder myString = new StringBuilder();
		for (Iterator<?> iter = this.pos2pos.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			CallStringContext from = (CallStringContext) entry.getKey();
			CallStringContext to = (CallStringContext) entry.getValue();
			myString.append(from);
			myString.append(" -> ");
			myString.append(to);
			myString.append(System.getProperty("line.separator"));
		}
		return myString.substring(0, myString.length() - 1);
	}
}
