package at.ac.tuwien.infosys.www.pixy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.VisualizePT.GraphViz;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.AbstractNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.BuiltinFunctionNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.DependencyGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.NormalNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.UninitializedNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;
import at.ac.tuwien.infosys.www.pixy.sanitation.AbstractSanitationAnalysis;

public class XssAnalysis extends AbstractVulnerabilityAnalysis {
	public XssAnalysis(DependencyAnalysis depAnalysis) {
		super(depAnalysis);
	}

	public List<Integer> detectVulns() {
		List<Integer> retMe = new LinkedList<Integer>();

		try {
			File file = new File(MyOptions.outputHtmlPath + "/Report.html");

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("<html>" + "<head>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>"
					+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>" + "<title>Report- XSS</title>"
					+ "<style type=\"text/css\">" + "<!--" + "@import url(\"Style//style.css\");" + "-->" + "</style>"
					+ "</head>" + "<body>");

			System.out.println("Creating XSS Analysis Report");
			bw.write("<p class=\"title\">XSS Analysis Report</p>");

			List<Sink> sinks = this.collectSinks();
			Collections.sort(sinks);

			bw.write("<table id=\"rounded-corner\" summary=\"XSS Scan Report\" >" + "<thead>" + "<tr>"
					+ "<th scope=\"col\" class=\"rounded-company\">Vulnerablitiy Class</th>"
					+ "<th scope=\"col\" class=\"rounded-q2\">Vulnerablitiy Type</th>"
					+ "<th scope=\"col\" class=\"rounded-q1\">File Name</th>"
					+ "<th scope=\"col\" class=\"rounded-q1\">Line Number</th>"
					+ "<th scope=\"col\" class=\"rounded-q3\">D.Graph Name</th>" + "</tr>" + "</thead>" + "<tbody>");

			List<String> uniquevul = new LinkedList<String>();
			StringBuilder sink2Graph = new StringBuilder();
			StringBuilder quickReport = new StringBuilder();

			String fileName = MyOptions.entryFile.getName();

			int graphcount = 0;
			int vulncount = 0;
			for (Sink sink : sinks) {
				Collection<DependencyGraph> depGraphs = depAnalysis.getDepGraph(sink);

				for (DependencyGraph depGraph : depGraphs) {

					graphcount++;

					String graphNameBase = "xss_" + fileName + "_" + graphcount;

					if (!MyOptions.optionW) {
						depGraph.dumpDot(graphNameBase + "_dep", MyOptions.graphPath, this.dci);
					}

					// if(MyOptions.option_VXSS){
					if (true) {
						try {
							String input = MyOptions.graphPath + "/" + graphNameBase + "_dep" + ".dot";
							GraphViz gv = new GraphViz();
							gv.readSource(input);

							String type = "gif";
							File out = new File(MyOptions.graphPath + "/" + graphNameBase + "_dep" + "." + type);
							gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);

						}

						catch (Exception e) {
							System.out.println("Error in Visualization!!!");
						}
					}

					DependencyGraph relevant = this.getRelevant(depGraph);
					if (relevant == null)
						continue;

					Map<UninitializedNode, InitialTaint> dangerousUninit = this.findDangerousUninit(relevant);

					if (!dangerousUninit.isEmpty()) {

						relevant.reduceWithLeaves(dangerousUninit.keySet());

						Set<? extends AbstractNode> fillUs;
						if (MyOptions.option_V) {
							relevant.removeTemporaries();
							fillUs = relevant.removeUninitNodes();
						} else {
							fillUs = dangerousUninit.keySet();
						}

						vulncount++;
						NormalNode root = depGraph.getRoot();
						AbstractCfgNode cfgNode = root.getCfgNode();
						retMe.add(cfgNode.getOriginalLineNumber());
						if (uniquevul.contains(cfgNode.getLoc())) {
							vulncount--;

						} else {
							bw.write("<tr>");
							bw.write("<td>XSS</td>");

							if (dangerousUninit.values().contains(InitialTaint.ALWAYS)) {
								bw.write("<td>unconditional</td>");
							} else {
								bw.write("<td>conditional on register_globals=on</td>");
							}
							bw.write("<td><a href=file:///" + cfgNode.getFileName() + ">" + cfgNode.getFileName()
									+ "</a></td>");
							bw.write("<td>" + cfgNode.getOriginalLineNumber() + "</td>");
							bw.write("<td>xss" + graphcount + "</td>");
							bw.write("</tr>");
							uniquevul.add(cfgNode.getLoc());
							relevant.dumpDot(graphNameBase + "_min", MyOptions.graphPath, fillUs, this.dci);
						}
						if (MyOptions.optionW) {
							sink2Graph.append(sink.getLineNo());
							sink2Graph.append(":");
							sink2Graph.append(graphNameBase + "_min");
							sink2Graph.append("\n");

							quickReport.append("Line ");
							quickReport.append(sink.getLineNo());
							quickReport.append("\nSources:\n");
							for (AbstractNode leafX : relevant.getLeafNodes()) {
								quickReport.append("  ");
								if (leafX instanceof NormalNode) {
									NormalNode leaf = (NormalNode) leafX;
									quickReport.append(leaf.getLine());
									quickReport.append(" : ");
									quickReport.append(leaf.getPlace());
								} else if (leafX instanceof BuiltinFunctionNode) {
									BuiltinFunctionNode leaf = (BuiltinFunctionNode) leafX;
									quickReport.append(leaf.getLine());
									quickReport.append(" : ");
									quickReport.append(leaf.getName());
								}
								quickReport.append("\n");
							}
							quickReport.append("\n");
						}
					}
				}
			}

			if (MyOptions.optionV) {
				System.out.println("Total Graph Count: " + graphcount);
			}

			bw.write("<tfoot>" +

					"<tr>" + "<td colspan=\"4\" class=\"rounded-foot-left\" <em> Total Vulnerabilities Count: "
					+ vulncount + "</em></td>" + "<td class=\"rounded-foot-right\">&nbsp;</td>" + "</tr>" + "<tr>"
					+ "<td colspan=\"4\" class=\"rounded-foot-left\"><em>The above data were created using an open source version of Static Code Analyzer \"\"</em></td>"
					+ "<td class=\"rounded-foot-right\">&nbsp;</td>" + "</tr>" + "</tfoot>");

			bw.write("</tbody>");
			bw.write("</table>");

			System.out.println("XSS Report Created");
			System.out.println("\n");

			if (MyOptions.optionW) {
				Utils.writeToFile(sink2Graph.toString(), MyOptions.graphPath + "/xssSinks2Urls.txt");
				Utils.writeToFile(quickReport.toString(), MyOptions.graphPath + "/xssQuickReport.txt");
			}
			bw.close();
		} catch (Exception e) {

			System.out.println(e.getStackTrace());
		}
		return retMe;
	}

	public VulnerabilityInformation detectAlternative() {

		VulnerabilityInformation retMe = new VulnerabilityInformation();

		List<Sink> sinks = this.collectSinks();
		Collections.sort(sinks);

		int graphcount = 0;
		int totalPathCount = 0;
		int basicPathCount = 0;
		int hasCustomSanitCount = 0;
		int customSanitThrownAwayCount = 0;
		for (Sink sink : sinks) {

			Collection<DependencyGraph> depGraphs = depAnalysis.getDepGraph(sink);

			for (DependencyGraph depGraph : depGraphs) {

				graphcount++;

				DependencyGraph relevant = this.getRelevant(depGraph);

				Map<UninitializedNode, InitialTaint> dangerousUninit = this.findDangerousUninit(relevant);

				boolean tainted = false;
				if (!dangerousUninit.isEmpty()) {
					tainted = true;

					relevant.reduceWithLeaves(dangerousUninit.keySet());

					retMe.addDepGraph(depGraph, relevant);
				}

				if (MyOptions.countPaths) {
					int pathNum = depGraph.countPaths();
					totalPathCount += pathNum;
					if (tainted) {
						basicPathCount += pathNum;
					}
				}

				if (!AbstractSanitationAnalysis.findCustomSanit(depGraph).isEmpty()) {
					hasCustomSanitCount++;
					if (!tainted) {
						customSanitThrownAwayCount++;
					}
				}
			}

		}

		retMe.setInitialGraphCount(graphcount);
		retMe.setTotalPathCount(totalPathCount);
		retMe.setBasicPathCount(basicPathCount);
		retMe.setCustomSanitCount(hasCustomSanitCount);
		retMe.setCustomSanitThrownAwayCount(customSanitThrownAwayCount);
		return retMe;

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
