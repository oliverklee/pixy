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

        List<Integer> lineNumbersOfVulnerabilities = new LinkedList<>();

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

        int numberOfDependencyGraphs = 0;
        int numberOfVulnerabilities = 0;
        for (Sink sink : sinks) {
            Collection<DependencyGraph> dependencyGraphs = dependencyAnalysis.getDepGraph(sink);

            for (DependencyGraph dependencyGraph : dependencyGraphs) {
                numberOfDependencyGraphs++;

                String graphNameBase = "xss_" + fileName + "_" + numberOfDependencyGraphs;

                if (!MyOptions.optionW) {
                    dependencyGraph.dumpDot(graphNameBase + "_dep", MyOptions.graphPath, this.vulnerabilityAnalysisInformation);
                }

                // create the relevant subgraph
                DependencyGraph relevant = this.getRelevant(dependencyGraph);

                // find those uninit nodes that are dangerous
                Map<UninitializedNode, InitialTaint> dangerousUninit = this.findDangerousUninitialized(relevant);

                // if there are any dangerous uninit nodes...
                if (!dangerousUninit.isEmpty()) {

                    // make the relevant subgraph smaller
                    relevant.reduceWithLeaves(dangerousUninit.keySet());

                    Set<? extends AbstractNode> fillUs;
                    if (MyOptions.option_V) {
                        relevant.removeTemporaries();
                        fillUs = relevant.removeUninitNodes();
                    } else {
                        fillUs = dangerousUninit.keySet();
                    }

                    numberOfVulnerabilities++;
                    NormalNode root = dependencyGraph.getRoot();
                    AbstractCfgNode cfgNode = root.getCfgNode();
                    lineNumbersOfVulnerabilities.add(cfgNode.getOrigLineno());
                    System.out.println("Vulnerability detected!");
                    if (dangerousUninit.values().contains(InitialTaint.ALWAYS)) {
                        System.out.println("- unconditional");
                    } else {
                        System.out.println("- conditional on register_globals=on");
                    }
                    System.out.println("- " + cfgNode.getLoc());

                    System.out.println("- Graph: xss" + numberOfDependencyGraphs);
                    relevant.dumpDot(graphNameBase + "_min", MyOptions.graphPath, fillUs, this.vulnerabilityAnalysisInformation);
                    System.out.println();

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

        // initial sink count and final graph count may differ (e.g., if some sinks
        // are not reachable)
        if (MyOptions.optionV) {
            System.out.println("Total Graph Count: " + numberOfDependencyGraphs);
        }
        System.out.println("Total Vuln Count: " + numberOfVulnerabilities);

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

    /**
     * Alternative to detectVulnerabilities;
     *
     * Returns those DependencyGraphs for which a vulnerability was detected.
     *
     * @return
     */
    public VulnerabilityInformation detectAlternative() {
        // will contain depgraphs for which a vulnerability was detected
        VulnerabilityInformation retMe = new VulnerabilityInformation();

        // collect sinks
        List<Sink> sinks = this.collectSinks();
        Collections.sort(sinks);

        int graphcount = 0;
        int totalPathCount = 0;
        int basicPathCount = 0;
        int hasCustomSanitCount = 0;
        int customSanitThrownAwayCount = 0;
        for (Sink sink : sinks) {
            Collection<DependencyGraph> dependencyGraphs = dependencyAnalysis.getDepGraph(sink);

            for (DependencyGraph dependencyGraph : dependencyGraphs) {
                graphcount++;

                // create the relevant subgraph
                DependencyGraph relevant = this.getRelevant(dependencyGraph);

                // find those uninit nodes that are dangerous
                Map<UninitializedNode, InitialTaint> dangerousUninit = this.findDangerousUninitialized(relevant);

                // if there are any dangerous uninit nodes...
                boolean tainted = false;
                if (!dangerousUninit.isEmpty()) {
                    tainted = true;

                    // make the relevant subgraph smaller
                    relevant.reduceWithLeaves(dangerousUninit.keySet());

                    retMe.addDepGraph(dependencyGraph, relevant);
                }

                if (MyOptions.countPaths) {
                    int pathNum = dependencyGraph.countPaths();
                    totalPathCount += pathNum;
                    if (tainted) {
                        basicPathCount += pathNum;
                    }
                }

                if (!AbstractSanitationAnalysis.findCustomSanit(dependencyGraph).isEmpty()) {
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
        if (cfgNodeX instanceof Echo) {
            // echo() or print()
            Echo cfgNode = (Echo) cfgNodeX;

            // create sink object for this node
            Sink sink = new Sink(cfgNode, traversedFunction);
            sink.addSensitivePlace(cfgNode.getPlace());

            // add it to the list of sensitive sinks
            sinks.add(sink);
        } else if (cfgNodeX instanceof CallBuiltinFunction) {
            // builtin function sinks
            CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
            String functionName = cfgNode.getFunctionName();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
        } else if (cfgNodeX instanceof CallPreparation) {
            CallPreparation cfgNode = (CallPreparation) cfgNodeX;
            String functionName = cfgNode.getFunctionNamePlace().toString();

            // user-defined custom sinks
            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
        }
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