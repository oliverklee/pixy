package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.transferfunction.Add;
import at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural.IntraAnalysis;
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
public class InclusionDominatorAnalysis extends IntraAnalysis {
    private GenericRepository<LatticeElement> repos;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  InclusionDominatorAnalysis *****************************************************************

    public InclusionDominatorAnalysis(TacFunction function) {
        this.repos = new GenericRepository<>();
        this.initGeneral(function);
    }

//  initLattice ********************************************************************

    protected void initLattice() {
        this.lattice = new InclusionDominatorLattice(this);
        this.startValue = this.recycle(new InclusionDominatorLatticeElement());
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
        return new Add(cfgNodeX, this);
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
        InclusionDominatorLatticeElement latElem = (InclusionDominatorLatticeElement) this.getAnalysisNode(cfgNode).getInValue();
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
        InclusionDominatorAnalysis inclusionDominatorAnalysis = new InclusionDominatorAnalysis(function);
        inclusionDominatorAnalysis.analyze();
        return inclusionDominatorAnalysis.getIncludeChain(cfgNode);
    }

//  recycle ************************************************************************

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }
}