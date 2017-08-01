package at.ac.tuwien.infosys.www.pixy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class FileAnalysis extends AbstractVulnerabilityAnalysis {

	public FileAnalysis(DependencyAnalysis depAnalysis) {
		super(depAnalysis);
	}

	public List<Integer> detectVulns() {

		System.out.println();
		System.out.println("*****************");
		System.out.println("File Analysis BEGIN");
		System.out.println("*****************");
		System.out.println();

		List<Integer> retMe = new LinkedList<Integer>();

		List<Sink> sinks = this.collectSinks();

		System.out.println("Creating DepGraphs for " + sinks.size() + " sinks...");
		System.out.println();
		Collection<DependencyGraph> depGraphs = depAnalysis.getDepGraphs(sinks);

		System.out.println("File Capab Analysis Output");
		System.out.println("----------------------------");
		System.out.println();

		int graphcount = 0;
		for (DependencyGraph depGraph : depGraphs) {
			graphcount++;

			DependencyGraph stringGraph = new DependencyGraph(depGraph);
			if (depGraph == null || depGraph.getRoot() == null || depGraph.getRoot().getCfgNode() == null) {
				continue;
			}
			NormalNode root = depGraph.getRoot();
			AbstractCfgNode cfgNode = root.getCfgNode();

			depGraph = null; 

			Automaton auto = this.toAutomaton(stringGraph);

			String fileName = cfgNode.getFileName();
			if (MyOptions.optionB) {
				fileName = Utils.basename(fileName);
			}
			System.out.println("Line:  " + cfgNode.getOriginalLineNumber());
			System.out.println("File:  " + fileName);
			System.out.println("Graph: file" + graphcount);

			this.dumpDotAuto(auto, "file" + graphcount, MyOptions.graphPath);
		}
		if (MyOptions.optionV)
			System.out.println("Total Graph Count: " + graphcount);

		System.out.println();
		System.out.println("*****************");
		System.out.println("File Analysis END");
		System.out.println("*****************");
		System.out.println();

		return retMe;

	}

	public VulnerabilityInformation detectAlternative() {
		throw new RuntimeException("not yet");
	}

	private void dumpDotAuto(Automaton auto, String graphName, String path) {

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
				System.out.println("IS FINITE");
				System.out.println();
				for (String finite : finiteStrings) {
					System.out.println("Finite BEGIN");
					System.out.println(finite);
					System.out.println("Finite END");
					System.out.println();
				}
			}

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

	private Automaton toAutomaton(DependencyGraph depGraph) {
		depGraph.eliminateCycles();
		AbstractNode root = depGraph.getRoot();
		Map<AbstractNode, Automaton> deco = new HashMap<AbstractNode, Automaton>();
		Set<AbstractNode> visited = new HashSet<AbstractNode>();
		this.decorate(root, deco, visited, depGraph);
		Automaton rootDeco = deco.get(root).clone();
		return rootDeco;
	}

	private void decorate(AbstractNode node, Map<AbstractNode, Automaton> deco, Set<AbstractNode> visited,
			DependencyGraph depGraph) {

		visited.add(node);

		List<AbstractNode> successors = depGraph.getSuccessors(node);
		if (successors != null && !successors.isEmpty()) {
			for (AbstractNode succ : successors) {
				if (!visited.contains(succ) && deco.get(succ) == null) {
					decorate(succ, deco, visited, depGraph);
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

					System.err.println(normalNode.getCfgNode().getLoc());
					throw new RuntimeException("SNH: " + place);
				}
			} else {

				for (AbstractNode succ : successors) {
					Automaton succAuto = deco.get(succ);
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
			auto = Automaton.makeAnyString(Transition.Taint.Directly);

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

			} else if (pre instanceof BuiltinFunctionNode) {
				throw new RuntimeException("not yet");
			} else if (pre instanceof CompleteGraphNode) {
				auto = Automaton.makeAnyString(Transition.Taint.Directly);
			} else {
				throw new RuntimeException("SNH");
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
		if (opName.equals(".")) {
			for (AbstractNode succ : successors) {
				Automaton succAuto = deco.get(succ);
				if (retMe == null) {
					retMe = succAuto;
				} else {
					retMe = retMe.concatenate(succAuto);
				}
			}
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
}
