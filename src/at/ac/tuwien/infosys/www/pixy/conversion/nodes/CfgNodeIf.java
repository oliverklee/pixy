package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

//*********************************************************************************
//CfgNodeIf ***********************************************************************
//*********************************************************************************

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeIf
    extends CfgNode {

    private TacPlace leftOperand;
    private TacPlace rightOperand;  // may only be Constant.TRUE or Constant.FALSE
    private int op;

//CONSTRUCTORS ********************************************************************

    public CfgNodeIf(TacPlace leftOperand, TacPlace rightOperand, int op, ParseNode node) {
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
        if (this.leftOperand instanceof Variable) {
            retMe.add((Variable) this.leftOperand);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

//  SET ****************************************************************************

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