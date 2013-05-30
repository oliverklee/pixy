package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.LinkedList;

/**
 * Base class for intraprocedural analyses.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractIntraproceduralAnalysis extends AbstractAnalysis {
    // INPUT ***********************************************************************

    // <see superclass>

    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an IntraproceduralAnalysisNode)
    protected IntraproceduralAnalysisInformation analysisInfo;

    // OTHER ***********************************************************************

    // worklist consisting of pairs (ControlFlowGraph node, lattice element)
    IntraproceduralWorklist workList;

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
        this.workList = new IntraproceduralWorklist();
        this.workList.add(function.getControlFlowGraph().getHead());

        // initialize analysis nodes
        this.analysisInfo = new IntraproceduralAnalysisInformation();
        this.genericAnalysisInformation = analysisInfo;

        // assign transfer functions to analysis nodes
        this.traverseCfg(function.getControlFlowGraph(), function);
        // this.asfsafsaf: initTransferFunctions

        // initialize inValue for start node
        IntraproceduralAnalysisNode startAnalysisNode = this.analysisInfo.getAnalysisNode(function.getControlFlowGraph().getHead());
        startAnalysisNode.setInValue(this.startValue);
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

// getTransferFunction *************************************************************

    public AbstractTransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        return this.analysisInfo.getAnalysisNode(cfgNode).getTransferFunction();
    }

//  getAnalysisInfo *****************************************************************

    public IntraproceduralAnalysisInformation getAnalysisInfo() {
        return this.analysisInfo;
    }

//  getAnalysisNode ****************************************************************

    public IntraproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return this.analysisInfo.getAnalysisNode(cfgNode);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

//  makeAnalysisNode ***************************************************************

    // creates and returns an analysis node for the given parameters
    protected AbstractAnalysisNode makeAnalysisNode(AbstractCfgNode node, AbstractTransferFunction tf) {
        return new IntraproceduralAnalysisNode(tf);
    }

//  recycle ************************************************************************

    public abstract AbstractLatticeElement recycle(AbstractLatticeElement recycleMe);

// analyze *************************************************************************

    // this method applies the worklist algorithm
    public void analyze() {

        // for each element in the worklist...
        while (this.workList.hasNext()) {

            // remove the element from the worklist
            AbstractCfgNode node = this.workList.removeNext();

            // get incoming value at node n
            IntraproceduralAnalysisNode analysisNode = this.analysisInfo.getAnalysisNode(node);
            AbstractLatticeElement inValue = analysisNode.getInValue();
            if (inValue == null) {
                throw new RuntimeException("SNH");
            }

            try {

                // apply transfer function to incoming value
                AbstractLatticeElement outValue;
                outValue = this.analysisInfo.getAnalysisNode(node).transfer(inValue);

                // for each outgoing edge...
                CfgEdge[] outEdges = node.getOutEdges();
                for (CfgEdge outEdge : outEdges) {
                    if (outEdge != null) {

                        // determine the successor
                        AbstractCfgNode succ = outEdge.getDestination();

                        // propagate the result of applying the transfer function
                        // to the successor
                        propagate(outValue, succ);
                    }
                }
            } catch (RuntimeException ex) {
                System.out.println("File:" + node.getFileName() + ", Line: " + node.getOriginalLineNumber());
                throw ex;
            }
        }

        // worklist algorithm finished!
    }

//  propagate ***********************************************************************

    // helper method for analyze();
    // propagates a value to the target node
    void propagate(AbstractLatticeElement value, AbstractCfgNode target) {

        // analysis information for the target node
        IntraproceduralAnalysisNode analysisNode = this.analysisInfo.getAnalysisNode(target);

        if (analysisNode == null) {
            System.out.println(Dumper.makeCfgNodeName(target));
            throw new RuntimeException("SNH: " + target.getClass());
        }

        // determine the target's old invalue
        AbstractLatticeElement oldInValue = analysisNode.getInValue();
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
        AbstractLatticeElement newInValue = this.lattice.lub(value, oldInValue);

        // if the invalue changed...
        if (!oldInValue.equals(newInValue)) {

            // update analysis information
            analysisNode.setInValue(newInValue);

            // add this node to the worklist
            this.workList.add(target);
        }
    }
}