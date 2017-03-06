package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class InterproceduralWorklistElement {

	private final AbstractCfgNode cfgNode;
	private final AbstractContext context;

	InterproceduralWorklistElement(AbstractCfgNode cfgNode, AbstractContext context) {
		this.cfgNode = cfgNode;
		this.context = context;
		if (context == null) {
			throw new RuntimeException("SNH");
		}
	}

	AbstractCfgNode getCfgNode() {
		return this.cfgNode;
	}

	AbstractContext getContext() {
		return this.context;
	}

	public boolean equals(Object compX) {
		if (compX == this) {
			return true;
		}
		if (!(compX instanceof InterproceduralWorklistElement)) {
			return false;
		}
		InterproceduralWorklistElement comp = (InterproceduralWorklistElement) compX;
		if (!this.cfgNode.equals(comp.cfgNode)) {
			return false;
		}
		if (!this.context.equals(comp.context)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.cfgNode.hashCode();
		hashCode = 37 * hashCode + this.context.hashCode();
		return hashCode;
	}

}
