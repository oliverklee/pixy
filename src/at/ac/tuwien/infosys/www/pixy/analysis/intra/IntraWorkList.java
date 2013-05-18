package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.LinkedList;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class IntraWorkList {

    private LinkedList<CfgNode> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    IntraWorkList() {
        this.workList = new LinkedList<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    void add(CfgNode cfgNode) {
        this.workList.add(cfgNode);
    }

    // actually implemented as FIFO
    // EFF: it would be more efficient to analyze all conditional branches first
    // before going on to the code behind the branches (reverse postorder)
    CfgNode removeNext() {
        return this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0);
    }
}