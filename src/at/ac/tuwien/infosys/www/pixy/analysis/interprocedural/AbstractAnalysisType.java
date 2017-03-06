package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public abstract class AbstractAnalysisType {

	protected AbstractInterproceduralAnalysis enclosedAnalysis;

	public abstract AbstractContext getPropagationContext(Call callNode, AbstractContext context);

	public abstract List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, AbstractContext contextX);

	public void setAnalysis(AbstractInterproceduralAnalysis enclosedAnalysis) {
		this.enclosedAnalysis = enclosedAnalysis;
	}

	public abstract AbstractInterproceduralAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode,
			AbstractTransferFunction tf);

	public abstract boolean useSummaries();

	public abstract AbstractContext initContext(AbstractInterproceduralAnalysis analysis);

}
