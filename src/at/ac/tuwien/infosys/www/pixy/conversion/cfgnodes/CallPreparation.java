package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a call node's predecessor.
 *
 * Almost identical to Call.
 *
 * Doesn't do 'function.addCall(this)' in the constructor and in setFunction, and has cbrPairs.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallPreparation extends AbstractCfgNode {
    public CallPreparation(ParseNode node) {
        super(node);
    }

    public TacFunction getCallee() {
        return this.getCallNode().getCallee();
    }

    public TacFunction getCaller() {
        return this.getCallNode().getEnclosingFunction();
    }

    public AbstractTacPlace getFunctionNamePlace() {
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

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }
}