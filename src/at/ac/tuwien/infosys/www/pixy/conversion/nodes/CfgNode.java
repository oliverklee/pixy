package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public abstract class CfgNode {

    protected final ParseNode parseNode;

    protected List<CfgEdge> inEdges;
    // index 0: for false edge (or normal edge)
    // index 1: for true edge
    protected CfgEdge[] outEdges;

    // number of this cfg node in reverse postorder (speeds up the analysis
    // if used by the worklist); -1 if uninitialized
    private int reversePostOrder;

    // this can be one of the following:
    // - the enclosing basic block, if there is one (CfgNodeBasicBlock)
    // - a function's CfgNodeEntry, if this cfg node is member of one of this
    //   function's default param cfgs
    // - null, if neither of the above applies
    private CfgNode enclosingNode;

    // function that contains this cfgNode;
    // note: you can't just set this in the constructor, since it
    // might change during include file resolution
    private TacFunction enclosingFunction = null;

// CONSTRUCTORS ********************************************************************

    CfgNode() {
        this(null);
    }

    CfgNode(ParseNode parseNode) {
        this.parseNode = parseNode;
        this.inEdges = new ArrayList<CfgEdge>();
        this.outEdges = new CfgEdge[2];
        this.outEdges[0] = this.outEdges[1] = null;
        this.reversePostOrder = -1;
        this.enclosingNode = null;
    }

// GET *****************************************************************************

    // returns
    // - the enclosing basic block, if it is enclosed in one
    // - the entry node of the function default cfg, if it is inside such a cfg
    // - itself otherwise
    public CfgNode getSpecial() {
        CfgNode retMe;

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

    public CfgNode getSuccessor(int index) {
        if (this.outEdges[index] != null) {
            return this.outEdges[index].getDest();
        } else {
            return null;
        }
    }

    public List<CfgNode> getSuccessors() {
        List<CfgNode> successors = new LinkedList<CfgNode>();
        if (this.outEdges[0] != null) {
            successors.add(this.outEdges[0].getDest());
            if (this.outEdges[1] != null) {
                successors.add(this.outEdges[1].getDest());
            }
        }
        return successors;
    }

    // returns the unique predecessor if there is one;
    // throws an exception otherwise
    public CfgNode getPredecessor() {
        List<CfgNode> predecessors = this.getPredecessors();
        if (predecessors.size() != 1) {
            throw new RuntimeException("SNH: " + predecessors.size());
        }
        return predecessors.get(0);
    }

    public List<CfgNode> getPredecessors() {
        List<CfgNode> predecessors = new LinkedList<CfgNode>();
        for (Iterator iter = this.inEdges.iterator(); iter.hasNext(); ) {
            CfgEdge inEdge = (CfgEdge) iter.next();
            predecessors.add(inEdge.getSource());
        }
        return predecessors;
    }

    public int getOrigLineno() {
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
            return this.getFileName() + ":" + this.getOrigLineno();
        } else {
            return Utils.basename(this.getFileName()) + ":" + this.getOrigLineno();
        }
    }

    public TacFunction getEnclosingFunction() {
        if (this.enclosingFunction == null) {
            System.out.println(this.getFileName());
            System.out.println(this.toString() + ", " + this.getOrigLineno());
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
    public CfgNodeBasicBlock getEnclosingBasicBlock() {
        if (this.enclosingNode == null) {
            return null;
        }
        if (this.enclosingNode instanceof CfgNodeBasicBlock) {
            return (CfgNodeBasicBlock) this.enclosingNode;
        } else {
            return null;
        }
    }

    // returns either null or the entry node of the corresponding function
    // (if this node belongs to the default cfg of a function's formal parameter)
    public CfgNodeEntry getDefaultParamEntry() {
        if (this.enclosingNode == null) {
            return null;
        }
        if (this.enclosingNode instanceof CfgNodeEntry) {
            return (CfgNodeEntry) this.enclosingNode;
        } else {
            return null;
        }
    }

// SET *****************************************************************************

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

    public void setEnclosingBasicBlock(CfgNodeBasicBlock basicBlock) {
        this.enclosingNode = basicBlock;
    }

    public void setDefaultParamPrep(CfgNodeEntry callPrep) {
        this.enclosingNode = callPrep;
    }

    public void setEnclosingFunction(TacFunction function) {
        this.enclosingFunction = function;
    }

// OTHER ***************************************************************************

    public void addInEdge(CfgEdge edge) {
        this.inEdges.add(edge);
    }

    // removes the edge coming in from the given predecessor
    public void removeInEdge(CfgNode predecessor) {
        for (Iterator iter = this.inEdges.iterator(); iter.hasNext(); ) {
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