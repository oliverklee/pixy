package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// *********************************************************************************
// CfgNodeUnset ********************************************************************
// *********************************************************************************

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeUnset
    extends CfgNode {

    private Variable operand;

// CONSTRUCTORS ********************************************************************

    public CfgNodeUnset(TacPlace operand, ParseNode node) {
        super(node);
        this.operand = (Variable) operand;  // must be a variable
    }

// GET *****************************************************************************

    public Variable getOperand() {
        return this.operand;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
        if (this.operand instanceof Variable) {
            retMe.add((Variable) this.operand);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
            case 0:
                this.operand = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}