package at.ac.tuwien.infosys.www.pixy.sanitation;

import at.ac.tuwien.infosys.www.pixy.AbstractVulnerabilityAnalysis;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.VulnerabilityInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.*;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;

import java.io.File;
import java.util.*;

/**
 * Superclass for sanitation analyses.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractSanitationAnalysis extends AbstractVulnerabilityAnalysis {
    /**
     * If this flag is active, untainted values (most notably: static strings) are treated as empty strings during
     * dependency graph decoration.
     */
    private boolean trimUntainted = !MyOptions.optionR;

    /** automaton representing the undesired stuff */
    protected FSAAutomaton undesiredAutomaton;

    /** "xss", "sql", ... */
    protected String name;

    protected AbstractSanitationAnalysis(String name, DependencyAnalysis dependencyAnalysis, FSAAutomaton undesired) {
        super(dependencyAnalysis);
        this.name = name;
        this.undesiredAutomaton = undesired;
    }

    /**
     * Detects vulnerabilities and returns a list with the line numbers of the detected vulnerabilities.
     *
     * @return the line numbers of the detected vulnerabilities
     */
    public List<Integer> detectVulnerabilities(AbstractVulnerabilityAnalysis dependencyClient) {
        System.out.println();
        System.out.println("*****************");
        System.out.println(name.toUpperCase() + " Sanit Analysis BEGIN");
        System.out.println("*****************");
        System.out.println();

        // let the basic analysis do the preliminary work
        VulnerabilityInformation vulnerabilityInformation = dependencyClient.detectAlternative();
        List<DependencyGraph> vulnDependencyGraphs = vulnerabilityInformation.getDependencyGraphs();
        List<DependencyGraph> minDependencyGraphs = vulnerabilityInformation.getDependencyGraphsMin();

        // stats
        int scanned = vulnerabilityInformation.getInitialGraphCount();
        int reported_by_basic = vulnDependencyGraphs.size();
        int have_sanit = 0;
        int no_sanit = 0;
        int sure_vuln_1 = 0;
        int sure_vuln_2 = 0;
        int possible_vuln = 0;
        int eliminated = 0;

        System.out.println(name.toUpperCase() + " Sanit Analysis Output");
        System.out.println("--------------------");
        System.out.println();

        // dump the automaton that represents the undesired stuff
        this.dumpDotAuto(this.undesiredAutomaton, "undesired_" + name, MyOptions.graphPath);

        // info for dynamic analysis
        StringBuilder dynInfo = new StringBuilder();

        int dynpathcount = 0;

        int graphcount = 0;
        Iterator<DependencyGraph> minIter = minDependencyGraphs.iterator();
        for (DependencyGraph dependencyGraph : vulnDependencyGraphs) {
            graphcount++;

            DependencyGraph minGraph = minIter.next();

            // in any case, dump the vulnerable depgraphs
            dependencyGraph.dumpDot(name + "sanitation" + graphcount + "i", MyOptions.graphPath, dependencyGraph.getUninitNodes(), this.vulnerabilityAnalysisInformation);
            minGraph.dumpDot(name + "sanitation" + graphcount + "m", MyOptions.graphPath, dependencyGraph.getUninitNodes(), this.vulnerabilityAnalysisInformation);

            AbstractCfgNode cfgNode = dependencyGraph.getRootNode().getCfgNode();

            // retrieve custom sanitization routines from the minGraph
            List<AbstractNode> customSanitNodes = findCustomSanit(minGraph);

            if (customSanitNodes.isEmpty()) {
                // we don't have to perform our detailed sanitization analysis
                // if no custom sanitization is performed
                System.out.println("No Sanitization!");
                System.out.println("- " + cfgNode.getLoc());
                System.out.println("- Graphs: " + name + "sanitation" + graphcount);
                sure_vuln_1++;
                no_sanit++;
                continue;
            }
            have_sanit++;

            DependencyGraph workGraph = new DependencyGraph(dependencyGraph);

            Map<AbstractNode, FSAAutomaton> deco = new HashMap<>();
            FSAAutomaton auto = this.toAutomatonSanit(workGraph, dependencyGraph, deco);

            // intersect this automaton with the undesired stuff;
            // if the intersection is empty, it means that we are safe!
            FSAAutomaton intersection = auto.intersect(this.undesiredAutomaton);
            if (!intersection.isEmpty()) {

                // dump the intersection automaton:
                // represents counterexamples!
                this.dumpDotAuto(intersection, name + "sanitation" + graphcount + "intersect", MyOptions.graphPath);

                // create a graph that is further minimized to the sanitization routines
                // (regardless of the effectiveness of the applied sanitization)
                DependencyGraph sanitMinGraph = new DependencyGraph(minGraph);
                sanitMinGraph.reduceToInnerNodes(customSanitNodes);

                // and now reduce this graph to the ineffective sanitization routines
                int ineffBorder = sanitMinGraph.reduceToIneffectiveSanitation(deco, this);

                if (ineffBorder != 0) {

                    System.out.println("Ineffective Sanitization!");
                    System.out.println("- " + cfgNode.getLoc());
                    System.out.println("- Graphs: " + name + "sanitation" + graphcount);
                    possible_vuln++;

                    // dump the minimized graph
                    sanitMinGraph.dumpDot(name + "sanitation" + graphcount + "mm", MyOptions.graphPath, dependencyGraph.getUninitNodes(), this.vulnerabilityAnalysisInformation);

                    dynInfo.append("SINK:\n");
                    dynInfo.append(sanitMinGraph.getRootNode().toString());
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
                        // System.out.println("- paths: " + paths);
                    }
                } else {
                    // this means that this graph contains custom sanitization routines,
                    // but they are not responsible for the vulnerability
                    System.out.println("No Sanitization!");
                    System.out.println("- " + cfgNode.getLoc());
                    System.out.println("- Graphs: " + name + "sanitation" + graphcount);
                    sure_vuln_2++;
                }
            } else {
                // eliminated false positive!
                eliminated++;
            }

            this.dumpDotAuto(auto, name + "sanitation" + graphcount + "auto", MyOptions.graphPath);
        }

        Utils.writeToFile(dynInfo.toString(), MyOptions.graphPath + "/" + name + "info.txt");

        System.out.println();
        System.out.println("Scanned depgraphs: " + scanned);
        System.out.println("Depgraphs reported by basic analysis: " + reported_by_basic);
        System.out.println();
        if (MyOptions.countPaths) {
            System.out.println("Total initial paths: " + vulnerabilityInformation.getTotalPathCount());
            System.out.println("Total basic paths: " + vulnerabilityInformation.getBasicPathCount());
            System.out.println("Paths for dynamic analysis: " + dynpathcount);
            System.out.println();
        }
        System.out.println("Total DepGraphs with custom sanitization: " + vulnerabilityInformation.getCustomSanitCount());
        System.out.println("DepGraphs with custom sanitization thrown away by basic analysis: " +
            vulnerabilityInformation.getCustomSanitThrownAwayCount());
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

        return new LinkedList<>();
    }

    /**
     * Returns the automaton representation of the given dependency graph.
     *
     * This is done by decorating the nodes of the graph with automata bottom-up, and returning the automaton that
     * eventually decorates the root.
     *
     * Beware: this also eliminates cycles!
     *
     * @param dependencyGraph
     * @param origDependencyGraph
     * @param deco
     *
     * @return
     */
    protected FSAAutomaton toAutomatonSanit(
        DependencyGraph dependencyGraph, DependencyGraph origDependencyGraph, Map<AbstractNode, FSAAutomaton> deco
    ) {
        dependencyGraph.eliminateCycles();
        AbstractNode root = dependencyGraph.getRootNode();
        Set<AbstractNode> visited = new HashSet<>();
        this.decorateSanit(root, deco, visited, dependencyGraph, origDependencyGraph, true);
        FSAAutomaton rootDeco = deco.get(root).clone();

        return rootDeco;
    }

    /**
     * Decorates the given node (and all its successors) with an automaton.
     *
     * @param node
     * @param deco
     * @param visited
     * @param dependencyGraph
     * @param origDependencyGraph
     * @param trimAllowed
     */
    private void decorateSanit(
        AbstractNode node, Map<AbstractNode, FSAAutomaton> deco, Set<AbstractNode> visited,
        DependencyGraph dependencyGraph, DependencyGraph origDependencyGraph, boolean trimAllowed
    ) {
        visited.add(node);

        TrimInfo trimInfo;
        if (trimAllowed) {
            trimInfo = this.checkTrim(node);
        } else {
            trimInfo = new TrimInfo();
            trimInfo.setDefaultTrim(false);
        }

        // if this node has successors, decorate them first (if not done yet)
        List<AbstractNode> successors = dependencyGraph.getSuccessors(node);
        if (successors != null && !successors.isEmpty()) {
            int i = 0;
            for (AbstractNode succ : successors) {
                if (!visited.contains(succ) && deco.get(succ) == null) {
                    decorateSanit(succ, deco, visited, dependencyGraph, origDependencyGraph, trimInfo.mayTrim(i));
                }
                i++;
            }
        }

        // now that all successors are decorated, we can decorate this node
        FSAAutomaton auto = null;
        if (node instanceof NormalNode) {
            NormalNode normalNode = (NormalNode) node;
            if (successors == null || successors.isEmpty()) {
                // this should be a string leaf node
                AbstractTacPlace place = normalNode.getPlace();
                if (place.isLiteral()) {
                    if (trimUntainted && trimAllowed) {
                        auto = FSAAutomaton.makeString("");
                    } else {
                        auto = FSAAutomaton.makeString(place.toString());
                    }
                } else {
                    // this case should not happen any longer (now that
                    // we have "uninit" nodes, see below)
                    throw new RuntimeException("SNH: " + place + ", " + normalNode.getCfgNode().getFileName() + "," +
                        normalNode.getCfgNode().getOrigLineno());
                }
            } else {
                // this is an interior node, not a leaf node;
                // the automaton for this node is the union of all the
                // successor automatas
                for (AbstractNode succ : successors) {
                    if (succ == node) {
                        // a simple loop, can be ignored
                        continue;
                    }
                    FSAAutomaton succAuto = deco.get(succ);
                    if (succAuto == null) {
                        throw new RuntimeException("SNH");
                    }
                    if (auto == null) {
                        auto = succAuto;  // cloning not necessary here
                    } else {
                        auto = auto.union(succAuto);
                    }
                }
            }
        } else if (node instanceof BuiltinFunctionNode) {
            auto = this.makeAutoForOp((BuiltinFunctionNode) node, deco, dependencyGraph, trimAllowed);
        } else if (node instanceof CompleteGraphNode) {
            // for SCC nodes, we generate a coarse string approximation (.* automaton);
            // the taint value depends on the taint value of the successors:
            // if any of the successors is tainted in any way, we make the resulting
            // automaton tainted as well

            /*
             * this approach works under the assumption that the SCC contains
             * no "evil" functions (functions that always return a tainted value),
             * and that the direct successors of the SCC are not <uninit> nodes
             * (see there, below);
             * it is primarily based on the fact that SCCs can never contain
             * <uninit> nodes, because <uninit> nodes are always leaf nodes in the dependency
             * graph (since they have no successors);
             * under the above assumptions and observations, it is valid to
             * say that the taint value of an SCC node solely depends on
             * the taint values of its successors, and that is exactly what we
             * do here
             *
             */
            if (trimUntainted && trimAllowed) {
                auto = FSAAutomaton.makeString("");

                for (AbstractNode succ : successors) {
                    if (succ == node) {
                        // a simple loop, should be part of the SCC
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
                // this case can really happen (e.g.: dcpportal: advertiser.php, forums.php);

                // take a look at the "real" predecessors (i.e., take a look "into"
                // the SCC node): if there is exactly one predecessor, namely a
                // NormalNode, and if the contained place is initially untainted,
                // there is no danger from here; else: we will have to set it to tainted
                Set<AbstractNode> origPreds = origDependencyGraph.getPredecessors(node);
                if (origPreds.size() == 1) {
                    AbstractNode origPre = origPreds.iterator().next();
                    if (origPre instanceof NormalNode) {
                        NormalNode origPreNormal = (NormalNode) origPre;

                        switch (this.initiallyTainted(origPreNormal.getPlace())) {
                            case ALWAYS:
                            case IF_REGISTER_GLOBALS:
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
                    // conservative decision for this SCC
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

    /**
     * Returns an automaton for the given operation node.
     *
     * @param node
     * @param deco
     * @param dependencyGraph
     * @param trimAllowed
     *
     * @return
     */
    private FSAAutomaton makeAutoForOp(
        BuiltinFunctionNode node, Map<AbstractNode, FSAAutomaton> deco, DependencyGraph dependencyGraph,
        boolean trimAllowed
    ) {
        List<AbstractNode> successors = dependencyGraph.getSuccessors(node);
        if (successors == null) {
            successors = new LinkedList<>();
        }

        FSAAutomaton automaton = null;

        String opName = node.getName();

        List<Integer> multiList = new LinkedList<>();

        if (!node.isBuiltin()) {
            // call to function or method for which no definition
            // could be found
            AbstractCfgNode cfgNodeX = node.getCfgNode();
            if (cfgNodeX instanceof CallUnknownFunction) {
                CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;
                if (cfgNode.isMethod()) {
                    if (trimUntainted && trimAllowed) {
                        automaton = FSAAutomaton.makeString("");
                    } else {
                        automaton = FSAAutomaton.makeAnyString();
                    }
                } else {
                    automaton = FSAAutomaton.makeAnyString();
                }
            } else {
                throw new RuntimeException("SNH");
            }
        } else if (opName.equals(".")) {
            // CONCAT
            for (AbstractNode succ : successors) {
                FSAAutomaton succAuto = deco.get(succ);
                if (automaton == null) {
                    automaton = succAuto;
                } else {
                    automaton = automaton.concatenate(succAuto);
                }
            }

            // TRANSDUCIBLES ****************************************
        } else if (opName.equals("preg_replace")) {
            if (successors.size() < 3) {
                throw new RuntimeException("SNH");
            }
            FSAAutomaton searchAuto = deco.get(successors.get(0));
            FSAAutomaton replaceAuto = deco.get(successors.get(1));
            FSAAutomaton subjectAuto = deco.get(successors.get(2));

            // if the replacement is evil, be conservative
            if (trimUntainted && !replaceAuto.isEmpty()) {
                return FSAAutomaton.makeAnyString();
            }

            FSAAutomaton transduced = FSAUtils.reg_replace(searchAuto, replaceAuto,
                subjectAuto, true, node.getCfgNode());
            return transduced;
        } else if (opName.equals("ereg_replace")) {
            if (successors.size() < 3) {
                throw new RuntimeException("SNH");
            }
            FSAAutomaton searchAuto = deco.get(successors.get(0));
            FSAAutomaton replaceAuto = deco.get(successors.get(1));
            FSAAutomaton subjectAuto = deco.get(successors.get(2));

            // if the replacement is evil, be conservative
            if (trimUntainted && !replaceAuto.isEmpty()) {
                return FSAAutomaton.makeAnyString();
            }

            FSAAutomaton transduced = FSAUtils.reg_replace(searchAuto, replaceAuto,
                subjectAuto, false, node.getCfgNode());
            return transduced;
        } else if (opName.equals("str_replace")) {
            if (successors.size() < 3) {
                throw new RuntimeException("SNH");
            }
            FSAAutomaton searchAuto = deco.get(successors.get(0));
            FSAAutomaton replaceAuto = deco.get(successors.get(1));
            FSAAutomaton subjectAuto = deco.get(successors.get(2));

            // if the replacement is evil, be conservative
            if (trimUntainted && !replaceAuto.isEmpty()) {
                return FSAAutomaton.makeAnyString();
            }

            FSAAutomaton transduced = FSAUtils.str_replace(
                searchAuto, replaceAuto, subjectAuto, node.getCfgNode());
            return transduced;
        } else if (opName.equals("addslashes")) {
            if (successors.size() != 1) {
                throw new RuntimeException("SNH");
            }
            FSAAutomaton paramAuto = deco.get(successors.get(0));

            FSAAutomaton transduced = FSAUtils.addslashes(
                paramAuto, node.getCfgNode());
            return transduced;

            // WEAK SANITIZATION FUNCTIONS *******************************
            // ops that perform sanitization, but which are insufficient
            // in cases where the output is not enclosed by quotes in an SQL query
        } else if (isWeakSanitation(opName, multiList)) {
            if (trimUntainted && trimAllowed) {
                automaton = FSAAutomaton.makeString("");
            } else {
                automaton = FSAAutomaton.makeAnyString();
            }

            // STRONG SANITIZATION FUNCTIONS *******************************
            // e.g., ops that return numeric values
        } else if (isStrongSanitation(opName)) {
            if (trimUntainted && trimAllowed) {
                automaton = FSAAutomaton.makeString("");
            } else {
                automaton = FSAAutomaton.makeAnyString();
            }

            // EVIL FUNCTIONS ***************************************
            // take care: if you define evil functions, you must adjust
            // the treatment of SCC nodes in decorate()

            // MULTI-OR-DEPENDENCY **********************************
        } else if (isMultiDependencyOperation(opName, multiList)) {
            automaton = this.multiDependencyAutoSanit(successors, deco, multiList, false);
        } else if (isInverseMultiDependencyOperation(opName, multiList)) {
            automaton = this.multiDependencyAutoSanit(successors, deco, multiList, true);

            // CATCH-ALL ********************************************
        } else {
            System.out.println("Unmodeled builtin function (SQL-Sanit): " + opName);

            // conservative decision for operations that have not been
            // modeled yet: .*
            automaton = FSAAutomaton.makeAnyString();
        }

        return automaton;
    }

    /**
     * If trimUntainted == false: always returns .*
     * else:
     * - if all successors are empty: returns empty
     * - else: returns .*
     *
     * @param successors
     * @param deco
     * @param indices
     * @param inverse
     *
     * @return
     */
    private FSAAutomaton multiDependencyAutoSanit(
        List<AbstractNode> successors, Map<AbstractNode, FSAAutomaton> deco, List<Integer> indices, boolean inverse
    ) {
        if (!trimUntainted) {
            return FSAAutomaton.makeAnyString();
        }

        Set<Integer> indexSet = new HashSet<>(indices);

        int count = -1;
        for (AbstractNode succ : successors) {
            count++;

            // check if there is a dependency on this successor
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

    /**
     * Checks if the given node is a custom sanitization node.
     *
     * @param node
     *
     * @return
     */
    public static boolean isCustomSanit(AbstractNode node) {
        if (node instanceof NormalNode) {
            return false;
        } else if (node instanceof UninitializedNode) {
            return false;
        } else if (node instanceof BuiltinFunctionNode) {
            // check if this operation could be used for custom sanitization
            BuiltinFunctionNode builtinFunctionNode = (BuiltinFunctionNode) node;
            if (builtinFunctionNode.isBuiltin()) {
                AbstractCfgNode cfgNode = builtinFunctionNode.getCfgNode();
                if (cfgNode instanceof CallBuiltinFunction) {
                    CallBuiltinFunction callBuiltin = (CallBuiltinFunction) cfgNode;
                    String funcName = callBuiltin.getFunctionName();

                    // here is the list of custom sanitization functions
                    return funcName.equals("ereg_replace") ||
                        funcName.equals("preg_replace") ||
                        funcName.equals("str_replace");
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
            // no decoration for this node: be conservative
            return true;
        }

        // intersect!
        FSAAutomaton intersection = auto.intersect(this.undesiredAutomaton);
        return !intersection.isEmpty();
    }

    /**
     * Locates custom sanitization nodes in the given dependency graph and returns them.
     *
     * @param dependencyGraph
     *
     * @return
     */
    public static List<AbstractNode> findCustomSanit(DependencyGraph dependencyGraph) {
        List<AbstractNode> customSanitationNOdes = new LinkedList<>();
        for (AbstractNode node : dependencyGraph.getNodes()) {
            if (isCustomSanit(node)) {
                customSanitationNOdes.add(node);
            }
        }

        return customSanitationNOdes;
    }

    /**
     * Take care: if trimAllowed == false, no need to call this method.
     *
     * @param node
     *
     * @return
     */
    private TrimInfo checkTrim(AbstractNode node) {
        // start with default triminfo: everything can be trimmed
        TrimInfo retMe = new TrimInfo();

        // handle cases where trimming is not allowed
        if (node instanceof BuiltinFunctionNode) {
            BuiltinFunctionNode builtinFunctionNode = (BuiltinFunctionNode) node;
            if (builtinFunctionNode.isBuiltin()) {
                String opName = builtinFunctionNode.getName();
                if (opName.equals("preg_replace") ||
                    opName.equals("ereg_replace") ||
                    opName.equals("str_replace")) {
                    retMe.addNoTrim(0);
                }
            }
        }

        return retMe;
    }

    /**
     * Helper class for exchanging information on whether to allow trimming.
     */
    private class TrimInfo {
        /** these indices must be trimmed */
        private List<Integer> trim;
        /** these indices must not be trimmed */
        private List<Integer> noTrim;
        /** what to do with all remaining indices */
        private boolean defaultTrim;

        TrimInfo() {
            this.defaultTrim = true;
            this.trim = new LinkedList<>();
            this.noTrim = new LinkedList<>();
        }

        void setDefaultTrim(boolean defaultTrim) {
            this.defaultTrim = defaultTrim;
        }

        void addNoTrim(int i) {
            this.noTrim.add(i);
        }

        boolean mayTrim(int i) {
            return trim.contains(i) || !noTrim.contains(i) && defaultTrim;
        }
    }
}