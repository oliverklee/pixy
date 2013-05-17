package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ReverseTarget {

    private CfgNodeCall callNode;

    // a set of Contexts
    private Set<Context> contexts;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

    public ReverseTarget(CfgNodeCall callNode, Set<Context> contexts) {
        this.callNode = callNode;
        this.contexts = contexts;
    }

//  *********************************************************************************
//  GET *****************************************************************************
//  *********************************************************************************

    public CfgNodeCall getCallNode() {
        return this.callNode;
    }

    public Set<Context> getContexts() {
        return this.contexts;
    }
}