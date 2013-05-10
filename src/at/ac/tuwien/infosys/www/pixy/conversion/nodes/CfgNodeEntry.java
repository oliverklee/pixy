package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

// *********************************************************************************
// CfgNodeEntry ********************************************************************
// *********************************************************************************

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeEntry
    extends CfgNode {

// CONSTRUCTORS ********************************************************************

    // necessary constructor for special functions (have no associated
    // parse node)
    public CfgNodeEntry() {
        super();
    }

    public CfgNodeEntry(ParseNode node) {
        super(node);
    }

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }
}