package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import java.util.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public final class IntraWorkList {

    private LinkedList<CfgNode> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************
    
    IntraWorkList() {
        this.workList = new LinkedList<CfgNode>();
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
        return (CfgNode) this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0 ? true : false);
    }
}
