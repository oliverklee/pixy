package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class InterproceduralWorklistPoor implements InterproceduralWorklist {

	private LinkedList<InterproceduralWorklistElement> unsortedWorkList;
	private SortedMap<Integer, InterproceduralWorklistElement> sortedWorkList;

	public InterproceduralWorklistPoor() {
		this.unsortedWorkList = new LinkedList<InterproceduralWorklistElement>();
		this.sortedWorkList = new TreeMap<Integer, InterproceduralWorklistElement>();
	}

	public void add(AbstractCfgNode cfgNode, AbstractContext context) {
		if (context == null) {
			throw new RuntimeException("SNH");
		}
		InterproceduralWorklistElement element = new InterproceduralWorklistElement(cfgNode, context);
		int rpo = cfgNode.getReversePostOrder();
		if (rpo == -1) {
			this.unsortedWorkList.add(element);
		} else {
			this.sortedWorkList.put(rpo, element);
		}
	}

	public InterproceduralWorklistElement removeNext() {
		if (!this.unsortedWorkList.isEmpty()) {
			return this.unsortedWorkList.removeFirst();
		} else {
			Integer key = this.sortedWorkList.firstKey();
			return this.sortedWorkList.remove(key);
		}
	}

	public boolean hasNext() {
		if (this.unsortedWorkList.isEmpty() && this.sortedWorkList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
