package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.ConnectorComputation;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.ReverseTarget;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class CallStringAnalysis extends AbstractAnalysisType {

	ConnectorComputation connectorComp;

	public CallStringAnalysis(ConnectorComputation connectorComp) {
		super();
		this.connectorComp = connectorComp;
	}

	public AbstractContext getPropagationContext(Call callNode, AbstractContext contextX) {

		CallStringContext context = (CallStringContext) contextX;
		return this.connectorComp.getTargetContext(callNode, context.getPosition());
	}

	public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, AbstractContext contextX) {

		CallStringContext context = (CallStringContext) contextX;
		return this.connectorComp.getReverseTargets(exitedFunction, context.getPosition());
	}

	public ConnectorComputation getConnectorComputation() {
		return this.connectorComp;
	}

	public boolean useSummaries() {
		return false;
	}

	public AbstractInterproceduralAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, AbstractTransferFunction tf) {
		return new CallStringAnalysisNode(cfgNode, tf);
	}

	public AbstractContext initContext(AbstractInterproceduralAnalysis analysis) {
		return new CallStringContext(0);
	}

}
