package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

public class UninitializedNode extends AbstractNode {

	public UninitializedNode() {
	}

	public String dotName() {
		return "<uninit>";
	}

	public String comparableName() {
		return dotName();
	}

	public String dotNameShort() {
		return dotName();
	}

	public String dotNameVerbose(boolean isModelled) {
		return dotName();
	}

	public int getLine() {
		return -1;
	}

}
