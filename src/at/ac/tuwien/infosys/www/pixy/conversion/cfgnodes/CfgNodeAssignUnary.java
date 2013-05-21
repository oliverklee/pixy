package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * "left = op right"
 *
 * "op" can be:
 * + - ! ~ (int) (double) (string) (array) (object) (bool) (unset)
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeAssignUnary extends CfgNode {
    private Variable left;
    private TacPlace right;
    private int op;

// CONSTRUCTORS ********************************************************************

    public CfgNodeAssignUnary(Variable left, TacPlace right, int op, ParseNode node) {
        super(node);
        this.left = left;
        this.right = right;
        this.op = op;
    }

//  GET ****************************************************************************

    public Variable getLeft() {
        return this.left;
    }

    public TacPlace getRight() {
        return this.right;
    }

    public int getOperator() {
        return this.op;
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