package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeEmpty extends AbstractCfgNode {
// CONSTRUCTORS ********************************************************************

    public CfgNodeEmpty() {
        super();
        // empty CFG nodes will be deleted from the CFG, so their ID's can be
        // recycled; TOO DANGEROUS TO DO IT HERE! better: additional pass over
        // all CFGs
    }

    public CfgNodeEmpty(ParseNode parseNode) {
        super(parseNode);
    }

//  GET ****************************************************************************

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
    }
}