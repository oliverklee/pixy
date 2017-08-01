package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class CallString {

	private LinkedList<Call> callNodeList;

	public CallString() {
		this.callNodeList = new LinkedList<Call>();
	}

	private CallString(LinkedList<Call> callNodeList) {
		this.callNodeList = callNodeList;
	}

	public CallString append(Call callNode, int kSize) {
		LinkedList<Call> newList = new LinkedList<Call>(this.callNodeList);
		newList.add(callNode);
		if (newList.size() > kSize) {
			newList.remove(0);
		}
		return new CallString(newList);
	}

	public Call getLast() {
		return (Call) this.callNodeList.getLast();
	}

	public List<Call> getCallNodeList() {
		return this.callNodeList;
	}

	public int hashCode() {
		return this.callNodeList.hashCode();
	}

	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof CallString)) {
			return false;
		}
		CallString comp = (CallString) obj;

		return this.callNodeList.equals(comp.getCallNodeList());
	}

	public String dump() {
		StringBuilder b = new StringBuilder();
		for (Call callNode : this.callNodeList) {
			b.append(callNode.getFileName());
			b.append(":");
			b.append(callNode.getOriginalLineNumber());
			b.append("\n");
		}
		return b.toString();
	}
}
