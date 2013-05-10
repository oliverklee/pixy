package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.analysis.Analysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.LinkedList;

// base class for intraprocedural analyses
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class IntraAnalysis
    extends Analysis {

    // INPUT ***********************************************************************

    // <see superclass>

    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an IntraAnalysisNode)
    protected IntraAnalysisInfo analysisInfo;

    // OTHER ***********************************************************************

    // worklist consisting of pairs (Cfg node, lattice element)
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
            this.functions = new LinkedList<TacFunction>();
            // initialize carrier lattice
            this.initLattice();
        }

        this.functions.add(function);

        // initialize worklist
        this.workList = new IntraWorkList();
        this.workList.add(function.getCfg().getHead());

        // initialize analysis nodes
        this.analysisInfo = new IntraAnalysisInfo();
        this.genericAnalysisInfo = analysisInfo;

        // assign transfer functions to analysis nodes
        this.traverseCfg(function.getCfg(), function);
        // this.asfsafsaf: initTransferFunctions

        // initialize inValue for start node
        IntraAnalysisNode startAnalysisNode =
            (IntraAnalysisNode) this.analysisInfo.getAnalysisNode(function.getCfg().getHead());
        startAnalysisNode.setInValue(this.startValue);
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

// getTransferFunction *************************************************************

    public TransferFunction getTransferFunction(CfgNode cfgNode) {
        return this.analysisInfo.getAnalysisNode(cfgNode).getTransferFunction();
    }

//  getAnalysisInfo *****************************************************************

    public IntraAnalysisInfo getAnalysisInfo() {
        return this.analysisInfo;
    }

//  getAnalysisNode ****************************************************************

    public IntraAnalysisNode getAnalysisNode(CfgNode cfgNode) {
        return (IntraAnalysisNode) this.analysisInfo.getAnalysisNode(cfgNode);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

//  makeAnalysisNode ***************************************************************

    // creates and returns an analysis node for the given parameters
    protected AnalysisNode makeAnalysisNode(CfgNode node, TransferFunction tf) {
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
            CfgNode node = this.workList.removeNext();

            // get incoming value at node n
            IntraAnalysisNode analysisNode = (IntraAnalysisNode) this.analysisInfo.getAnalysisNode(node);
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
                for (int i = 0; i < outEdges.length; i++) {
                    if (outEdges[i] != null) {

                        // determine the successor
                        CfgNode succ = outEdges[i].getDest();

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
    void propagate(LatticeElement value, CfgNode target) {

        // analysis information for the target node
        IntraAnalysisNode analysisNode = (IntraAnalysisNode) this.analysisInfo.getAnalysisNode(target);

        if (analysisNode == null) {
            System.out.println(Dumper.makeCfgNodeName(target));
            throw new RuntimeException("SNH: " + target.getClass());
        }

        if (analysisNode == null) {
            System.out.println(target.getOrigLineno());
            throw new RuntimeException("SNH");
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