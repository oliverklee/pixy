package at.ac.tuwien.infosys.www.pixy.sanitation;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.VulnerabilityInformation;
import at.ac.tuwien.infosys.www.pixy.XssAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;

public class XSSSanitationAnalysis extends AbstractSanitationAnalysis {

	public XSSSanitationAnalysis(DependencyAnalysis depAnalysis) {
		super("xss", depAnalysis, FSAAutomaton.getUndesiredXSSTest());
	}

	public List<Integer> detectVulns() {
		return detectVulns(new XssAnalysis(this.depAnalysis));
	}

	public VulnerabilityInformation detectAlternative() {
		throw new RuntimeException("not yet");
	}

	protected void checkForSink(AbstractCfgNode cfgNodeX, TacFunction traversedFunction, List<Sink> sinks) {

		if (cfgNodeX instanceof Echo) {

			Echo cfgNode = (Echo) cfgNodeX;

			Sink sink = new Sink(cfgNode, traversedFunction);
			sink.addSensitivePlace(cfgNode.getPlace());

			sinks.add(sink);

		} else if (cfgNodeX instanceof CallBuiltinFunction) {

			CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
			String functionName = cfgNode.getFunctionName();

			checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);

		} else if (cfgNodeX instanceof CallPreparation) {

			CallPreparation cfgNode = (CallPreparation) cfgNodeX;
			String functionName = cfgNode.getFunctionNamePlace().toString();

			checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
		} else {
		}
	}

	private void checkForSinkHelper(String functionName, AbstractCfgNode cfgNode, List<TacActualParameter> paramList,
			TacFunction traversedFunction, List<Sink> sinks) {

		if (this.dci.getSinks().containsKey(functionName)) {
			Sink sink = new Sink(cfgNode, traversedFunction);
			Set<Integer> indexList = this.dci.getSinks().get(functionName);
			if (indexList == null) {
				if (functionName.equals("printf")) {
					for (Iterator<TacActualParameter> iter = paramList.iterator(); iter.hasNext();) {
						TacActualParameter param = (TacActualParameter) iter.next();
						sink.addSensitivePlace(param.getPlace());
					}
					sinks.add(sink);
				}
			} else {
				for (Integer index : indexList) {
					if (paramList.size() > index) {
						sink.addSensitivePlace(paramList.get(index).getPlace());
						sinks.add(sink);
					}
				}
			}
		} else {
		}
	}
}