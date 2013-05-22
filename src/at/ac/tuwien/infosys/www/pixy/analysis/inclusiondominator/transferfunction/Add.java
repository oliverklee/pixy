package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * Transfer function for adding include dominators.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Add extends AbstractTransferFunction {
    private AbstractCfgNode cfgNode;
    private InclusionDominatorAnalysis inclusionDominatorAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Add(AbstractCfgNode cfgNode, InclusionDominatorAnalysis inclusionDominatorAnalysis) {
        this.cfgNode = cfgNode;
        this.inclusionDominatorAnalysis = inclusionDominatorAnalysis;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        InclusionDominatorLatticeElement in = (InclusionDominatorLatticeElement) inX;
        InclusionDominatorLatticeElement out = new InclusionDominatorLatticeElement(in);
        out.add(this.cfgNode);

        // recycle
        out = (InclusionDominatorLatticeElement) this.inclusionDominatorAnalysis.recycle(out);

        return out;
    }
}