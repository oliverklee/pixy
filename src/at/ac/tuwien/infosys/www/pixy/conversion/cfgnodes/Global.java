package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * A CFG node for assignments in the form "global variable".
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Global extends AbstractCfgNode {
    private Variable operand;

    public Global(AbstractTacPlace operand, ParseNode node) {
        super(node);
        this.operand = (Variable) operand;  // must be a variable
    }

    public Variable getOperand() {
        return this.operand;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
        if (this.operand != null) {
            retMe.add(this.operand);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

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