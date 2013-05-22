package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class FunctionalWorkListElement {
    private final AbstractCfgNode cfgNode;
    private final AbstractLatticeElement context;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    FunctionalWorkListElement(AbstractCfgNode cfgNode, AbstractLatticeElement context) {
        this.cfgNode = cfgNode;
        this.context = context;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    AbstractCfgNode getCfgNode() {
        return this.cfgNode;
    }

    AbstractLatticeElement getContext() {
        return this.context;
    }
}