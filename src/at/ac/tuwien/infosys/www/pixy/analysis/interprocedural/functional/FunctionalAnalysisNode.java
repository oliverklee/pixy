package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An AnalysisNode holds analysis-specific information for a certain CFGNode.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class FunctionalAnalysisNode extends AbstractInterproceduralAnalysisNode {
    // mapping input AbstractLatticeElement -> Set of context LatticeElements;
    // only needed for call nodes
    Map<AbstractLatticeElement, Set<FunctionalContext>> reversePhi;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public FunctionalAnalysisNode(AbstractCfgNode node, AbstractTransferFunction tf) {
        super(tf);
        // maintain reverse mapping for call nodes
        if (node instanceof Call) {
            this.reversePhi = new HashMap<>();
        } else {
            this.reversePhi = null;
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    // returns a set of contexts that are mapped to the given value
    Set<FunctionalContext> getReversePhiContexts(AbstractLatticeElement value) {
        return this.reversePhi.get(value);
    }

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    // sets the PHI value for the given context
    protected void setPhiValue(AbstractContext contextX, AbstractLatticeElement value) {

        FunctionalContext context = (FunctionalContext) contextX;

        super.setPhiValue(context, value);

        // maintain reverse mapping, if needed
        if (this.reversePhi != null) {
            Set<FunctionalContext> contextSet = this.reversePhi.get(value);
            if (contextSet == null) {
                contextSet = new HashSet<>();
                this.reversePhi.put(value, contextSet);
            }
            contextSet.add(context);
        }
    }
}