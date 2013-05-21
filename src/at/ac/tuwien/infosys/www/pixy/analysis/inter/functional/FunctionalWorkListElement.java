package at.ac.tuwien.infosys.www.pixy.analysis.inter.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class FunctionalWorkListElement {
    private final CfgNode cfgNode;
    private final LatticeElement context;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    FunctionalWorkListElement(CfgNode cfgNode, LatticeElement context) {
        this.cfgNode = cfgNode;
        this.context = context;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    CfgNode getCfgNode() {
        return this.cfgNode;
    }

    LatticeElement getContext() {
        return this.context;
    }
}