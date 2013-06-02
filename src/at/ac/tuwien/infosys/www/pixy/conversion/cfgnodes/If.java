package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class If extends AbstractCfgNode {
    private AbstractTacPlace leftOperand;
    private AbstractTacPlace rightOperand;  // may only be Constant.TRUE or Constant.FALSE
    private int op;

    public If(AbstractTacPlace leftOperand, AbstractTacPlace rightOperand, int op, ParseNode node) {
        super(node);
        // make sure that right operand is valid (i.e. true or false)
        if (!(rightOperand == Constant.TRUE || rightOperand == Constant.FALSE)) {
            throw new RuntimeException(
                "SNH: illegal right operand for if node at line " +
                    node.getLinenoLeft());
        }
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.op = op;
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
        if (this.leftOperand instanceof Variable) {
            retMe.add((Variable) this.leftOperand);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
            case 0:
                this.leftOperand = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}