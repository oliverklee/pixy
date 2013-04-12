package at.ac.tuwien.infosys.www.pixy.analysis.inter.functional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

// an AnalysisNode holds analysis-specific information for a certain CFGNode
public class FunctionalAnalysisNode
    extends InterAnalysisNode {

    // mapping input LatticeElement -> Set of context LatticeElements;
    // only needed for call nodes
    Map<LatticeElement, Set<FunctionalContext>> reversePhi;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public FunctionalAnalysisNode(CfgNode node, TransferFunction tf) {
        super(tf);
        // maintain reverse mapping for call nodes
        if (node instanceof CfgNodeCall) {
            this.reversePhi = new HashMap<LatticeElement, Set<FunctionalContext>>();
        } else {
            this.reversePhi = null;
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    // returns a set of contexts that are mapped to the given value
    Set<FunctionalContext> getReversePhiContexts(LatticeElement value) {
        return (this.reversePhi.get(value));
    }

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    // sets the PHI value for the given context
    protected void setPhiValue(Context contextX, LatticeElement value) {

        FunctionalContext context = (FunctionalContext) contextX;

        super.setPhiValue(context, value);

        // maintain reverse mapping, if needed
        if (this.reversePhi != null) {
            Set<FunctionalContext> contextSet = this.reversePhi.get(value);
            if (contextSet == null) {
                contextSet = new HashSet<FunctionalContext>();
                this.reversePhi.put(value, contextSet);
            }
            contextSet.add(context);
        }
    }
}