package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeEcho extends CfgNode {
    private TacPlace place;

// CONSTRUCTORS ********************************************************************

    public CfgNodeEcho(TacPlace place, ParseNode node) {
        super(node);
        this.place = place;
    }

//  GET *****************************************************************************

    public TacPlace getPlace() {
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

//  SET ****************************************************************************

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