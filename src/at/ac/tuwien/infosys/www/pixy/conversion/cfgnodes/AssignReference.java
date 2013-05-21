package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignReference extends AbstractCfgNode {
    private Variable left;
    private Variable right;

//  CONSTRUCTORS *******************************************************************

    public AssignReference(Variable left, Variable right, ParseNode node) {
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
        List<Variable> retMe = new LinkedList<>();
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