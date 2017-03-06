package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class BasicBlock extends AbstractCfgNode {

	List<AbstractCfgNode> containedNodes;

	public BasicBlock(AbstractCfgNode initialNode) {
		super(initialNode.getParseNode());
		this.containedNodes = new LinkedList<AbstractCfgNode>();
		this.containedNodes.add(initialNode);
	}

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
		List<Variable> variables = new LinkedList<Variable>();
		for (Iterator<AbstractCfgNode> iter = this.containedNodes.iterator(); iter.hasNext();) {
			AbstractCfgNode node = (AbstractCfgNode) iter.next();
			variables.addAll(node.getVariables());
		}
		return variables;
	}

	public void replaceVariable(int index, Variable replacement) {
		for (Iterator<AbstractCfgNode> iter = this.containedNodes.iterator(); iter.hasNext();) {
			AbstractCfgNode node = (AbstractCfgNode) iter.next();
			List<Variable> gotVariables = node.getVariables();
			if (gotVariables.size() > index) {
				node.replaceVariable(index, replacement);
				return;
			} else {
				index -= gotVariables.size();
			}
		}
		throw new RuntimeException("SNH");
	}

	public String getFileName() {
		if (!this.containedNodes.isEmpty()) {
			return ((AbstractCfgNode) this.containedNodes.get(0)).getFileName();
		} else {
			return super.getFileName();
		}
	}

	public int getOrigLineno() {
		if (!this.containedNodes.isEmpty()) {
			return ((AbstractCfgNode) this.containedNodes.get(0)).getOriginalLineNumber();
		} else {
			return super.getOriginalLineNumber();
		}
	}
}
