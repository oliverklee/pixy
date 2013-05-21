package at.ac.tuwien.infosys.www.pixy.analysis.incdom;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.incdom.tf.IncDomTfAdd;
import at.ac.tuwien.infosys.www.pixy.analysis.intra.IntraAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeStart;

import java.util.LinkedList;
import java.util.List;

/**
 * Inclusion dominator analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IncDomAnalysis extends IntraAnalysis {
    private GenericRepos<LatticeElement> repos;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  IncDomAnalysis *****************************************************************

    public IncDomAnalysis(TacFunction function) {
        this.repos = new GenericRepos<>();
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

    protected TransferFunction makeBasicBlockTf(BasicBlock basicBlock, TacFunction traversedFunction) {
        // we can override the general method from Analysis with this, because
        // analysis information must not change inside basic blocks
        // (all nodes inside a basic block should have an ID transfer function,
        // so we can use this shortcut)
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction includeStart(AbstractCfgNode cfgNodeX) {
        return new IncDomTfAdd(cfgNodeX, this);
    }

    protected TransferFunction includeEnd(AbstractCfgNode cfgNodeX) {
        return includeStart(cfgNodeX);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // computes and returns the include chain list (consisting of cfg nodes)
    // for the given cfg node
    public LinkedList<AbstractCfgNode> getIncludeChain(AbstractCfgNode cfgNode) {
        IncDomLatticeElement latElem = (IncDomLatticeElement) this.getAnalysisNode(cfgNode).getInValue();
        List<AbstractCfgNode> dominators = latElem.getDominators();

        // * input: a list of (dominating) cfg nodes, both includeEnd and
        //   includeStart
        // * output: a chain of unclosed includeStart nodes
        LinkedList<AbstractCfgNode> chain = new LinkedList<>();
        for (AbstractCfgNode dominator : dominators) {
            if (dominator instanceof IncludeStart) {
                chain.add(dominator);
            } else if (dominator instanceof IncludeEnd) {
                IncludeEnd incEnd = (IncludeEnd) dominator;
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
    public static LinkedList<AbstractCfgNode> computeChain(TacFunction function, AbstractCfgNode cfgNode) {
        IncDomAnalysis incDomAnalysis = new IncDomAnalysis(function);
        incDomAnalysis.analyze();
        return incDomAnalysis.getIncludeChain(cfgNode);
    }

//  recycle ************************************************************************

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }
}