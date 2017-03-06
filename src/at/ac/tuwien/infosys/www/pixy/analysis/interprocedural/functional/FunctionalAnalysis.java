package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.ReverseTarget;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class FunctionalAnalysis extends AbstractAnalysisType {

	public AbstractContext getPropagationContext(Call callNode, AbstractContext context) {
		AbstractLatticeElement inValue = this.enclosedAnalysis.getInterAnalysisInfo().getAnalysisNode(callNode)
				.getPhiValue(context);
		return new FunctionalContext(inValue);
	}

	public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, AbstractContext contextX) {

		List<ReverseTarget> retMe = new LinkedList<ReverseTarget>();
		FunctionalContext context = (FunctionalContext) contextX;
		List<?> calls = exitedFunction.getCalledFrom();
		for (Iterator<?> iter = calls.iterator(); iter.hasNext();) {

			Call callNode = (Call) iter.next();
			FunctionalAnalysisNode analysisNode = (FunctionalAnalysisNode) this.enclosedAnalysis.getInterAnalysisInfo()
					.getAnalysisNode(callNode);
			if (analysisNode == null) {
				continue;
			}
			Set<FunctionalContext> calleeContexts = analysisNode.getReversePhiContexts(context.getLatticeElement());
			if (calleeContexts != null) {
				ReverseTarget revTarget = new ReverseTarget(callNode, calleeContexts);
				retMe.add(revTarget);
			} else {
			}
		}
		return retMe;
	}

	public AbstractInterproceduralAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, AbstractTransferFunction tf) {
		return new FunctionalAnalysisNode(cfgNode, tf);
	}

	public boolean useSummaries() {
		return true;
	}

	public AbstractContext initContext(AbstractInterproceduralAnalysis analysis) {
		return new FunctionalContext(analysis.getStartValue());
	}
}