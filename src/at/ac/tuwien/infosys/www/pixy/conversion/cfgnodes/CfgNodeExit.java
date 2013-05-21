package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * Don't assign any transfer function other than ID to this node!
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeExit extends AbstractCfgNode {
// CONSTRUCTORS ********************************************************************

    public CfgNodeExit() {
        super();
    }

    public CfgNodeExit(ParseNode node) {
        super(node);
    }

// SET *****************************************************************************

// GET *****************************************************************************

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }
}