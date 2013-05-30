package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class BasicBlock extends AbstractCfgNode {
    List<AbstractCfgNode> containedNodes;

    // associate parse node of the basic block's initial node
    // with this basic block node (important for line number)
    public BasicBlock(AbstractCfgNode initialNode) {
        super(initialNode.getParseNode());
        this.containedNodes = new LinkedList<>();
        this.containedNodes.add(initialNode);
    }

    // informs enclosed nodes that they are inside a basic block
    public void informEnclosedNodes() {
        for (AbstractCfgNode cfgNode : this.containedNodes) {
            cfgNode.setEnclosingBasicBlock(this);
        }
    }

    public void addNode(AbstractCfgNode cfgNode) {
        this.containedNodes.add(cfgNode);
    }

    public List<AbstractCfgNode> getContainedNodes() {
        return this.containedNodes;
    }

    public List<Variable> getVariables() {
        List<Variable> variables = new LinkedList<>();
        for (AbstractCfgNode node : this.containedNodes) {
            variables.addAll(node.getVariables());
        }
        return variables;
    }

    public void replaceVariable(int index, Variable replacement) {
        for (AbstractCfgNode node : this.containedNodes) {
            List<Variable> gotVariables = node.getVariables();
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
            return this.containedNodes.get(0).getFileName();
        } else {
            return super.getFileName();
        }
    }

    public int getOriginalLineNumber() {
        if (!this.containedNodes.isEmpty()) {
            return this.containedNodes.get(0).getOriginalLineNumber();
        } else {
            return super.getOriginalLineNumber();
        }
    }
}