package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

public abstract class AbstractNode {

	public abstract String dotName();

	public abstract String comparableName();

	public abstract String dotNameShort();

	public abstract String dotNameVerbose(boolean isModelled);
}
