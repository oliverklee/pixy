package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class InterWorkListElement {

    private final CfgNode cfgNode;
    private final Context context;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    InterWorkListElement(CfgNode cfgNode, Context context) {
        this.cfgNode = cfgNode;
        this.context = context;
        if (context == null) {
            throw new RuntimeException("SNH");
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    CfgNode getCfgNode() {
        return this.cfgNode;
    }

    Context getContext() {
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
        if (!(compX instanceof InterWorkListElement)) {
            return false;
        }
        InterWorkListElement comp = (InterWorkListElement) compX;

        // the dep and CA maps have to be equal
        if (!this.cfgNode.equals(comp.cfgNode)) {
            return false;
        }
        return this.context.equals(comp.context);
    }

//  hashCode ***********************************************************************

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        hashCode = 37 * hashCode + this.context.hashCode();
        return hashCode;
    }
}