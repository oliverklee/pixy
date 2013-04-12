package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public final class CfgEdge {

    static final int FALSE_EDGE = 0;
    static final int TRUE_EDGE = 1;
    static public final int NORMAL_EDGE = 2;
    static final int NO_EDGE = 3;

    private final int type;
    private final CfgNode source;
    private CfgNode dest;

// CONSTRUCTORS ********************************************************************

    CfgEdge(CfgNode source, CfgNode dest, int type) {
        this.source = source;
        this.dest = dest;
        this.type = type;
    }

// GET *****************************************************************************

    public CfgNode getSource() {
        return this.source;
    }

    public CfgNode getDest() {
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

// SET *****************************************************************************

    // don't forget to inform the destination node about me with "addInEdge"
    void setDest(CfgNode dest) {
        this.dest = dest;
    }
}