package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;

import java.util.List;

/**
 * Vase class for inter- and intraprocedural analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class Analysis {
    // INPUT ***********************************************************************

    // functions to be analyzed (function name -> TacFunction)
    protected List<TacFunction> functions;

    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an AnalysisNode)
    protected AnalysisInfo genericAnalysisInfo;

    // OTHER ***********************************************************************

    // carrier lattice
    protected Lattice lattice;

    // initial value for the start node
    protected LatticeElement startValue;

    // initial value for all other nodes
    protected LatticeElement initialValue;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

// initLattice *********************************************************************

    // initializes the carrier lattice, the start value, and the initial value
    protected abstract void initLattice();

//  createTf ***********************************************************************

    // creates a transfer function for the given node;
    // the enclosingNode is either an enclosing basic block (if you already know
    // that it is enclosed by a basic block) or the node itself
    protected TransferFunction createTf(AbstractCfgNode cfgNodeX, TacFunction traversedFunction, AbstractCfgNode enclosingNode) {

        // EFF: more efficient implementation (hashmap?)

        // CAUTION: check for basic block first!
        if (cfgNodeX instanceof CfgNodeBasicBlock) {

            CfgNodeBasicBlock cfgNode = (CfgNodeBasicBlock) cfgNodeX;
            return this.makeBasicBlockTf(cfgNode, traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeAssignSimple) {

            return this.assignSimple(cfgNodeX, enclosingNode);
        } else if (cfgNodeX instanceof CfgNodeAssignUnary) {

            return this.assignUnary(cfgNodeX, enclosingNode);
        } else if (cfgNodeX instanceof CfgNodeAssignBinary) {

            return this.assignBinary(cfgNodeX, enclosingNode);
        } else if (cfgNodeX instanceof CfgNodeAssignRef) {

            return this.assignRef(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeUnset) {

            return this.unset(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeAssignArray) {

            return this.assignArray(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeIsset) {

            return this.isset(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeCallPrep) {

            return this.callPrep(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeEntry) {

            return this.entry(traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeCallRet) {

            return this.callRet(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeCallBuiltin) {

            return this.callBuiltin(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeCallUnknown) {

            return this.callUnknown(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeGlobal) {

            return this.global(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeDefine) {

            return this.define(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeTester) {

            return this.tester(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeEcho) {

            return this.echo(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CfgNodeStatic) {

            return this.staticNode();
        } else if (cfgNodeX instanceof CfgNodeInclude) {

            return this.include(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeIncludeStart) {

            return this.includeStart(cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeIncludeEnd) {

            return this.includeEnd(cfgNodeX);
        } else {
            // ID transfer function for all remaining cfg node types
            return TransferFunctionId.INSTANCE;
        }
    }

//  traverseCfg ********************************************************************

    protected void traverseCfg(ControlFlowGraph controlFlowGraph, TacFunction traversedFunction) {

        for (AbstractCfgNode cfgNodeX : controlFlowGraph.dfPreOrder()) {

            TransferFunction tf = this.createTf(cfgNodeX, traversedFunction, cfgNodeX);
            if (tf == null) {
                System.out.println(cfgNodeX.getLoc());
                throw new RuntimeException("SNH");
            }
            this.genericAnalysisInfo.add(cfgNodeX, this.makeAnalysisNode(
                cfgNodeX, tf));
        }
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

//  getFunctions *******************************************************************

    public List<TacFunction> getFunctions() {
        return this.functions;
    }

//  size ***************************************************************************

    // returns the number of cfgnode -> AnalysisNode mappings from AnalysisInfo
    public int size() {
        return this.genericAnalysisInfo.size();
    }

// getStartValue *******************************************************************

    public LatticeElement getStartValue() {
        return this.startValue;
    }

//  getLattice *********************************************************************

    public Lattice getLattice() {
        return this.lattice;
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

//  makeBasicBlockTf ***************************************************************

    // creates a transfer function for a whole basic block
    protected TransferFunction makeBasicBlockTf(CfgNodeBasicBlock basicBlock, TacFunction traversedFunction) {

        CompositeTransferFunction ctf = new CompositeTransferFunction();

        for (AbstractCfgNode cfgNodeX : basicBlock.getContainedNodes()) {
            ctf.add(this.createTf(cfgNodeX, traversedFunction, basicBlock));
        }
        return ctf;
    }

    // return a transfer function for a given cfg node;
    // traversedFunction: function that this node is contained int
    // aliasInNode:
    // - if cfgNodeX is not inside a basic block: the same node
    // - else: the basic block

    // these are only default implementations that ease the creation of new
    // analyses; be sure to think about the necessary transfer functions for
    // your concrete analysis

    protected TransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction assignRef(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction unset(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction assignArray(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction entry(TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction callUnknown(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction global(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction isset(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction define(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction tester(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction echo(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction staticNode() {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction include(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction includeStart(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction includeEnd(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  makeAnalysisNode ***************************************************************

    protected abstract AnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, TransferFunction tf);

//  recycle ************************************************************************

    public abstract LatticeElement recycle(LatticeElement recycleMe);
}