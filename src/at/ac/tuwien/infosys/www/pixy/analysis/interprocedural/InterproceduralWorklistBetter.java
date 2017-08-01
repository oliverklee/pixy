package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class InterproceduralWorklistBetter implements InterproceduralWorklist {

	private InterproceduralWorklistOrder order;
	private SortedMap<Integer, InterproceduralWorklistElement> sortedWorkList;

	public InterproceduralWorklistBetter(InterproceduralWorklistOrder order) {
		this.order = order;
		this.sortedWorkList = new TreeMap<Integer, InterproceduralWorklistElement>();
	}

	public void add(AbstractCfgNode cfgNode, AbstractContext context) {
		if (context == null) {
			throw new RuntimeException("SNH");
		}
		InterproceduralWorklistElement element = new InterproceduralWorklistElement(cfgNode, context);
		Integer rpo = this.order.getReversePostOrder(element);
		if (rpo == null) {
			throw new RuntimeException("SNH");
		}
		this.sortedWorkList.put(rpo, element);

	}

	public InterproceduralWorklistElement removeNext() {
		Integer key = this.sortedWorkList.firstKey();
		return this.sortedWorkList.remove(key);
	}

	public boolean hasNext() {
		if (this.sortedWorkList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
