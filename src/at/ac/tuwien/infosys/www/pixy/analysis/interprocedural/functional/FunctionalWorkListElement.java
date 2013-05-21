package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class FunctionalWorkListElement {
    private final AbstractCfgNode cfgNode;
    private final LatticeElement context;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    FunctionalWorkListElement(AbstractCfgNode cfgNode, LatticeElement context) {
        this.cfgNode = cfgNode;
        this.context = context;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    AbstractCfgNode getCfgNode() {
        return this.cfgNode;
    }

    LatticeElement getContext() {
        return this.context;
    }
}