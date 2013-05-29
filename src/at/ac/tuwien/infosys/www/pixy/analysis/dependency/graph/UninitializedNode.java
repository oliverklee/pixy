package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

/**
 * Special node representing uninitialized variables.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class UninitializedNode extends AbstractNode {
    public UninitializedNode() {
    }

    /**
     * Returns a name that can be used in dot file representation.
     *
     * @return
     */
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
}