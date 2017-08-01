package at.ac.tuwien.infosys.www.pixy.sanitation;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.AbstractVulnerabilityAnalysis;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.VulnerabilityInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.AbstractNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.BuiltinFunctionNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.CompleteGraphNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.DependencyGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.NormalNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.UninitializedNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;

public abstract class AbstractSanitationAnalysis extends AbstractVulnerabilityAnalysis {

	private boolean trimUntainted = !MyOptions.optionR;

	protected FSAAutomaton undesir;

	protected String name;

	protected AbstractSanitationAnalysis(String name, DependencyAnalysis depAnalysis, FSAAutomaton undesired) {
		super(depAnalysis);
		this.name = name;
		this.undesir = undesired;
	}

	public List<Integer> detectVulns(AbstractVulnerabilityAnalysis depClient) {

		System.out.println();
		System.out.println("*****************");
		System.out.println(name.toUpperCase() + " Sanit Analysis BEGIN");
		System.out.println("*****************");
		System.out.println();

		VulnerabilityInformation vulnInfo = depClient.detectAlternative();
		List<DependencyGraph> vulnDepGraphs = vulnInfo.getDepGraphs();
		List<DependencyGraph> minDepGraphs = vulnInfo.getDepGraphsMin();

		int scanned = vulnInfo.getInitialGraphCount();
		int reported_by_basic = vulnDepGraphs.size();
		int sure_vuln_1 = 0;
		int sure_vuln_2 = 0;
		int possible_vuln = 0;
		int eliminated = 0;

		System.out.println(name.toUpperCase() + " Sanit Analysis Output");
		System.out.println("--------------------");
		System.out.println();

		this.dumpDotAuto(this.undesir, "undesired_" + name, MyOptions.graphPath);

		StringBuilder dynInfo = new StringBuilder();

		int dynpathcount = 0;

		int graphcount = 0;
		Iterator<DependencyGraph> minIter = minDepGraphs.iterator();
		for (DependencyGraph depGraph : vulnDepGraphs) {

			graphcount++;

			DependencyGraph minGraph = minIter.next();

			depGraph.dumpDot(name + "sanit" + graphcount + "i", MyOptions.graphPath, depGraph.getUninitNodes(),
					this.dci);
			minGraph.dumpDot(name + "sanit" + graphcount + "m", MyOptions.graphPath, depGraph.getUninitNodes(),
					this.dci);

			AbstractCfgNode cfgNode = depGraph.getRoot().getCfgNode();

			List<AbstractNode> customSanitNodes = findCustomSanit(minGraph);

			if (customSanitNodes.isEmpty()) {
				System.out.println("No Sanitization!");
				System.out.println("- " + cfgNode.getLoc());
				System.out.println("- Graphs: " + name + "sanit" + graphcount);
				sure_vuln_1++;
				continue;
			}
			DependencyGraph workGraph = new DependencyGraph(depGraph);

			Map<AbstractNode, FSAAutomaton> deco = new HashMap<AbstractNode, FSAAutomaton>();
			FSAAutomaton auto = this.toAutomatonSanit(workGraph, depGraph, deco);

			FSAAutomaton intersection = auto.intersect(this.undesir);
			if (!intersection.isEmpty()) {

				this.dumpDotAuto(intersection, name + "sanit" + graphcount + "intersect", MyOptions.graphPath);

				DependencyGraph sanitMinGraph = new DependencyGraph(minGraph);
				sanitMinGraph.reduceToInnerNodes(customSanitNodes);

				int ineffBorder = sanitMinGraph.reduceToIneffectiveSanit(deco, this);

				if (ineffBorder != 0) {

					System.out.println("Ineffective Sanitization!");
					System.out.println("- " + cfgNode.getLoc());
					System.out.println("- Graphs: " + name + "sanit" + graphcount);
					possible_vuln++;

					sanitMinGraph.dumpDot(name + "sanit" + graphcount + "mm", MyOptions.graphPath,
							depGraph.getUninitNodes(), this.dci);

					dynInfo.append("SINK:\n");
					dynInfo.append(sanitMinGraph.getRoot().toString());
					dynInfo.append("\n");
					dynInfo.append("SOURCES:\n");
					List<NormalNode> dangerousSources = this.findDangerousSources(sanitMinGraph);
					for (NormalNode dangerousSource : dangerousSources) {
						dynInfo.append(dangerousSource.toString());
						dynInfo.append("\n");
					}
					dynInfo.append("\n");

					if (MyOptions.countPaths) {
						int paths = new DependencyGraph(sanitMinGraph).countPaths();
						dynpathcount += paths;
					}

				} else {
					System.out.println("No Sanitization!");
					System.out.println("- " + cfgNode.getLoc());
					System.out.println("- Graphs: " + name + "sanit" + graphcount);
					sure_vuln_2++;
				}

			} else {
				eliminated++;
			}

			this.dumpDotAuto(auto, name + "sanit" + graphcount + "auto", MyOptions.graphPath);
		}

		Utils.writeToFile(dynInfo.toString(), MyOptions.graphPath + "/" + name + "info.txt");

		System.out.println();
		System.out.println("Scanned depgraphs: " + scanned);
		System.out.println("Depgraphs reported by basic analysis: " + reported_by_basic);
		System.out.println();
		if (MyOptions.countPaths) {
			System.out.println("Total initial paths: " + vulnInfo.getTotalPathCount());
			System.out.println("Total basic paths: " + vulnInfo.getBasicPathCount());
			System.out.println("Paths for dynamic analysis: " + dynpathcount);
			System.out.println();
		}
		System.out.println("Total DepGraphs with custom sanitization: " + vulnInfo.getCustomSanitCount());
		System.out.println("DepGraphs with custom sanitization thrown away by basic analysis: "
				+ vulnInfo.getCustomSanitThrownAwayCount());
		System.out.println();
		System.out.println("Eliminated false positives: " + eliminated);
		System.out.println();
		System.out.println("Certain vulns: " + (sure_vuln_1 + sure_vuln_2));
		System.out.println("Possible vulns due to ineffective sanitization: " + possible_vuln);

		System.out.println();
		System.out.println("*****************");
		System.out.println(name.toUpperCase() + " Sanit Analysis END");
		System.out.println("*****************");
		System.out.println();

		return new LinkedList<Integer>();

	}

	protected FSAAutomaton toAutomatonSanit(DependencyGraph depGraph, DependencyGraph origDepGraph,
			Map<AbstractNode, FSAAutomaton> deco) {
		depGraph.eliminateCycles();
		AbstractNode root = depGraph.getRoot();
		Set<AbstractNode> visited = new HashSet<AbstractNode>();
		this.decorateSanit(root, deco, visited, depGraph, origDepGraph, true);
		FSAAutomaton rootDeco = deco.get(root).clone();
		return rootDeco;
	}

	private final void decorateSanit(AbstractNode node, Map<AbstractNode, FSAAutomaton> deco, Set<AbstractNode> visited,
			DependencyGraph depGraph, DependencyGraph origDepGraph, boolean trimAllowed) {

		visited.add(node);

		TrimInfo trimInfo;
		if (trimAllowed) {
			trimInfo = this.checkTrim(node);
		} else {
			trimInfo = new TrimInfo();
			trimInfo.setDefaultTrim(false);
		}

		List<AbstractNode> successors = depGraph.getSuccessors(node);
		if (successors != null && !successors.isEmpty()) {
			int i = 0;
			for (AbstractNode succ : successors) {
				if (!visited.contains(succ) && deco.get(succ) == null) {
					decorateSanit(succ, deco, visited, depGraph, origDepGraph, trimInfo.mayTrim(i));
				}
				i++;
			}
		}

		FSAAutomaton auto = null;
		if (node instanceof NormalNode) {
			NormalNode normalNode = (NormalNode) node;
			if (successors == null || successors.isEmpty()) {
				AbstractTacPlace place = normalNode.getPlace();
				if (place.isLiteral()) {
					if (trimUntainted && trimAllowed) {
						auto = FSAAutomaton.makeString("");
					} else {
						auto = FSAAutomaton.makeString(place.toString());
					}
				} else {
					throw new RuntimeException("SNH: " + place + ", " + normalNode.getCfgNode().getFileName() + ","
							+ normalNode.getCfgNode().getOriginalLineNumber());
				}
			} else {
				for (AbstractNode succ : successors) {
					if (succ == node) {
						continue;
					}
					FSAAutomaton succAuto = deco.get(succ);
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
			auto = this.makeAutoForOp((BuiltinFunctionNode) node, deco, depGraph, trimAllowed);

		} else if (node instanceof CompleteGraphNode) {

			if (trimUntainted && trimAllowed) {

				auto = FSAAutomaton.makeString("");

				for (AbstractNode succ : successors) {
					if (succ == node) {
						throw new RuntimeException("SNH");
					}
					FSAAutomaton succAuto = deco.get(succ);
					if (succAuto == null) {
						throw new RuntimeException("SNH");
					}
					if (succAuto.isEmpty()) {
						auto = FSAAutomaton.makeAnyString();
						break;
					}
				}

			} else {
				auto = FSAAutomaton.makeAnyString();
			}

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
					auto = FSAAutomaton.makeAnyString();
					break;
				case NEVER:
					if (trimUntainted && trimAllowed) {
						auto = FSAAutomaton.makeString("");
					} else {
						auto = FSAAutomaton.makeAnyString();
					}
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
							auto = FSAAutomaton.makeAnyString();
							break;
						case NEVER:
							if (trimUntainted && trimAllowed) {
								auto = FSAAutomaton.makeString("");
							} else {
								auto = FSAAutomaton.makeAnyString();
							}
							break;
						default:
							throw new RuntimeException("SNH");
						}

					} else {
						auto = FSAAutomaton.makeAnyString();
					}
				} else {
					auto = FSAAutomaton.makeAnyString();
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

	private final FSAAutomaton makeAutoForOp(BuiltinFunctionNode node, Map<AbstractNode, FSAAutomaton> deco,
			DependencyGraph depGraph, boolean trimAllowed) {

		List<AbstractNode> successors = depGraph.getSuccessors(node);
		if (successors == null) {
			successors = new LinkedList<AbstractNode>();
		}

		FSAAutomaton retMe = null;

		String opName = node.getName();

		List<Integer> multiList = new LinkedList<Integer>();

		if (!node.isBuiltin()) {
			AbstractCfgNode cfgNodeX = node.getCfgNode();
			if (cfgNodeX instanceof CallUnknownFunction) {
				CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;
				if (cfgNode.isMethod()) {
					if (trimUntainted && trimAllowed) {
						retMe = FSAAutomaton.makeString("");
					} else {
						retMe = FSAAutomaton.makeAnyString();
					}
				} else {
					retMe = FSAAutomaton.makeAnyString();
				}
			} else {
				throw new RuntimeException("SNH");
			}

		} else if (opName.equals(".")) {

			for (AbstractNode succ : successors) {
				FSAAutomaton succAuto = deco.get(succ);
				if (retMe == null) {
					retMe = succAuto;
				} else {
					retMe = retMe.concatenate(succAuto);
				}
			}

		} else if (opName.equals("preg_replace")) {

			if (successors.size() < 3) {
				throw new RuntimeException("SNH");
			}
			FSAAutomaton searchAuto = deco.get(successors.get(0));
			FSAAutomaton replaceAuto = deco.get(successors.get(1));
			FSAAutomaton subjectAuto = deco.get(successors.get(2));

			if (trimUntainted && !replaceAuto.isEmpty()) {
				return FSAAutomaton.makeAnyString();
			}

			FSAAutomaton transduced = FSAUtils.reg_replace(searchAuto, replaceAuto, subjectAuto, true,
					node.getCfgNode());
			return transduced;

		} else if (opName.equals("ereg_replace")) {

			if (successors.size() < 3) {
				throw new RuntimeException("SNH");
			}
			FSAAutomaton searchAuto = deco.get(successors.get(0));
			FSAAutomaton replaceAuto = deco.get(successors.get(1));
			FSAAutomaton subjectAuto = deco.get(successors.get(2));

			if (trimUntainted && !replaceAuto.isEmpty()) {
				return FSAAutomaton.makeAnyString();
			}

			FSAAutomaton transduced = FSAUtils.reg_replace(searchAuto, replaceAuto, subjectAuto, false,
					node.getCfgNode());
			return transduced;

		} else if (opName.equals("str_replace")) {

			if (successors.size() < 3) {
				throw new RuntimeException("SNH");
			}
			FSAAutomaton searchAuto = deco.get(successors.get(0));
			FSAAutomaton replaceAuto = deco.get(successors.get(1));
			FSAAutomaton subjectAuto = deco.get(successors.get(2));

			if (trimUntainted && !replaceAuto.isEmpty()) {
				return FSAAutomaton.makeAnyString();
			}

			FSAAutomaton transduced = FSAUtils.str_replace(searchAuto, replaceAuto, subjectAuto, node.getCfgNode());
			return transduced;

		} else if (opName.equals("addslashes")) {

			if (successors.size() != 1) {
				throw new RuntimeException("SNH");
			}
			FSAAutomaton paramAuto = deco.get(successors.get(0));

			FSAAutomaton transduced = FSAUtils.addslashes(paramAuto, node.getCfgNode());
			return transduced;

		} else if (isWeakSanit(opName, multiList)) {

			if (trimUntainted && trimAllowed) {
				retMe = FSAAutomaton.makeString("");
			} else {
				retMe = FSAAutomaton.makeAnyString();
			}

		} else if (isStrongSanit(opName)) {

			if (trimUntainted && trimAllowed) {
				retMe = FSAAutomaton.makeString("");
			} else {
				retMe = FSAAutomaton.makeAnyString();
			}

		} else if (isMulti(opName, multiList)) {

			retMe = this.multiDependencyAutoSanit(successors, deco, multiList, false);

		} else if (isInverseMulti(opName, multiList)) {

			retMe = this.multiDependencyAutoSanit(successors, deco, multiList, true);

		} else {
			retMe = FSAAutomaton.makeAnyString();
		}

		return retMe;
	}

	private FSAAutomaton multiDependencyAutoSanit(List<AbstractNode> succs, Map<AbstractNode, FSAAutomaton> deco,
			List<Integer> indices, boolean inverse) {

		if (!trimUntainted) {
			return FSAAutomaton.makeAnyString();
		}

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

			FSAAutomaton succAuto = deco.get(succ);
			if (succAuto == null) {
				throw new RuntimeException("SNH");
			}
			if (!succAuto.isEmpty()) {
				return FSAAutomaton.makeAnyString();
			}
		}

		return FSAAutomaton.makeString("");
	}

	protected void dumpDotAuto(FSAAutomaton auto, String graphName, String path) {

		String baseFileName = path + "/" + graphName;

		(new File(path)).mkdir();

		String dotFileName = baseFileName + ".dot";
		Utils.writeToFile(auto.toDot(), dotFileName);
	}

	public static boolean isCustomSanit(AbstractNode node) {

		if (node instanceof NormalNode) {
			return false;
		} else if (node instanceof UninitializedNode) {
			return false;
		} else if (node instanceof BuiltinFunctionNode) {
			BuiltinFunctionNode opNode = (BuiltinFunctionNode) node;
			if (opNode.isBuiltin()) {
				AbstractCfgNode cfgNode = opNode.getCfgNode();
				if (cfgNode instanceof CallBuiltinFunction) {
					CallBuiltinFunction callBuiltin = (CallBuiltinFunction) cfgNode;
					String funcName = callBuiltin.getFunctionName();
					if (funcName.equals("ereg_replace") || funcName.equals("preg_replace")
							|| funcName.equals("str_replace")) {

						return true;

					} else {
						return false;
					}

				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (node instanceof CompleteGraphNode) {
			throw new RuntimeException("SNH");
		} else {
			throw new RuntimeException("SNH");
		}
	}

	public boolean isIneffective(AbstractNode customSanit, Map<AbstractNode, FSAAutomaton> deco) {

		FSAAutomaton auto = deco.get(customSanit);
		if (auto == null) {
			return true;
		}

		FSAAutomaton intersection = auto.intersect(this.undesir);
		if (!intersection.isEmpty()) {
			return true;
		} else {
			return false;
		}

	}

	public static List<AbstractNode> findCustomSanit(DependencyGraph depGraph) {
		List<AbstractNode> retMe = new LinkedList<AbstractNode>();
		for (AbstractNode node : depGraph.getNodes()) {
			if (isCustomSanit(node)) {
				retMe.add(node);
			}
		}
		return retMe;
	}

	private TrimInfo checkTrim(AbstractNode node) {

		TrimInfo retMe = new TrimInfo();

		if (node instanceof BuiltinFunctionNode) {
			BuiltinFunctionNode opNode = (BuiltinFunctionNode) node;
			if (opNode.isBuiltin()) {
				String opName = opNode.getName();
				if (opName.equals("preg_replace") || opName.equals("ereg_replace") || opName.equals("str_replace")) {
					retMe.addNoTrim(0);
				}
			}
		}

		return retMe;
	}

	private class TrimInfo {

		private List<Integer> trim;
		private List<Integer> noTrim;
		private boolean defaultTrim;

		TrimInfo() {
			this.defaultTrim = true;
			this.trim = new LinkedList<Integer>();
			this.noTrim = new LinkedList<Integer>();
		}

		void setDefaultTrim(boolean defaultTrim) {
			this.defaultTrim = defaultTrim;
		}

		@SuppressWarnings("unused")
		void addTrim(int i) {
			this.trim.add(i);
		}

		void addNoTrim(int i) {
			this.noTrim.add(i);
		}

		boolean mayTrim(int i) {
			if (trim.contains(i)) {
				return true;
			} else if (noTrim.contains(i)) {
				return false;
			} else {
				return defaultTrim;
			}
		}
	}

}
