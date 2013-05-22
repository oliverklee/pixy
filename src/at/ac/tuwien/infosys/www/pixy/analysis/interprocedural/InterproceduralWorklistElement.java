package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class InterproceduralWorklistElement {
    private final AbstractCfgNode cfgNode;
    private final AbstractContext context;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    InterproceduralWorklistElement(AbstractCfgNode cfgNode, AbstractContext context) {
        this.cfgNode = cfgNode;
        this.context = context;
        if (context == null) {
            throw new RuntimeException("SNH");
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    AbstractCfgNode getCfgNode() {
        return this.cfgNode;
    }

    AbstractContext getContext() {
        return this.context;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  equals *************************************************************************

    public boolean equals(Object compX) {
        if (compX == this) {
            return true;
        }
        if (!(compX instanceof InterproceduralWorklistElement)) {
            return false;
        }
        InterproceduralWorklistElement comp = (InterproceduralWorklistElement) compX;

        // the dependency and CA maps have to be equal
        return this.cfgNode.equals(comp.cfgNode) && this.context.equals(comp.context);
    }

//  hashCode ***********************************************************************

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        hashCode = 37 * hashCode + this.context.hashCode();
        return hashCode;
    }
}