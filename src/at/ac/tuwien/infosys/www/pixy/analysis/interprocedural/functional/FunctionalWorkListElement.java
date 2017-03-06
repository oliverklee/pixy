package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class FunctionalWorkListElement {

	private final AbstractCfgNode cfgNode;
	private final AbstractLatticeElement context;

	FunctionalWorkListElement(AbstractCfgNode cfgNode, AbstractLatticeElement context) {
		this.cfgNode = cfgNode;
		this.context = context;
	}

	AbstractCfgNode getCfgNode() {
		return this.cfgNode;
	}

	AbstractLatticeElement getContext() {
		return this.context;
	}
}
