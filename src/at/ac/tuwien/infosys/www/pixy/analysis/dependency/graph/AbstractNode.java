package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

/**
 * Base class for dependency graph nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractNode {
    /**
     * Returns a name that can be used in dot file representation.
     *
     * @return
     */
    public abstract String dotName();

    public abstract String comparableName();

    public abstract String dotNameShort();

    public abstract String dotNameVerbose(boolean isModelled);
}