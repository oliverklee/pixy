package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeAssignRef
    extends CfgNode {

    private Variable left;
    private Variable right;

//  CONSTRUCTORS *******************************************************************

    public CfgNodeAssignRef(Variable left, Variable right, ParseNode node) {
        super(node);
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
        retMe.add(this.right);
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