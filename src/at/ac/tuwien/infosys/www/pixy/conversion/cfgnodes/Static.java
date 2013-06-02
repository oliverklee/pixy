package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Static extends AbstractCfgNode {
    private AbstractTacPlace operand;
    private AbstractTacPlace initialPlace;
    private boolean hasInitialPlace;

    public Static(AbstractTacPlace operand, ParseNode node) {
        super(node);
        this.operand = operand;
        this.hasInitialPlace = false;
        this.initialPlace = null;
    }

    public Static(AbstractTacPlace operand, AbstractTacPlace initialPlace, ParseNode node) {
        super(node);
        this.operand = operand;
        this.hasInitialPlace = true;
        this.initialPlace = initialPlace;
    }

    public boolean hasInitialPlace() {
        return this.hasInitialPlace;
    }

    public AbstractTacPlace getOperand() {
        return this.operand;
    }

    public AbstractTacPlace getInitialPlace() {
        return this.initialPlace;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
        if (this.operand instanceof Variable) {
            retMe.add((Variable) this.operand);
        } else {
            retMe.add(null);
        }
        if (this.initialPlace instanceof Variable) {
            retMe.add((Variable) this.initialPlace);
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
            case 1:
                this.initialPlace = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}