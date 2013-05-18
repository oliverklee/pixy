package at.ac.tuwien.infosys.www.pixy.analysis.inter.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.LinkedList;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class FunctionalWorkList {

    private LinkedList<FunctionalWorkListElement> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    FunctionalWorkList() {
        this.workList = new LinkedList<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    void add(FunctionalWorkListElement element) {
        this.workList.add(element);
    }

    void add(CfgNode cfgNode, LatticeElement context) {
        this.workList.add(new FunctionalWorkListElement(cfgNode, context));
    }

    // actually implemented as FIFO
    // EFF: it would be more efficient to analyze all conditional branches first
    // before going on to the code behind the branches
    FunctionalWorkListElement removeNext() {
        return this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0);
    }
}