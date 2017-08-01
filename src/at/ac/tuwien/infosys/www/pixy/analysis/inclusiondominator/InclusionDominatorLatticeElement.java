package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import java.util.*;

public class InclusionDominatorLatticeElement extends AbstractLatticeElement {

	private List<AbstractCfgNode> dominators;

	public InclusionDominatorLatticeElement() {
		this.dominators = new LinkedList<AbstractCfgNode>();
	}

	public InclusionDominatorLatticeElement(InclusionDominatorLatticeElement cloneMe) {
		this.dominators = new LinkedList<AbstractCfgNode>(cloneMe.getDominators());
	}

	public AbstractLatticeElement cloneMe() {
		return new InclusionDominatorLatticeElement(this);
	}

	public List<AbstractCfgNode> getDominators() {
		return this.dominators;
	}

	public void lub(AbstractLatticeElement element) {
		if (!(element instanceof InclusionDominatorLatticeElement)) {
			throw new RuntimeException("SNH");
		}
		this.lub((InclusionDominatorLatticeElement) element);
	}

	public void lub(InclusionDominatorLatticeElement element) {
		Iterator<AbstractCfgNode> foreignIter = element.getDominators().iterator();
		Iterator<AbstractCfgNode> myIter = this.dominators.iterator();
		List<AbstractCfgNode> newList = new LinkedList<AbstractCfgNode>();
		boolean goOn = true;
		while (foreignIter.hasNext() && myIter.hasNext() && goOn) {
			AbstractCfgNode myNode = (AbstractCfgNode) myIter.next();
			AbstractCfgNode foreignNode = (AbstractCfgNode) foreignIter.next();
			if (myNode == foreignNode) {
				newList.add(myNode);
			} else {
				goOn = false;
			}
		}
		this.dominators = newList;
	}

	public void add(AbstractCfgNode cfgNode) {
		this.dominators.add(cfgNode);
	}

	public boolean structureEquals(Object compX) {
		InclusionDominatorLatticeElement comp = (InclusionDominatorLatticeElement) compX;
		if (this.dominators.equals(comp.getDominators())) {
			return true;
		} else {
			return false;
		}
	}

	public int structureHashCode() {
		return this.dominators.hashCode();
	}

	public void dump() {
		System.out.println("InclusionDominatorLatticeElement.dump(): not yet");
	}
}
