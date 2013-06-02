package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents an echo call in the form echo(variable).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Echo extends AbstractCfgNode {
    private AbstractTacPlace place;

    public Echo(AbstractTacPlace place, ParseNode node) {
        super(node);
        this.place = place;
    }

    public AbstractTacPlace getPlace() {
        return this.place;
    }

    public List<Variable> getVariables() {
        if (this.place instanceof Variable) {
            List<Variable> retMe = new LinkedList<>();
            retMe.add((Variable) this.place);
            return retMe;
        } else {
            return Collections.emptyList();
        }
    }

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
            case 0:
                this.place = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }
}