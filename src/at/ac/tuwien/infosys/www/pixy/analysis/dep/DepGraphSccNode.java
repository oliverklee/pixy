package at.ac.tuwien.infosys.www.pixy.analysis.dep;

// special node for approximating SCCs in the string graph
public class DepGraphSccNode
extends DepGraphNode {

    DepGraphSccNode() {
    }

    // returns a name that can be used in dot file representation
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