package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import java.util.*;

public final class CallStringWorkList {

	private LinkedList<CallStringWorkListElement> workList;

	CallStringWorkList() {
		this.workList = new LinkedList<CallStringWorkListElement>();
	}

	void add(CallStringWorkListElement element) {
		this.workList.add(element);
	}

	CallStringWorkListElement removeNext() {
		return this.workList.removeFirst();
	}

	boolean hasNext() {
		return (this.workList.size() > 0 ? true : false);
	}
}
