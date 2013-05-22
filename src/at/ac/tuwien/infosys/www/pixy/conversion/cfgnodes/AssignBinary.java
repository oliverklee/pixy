package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
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
public class AssignBinary extends AbstractCfgNode {
    private Variable left;
    private AbstractTacPlace leftOperand;
    private AbstractTacPlace rightOperand;
    private int op;

//  CONSTRUCTORS *******************************************************************

    public AssignBinary(
        Variable left, AbstractTacPlace leftOperand, AbstractTacPlace rightOperand,
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

    public AbstractTacPlace getLeftOperand() {
        return this.leftOperand;
    }

    public AbstractTacPlace getRightOperand() {
        return this.rightOperand;
    }

    public int getOperator() {
        return this.op;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
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