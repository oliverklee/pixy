package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class CfgNodeBasicBlock
extends CfgNode {

    List<CfgNode> containedNodes;

    // associate parse node of the basic block's initial node
    // with this basic block node (important for line number)
    public CfgNodeBasicBlock(CfgNode initialNode) {
        super(initialNode.getParseNode());
        this.containedNodes = new LinkedList<CfgNode>();
        this.containedNodes.add(initialNode);
    }

    // informs enclosed nodes that they are inside a basic block
    public void informEnclosedNodes() {
        for (CfgNode cfgNode : this.containedNodes) {
            cfgNode.setEnclosingBasicBlock(this);
        }
    }

    public void addNode(CfgNode cfgNode) {
        this.containedNodes.add(cfgNode);
    }

    public List<CfgNode> getContainedNodes() {
        return this.containedNodes;
    }

    public List<Variable> getVariables() {
        List<Variable> variables = new LinkedList<Variable>();
        for (Iterator iter = this.containedNodes.iterator(); iter.hasNext();) {
            CfgNode node = (CfgNode) iter.next();
            variables.addAll(node.getVariables());
        }
        return variables;
    }

    public void replaceVariable(int index, Variable replacement) {
        for (Iterator iter = this.containedNodes.iterator(); iter.hasNext();) {
            CfgNode node = (CfgNode) iter.next();
            List gotVariables = node.getVariables();
            if (gotVariables.size() > index) {
                node.replaceVariable(index, replacement);
                return;
            } else {
                index -= gotVariables.size();
            }
        }
        // if you reach this point, no variable was replaced
        throw new RuntimeException("SNH");
    }

    public String getFileName() {
        if (!this.containedNodes.isEmpty()) {
            return ((CfgNode) this.containedNodes.get(0)).getFileName();
        } else {
            return super.getFileName();
        }
    }

    public int getOrigLineno() {
        if (!this.containedNodes.isEmpty()) {
            return ((CfgNode) this.containedNodes.get(0)).getOrigLineno();
        } else {
            return super.getOrigLineno();
        }
    }
}