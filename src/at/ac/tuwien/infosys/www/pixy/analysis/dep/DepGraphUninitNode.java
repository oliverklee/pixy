package at.ac.tuwien.infosys.www.pixy.analysis.dep;

/**
 * Special node representing uninitialized variables.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepGraphUninitNode
    extends DepGraphNode {

    public DepGraphUninitNode() {
    }

    // returns a name that can be used in dot file representation
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