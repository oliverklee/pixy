package at.ac.tuwien.infosys.www.pixy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.VisualizePT.GraphViz;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.AbstractNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.BuiltinFunctionNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.CompleteGraphNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.DependencyGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.NormalNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.UninitializedNode;
import at.ac.tuwien.infosys.www.pixy.automaton.Automaton;
import at.ac.tuwien.infosys.www.pixy.automaton.Transition;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;
import at.ac.tuwien.infosys.www.pixy.sanitation.AbstractSanitationAnalysis;
import at.ac.tuwien.infosys.www.pixy.transduction.MyTransductions;

public class SqlAnalysis extends AbstractVulnerabilityAnalysis {

	private boolean useTransducers = false;

	public SqlAnalysis(DependencyAnalysis depAnalysis) {
		super(depAnalysis);
		this.getIsTainted = !MyOptions.optionI;
	}

	public List<Integer> detectVulns() {
		List<Integer> retMe = new LinkedList<Integer>();

		try {
			File file = new File(MyOptions.outputHtmlPath + "/Report.html");

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			PrintWriter bw = new PrintWriter(new BufferedWriter(fw));

			System.out.println("Creating SQL Injection Analysis Report");
			bw.append("<p class=\"title\">SQL Injection Analysis Report</p>");
			List<Sink> sinks = this.collectSinks();
			Collections.sort(sinks);
			bw.append("<table id=\"rounded-corner\" summary=\"XSS Scan Report\" >" + "<thead>" + "<tr>"
					+ "<th scope=\"col\" class=\"rounded-company\">Vulnerablitiy Class</th>"
					+ "<th scope=\"col\" class=\"rounded-q2\">Vulnerablitiy Type</th>"
					+ "<th scope=\"col\" class=\"rounded-q1\">File Name</th>"
					+ "<th scope=\"col\" class=\"rounded-q1\">Line Number</th>"
					+ "<th scope=\"col\" class=\"rounded-q3\">D.Graph Name</th>" + "</tr>" + "</thead>" + "<tbody>");

			List<String> uniquevul = new LinkedList<String>();
			String fileName = MyOptions.entryFile.getName();

			int graphcount = 0;
			int vulncount = 0;
			for (Sink sink : sinks) {
				Collection<DependencyGraph> depGraphs = depAnalysis.getDepGraph(sink);
				for (DependencyGraph depGraph : depGraphs) {
					graphcount++;
					String graphNameBase = "sql_" + fileName + "_" + graphcount;

					DependencyGraph sqlGraph = new DependencyGraph(depGraph);
					AbstractCfgNode cfgNode = null;
					if (depGraph.getRoot() != null) {
						if (depGraph.getRoot().getCfgNode() != null) {
							cfgNode = depGraph.getRoot().getCfgNode();
						}
					}
					if (cfgNode == null) {
						continue;
					}
					depGraph.dumpDot(graphNameBase + "_dep", MyOptions.graphPath, depGraph.getUninitNodes(), this.dci);

					Automaton auto = this.toAutomaton(sqlGraph, depGraph);

					boolean tainted = false;
					if (auto.hasDirectlyTaintedTransitions()) {
						tainted = true;
					}
					if (auto.hasIndirectlyTaintedTransitions()) {
						if (auto.hasDangerousIndirectTaint()) {
							tainted = true;
						} else {
						}
					}
					if (!tainted) {
					} else {
						vulncount++;

						retMe.add(cfgNode.getOriginalLineNumber());
					}
					if (tainted) {
						if (uniquevul.contains(cfgNode.getLoc())) {
							vulncount--;

						} else {

							DependencyGraph relevant = this.getRelevant(depGraph);
							Map<UninitializedNode, InitialTaint> dangerousUninit = this.findDangerousUninit(relevant);
							if (!dangerousUninit.isEmpty()) {
								bw.append("<tr>");
								bw.append("<td>SQLI</td>");

								if (dangerousUninit.values().contains(InitialTaint.ALWAYS)) {

									bw.append("<td>unconditional</td>");

								} else {

									bw.append("<td>conditional on register_globals=on</td>");

								}
								relevant.reduceWithLeaves(dangerousUninit.keySet());
								Set<? extends AbstractNode> fillUs;
								if (MyOptions.option_V) {
									relevant.removeTemporaries();
									fillUs = relevant.removeUninitNodes();
								} else {
									fillUs = dangerousUninit.keySet();
								}
								bw.append("<td><a href=file:///" + cfgNode.getFileName() + ">" + cfgNode.getFileName()
										+ "</a></td>");
								bw.append("<td>" + cfgNode.getOriginalLineNumber() + "</td>");

								bw.append("<td>sql" + graphcount + "</td>");

								bw.append("</tr>");

								uniquevul.add(cfgNode.getLoc());

								relevant.dumpDot(graphNameBase + "_min", MyOptions.graphPath, fillUs, this.dci);
							} else {
								vulncount--;
							}
						}
						System.out.println();
					}

					this.dumpDotAuto(auto, graphNameBase + "_auto", MyOptions.graphPath);

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

				}

			}

			if (MyOptions.optionV) {

			}

			bw.append("<tfoot>" +

					"<tr>" + "<td colspan=\"4\" class=\"rounded-foot-left\" <em> Total Vulnerabilities Count: "
					+ vulncount + "</em></td>" + "<td class=\"rounded-foot-right\">&nbsp;</td>" + "</tr>" + "<tr>"
					+ "<td colspan=\"4\" class=\"rounded-foot-left\"><em>The above data were created using an open source version of Static Code Analyzer \"\"</em></td>"
					+ "<td class=\"rounded-foot-right\">&nbsp;</td>" + "</tr>" + "</tfoot>");

			bw.append("</tbody>");
			bw.append("</table>");

			System.out.println("SQL Report Created");
			System.out.println("\n");
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

				DependencyGraph workGraph = new DependencyGraph(depGraph);
				Automaton auto = this.toAutomaton(workGraph, depGraph);

				boolean tainted = false;
				if (auto.hasDirectlyTaintedTransitions()) {
					tainted = true;
				}
				if (auto.hasIndirectlyTaintedTransitions()) {
					if (auto.hasDangerousIndirectTaint()) {
						tainted = true;
					}
				}
				if (tainted) {

					DependencyGraph relevant = this.getRelevant(depGraph);
					Map<UninitializedNode, InitialTaint> dangerousUninit = this.findDangerousUninit(relevant);
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

	Automaton toAutomaton(DependencyGraph depGraph, DependencyGraph origDepGraph) {
		depGraph.eliminateCycles();
		AbstractNode root = depGraph.getRoot();
		Map<AbstractNode, Automaton> deco = new HashMap<AbstractNode, Automaton>();
		Set<AbstractNode> visited = new HashSet<AbstractNode>();
		this.decorate(root, deco, visited, depGraph, origDepGraph);
		Automaton rootDeco = deco.get(root).clone();
		return rootDeco;
	}

	private void decorate(AbstractNode node, Map<AbstractNode, Automaton> deco, Set<AbstractNode> visited,
			DependencyGraph depGraph, DependencyGraph origDepGraph) {

		visited.add(node);

		List<AbstractNode> successors = depGraph.getSuccessors(node);
		if (successors != null && !successors.isEmpty()) {
			for (AbstractNode succ : successors) {
				if (!visited.contains(succ) && deco.get(succ) == null) {
					decorate(succ, deco, visited, depGraph, origDepGraph);
				}
			}
		}

		Automaton auto = null;
		if (node instanceof NormalNode) {
			NormalNode normalNode = (NormalNode) node;
			if (successors == null || successors.isEmpty()) {
				AbstractTacPlace place = normalNode.getPlace();
				if (place.isLiteral()) {
					auto = Automaton.makeString(place.toString());
				} else {
					throw new RuntimeException("SNH: " + place + ", " + normalNode.getCfgNode().getFileName() + ","
							+ normalNode.getCfgNode().getOriginalLineNumber());
				}
			} else {
				for (AbstractNode succ : successors) {
					if (succ == node) {
						continue;
					}
					Automaton succAuto = deco.get(succ);
					if (succAuto == null) {
						throw new RuntimeException("SNH");
					}
					if (auto == null) {
						auto = succAuto;
					} else {
						auto = auto.union(succAuto);
					}
				}
			}

		} else if (node instanceof BuiltinFunctionNode) {
			auto = this.makeAutoForOp((BuiltinFunctionNode) node, deco, depGraph);

		} else if (node instanceof CompleteGraphNode) {

			Transition.Taint taint = Transition.Taint.Untainted;
			for (AbstractNode succ : successors) {
				if (succ == node) {
					throw new RuntimeException("SNH");
				}
				Automaton succAuto = deco.get(succ);
				if (succAuto == null) {
					throw new RuntimeException("SNH");
				}
				if (succAuto.hasTaintedTransitions()) {
					taint = Transition.Taint.Directly;
					break;
				}
			}
			auto = Automaton.makeAnyString(taint);

		} else if (node instanceof UninitializedNode) {

			Set<AbstractNode> preds = depGraph.getPredecessors(node);
			if (preds.size() != 1) {
				throw new RuntimeException("SNH");
			}
			AbstractNode pre = preds.iterator().next();

			if (pre instanceof NormalNode) {
				NormalNode preNormal = (NormalNode) pre;
				switch (this.initiallyTainted(preNormal.getPlace())) {
				case ALWAYS:
				case IFRG:
					auto = Automaton.makeAnyString(Transition.Taint.Directly);
					break;
				case NEVER:
					auto = Automaton.makeAnyString(Transition.Taint.Untainted);
					break;
				default:
					throw new RuntimeException("SNH");
				}

			} else if (pre instanceof CompleteGraphNode) {

				Set<AbstractNode> origPreds = origDepGraph.getPredecessors(node);
				if (origPreds.size() == 1) {
					AbstractNode origPre = origPreds.iterator().next();
					if (origPre instanceof NormalNode) {
						NormalNode origPreNormal = (NormalNode) origPre;

						switch (this.initiallyTainted(origPreNormal.getPlace())) {
						case ALWAYS:
						case IFRG:
							auto = Automaton.makeAnyString(Transition.Taint.Directly);
							break;
						case NEVER:
							auto = Automaton.makeAnyString(Transition.Taint.Untainted);
							break;
						default:
							throw new RuntimeException("SNH");
						}

					} else {
						auto = Automaton.makeAnyString(Transition.Taint.Directly);
					}
				} else {
					auto = Automaton.makeAnyString(Transition.Taint.Directly);
				}

			} else {
				throw new RuntimeException("SNH: " + pre.getClass());
			}

		} else {
			throw new RuntimeException("SNH");
		}

		if (auto == null) {
			throw new RuntimeException("SNH");
		}

		deco.put(node, auto);

	}

	private Automaton makeAutoForOp(BuiltinFunctionNode node, Map<AbstractNode, Automaton> deco,
			DependencyGraph depGraph) {

		List<AbstractNode> successors = depGraph.getSuccessors(node);
		if (successors == null) {
			successors = new LinkedList<AbstractNode>();
		}

		Automaton retMe = null;

		String opName = node.getName();

		List<Integer> multiList = new LinkedList<Integer>();

		if (!node.isBuiltin()) {
			AbstractCfgNode cfgNodeX = node.getCfgNode();
			if (cfgNodeX instanceof CallUnknownFunction) {
				CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;
				if (cfgNode.isMethod()) {
					retMe = Automaton.makeAnyString(Transition.Taint.Untainted);
				} else {
					retMe = Automaton.makeAnyString(Transition.Taint.Directly);
				}
			} else {
				throw new RuntimeException("SNH");
			}

		} else if (opName.equals(".")) {
			for (AbstractNode succ : successors) {
				Automaton succAuto = deco.get(succ);
				if (retMe == null) {
					retMe = succAuto;
				} else {
					retMe = retMe.concatenate(succAuto);
				}
			}

		} else if (isWeakSanit(opName, multiList)) {

			retMe = Automaton.makeAnyString(Transition.Taint.Indirectly);

		} else if (isStrongSanit(opName)) {

			retMe = Automaton.makeAnyString(Transition.Taint.Untainted);

		} else if (useTransducers && opName.equals("str_replace")) {

			if (successors.size() < 3) {
				throw new RuntimeException("SNH");
			}
			Automaton searchAuto = deco.get(successors.get(0));
			Automaton replaceAuto = deco.get(successors.get(1));
			Automaton subjectAuto = deco.get(successors.get(2));

			boolean supported = true;
			String searchString = null;
			String replaceString = null;
			if (searchAuto.isFinite() && replaceAuto.isFinite()) {
				Set<String> searchSet = searchAuto.getFiniteStrings();
				Set<String> replaceSet = replaceAuto.getFiniteStrings();
				if (searchSet.size() != 1 || replaceSet.size() != 1) {
					supported = false;
				} else {
					searchString = searchSet.iterator().next();
					replaceString = replaceSet.iterator().next();
				}
			} else {
				supported = false;
			}
			if (supported == false) {
				throw new RuntimeException("not supported yet");
			}

			Automaton transduced = new MyTransductions().str_replace(searchString, replaceString, subjectAuto);
			return transduced;

		} else if (isMulti(opName, multiList)) {

			Transition.Taint taint = this.multiDependencyAuto(successors, deco, multiList, false);
			retMe = Automaton.makeAnyString(taint);

		} else if (isInverseMulti(opName, multiList)) {

			Transition.Taint taint = this.multiDependencyAuto(successors, deco, multiList, true);
			retMe = Automaton.makeAnyString(taint);

		} else {

			retMe = Automaton.makeAnyString(Transition.Taint.Directly);
		}

		return retMe;
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

	private Transition.Taint multiDependencyAuto(List<AbstractNode> succs, Map<AbstractNode, Automaton> deco,
			List<Integer> indices, boolean inverse) {

		boolean indirectly = false;
		Set<Integer> indexSet = new HashSet<Integer>(indices);

		int count = -1;
		for (AbstractNode succ : succs) {
			count++;

			if (inverse) {
				if (indexSet.contains(count)) {
					continue;
				}
			} else {
				if (!indexSet.contains(count)) {
					continue;
				}
			}

			Automaton succAuto = deco.get(succ);
			if (succAuto == null) {
				throw new RuntimeException("SNH");
			}
			if (succAuto.hasDirectlyTaintedTransitions()) {
				return Transition.Taint.Directly;
			}
			if (succAuto.hasIndirectlyTaintedTransitions()) {
				indirectly = true;
			}
		}

		if (indirectly) {
			return Transition.Taint.Indirectly;
		} else {
			return Transition.Taint.Untainted;
		}
	}

	void dumpDotAuto(Automaton auto, String graphName, String path) {

		String filename = graphName + ".dot";
		(new File(path)).mkdir();

		try {
			Writer outWriter = new FileWriter(path + "/" + filename);
			String autoDot = auto.toDot();
			outWriter.write(autoDot);
			outWriter.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}

		if (MyOptions.option_P) {
			if (auto.isFinite()) {
				Set<String> finiteStringsSet = auto.getFiniteStrings();
				List<String> finiteStrings = new LinkedList<String>(finiteStringsSet);
				Collections.sort(finiteStrings);
				System.out.println();
				System.out.println("IS FINITE");
				for (String finite : finiteStrings) {
					System.out.println();
					System.out.println("Finite BEGIN");
					System.out.println(finite);
					System.out.println("Finite END");
				}
			}

			System.out.println();
			System.out.println("Prefix BEGIN");
			System.out.println(auto.getCommonPrefix());
			System.out.println("Prefix END");
			System.out.println();
			System.out.println("Suffix BEGIN");
			System.out.println(auto.getCommonSuffix());
			System.out.println("Suffix END");
			System.out.println();
		}

	}

	void dumpDotAutoUnique(Automaton auto, String graphName, String path) {

		String filename = graphName + ".dot";
		(new File(path)).mkdir();

		try {
			Writer outWriter = new FileWriter(path + "/" + filename);
			String autoDot = auto.toDotUnique();
			outWriter.write(autoDot);
			outWriter.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}
	}

}
