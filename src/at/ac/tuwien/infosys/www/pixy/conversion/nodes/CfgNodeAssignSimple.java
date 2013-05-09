package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// *********************************************************************************
// CfgNodeAssignSimple *************************************************************
// *********************************************************************************

// "left = right"
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeAssignSimple
    extends CfgNode {

    private Variable left;
    private TacPlace right;

//  CONSTRUCTORS *******************************************************************

    public CfgNodeAssignSimple(Variable left, TacPlace right, ParseNode parseNode) {
        super(parseNode);
        this.left = left;
        this.right = right;
    }

//  GET ****************************************************************************

    public Variable getLeft() {
        return this.left;
    }

    public TacPlace getRight() {
        return this.right;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
        retMe.add(this.left);
        if (this.right instanceof Variable) {
            retMe.add((Variable) this.right);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
            case 0:
                this.left = replacement;
                break;
            case 1:
                this.right = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}