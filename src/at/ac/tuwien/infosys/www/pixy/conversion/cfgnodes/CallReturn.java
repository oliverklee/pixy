package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;


/**
 * This class represents a call node's successor.
 *
 * Return from called function. "2nd half" of a function call node.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallReturn extends AbstractCfgNode {
    public CallReturn(ParseNode parseNode) {
        super(parseNode);
    }

    public Variable getRetVar() {
        return this.getCallNode().getRetVar();
    }

    public Variable getTempVar() {
        return this.getCallNode().getTempVar();
    }

    public CallPreparation getCallPrepNode() {
        return (CallPreparation) this.getPredecessor().getPredecessor();
    }

    public Call getCallNode() {
        return (Call) this.getPredecessor();
    }

    // not relevant for globals replacement
    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }

    public void setRetVar(Variable retVar) {
        this.getCallNode().setRetVar(retVar);
    }
}