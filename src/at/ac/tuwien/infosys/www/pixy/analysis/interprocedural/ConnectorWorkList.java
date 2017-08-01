package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

public final class ConnectorWorkList {

	private LinkedList<ConnectorWorkListElement> workList;

	ConnectorWorkList() {
		this.workList = new LinkedList<ConnectorWorkListElement>();
	}

	void add(ConnectorWorkListElement element) {
		this.workList.add(element);
	}

	ConnectorWorkListElement removeNext() {
		return (ConnectorWorkListElement) this.workList.removeFirst();
	}

	boolean hasNext() {
		return (this.workList.size() > 0 ? true : false);
	}
}
