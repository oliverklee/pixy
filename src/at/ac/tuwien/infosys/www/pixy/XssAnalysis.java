package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.*;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;
import at.ac.tuwien.infosys.www.pixy.sanitation.AbstractSanitationAnalysis;

import java.util.*;

/**
 * XSS detection.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class XssAnalysis extends AbstractVulnerabilityAnalysis {

    private int dependencyGraphCount;
    private int vulnerabilityCount;
    private List<Integer> lineNumbersOfVulnerabilities;

    public XssAnalysis(DependencyAnalysis dependencyAnalysis) {
        super(dependencyAnalysis);
    }

    /**
     * Detects vulnerabilities and returns a list with the line numbers of the detected vulnerabilities.
     *
     * How it works:
     *
     * - extracts the "relevant subgraph" (see there for an explanation)
     * - this relevant subgraph has the nice property that we can check for
     * a vulnerability by simply looking at the remaining <uninit> nodes:
     * if all these nodes belong to variables that are initially harmless,
     * everything is OK; otherwise, we have a vulnerability
     *
     * @return the line numbers of the detected vulnerabilities
     */
    public List<Integer> detectVulnerabilities() {
        System.out.println();
        System.out.println("*****************");
        System.out.println("XSS Analysis BEGIN");
        System.out.println("*****************");
        System.out.println();

        lineNumbersOfVulnerabilities = new LinkedList<>();

        List<Sink> sinks = this.collectSinks();
        Collections.sort(sinks);

        System.out.println("Number of sinks: " + sinks.size());
        System.out.println();

        System.out.println("XSS Analysis Output");
        System.out.println("--------------------");
        System.out.println();

        // for the web interface
        StringBuilder sink2Graph = new StringBuilder();
        StringBuilder quickReport = new StringBuilder();

        String fileName = MyOptions.entryFile.getName();

        dependencyGraphCount = 0;
        vulnerabilityCount = 0;
        for (Sink sink : sinks) {
            detectVulnerabilitiesForSink(sink2Graph, quickReport, fileName, sink);
        }

        // initial sink count and final graph count may differ (e.g., if some sinks
        // are not reachable)
        if (MyOptions.optionV) {
            System.out.println("Total Graph Count: " + dependencyGraphCount);
        }
        System.out.println("Total Vuln Count: " + vulnerabilityCount);

        System.out.println();
        System.out.println("*****************");
        System.out.println("XSS Analysis END");
        System.out.println("*****************");
        System.out.println();

        if (MyOptions.optionW) {
            Utils.writeToFile(sink2Graph.toString(), MyOptions.graphPath + "/xssSinks2Urls.txt");
            Utils.writeToFile(quickReport.toString(), MyOptions.graphPath + "/xssQuickReport.txt");
        }

        return lineNumbersOfVulnerabilities;
    }

    private void detectVulnerabilitiesForSink(
        StringBuilder sink2Graph, StringBuilder quickReport, String fileName, Sink sink
    ) {
        for (DependencyGraph dependencyGraph : dependencyAnalysis.getDependencyGraphsForSink(sink)) {
            dependencyGraphCount++;

            String graphNameBase = "xss_" + fileName + "_" + dependencyGraphCount;

            if (!MyOptions.optionW) {
                dependencyGraph.dumpDot(graphNameBase + "_dep", MyOptions.graphPath, this.vulnerabilityAnalysisInformation);
            }

            detectVulnerabilitiesInDependencyGraphForSink(sink2Graph, quickReport, sink, dependencyGraph, graphNameBase);
        }
    }

    private void detectVulnerabilitiesInDependencyGraphForSink(
        StringBuilder sink2Graph, StringBuilder quickReport, Sink sink, DependencyGraph dependencyGraph, String graphNameBase
    ) {
        DependencyGraph relevantSubgraph = this.getRelevant(dependencyGraph);

        Map<UninitializedNode, InitialTaint> dangerousUninitializedNodes
            = this.findDangerousUninitializedNodes(relevantSubgraph);

        if (dangerousUninitializedNodes.isEmpty()) {
            return;
        }

        relevantSubgraph.reduceWithLeaves(dangerousUninitializedNodes.keySet());

        Set<? extends AbstractNode> fillUs;
        if (MyOptions.option_V) {
            relevantSubgraph.removeTemporaries();
            fillUs = relevantSubgraph.removeUninitNodes();
        } else {
            fillUs = dangerousUninitializedNodes.keySet();
        }

        vulnerabilityCount++;
        NormalNode root = dependencyGraph.getRoot();
        AbstractCfgNode cfgNode = root.getCfgNode();
        lineNumbersOfVulnerabilities.add(cfgNode.getOrigLineno());
        System.out.println("Vulnerability detected!");
        if (dangerousUninitializedNodes.values().contains(InitialTaint.ALWAYS)) {
            System.out.println("- unconditional");
        } else {
            System.out.println("- conditional on register_globals=on");
        }
        System.out.println("- " + cfgNode.getLoc());

        System.out.println("- Graph: xss" + dependencyGraphCount);
        relevantSubgraph.dumpDot(graphNameBase + "_min", MyOptions.graphPath, fillUs, this.vulnerabilityAnalysisInformation);
        System.out.println();

        if (MyOptions.optionW) {
            sink2Graph.append(sink.getLineNo());
            sink2Graph.append(":");
            sink2Graph.append(graphNameBase + "_min");
            sink2Graph.append("\n");

            quickReport.append("Line ");
            quickReport.append(sink.getLineNo());
            quickReport.append("\nSources:\n");
            for (AbstractNode leafX : relevantSubgraph.getLeafNodes()) {
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

    /**
     * Alternative to detectVulnerabilities;
     *
     * Returns those DependencyGraphs for which a vulnerability was detected.
     *
     * @return
     */
    public VulnerabilityInformation detectAlternative() {
        VulnerabilityInformation dependencyGraphsWithVulnerabilities = new VulnerabilityInformation();

        List<Sink> sinks = this.collectSinks();
        Collections.sort(sinks);

        int graphCount = 0;
        int totalPathCount = 0;
        int basicPathCount = 0;
        int hasCustomSanitationCount = 0;
        int customSanitationThrownAwayCount = 0;
        for (Sink sink : sinks) {
            Collection<DependencyGraph> dependencyGraphs = dependencyAnalysis.getDependencyGraphsForSink(sink);

            for (DependencyGraph dependencyGraph : dependencyGraphs) {
                graphCount++;

                DependencyGraph relevantSubgraph = this.getRelevant(dependencyGraph);
                Map<UninitializedNode, InitialTaint> dangerousUninitializedNodes
                    = this.findDangerousUninitializedNodes(relevantSubgraph);

                boolean tainted;
                if (dangerousUninitializedNodes.isEmpty()) {
                    tainted = false;
                } else {
                    tainted = true;

                    relevantSubgraph.reduceWithLeaves(dangerousUninitializedNodes.keySet());

                    dependencyGraphsWithVulnerabilities.addDepGraph(dependencyGraph, relevantSubgraph);
                }

                if (MyOptions.countPaths) {
                    int pathNum = dependencyGraph.countPaths();
                    totalPathCount += pathNum;
                    if (tainted) {
                        basicPathCount += pathNum;
                    }
                }

                if (!AbstractSanitationAnalysis.findCustomSanit(dependencyGraph).isEmpty()) {
                    hasCustomSanitationCount++;
                    if (!tainted) {
                        customSanitationThrownAwayCount++;
                    }
                }
            }
        }

        dependencyGraphsWithVulnerabilities.setInitialGraphCount(graphCount);
        dependencyGraphsWithVulnerabilities.setTotalPathCount(totalPathCount);
        dependencyGraphsWithVulnerabilities.setBasicPathCount(basicPathCount);
        dependencyGraphsWithVulnerabilities.setCustomSanitCount(hasCustomSanitationCount);
        dependencyGraphsWithVulnerabilities.setCustomSanitThrownAwayCount(customSanitationThrownAwayCount);

        return dependencyGraphsWithVulnerabilities;
    }

    /**
     * Checks if the given node (inside the given function) is a sensitive sink.
     *
     * If it is a sink, adds an appropriate sink object to the given list.
     *
     * @param cfgNode the node to check for whether it is a sink
     * @param traversedFunction
     * @param sinks the current sink list to which the node should be added
     */
    protected void checkForSink(AbstractCfgNode cfgNode, TacFunction traversedFunction, List<Sink> sinks) {
        if (cfgNode instanceof Echo) {
            processEchoSink((Echo) cfgNode, traversedFunction, sinks);
        } else if (cfgNode instanceof CallBuiltinFunction) {
            processCallBuiltinFunctionPotentialSink((CallBuiltinFunction) cfgNode, traversedFunction, sinks);
        } else if (cfgNode instanceof CallPreparation) {
            processCallPreparationPotentialSink((CallPreparation) cfgNode, traversedFunction, sinks);
        }
    }

    private void processEchoSink(Echo echoNode, TacFunction traversedFunction, List<Sink> sinks) {
        Sink sink = new Sink(echoNode, traversedFunction);
        sink.addSensitivePlace(echoNode.getPlace());

        sinks.add(sink);
    }

    private void processCallBuiltinFunctionPotentialSink(
        CallBuiltinFunction builtinFunctionNode, TacFunction traversedFunction, List<Sink> sinks
    ) {
        String functionName = builtinFunctionNode.getFunctionName();

        checkForSinkHelper(
            functionName, builtinFunctionNode, builtinFunctionNode.getParamList(), traversedFunction, sinks
        );
    }

    private void processCallPreparationPotentialSink(
        CallPreparation callPreparationNode, TacFunction traversedFunction, List<Sink> sinks
    ) {
        String functionName = callPreparationNode.getFunctionNamePlace().toString();

        checkForSinkHelper(functionName, callPreparationNode, callPreparationNode.getParamList(), traversedFunction, sinks);
    }

    /**
     * Later: This method looks very similar in all client analyses. Possibility to reduce code redundancy.
     *
     * @param functionName
     * @param cfgNode
     * @param paramList
     * @param traversedFunction
     * @param sinks
     */
    private void checkForSinkHelper(
        String functionName, AbstractCfgNode cfgNode, List<TacActualParameter> paramList, TacFunction traversedFunction,
        List<Sink> sinks
    ) {
        if (this.vulnerabilityAnalysisInformation.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            Set<Integer> indexList = this.vulnerabilityAnalysisInformation.getSinks().get(functionName);
            if (indexList == null) {
                // special treatment is necessary here
                if (functionName.equals("printf")) {
                    // none of the arguments to printf must be tainted
                    for (TacActualParameter param : paramList) {
                        sink.addSensitivePlace(param.getPlace());
                    }
                    sinks.add(sink);
                }
            } else {
                for (Integer index : indexList) {
                    if (paramList.size() > index) {
                        sink.addSensitivePlace(paramList.get(index).getPlace());
                        // add this sink to the list of sensitive sinks
                        sinks.add(sink);
                    }
                }
            }
        }
    }
}