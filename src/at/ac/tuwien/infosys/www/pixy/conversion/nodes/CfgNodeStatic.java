package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// *********************************************************************************
// CfgNodeStatic *******************************************************************
// *********************************************************************************

public class CfgNodeStatic
extends CfgNode {

    private TacPlace operand;
    private TacPlace initialPlace;
    private boolean hasInitialPlace;

// CONSTRUCTORS ********************************************************************

    public CfgNodeStatic(TacPlace operand, ParseNode node) {
        super(node);
        this.operand = operand;
        this.hasInitialPlace = false;
        this.initialPlace = null;
    }

    public CfgNodeStatic(TacPlace operand, TacPlace initialPlace, ParseNode node) {
        super(node);
        this.operand = operand;
        this.hasInitialPlace = true;
        this.initialPlace = initialPlace;
    }

// GET *****************************************************************************

    public boolean hasInitialPlace() {
        return this.hasInitialPlace;
    }

    public TacPlace getOperand() {
        return this.operand;
    }

    public TacPlace getInitialPlace() {
        return this.initialPlace;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
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

//  SET ****************************************************************************

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