package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class CallStringWorkListElement {

	private final AbstractCfgNode cfgNode;
	private final int position;

	CallStringWorkListElement(AbstractCfgNode cfgNode, int position) {
		this.cfgNode = cfgNode;
		this.position = position;
	}

	AbstractCfgNode getCfgNode() {
		return this.cfgNode;
	}

	int getPosition() {
		return this.position;
	}
}
