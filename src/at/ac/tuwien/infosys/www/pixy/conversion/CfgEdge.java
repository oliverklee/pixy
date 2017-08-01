package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public final class CfgEdge {

	static final int FALSE_EDGE = 0;
	static final int TRUE_EDGE = 1;
	static public final int NORMAL_EDGE = 2;
	static final int NO_EDGE = 3;

	private final int type;
	private final AbstractCfgNode source;
	private AbstractCfgNode dest;

	CfgEdge(AbstractCfgNode source, AbstractCfgNode dest, int type) {
		this.source = source;
		this.dest = dest;
		this.type = type;
	}

	public AbstractCfgNode getSource() {
		return this.source;
	}

	public AbstractCfgNode getDest() {
		return this.dest;
	}

	public int getType() {
		return this.type;
	}

	public String getName() {
		switch (this.type) {
		case CfgEdge.FALSE_EDGE:
			return "false";
		case CfgEdge.TRUE_EDGE:
			return "true";
		case CfgEdge.NORMAL_EDGE:
			return "normal";
		case CfgEdge.NO_EDGE:
			return "none";
		default:
			return "unknown";
		}
	}

	void setDest(AbstractCfgNode dest) {
		this.dest = dest;
	}
}