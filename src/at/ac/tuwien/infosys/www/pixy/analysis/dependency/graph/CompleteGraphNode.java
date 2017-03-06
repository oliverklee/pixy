package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

public class CompleteGraphNode extends AbstractNode {

	CompleteGraphNode() {
	}

	public String dotName() {
		return "SCC";
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
