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
public abstract class AbstractAnalysis {
    // INPUT ***********************************************************************

    // functions to be analyzed (function name -> TacFunction)
    protected List<TacFunction> functions;

    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an AbstractAnalysisNode)
    protected AbstractAnalysisInformation genericAnalysisInformation;

    // OTHER ***********************************************************************

    // carrier lattice
    protected AbstractLattice lattice;

    // initial value for the start node
    protected AbstractLatticeElement startValue;

    // initial value for all other nodes
    protected AbstractLatticeElement initialValue;

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
    protected AbstractTransferFunction createTf(AbstractCfgNode cfgNodeX, TacFunction traversedFunction, AbstractCfgNode enclosingNode) {

        // EFF: more efficient implementation (hashmap?)

        // CAUTION: check for basic block first!
        if (cfgNodeX instanceof BasicBlock) {

            BasicBlock cfgNode = (BasicBlock) cfgNodeX;
            return this.makeBasicBlockTf(cfgNode, traversedFunction);
        } else if (cfgNodeX instanceof AssignSimple) {

            return this.assignSimple(cfgNodeX, enclosingNode);
        } else if (cfgNodeX instanceof AssignUnary) {

            return this.assignUnary(cfgNodeX, enclosingNode);
        } else if (cfgNodeX instanceof AssignBinary) {

            return this.assignBinary(cfgNodeX, enclosingNode);
        } else if (cfgNodeX instanceof AssignReference) {

            return this.assignRef(cfgNodeX);
        } else if (cfgNodeX instanceof Unset) {

            return this.unset(cfgNodeX);
        } else if (cfgNodeX instanceof AssignArray) {

            return this.assignArray(cfgNodeX);
        } else if (cfgNodeX instanceof Isset) {

            return this.isset(cfgNodeX);
        } else if (cfgNodeX instanceof CallPreparation) {

            return this.callPrep(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CfgEntry) {

            return this.entry(traversedFunction);
        } else if (cfgNodeX instanceof CallReturn) {

            return this.callRet(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CallBuiltinFunction) {

            return this.callBuiltin(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof CallUnknownFunction) {

            return this.callUnknown(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof Global) {

            return this.global(cfgNodeX);
        } else if (cfgNodeX instanceof Define) {

            return this.define(cfgNodeX);
        } else if (cfgNodeX instanceof Tester) {

            return this.tester(cfgNodeX);
        } else if (cfgNodeX instanceof Echo) {

            return this.echo(cfgNodeX, traversedFunction);
        } else if (cfgNodeX instanceof Static) {

            return this.staticNode();
        } else if (cfgNodeX instanceof Include) {

            return this.include(cfgNodeX);
        } else if (cfgNodeX instanceof IncludeStart) {

            return this.includeStart(cfgNodeX);
        } else if (cfgNodeX instanceof IncludeEnd) {

            return this.includeEnd(cfgNodeX);
        } else {
            // ID transfer function for all remaining cfg node types
            return TransferFunctionId.INSTANCE;
        }
    }

//  traverseCfg ********************************************************************

    protected void traverseCfg(ControlFlowGraph controlFlowGraph, TacFunction traversedFunction) {

        for (AbstractCfgNode cfgNodeX : controlFlowGraph.dfPreOrder()) {

            AbstractTransferFunction tf = this.createTf(cfgNodeX, traversedFunction, cfgNodeX);
            if (tf == null) {
                System.out.println(cfgNodeX.getLoc());
                throw new RuntimeException("SNH");
            }
            this.genericAnalysisInformation.add(cfgNodeX, this.makeAnalysisNode(
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

    // returns the number of cfgnode -> AbstractAnalysisNode mappings from AbstractAnalysisInformation
    public int size() {
        return this.genericAnalysisInformation.size();
    }

// getStartValue *******************************************************************

    public AbstractLatticeElement getStartValue() {
        return this.startValue;
    }

//  getLattice *********************************************************************

    public AbstractLattice getLattice() {
        return this.lattice;
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

//  makeBasicBlockTf ***************************************************************

    // creates a transfer function for a whole basic block
    protected AbstractTransferFunction makeBasicBlockTf(BasicBlock basicBlock, TacFunction traversedFunction) {

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

    protected AbstractTransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction assignRef(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction unset(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction assignArray(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction entry(TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction callUnknown(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction global(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction isset(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction define(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction tester(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction echo(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction staticNode() {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction include(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction includeStart(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

    protected AbstractTransferFunction includeEnd(AbstractCfgNode cfgNodeX) {
        return TransferFunctionId.INSTANCE;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  makeAnalysisNode ***************************************************************

    protected abstract AbstractAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, AbstractTransferFunction tf);

//  recycle ************************************************************************

    public abstract AbstractLatticeElement recycle(AbstractLatticeElement recycleMe);
}