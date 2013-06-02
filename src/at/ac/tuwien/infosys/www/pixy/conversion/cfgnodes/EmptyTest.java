package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class EmptyTest extends AbstractCfgNode {
    private AbstractTacPlace left;
    private AbstractTacPlace right;

    public EmptyTest(AbstractTacPlace left, AbstractTacPlace right, ParseNode node) {
        super(node);
        this.left = left;
        this.right = right;
    }

    public AbstractTacPlace getLeft() {
        return this.left;
    }

    public AbstractTacPlace getRight() {
        return this.right;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
        if (this.left instanceof Variable) {
            retMe.add((Variable) this.left);
        } else {
            retMe.add(null);
        }
        if (this.right instanceof Variable) {
            retMe.add((Variable) this.right);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

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