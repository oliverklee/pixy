package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class FunctionalAnalysisNode extends AbstractInterproceduralAnalysisNode {

	Map<AbstractLatticeElement, Set<FunctionalContext>> reversePhi;

	public FunctionalAnalysisNode(AbstractCfgNode node, AbstractTransferFunction tf) {
		super(tf);
		if (node instanceof Call) {
			this.reversePhi = new HashMap<AbstractLatticeElement, Set<FunctionalContext>>();
		} else {
			this.reversePhi = null;
		}
	}

	Set<FunctionalContext> getReversePhiContexts(AbstractLatticeElement value) {
		return (this.reversePhi.get(value));
	}

	protected void setPhiValue(AbstractContext contextX, AbstractLatticeElement value) {

		FunctionalContext context = (FunctionalContext) contextX;

		super.setPhiValue(context, value);

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