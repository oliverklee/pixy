package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * This class represents a (directed) edge in the control flow graph. An edge always is between two
 * AbstractCfgNodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class CfgEdge {
    static final int FALSE_EDGE = 0;
    static final int TRUE_EDGE = 1;
    static public final int NORMAL_EDGE = 2;
    static final int NO_EDGE = 3;

    private final int type;
    private final AbstractCfgNode source;
    private AbstractCfgNode destination;

    CfgEdge(AbstractCfgNode source, AbstractCfgNode destination, int type) {
        this.source = source;
        this.destination = destination;
        this.type = type;
    }

    public AbstractCfgNode getSource() {
        return this.source;
    }

    public AbstractCfgNode getDestination() {
        return this.destination;
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

    // don't forget to inform the destination node about me with "addInEdge"
    void setDestination(AbstractCfgNode destination) {
        this.destination = destination;
    }
}