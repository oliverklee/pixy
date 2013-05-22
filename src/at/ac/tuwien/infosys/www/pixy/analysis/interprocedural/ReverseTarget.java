package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ReverseTarget {
    private Call callNode;

    // a set of Contexts
    private Set<? extends AbstractContext> contexts;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

    public ReverseTarget(Call callNode, Set<? extends AbstractContext> contexts) {
        this.callNode = callNode;
        this.contexts = contexts;
    }

//  *********************************************************************************
//  GET *****************************************************************************
//  *********************************************************************************

    public Call getCallNode() {
        return this.callNode;
    }

    public Set<? extends AbstractContext> getContexts() {
        return this.contexts;
    }
}