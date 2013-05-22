package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * "left = right"
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignSimple extends AbstractCfgNode {
    private Variable left;
    private AbstractTacPlace right;

//  CONSTRUCTORS *******************************************************************

    public AssignSimple(Variable left, AbstractTacPlace right, ParseNode parseNode) {
        super(parseNode);
        this.left = left;
        this.right = right;
    }

//  GET ****************************************************************************

    public Variable getLeft() {
        return this.left;
    }

    public AbstractTacPlace getRight() {
        return this.right;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
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