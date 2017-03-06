package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class IntraproceduralWorklist {

	private LinkedList<AbstractCfgNode> workList;

	IntraproceduralWorklist() {
		this.workList = new LinkedList<AbstractCfgNode>();
	}

	void add(AbstractCfgNode cfgNode) {
		this.workList.add(cfgNode);
	}

	AbstractCfgNode removeNext() {
		return (AbstractCfgNode) this.workList.removeFirst();
	}

	boolean hasNext() {
		return (this.workList.size() > 0 ? true : false);
	}
}
