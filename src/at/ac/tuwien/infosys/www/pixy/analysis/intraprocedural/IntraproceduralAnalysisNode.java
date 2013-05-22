package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;

/**
 * An AnalysisNode holds analysis-specific information for a certain CFGNode.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IntraproceduralAnalysisNode extends AbstractAnalysisNode {
    // input lattice element at current CFG node
    AbstractLatticeElement inValue;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    protected IntraproceduralAnalysisNode(AbstractTransferFunction tf) {
        super(tf);
        this.inValue = null;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public AbstractLatticeElement getInValue() {
        return this.inValue;
    }

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    protected void setInValue(AbstractLatticeElement inValue) {
        this.inValue = inValue;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************
}