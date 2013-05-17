package at.ac.tuwien.infosys.www.pixy.analysis.incdom;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.incdom.tf.IncDomTfAdd;
import at.ac.tuwien.infosys.www.pixy.analysis.intra.IntraAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeBasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeIncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeIncludeStart;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Inclusion dominator analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IncDomAnalysis
    extends IntraAnalysis {

    private GenericRepos<LatticeElement> repos;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  IncDomAnalysis *****************************************************************

    public IncDomAnalysis(TacFunction function) {
        this.repos = new GenericRepos<LatticeElement>();
        this.initGeneral(function);
    }

//  initLattice ********************************************************************

    protected void initLattice() {
        this.lattice = new IncDomLattice(this);
        this.startValue = this.recycle(new IncDomLatticeElement());
        this.initialValue = this.lattice.getBottom();
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

//  makeBasicBlockTf ***************************************************************

    protected TransferFunction makeBasicBlockTf(CfgNodeBasicBlock basicBlock, TacFunction traversedFunction) {
        // we can override the general method from Analysis with this, because
        // analysis information must not change inside basic blocks
        // (all nodes inside a basic block should have an ID transfer function,
        // so we can use this shortcut)
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction includeStart(CfgNode cfgNodeX) {
        return new IncDomTfAdd(cfgNodeX, this);
    }

    protected TransferFunction includeEnd(CfgNode cfgNodeX) {
        return includeStart(cfgNodeX);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // computes and returns the include chain list (consisting of cfg nodes)
    // for the given cfg node
    public List getIncludeChain(CfgNode cfgNode) {

        IncDomLatticeElement latElem =
            (IncDomLatticeElement) this.getAnalysisNode(cfgNode).getInValue();
        List<CfgNode> dominators = latElem.getDominators();

        // * input: a list of (dominating) cfg nodes, both includeEnd and
        //   includeStart
        // * output: a chain of unclosed includeStart nodes
        LinkedList<CfgNode> chain = new LinkedList<CfgNode>();
        for (CfgNode dominator : dominators) {
            if (dominator instanceof CfgNodeIncludeStart) {
                chain.add(dominator);
            } else if (dominator instanceof CfgNodeIncludeEnd) {
                CfgNodeIncludeEnd incEnd = (CfgNodeIncludeEnd) dominator;
                if (incEnd.isPeer(chain.getLast())) {
                    chain.removeLast();
                } else {
                    throw new RuntimeException("SNH");
                }
            } else {
                throw new RuntimeException("SNH");
            }
        }
        return chain;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  computeChain *******************************************************************

    // shortcut function
    public static List computeChain(TacFunction function, CfgNode cfgNode) {
        IncDomAnalysis incDomAnalysis = new IncDomAnalysis(function);
        incDomAnalysis.analyze();
        return incDomAnalysis.getIncludeChain(cfgNode);
    }

//  recycle ************************************************************************

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }
}