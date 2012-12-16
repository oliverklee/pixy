package at.ac.tuwien.infosys.www.pixy.analysis.incdom;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.*;
import at.ac.tuwien.infosys.www.pixy.analysis.incdom.tf.IncDomTfAdd;
import at.ac.tuwien.infosys.www.pixy.analysis.intra.IntraAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

// inclusion dominator analysis
public class IncDomAnalysis 
extends IntraAnalysis {

    private GenericRepos<LatticeElement> repos;
    
//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************
    
//  IncDomAnalysis *****************************************************************
    
    public IncDomAnalysis (TacFunction function) {
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
        List dominators = latElem.getDominators();
        
        // * input: a list of (dominating) cfg nodes, both includeEnd and 
        //   includeStart
        // * output: a chain of unclosed includeStart nodes
        LinkedList<CfgNode> chain = new LinkedList<CfgNode>();
        for (Iterator iterator = dominators.iterator(); iterator.hasNext();) {
            CfgNode dom = (CfgNode) iterator.next();
            if (dom instanceof CfgNodeIncludeStart) {
                chain.add(dom);
            } else if (dom instanceof CfgNodeIncludeEnd) {
                CfgNodeIncludeEnd incEnd = (CfgNodeIncludeEnd) dom;
                if (incEnd.isPeer((CfgNode) chain.getLast())) {
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
