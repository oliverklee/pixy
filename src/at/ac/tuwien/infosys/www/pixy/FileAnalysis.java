package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.*;
import at.ac.tuwien.infosys.www.pixy.automaton.Automaton;
import at.ac.tuwien.infosys.www.pixy.automaton.Transition;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * This class extracts strings used in file access functions.
 *
 * Note: This class will be instantiated via reflection in GenericTaintAnalysis.createAnalysis. It is registered in
 * MyOptions.VulnerabilityAnalysisInformation.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class FileAnalysis extends AbstractVulnerabilityAnalysis {
    public FileAnalysis(DependencyAnalysis dependencyAnalysis) {
        super(dependencyAnalysis);
    }

    /**
     * Detects vulnerabilities and returns a list with the line numbers of the detected vulnerabilities.
     *
     * @return the line numbers of the detected vulnerabilities
     */
    public List<Integer> detectVulnerabilities() {
        System.out.println();
        System.out.println("*****************");
        System.out.println("File Analysis BEGIN");
        System.out.println("*****************");
        System.out.println();

        List<Integer> lineNumbersOfVulnerabilities = new LinkedList<>();

        List<Sink> sinks = this.collectSinks();

        System.out.println("Creating DepGraphs for " + sinks.size() + " sinks...");
        System.out.println();
        Collection<DependencyGraph> dependencyGraphs = dependencyAnalysis.getDependencyGraphs(sinks);

        System.out.println("File Capab Analysis Output");
        System.out.println("----------------------------");
        System.out.println();

        int numberOfDependencyGraphs = 0;
        for (DependencyGraph dependencyGraph : dependencyGraphs) {
            numberOfDependencyGraphs++;

            DependencyGraph stringGraph = new DependencyGraph(dependencyGraph);
            NormalNode root = dependencyGraph.getRootNode();
            AbstractCfgNode cfgNode = root.getCfgNode();

            dependencyGraph = null;    // don't touch this one

            Automaton auto = this.toAutomaton(stringGraph);

            String fileName = cfgNode.getFileName();
            if (MyOptions.optionB) {
                fileName = Utils.basename(fileName);
            }
            System.out.println("Line:  " + cfgNode.getOrigLineno());
            System.out.println("File:  " + fileName);
            System.out.println("Graph: file" + numberOfDependencyGraphs);

            this.dumpDotAuto(auto, "file" + numberOfDependencyGraphs, MyOptions.graphPath);
        }

        // initial sink count and final graph count may differ (e.g., if some sinks
        // are not reachable)
        if (MyOptions.optionV) {
            System.out.println("Total Graph Count: " + numberOfDependencyGraphs);
        }

        System.out.println();
        System.out.println("*****************");
        System.out.println("File Analysis END");
        System.out.println("*****************");
        System.out.println();

        return lineNumbersOfVulnerabilities;
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
                List<String> finiteStrings = new LinkedList<>(finiteStringsSet);
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

    /**
     * Returns the automaton representation of the given dependency graph.
     *
     * This is done by decorating the nodes of the graph with automata bottom-up, and returning the automaton that
     * eventually decorates the root.
     *
     * Beware: This also eliminates cycles!
     *
     * @param dependencyGraph
     *
     * @return
     */
    private Automaton toAutomaton(DependencyGraph dependencyGraph) {
        dependencyGraph.eliminateCycles();
        AbstractNode root = dependencyGraph.getRootNode();
        Map<AbstractNode, Automaton> deco = new HashMap<>();
        Set<AbstractNode> visited = new HashSet<>();
        this.decorate(root, deco, visited, dependencyGraph);
        Automaton rootDeco = deco.get(root).clone();

        // BEWARE: minimization can lead to an automaton that is less human-readable
        //rootDeco.minimize();
        return rootDeco;
    }

    /**
     * Decorates the given node (and all its successors) with an automaton.
     *
     * @param node
     * @param deco
     * @param visited
     * @param dependencyGraph
     */
    private void decorate(
        AbstractNode node, Map<AbstractNode, Automaton> deco, Set<AbstractNode> visited, DependencyGraph dependencyGraph
    ) {
        visited.add(node);

        // if this node has successors, decorate them first (if not done yet)
        List<AbstractNode> successors = dependencyGraph.getSuccessors(node);
        if (successors != null && !successors.isEmpty()) {
            for (AbstractNode succ : successors) {
                if (!visited.contains(succ) && deco.get(succ) == null) {
                    decorate(succ, deco, visited, dependencyGraph);
                }
            }
        }

        // now that all successors are decorated, we can decorate this node
        Automaton auto = null;
        if (node instanceof NormalNode) {
            NormalNode normalNode = (NormalNode) node;
            if (successors == null || successors.isEmpty()) {
                // this should be a string leaf node
                AbstractTacPlace place = normalNode.getPlace();
                if (place.isLiteral()) {
                    auto = Automaton.makeString(place.toString());
                } else {
                    // this case should not happen any longer (now that
                    // we have "uninit" nodes, see below)
                    System.err.println(normalNode.getCfgNode().getLoc());
                    throw new RuntimeException("SNH: " + place);
                }
            } else {
                // this is an interior node, not a leaf node;
                // the automaton for this node is the union of all the
                // successor automatas
                for (AbstractNode succ : successors) {
                    Automaton succAuto = deco.get(succ);
                    if (auto == null) {
                        auto = succAuto;  // cloning not necessary here
                    } else {
                        auto = auto.union(succAuto);
                    }
                }
            }
        } else if (node instanceof BuiltinFunctionNode) {
            auto = this.makeAutomatonForOperationNode((BuiltinFunctionNode) node, deco, dependencyGraph);
        } else if (node instanceof CompleteGraphNode) {
            // conservative decision for SCCs
            auto = Automaton.makeAnyString(Transition.Taint.Directly);
        } else if (node instanceof UninitializedNode) {
            // retrieve predecessor
            Set<AbstractNode> predecessors = dependencyGraph.getPredecessors(node);
            if (predecessors.size() != 1) {
                throw new RuntimeException("SNH");
            }
            AbstractNode pre = predecessors.iterator().next();

            if (pre instanceof NormalNode) {
                NormalNode preNormal = (NormalNode) pre;

                switch (this.initiallyTainted(preNormal.getPlace())) {
                    case ALWAYS:
                    case IF_REGISTER_GLOBALS:
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
                // conservative decision for SCCs
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

    /**
     * Returns an automaton for the given operation node.
     *
     * @param node
     * @param deco
     * @param dependencyGraph
     *
     * @return
     */
    private Automaton makeAutomatonForOperationNode(
        BuiltinFunctionNode node, Map<AbstractNode, Automaton> deco, DependencyGraph dependencyGraph)
    {
        List<AbstractNode> successors = dependencyGraph.getSuccessors(node);
        if (successors == null) {
            successors = new LinkedList<>();
        }

        Automaton retMe = null;

        String opName = node.getName();
        if (opName.equals(".")) {
            // CONCAT
            for (AbstractNode successor : successors) {
                Automaton successorAutomaton = deco.get(successor);
                if (retMe == null) {
                    retMe = successorAutomaton;
                } else {
                    retMe = retMe.concatenate(successorAutomaton);
                }
            }
        } else {
            // conservative decision for operations that have not been
            // modeled yet: .*
            retMe = Automaton.makeAnyString(Transition.Taint.Directly);
        }

        return retMe;
    }

    /**
     * Checks if the given node (inside the given function) is a sensitive sink.
     *
     * Adds an appropriate sink object to the given list if it is a sink.
     *
     * @param cfgNodeX
     * @param traversedFunction
     * @param sinks
     */
    protected void checkForSink(AbstractCfgNode cfgNodeX, TacFunction traversedFunction, List<Sink> sinks) {
        if (cfgNodeX instanceof CallBuiltinFunction) {
            // builtin function sinks
            CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
            String functionName = cfgNode.getFunctionName();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
        } else if (cfgNodeX instanceof CallPreparation) {
            // user-defined custom sinks
            CallPreparation cfgNode = (CallPreparation) cfgNodeX;
            String functionName = cfgNode.getFunctionNamePlace().toString();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
        }
    }

    private void checkForSinkHelper(
        String functionName, AbstractCfgNode cfgNode, List<TacActualParameter> paramList, TacFunction traversedFunction,
        List<Sink> sinks
    ) {
        if (this.vulnerabilityAnalysisInformation.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            for (Integer param : this.vulnerabilityAnalysisInformation.getSinks().get(functionName)) {
                if (paramList.size() > param) {
                    sink.addSensitivePlace(paramList.get(param).getPlace());
                    // add this sink to the list of sensitive sinks
                    sinks.add(sink);
                }
            }
        }
    }
}