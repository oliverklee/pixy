package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class CallStringWorklistElement {
    private final AbstractCfgNode cfgNode;
    private final int position;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    CallStringWorklistElement(AbstractCfgNode cfgNode, int position) {
        this.cfgNode = cfgNode;
        this.position = position;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    AbstractCfgNode getCfgNode() {
        return this.cfgNode;
    }

    int getPosition() {
        return this.position;
    }
}