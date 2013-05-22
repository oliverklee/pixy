package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * Almost identical to Call.
 *
 * Doesn't do 'function.addCall(this)' in the constructor and in setFunction, and has cbrPairs.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallPreparation extends AbstractCfgNode {
// CONSTRUCTORS ********************************************************************

    public CallPreparation(ParseNode node) {
        super(node);
    }

// GET *****************************************************************************

    public TacFunction getCallee() {
        return this.getCallNode().getCallee();
    }

    public TacFunction getCaller() {
        return this.getCallNode().getEnclosingFunction();
    }

    public TacPlace getFunctionNamePlace() {
        return this.getCallNode().getFunctionNamePlace();
    }

    public List<TacActualParameter> getParamList() {
        return this.getCallNode().getParamList();
    }

    public CallReturn getCallRetNode() {
        return (CallReturn) this.getSuccessor(0).getSuccessor(0);
    }

    public Call getCallNode() {
        return (Call) this.getSuccessor(0);
    }

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

    // returns a list consisting of two-element-lists consisting of
    // (actual cbr-param, formal cbr-param) (Variable objects)
    public List<List<Variable>> getCbrParams() {
        return this.getCallNode().getCbrParams();
    }

// SET *****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }
}