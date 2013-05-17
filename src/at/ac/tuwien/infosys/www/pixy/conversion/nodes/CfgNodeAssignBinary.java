package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * "left = leftOperand op rightOperand"
 *
 * "op" can be: TacOperators.<...>
 * CONCAT, BOOLEAN_AND, BITWISE_OR, BITWISE_AND, BITWISE_XOR,
 * PLUS, MINUS, MULT, DIV, MODULO,
 * SL, SR, IS_IDENTICAL, IS_NOT_IDENTICAL, IS_EQUAL, IS_NOT_EQUAL,
 * IS_SMALLER, IS_SMALLER_OR_EQUAL, IS_GREATER, IS_GREATER_OR_EQUAL
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeAssignBinary
    extends CfgNode {

    private Variable left;
    private TacPlace leftOperand;
    private TacPlace rightOperand;
    private int op;

//  CONSTRUCTORS *******************************************************************

    public CfgNodeAssignBinary(
        Variable left, TacPlace leftOperand, TacPlace rightOperand,
        int op, ParseNode node) {

        super(node);
        this.left = left;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.op = op;
    }

//  GET ****************************************************************************

    public Variable getLeft() {
        return this.left;
    }

    public TacPlace getLeftOperand() {
        return this.leftOperand;
    }

    public TacPlace getRightOperand() {
        return this.rightOperand;
    }

    public int getOperator() {
        return this.op;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
        retMe.add(this.left);
        if (this.leftOperand instanceof Variable) {
            retMe.add((Variable) this.leftOperand);
        } else {
            retMe.add(null);
        }
        if (this.rightOperand instanceof Variable) {
            retMe.add((Variable) this.rightOperand);
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
                this.leftOperand = replacement;
                break;
            case 2:
                this.rightOperand = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}