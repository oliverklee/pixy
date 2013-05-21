package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.analysis.Analysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.LinkedList;

/**
 * Base class for intraprocedural analyses.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class IntraAnalysis extends Analysis {
    // INPUT ***********************************************************************

    // <see superclass>

    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an IntraAnalysisNode)
    protected IntraAnalysisInfo analysisInfo;

    // OTHER ***********************************************************************

    // worklist consisting of pairs (ControlFlowGraph node, lattice element)
    IntraWorkList workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

// initGeneral *********************************************************************

    // general initialization work; taken out of the constructor to bypass the
    // restriction that superclass constructors have to be called first;
    protected void initGeneral(TacFunction function) {

        // first-time initialization
        if (this.functions == null) {
            this.functions = new LinkedList<>();
            // initialize carrier lattice
            this.initLattice();
        }

        this.functions.add(function);

        // initialize worklist
        this.workList = new IntraWorkList();
        this.workList.add(function.getControlFlowGraph().getHead());

        // initialize analysis nodes
        this.analysisInfo = new IntraAnalysisInfo();
        this.genericAnalysisInfo = analysisInfo;

        // assign transfer functions to analysis nodes
        this.traverseCfg(function.getControlFlowGraph(), function);
        // this.asfsafsaf: initTransferFunctions

        // initialize inValue for start node
        IntraAnalysisNode startAnalysisNode = this.analysisInfo.getAnalysisNode(function.getControlFlowGraph().getHead());
        startAnalysisNode.setInValue(this.startValue);
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

// getTransferFunction *************************************************************

    public TransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        return this.analysisInfo.getAnalysisNode(cfgNode).getTransferFunction();
    }

//  getAnalysisInfo *****************************************************************

    public IntraAnalysisInfo getAnalysisInfo() {
        return this.analysisInfo;
    }

//  getAnalysisNode ****************************************************************

    public IntraAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return this.analysisInfo.getAnalysisNode(cfgNode);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

//  makeAnalysisNode ***************************************************************

    // creates and returns an analysis node for the given parameters
    protected AnalysisNode makeAnalysisNode(AbstractCfgNode node, TransferFunction tf) {
        return new IntraAnalysisNode(tf);
    }

//  recycle ************************************************************************

    public abstract LatticeElement recycle(LatticeElement recycleMe);

// analyze *************************************************************************

    // this method applies the worklist algorithm
    public void analyze() {

        // for each element in the worklist...
        while (this.workList.hasNext()) {

            // remove the element from the worklist
            AbstractCfgNode node = this.workList.removeNext();

            // get incoming value at node n
            IntraAnalysisNode analysisNode = this.analysisInfo.getAnalysisNode(node);
            LatticeElement inValue = analysisNode.getInValue();
            if (inValue == null) {
                throw new RuntimeException("SNH");
            }

            try {

                // apply transfer function to incoming value
                LatticeElement outValue;
                outValue = this.analysisInfo.getAnalysisNode(node).transfer(inValue);

                // for each outgoing edge...
                CfgEdge[] outEdges = node.getOutEdges();
                for (CfgEdge outEdge : outEdges) {
                    if (outEdge != null) {

                        // determine the successor
                        AbstractCfgNode succ = outEdge.getDest();

                        // propagate the result of applying the transfer function
                        // to the successor
                        propagate(outValue, succ);
                    }
                }
            } catch (RuntimeException ex) {
                System.out.println("File:" + node.getFileName() + ", Line: " + node.getOrigLineno());
                throw ex;
            }
        }

        // worklist algorithm finished!
    }

//  propagate ***********************************************************************

    // helper method for analyze();
    // propagates a value to the target node
    void propagate(LatticeElement value, AbstractCfgNode target) {

        // analysis information for the target node
        IntraAnalysisNode analysisNode = this.analysisInfo.getAnalysisNode(target);

        if (analysisNode == null) {
            System.out.println(Dumper.makeCfgNodeName(target));
            throw new RuntimeException("SNH: " + target.getClass());
        }

        // determine the target's old invalue
        LatticeElement oldInValue = analysisNode.getInValue();
        if (oldInValue == null) {
            // initial value of this analysis
            oldInValue = this.initialValue;
        }

        // speedup: if incoming value and target value are exactly the same
        // object, then the result certainly can't change
        if (value == oldInValue) {
            return;
        }

        // the new invalue is computed as usual (with lub)
        LatticeElement newInValue = this.lattice.lub(value, oldInValue);

        // if the invalue changed...
        if (!oldInValue.equals(newInValue)) {

            // update analysis information
            analysisNode.setInValue(newInValue);

            // add this node to the worklist
            this.workList.add(target);
        }
    }
}