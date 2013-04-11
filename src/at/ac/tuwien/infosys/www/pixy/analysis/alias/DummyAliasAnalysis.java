package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class DummyAliasAnalysis
extends AliasAnalysis {

    public DummyAliasAnalysis () {
        super();
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // returns the set of must-aliases (Variable's) for the given variable
    // at the given node (folded over all contexts)
    public Set<Variable> getMustAliases(Variable var, CfgNode cfgNode) {
        Set<Variable> retMe = new HashSet<Variable>();
        retMe.add(var);
        return retMe;
    }

    // returns the set of may-aliases (Variable's) for the given variable
    // at the given node (folded over all contexts)
    public Set getMayAliases(Variable var, CfgNode cfgNode) {
        return Collections.EMPTY_SET;
    }

    // returns an arbitrary global must-alias of the given variable at
    // the given node (folded over all contexts); null if there is none
    public Variable getGlobalMustAlias(Variable var, CfgNode cfgNode) {
        return null;
    }

    // returns a set of local must-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set getLocalMustAliases(Variable var, CfgNode cfgNode) {
        return Collections.EMPTY_SET;
    }

    // returns a set of global may-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set getGlobalMayAliases(Variable var, CfgNode cfgNode) {
        return Collections.EMPTY_SET;
    }

    // returns a set of local may-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set getLocalMayAliases(Variable var, CfgNode cfgNode) {
        return Collections.EMPTY_SET;
    }
}