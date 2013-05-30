package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a node in the control flow graph.
 *
 * A node generally can have several ingoing edges, but only one real outgoing edge.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractCfgNode {
    /** the parse node (from the parse tree) to which this node refers */
    protected final ParseNode parseNode;

    protected List<CfgEdge> inEdges = new ArrayList<>();
    // index 0: for false edge (or normal edge)
    // index 1: for true edge
    protected CfgEdge[] outEdges = new CfgEdge[2];

    // number of this cfg node in reverse post-order (speeds up the analysis
    // if used by the worklist); -1 if uninitialized
    private int reversePostOrder = -1;

    // this can be one of the following:
    // - the enclosing basic block, if there is one (BasicBlock)
    // - a function's CfgEntry, if this cfg node is member of one of this
    //   function's default param cfgs
    // - null, if neither of the above applies
    private AbstractCfgNode enclosingNode = null;

    // function that contains this cfgNode;
    // note: you can't just set this in the constructor, since it
    // might change during include file resolution
    private TacFunction enclosingFunction = null;

    AbstractCfgNode() {
        this(null);
    }

    AbstractCfgNode(ParseNode parseNode) {
        this.parseNode = parseNode;
        this.outEdges[0] = null;
        this.outEdges[1] = null;
    }

    // returns
    // - the enclosing basic block, if it is enclosed in one
    // - the entry node of the function default cfg, if it is inside such a cfg
    // - itself otherwise
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

    // can return null!
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
            return this.outEdges[index].getDestination();
        } else {
            return null;
        }
    }

    public List<AbstractCfgNode> getSuccessors() {
        List<AbstractCfgNode> successors = new LinkedList<>();
        if (this.outEdges[0] != null) {
            successors.add(this.outEdges[0].getDestination());
            if (this.outEdges[1] != null) {
                successors.add(this.outEdges[1].getDestination());
            }
        }

        return successors;
    }

    // returns the unique predecessor if there is one;
    // throws an exception otherwise
    public AbstractCfgNode getPredecessor() {
        List<AbstractCfgNode> predecessors = this.getPredecessors();
        if (predecessors.size() != 1) {
            throw new RuntimeException("SNH: " + predecessors.size());
        }
        return predecessors.get(0);
    }

    public List<AbstractCfgNode> getPredecessors() {
        List<AbstractCfgNode> predecessors = new LinkedList<>();
        for (CfgEdge inEdge : this.inEdges) {
            predecessors.add(inEdge.getSource());
        }
        return predecessors;
    }

    public int getOriginalLineNumber() {
        // in some cases, this method is currently not very useful because
        // it returns "-2" (i.e., the line number of the epsilon node), especially
        // for constructs such as $x = "hello $world";
        // PhpParser needs to be improved to overcome this problem
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

    // returns a list of Variables referenced by this node; an empty list
    // if there are none; can also contain null values (placeholders);
    // targeted at the replacement of $GLOBALS, so you
    // should take a look at the actual implementations before using it
    // for something else
    public abstract List<Variable> getVariables();

    public int getReversePostOrder() {
        return this.reversePostOrder;
    }

    // returns either null or the enclosing basic block
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

    // returns either null or the entry node of the corresponding function
    // (if this node belongs to the default cfg of a function's formal parameter)
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

    // replaces the variable with the given index in the list returned by getVariables
    // by the given replacement variable
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

    // removes the edge coming in from the given predecessor
    public void removeInEdge(AbstractCfgNode predecessor) {
        for (Iterator<CfgEdge> iterator = this.inEdges.iterator(); iterator.hasNext(); ) {
            CfgEdge inEdge = iterator.next();
            if (inEdge.getSource() == predecessor) {
                iterator.remove();
            }
        }
    }

    public void clearInEdges() {
        this.inEdges = new LinkedList<>();
    }

    public void clearOutEdges() {
        this.outEdges[0] = this.outEdges[1] = null;
    }

    public String toString() {
        return Dumper.makeCfgNodeName(this);
    }
}