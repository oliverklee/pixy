package at.ac.tuwien.infosys.www.pixy.sanitation;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.SqlAnalysis;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.VulnerabilityInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;

public class SQLSanitationAnalysis extends AbstractSanitationAnalysis {

	public SQLSanitationAnalysis(DependencyAnalysis depAnalysis) {
		this(depAnalysis, true);
	}

	public SQLSanitationAnalysis(DependencyAnalysis depAnalysis, boolean getIsTainted) {
		super("sql", depAnalysis, FSAAutomaton.getUndesiredSQLTest());
		this.getIsTainted = getIsTainted;
		if (MyOptions.fsa_home == null) {
			Utils.bail("SQL Sanitization analysis requires FSA Utilities.\n"
					+ "Please set a valid path in the config file.");
		}
	}

	public List<Integer> detectVulns() {
		return detectVulns(new SqlAnalysis(this.depAnalysis));
	}

	public VulnerabilityInformation detectAlternative() {
		throw new RuntimeException("not yet");
	}

	protected void checkForSink(AbstractCfgNode cfgNodeX, TacFunction traversedFunction, List<Sink> sinks) {

		if (cfgNodeX instanceof CallBuiltinFunction) {

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
			for (Integer param : this.dci.getSinks().get(functionName)) {
				if (paramList.size() > param) {
					sink.addSensitivePlace(paramList.get(param).getPlace());
					sinks.add(sink);
				}
			}
		} else {
		}

	}

}
