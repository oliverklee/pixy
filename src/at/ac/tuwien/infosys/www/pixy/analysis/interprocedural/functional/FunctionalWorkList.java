package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class FunctionalWorkList {

	private LinkedList<FunctionalWorkListElement> workList;

	FunctionalWorkList() {
		this.workList = new LinkedList<FunctionalWorkListElement>();
	}

	void add(FunctionalWorkListElement element) {
		this.workList.add(element);
	}

	void add(AbstractCfgNode cfgNode, AbstractLatticeElement context) {
		this.workList.add(new FunctionalWorkListElement(cfgNode, context));
	}

	FunctionalWorkListElement removeNext() {
		return (FunctionalWorkListElement) this.workList.removeFirst();
	}

	boolean hasNext() {
		return (this.workList.size() > 0 ? true : false);
	}
}
