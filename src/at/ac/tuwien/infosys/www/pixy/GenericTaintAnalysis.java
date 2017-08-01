package at.ac.tuwien.infosys.www.pixy;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification.GlobalsModificationAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;

public class GenericTaintAnalysis {

	private List<AbstractVulnerabilityAnalysis> depClients;

	public DependencyAnalysis depAnalysis;

	private GenericTaintAnalysis() {
		this.depClients = new LinkedList<AbstractVulnerabilityAnalysis>();
	}

	private void addDepClient(AbstractVulnerabilityAnalysis depClient) {
		this.depClients.add(depClient);
	}

	static GenericTaintAnalysis createAnalysis(TacConverter tac, AbstractAnalysisType enclosingAnalysis,
			Checker checker, InterproceduralWorklist workList, GlobalsModificationAnalysis modAnalysis) {

		GenericTaintAnalysis gta = new GenericTaintAnalysis();

		gta.depAnalysis = new DependencyAnalysis(tac, checker.aliasAnalysis, checker.literalAnalysis, enclosingAnalysis,
				workList, modAnalysis);

		try {

			Class.forName("at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis");
			for (VulnerabilityAnalysisInformation dci : MyOptions.getDepClients()) {
				if (!dci.performMe()) {
					continue;
				}
				if (dci.getClassName() == "XssAnalysis" || dci.getClassName().equalsIgnoreCase("XssAnalysis")) {
					DependencyAnalysis dep = gta.depAnalysis;
					AbstractVulnerabilityAnalysis depClient = new XssAnalysis(dep);
					gta.addDepClient(depClient);
				}

				if (dci.getClassName() == "SqlAnalysis" || dci.getClassName().equalsIgnoreCase("SqlAnalysis")) {
					DependencyAnalysis dep = gta.depAnalysis;
					AbstractVulnerabilityAnalysis depClient = new SqlAnalysis(dep);
					gta.addDepClient(depClient);
				}

				if (dci.getClassName() == "XPathAnalysis" || dci.getClassName().equalsIgnoreCase("XPathAnalysis")) {
					DependencyAnalysis dep = gta.depAnalysis;
					AbstractVulnerabilityAnalysis depClient = new XPathAnalysis(dep);
					gta.addDepClient(depClient);
				}

				if (dci.getClassName() == "CommandExecutionAnalysis"
						|| dci.getClassName().equalsIgnoreCase("CommandExecutionAnalysis")) {
					DependencyAnalysis dep = gta.depAnalysis;
					AbstractVulnerabilityAnalysis depClient = new CommandExecutionAnalysis(dep);
					gta.addDepClient(depClient);
				}
				if (dci.getClassName() == "CodeEvaluatingAnalysis"
						|| dci.getClassName().equalsIgnoreCase("CodeEvaluatingAnalysis")) {
					DependencyAnalysis dep = gta.depAnalysis;
					AbstractVulnerabilityAnalysis depClient = new CodeEvaluatingAnalysis(dep);
					gta.addDepClient(depClient);
				}

				if (dci.getClassName() == "FileAnalysis" || dci.getClassName().equalsIgnoreCase("FileAnalysis")) {
					DependencyAnalysis dep = gta.depAnalysis;
					AbstractVulnerabilityAnalysis depClient = new FileAnalysis(dep);
					gta.addDepClient(depClient);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return gta;
	}

	void analyze() {
		this.depAnalysis.analyze();
		this.depAnalysis.checkReachability();
	}

	List<Integer> detectVulns() {
		List<Integer> retMe = new LinkedList<Integer>();
		for (AbstractVulnerabilityAnalysis depClient : this.depClients) {
			retMe.addAll(depClient.detectVulns());
		}
		return retMe;
	}

	List<AbstractVulnerabilityAnalysis> getDepClients() {
		return this.depClients;
	}

}
