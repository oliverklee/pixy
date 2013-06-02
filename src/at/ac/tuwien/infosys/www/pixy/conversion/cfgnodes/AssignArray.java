package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * A CFG node for assignments in the form "variable = array()".
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignArray extends AbstractCfgNode {
    private Variable left;

    public AssignArray(Variable left, ParseNode node) {
        super(node);
        this.left = left;
    }

    public Variable getLeft() {
        return this.left;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
        retMe.add(this.left);
        return retMe;
    }

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
            case 0:
                this.left = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}