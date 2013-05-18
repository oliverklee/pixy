package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;


/**
 * Return from called function. "2nd half" of a function call node.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeCallRet
    extends CfgNode {

// CONSTRUCTORS ********************************************************************

    public CfgNodeCallRet(ParseNode parseNode) {
        super(parseNode);
    }

//  GET ****************************************************************************

    public Variable getRetVar() {
        return this.getCallNode().getRetVar();
    }

    public Variable getTempVar() {
        return this.getCallNode().getTempVar();
    }

    public CfgNodeCallPrep getCallPrepNode() {
        return (CfgNodeCallPrep) this.getPredecessor().getPredecessor();
    }

    public CfgNodeCall getCallNode() {
        return (CfgNodeCall) this.getPredecessor();
    }

    List<TacActualParam> getParamsList() {
        return this.getCallPrepNode().getParamList();
    }

    // not relevant for globals replacement
    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }

    public void setRetVar(Variable retVar) {
        this.getCallNode().setRetVar(retVar);
    }
}