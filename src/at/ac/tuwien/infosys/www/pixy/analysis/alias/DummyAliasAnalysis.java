package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DummyAliasAnalysis
    extends AliasAnalysis {

    public DummyAliasAnalysis() {
        super();
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // returns the set of must-aliases (Variable's) for the given variable
    // at the given node (folded over all contexts)
    public Set<Variable> getMustAliases(Variable var, CfgNode cfgNode) {
        Set<Variable> retMe = new HashSet<>();
        retMe.add(var);
        return retMe;
    }

    // returns the set of may-aliases (Variable's) for the given variable
    // at the given node (folded over all contexts)
    public Set<Variable> getMayAliases(Variable var, CfgNode cfgNode) {
        return Collections.emptySet();
    }

    // returns an arbitrary global must-alias of the given variable at
    // the given node (folded over all contexts); null if there is none
    public Variable getGlobalMustAlias(Variable var, CfgNode cfgNode) {
        return null;
    }

    // returns a set of local must-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set<Variable> getLocalMustAliases(Variable var, CfgNode cfgNode) {
        return Collections.emptySet();
    }

    // returns a set of global may-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set<Variable> getGlobalMayAliases(Variable var, CfgNode cfgNode) {
        return Collections.emptySet();
    }

    // returns a set of local may-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set<Variable> getLocalMayAliases(Variable var, CfgNode cfgNode) {
        return Collections.emptySet();
    }
}