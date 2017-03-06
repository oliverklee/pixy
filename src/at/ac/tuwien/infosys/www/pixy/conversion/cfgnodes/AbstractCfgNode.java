package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public abstract class AbstractCfgNode {

	protected final ParseNode parseNode;

	protected List<CfgEdge> inEdges;
	protected CfgEdge[] outEdges;

	private int reversePostOrder;

	private AbstractCfgNode enclosingNode;

	private TacFunction enclosingFunction = null;

	AbstractCfgNode() {
		this(null);
	}

	AbstractCfgNode(ParseNode parseNode) {
		this.parseNode = parseNode;
		this.inEdges = new ArrayList<CfgEdge>();
		this.outEdges = new CfgEdge[2];
		this.outEdges[0] = this.outEdges[1] = null;
		this.reversePostOrder = -1;
		this.enclosingNode = null;
	}

	public AbstractCfgNode getSpecial() {
		AbstractCfgNode retMe;

		retMe = this.getEnclosingBasicBlock();
		if (retMe != null) {
			return retMe;
		}

		retMe = this.getDefaultParamEntry();
		if (retMe != null) {
			return retMe;
		}

		return this;
	}

	public ParseNode getParseNode() {
		return this.parseNode;
	}

	public List<CfgEdge> getInEdges() {
		return this.inEdges;
	}

	public CfgEdge[] getOutEdges() {
		return this.outEdges;
	}

	public CfgEdge getOutEdge(int index) {
		return this.outEdges[index];
	}

	public AbstractCfgNode getSuccessor(int index) {
		if (this.outEdges[index] != null) {
			return this.outEdges[index].getDest();
		} else {
			return null;
		}
	}

	public List<AbstractCfgNode> getSuccessors() {
		List<AbstractCfgNode> successors = new LinkedList<AbstractCfgNode>();
		if (this.outEdges[0] != null) {
			successors.add(this.outEdges[0].getDest());
			if (this.outEdges[1] != null) {
				successors.add(this.outEdges[1].getDest());
			}
		}
		return successors;
	}

	public AbstractCfgNode getPredecessor() {
		List<AbstractCfgNode> predecessors = this.getPredecessors();
		if (predecessors.size() != 1) {
			throw new RuntimeException("SNH: " + predecessors.size());
		}
		return predecessors.get(0);
	}

	public List<AbstractCfgNode> getPredecessors() {
		List<AbstractCfgNode> predecessors = new LinkedList<AbstractCfgNode>();
		for (Iterator<CfgEdge> iter = this.inEdges.iterator(); iter.hasNext();) {
			CfgEdge inEdge = (CfgEdge) iter.next();
			predecessors.add(inEdge.getSource());
		}
		return predecessors;
	}

	public int getOriginalLineNumber() {
		if (this.parseNode != null) {
			return this.parseNode.getLinenoLeft();
		} else {
			return -1;
		}
	}

	public String getFileName() {
		if (this.parseNode != null) {
			return this.parseNode.getFileName();
		} else {
			return "<file name unknown>";
		}
	}

	public String getLoc() {
		if (!MyOptions.optionB && !MyOptions.optionW) {
			return this.getFileName() + ":" + this.getOriginalLineNumber();
		} else {
			return Utils.basename(this.getFileName()) + ":" + this.getOriginalLineNumber();
		}
	}

	public TacFunction getEnclosingFunction() {
		if (this.enclosingFunction == null) {
			System.out.println(this.getFileName());
			System.out.println(this.toString() + ", " + this.getOriginalLineNumber());
			throw new RuntimeException("SNH");
		}
		return this.enclosingFunction;
	}

	public abstract List<Variable> getVariables();

	public int getReversePostOrder() {
		return this.reversePostOrder;
	}

	public BasicBlock getEnclosingBasicBlock() {
		if (this.enclosingNode == null) {
			return null;
		}
		if (this.enclosingNode instanceof BasicBlock) {
			return (BasicBlock) this.enclosingNode;
		} else {
			return null;
		}
	}

	public CfgEntry getDefaultParamEntry() {
		if (this.enclosingNode == null) {
			return null;
		}
		if (this.enclosingNode instanceof CfgEntry) {
			return (CfgEntry) this.enclosingNode;
		} else {
			return null;
		}
	}

	public abstract void replaceVariable(int index, Variable replacement);

	public void setOutEdge(int index, CfgEdge edge) {
		this.outEdges[index] = edge;
	}

	public void setReversePostOrder(int i) {
		if (i == Integer.MAX_VALUE) {
			throw new RuntimeException("Integer Overflow");
		}
		this.reversePostOrder = i;
	}

	public void setEnclosingBasicBlock(BasicBlock basicBlock) {
		this.enclosingNode = basicBlock;
	}

	public void setDefaultParamPrep(CfgEntry callPrep) {
		this.enclosingNode = callPrep;
	}

	public void setEnclosingFunction(TacFunction function) {
		this.enclosingFunction = function;
	}

	public void addInEdge(CfgEdge edge) {
		this.inEdges.add(edge);
	}

	public void removeInEdge(AbstractCfgNode predecessor) {
		for (Iterator<CfgEdge> iter = this.inEdges.iterator(); iter.hasNext();) {
			CfgEdge inEdge = (CfgEdge) iter.next();
			if (inEdge.getSource() == predecessor) {
				iter.remove();
			}
		}
	}

	public void clearInEdges() {
		this.inEdges = new LinkedList<CfgEdge>();
	}

	public void clearOutEdges() {
		this.outEdges[0] = this.outEdges[1] = null;
	}

	public String toString() {
		return Dumper.makeCfgNodeName(this);
	}

}
