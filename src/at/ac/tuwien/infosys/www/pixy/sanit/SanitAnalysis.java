package at.ac.tuwien.infosys.www.pixy.sanit;


import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.DepClient;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.VulnInfo;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphNormalNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphOpNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphSccNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphUninitNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallBuiltin;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallUnknown;

// superclass for sanitization analyses
public abstract class SanitAnalysis 
extends DepClient {

    // if this flag is active, untainted values (most notably: static strings)
    // are treated as empty strings during depgraph decoration
    private boolean trimUntainted = !MyOptions.optionR;
    
    // automaton representing the undesired stuff
    protected FSAAutomaton undesir;
    
    // xss, sql, ...
    protected String name;
    
    protected SanitAnalysis(String name, DepAnalysis depAnalysis, FSAAutomaton undesired) {
        super(depAnalysis);
        this.name = name;
        this.undesir = undesired;
    }

//  ********************************************************************************
    
    public List<Integer> detectVulns(DepClient depClient) {
        
        System.out.println();
        System.out.println("*****************");
        System.out.println(name.toUpperCase() + " Sanit Analysis BEGIN");
        System.out.println("*****************");
        System.out.println();

        // let the basic analysis do the preliminary work
        VulnInfo vulnInfo = depClient.detectAlternative();
        List<DepGraph> vulnDepGraphs = vulnInfo.getDepGraphs();
        List<DepGraph> minDepGraphs = vulnInfo.getDepGraphsMin();
        
        // stats
        int scanned = vulnInfo.getInitialGraphCount();
        int reported_by_basic = vulnDepGraphs.size();
        int have_sanit = 0;
        int no_sanit = 0;
        //int have_vuln = 0;
        int sure_vuln_1 = 0;
        int sure_vuln_2 = 0;
        int possible_vuln = 0;
        int eliminated = 0;

        System.out.println(name.toUpperCase() + " Sanit Analysis Output");
        System.out.println("--------------------");
        System.out.println();
        
        // dump the automaton that represents the undesired stuff
        this.dumpDotAuto(this.undesir, "undesired_" + name, MyOptions.graphPath);
        
        // info for dynamic analysis
        StringBuilder dynInfo = new StringBuilder();

        int dynpathcount = 0;
        
        int graphcount = 0;
        Iterator<DepGraph> minIter = minDepGraphs.iterator();
        for (DepGraph depGraph : vulnDepGraphs) {
            
                graphcount++;
                
                DepGraph minGraph = minIter.next();

                // in any case, dump the vulnerable depgraphs
                depGraph.dumpDot(name + "sanit" + graphcount + "i", MyOptions.graphPath, depGraph.getUninitNodes(), this.dci);
                minGraph.dumpDot(name + "sanit" + graphcount + "m", MyOptions.graphPath, depGraph.getUninitNodes(), this.dci);

                CfgNode cfgNode = depGraph.getRoot().getCfgNode();
                
                // retrieve custom sanitization routines from the minGraph
                List<DepGraphNode> customSanitNodes = findCustomSanit(minGraph);
                
                if (customSanitNodes.isEmpty()) {
                    // we don't have to perform our detailed sanitization analysis
                    // if no custom sanitization is performed
                    System.out.println("No Sanitization!");
                    System.out.println("- " + cfgNode.getLoc());
                    System.out.println("- Graphs: "+name+"sanit" + graphcount);
                    sure_vuln_1++;
                    no_sanit++;
                    continue;
                }
                have_sanit++;
                
                DepGraph workGraph = new DepGraph(depGraph);
                
                Map<DepGraphNode,FSAAutomaton> deco = new HashMap<DepGraphNode,FSAAutomaton>();
                FSAAutomaton auto = this.toAutomatonSanit(workGraph, depGraph, deco);

                // intersect this automaton with the undesired stuff;
                // if the intersection is empty, it means that we are safe!
                FSAAutomaton intersection = auto.intersect(this.undesir);
                if (!intersection.isEmpty()) {
                    
                    // dump the intersection automaton:
                    // represents counterexamples!
                    this.dumpDotAuto(intersection, name+"sanit" + graphcount + "intersect", MyOptions.graphPath);
                    
                    // create a graph that is further minimized to the sanitization routines
                    // (regardless of the effectiveness of the applied sanitization)
                    DepGraph sanitMinGraph = new DepGraph(minGraph);
                    sanitMinGraph.reduceToInnerNodes(customSanitNodes);
                    
                    // and now reduce this graph to the ineffective sanitization routines
                    int ineffBorder = sanitMinGraph.reduceToIneffectiveSanit(deco, this);

                    if (ineffBorder != 0) {
                        
                        System.out.println("Ineffective Sanitization!");
                        System.out.println("- " + cfgNode.getLoc());
                        System.out.println("- Graphs: "+name+"sanit" + graphcount);
                        possible_vuln++;
                        
                        // dump the minimized graph
                        sanitMinGraph.dumpDot(name+"sanit" + graphcount + "mm", MyOptions.graphPath, depGraph.getUninitNodes(), this.dci);

                        dynInfo.append("SINK:\n");
                        dynInfo.append(sanitMinGraph.getRoot().toString());
                        dynInfo.append("\n");
                        dynInfo.append("SOURCES:\n");
                        List<DepGraphNormalNode> dangerousSources = this.findDangerousSources(sanitMinGraph);
                        for (DepGraphNormalNode dangerousSource : dangerousSources) {
                            dynInfo.append(dangerousSource.toString());
                            dynInfo.append("\n");
                        }
                        dynInfo.append("\n");
                        
                        if (MyOptions.countPaths) {
                            int paths = new DepGraph(sanitMinGraph).countPaths();
                            dynpathcount += paths;
                            // System.out.println("- paths: " + paths);
                        }

                    } else {
                        // this means that this graph contains custom sanitization routines,
                        // but they are not responsible for the vulnerability
                        System.out.println("No Sanitization!");
                        System.out.println("- " + cfgNode.getLoc());
                        System.out.println("- Graphs: "+name+"sanit" + graphcount);
                        sure_vuln_2++;
                    }

                } else {
                    // eliminated false positive!
                    eliminated++;
                }
                
                this.dumpDotAuto(auto, name+"sanit" + graphcount + "auto", MyOptions.graphPath);
        }

        Utils.writeToFile(dynInfo.toString(), MyOptions.graphPath + "/"+name+"info.txt");
        
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
        System.out.println("DepGraphs with custom sanitization thrown away by basic analysis: " + 
                vulnInfo.getCustomSanitThrownAwayCount());
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

//  ********************************************************************************
    
    // returns the automaton representation of the given dependency graph;
    // is done by decorating the nodes of the graph with automata bottom-up,
    // and returning the automaton that eventually decorates the root;
    // BEWARE: this also eliminates cycles!
    protected FSAAutomaton toAutomatonSanit(DepGraph depGraph, DepGraph origDepGraph,
            Map<DepGraphNode,FSAAutomaton> deco) {
        depGraph.eliminateCycles();
        DepGraphNode root = depGraph.getRoot();
        //Map<DepGraphNode,FSAAutomaton> deco = new HashMap<DepGraphNode,FSAAutomaton>();
        Set<DepGraphNode> visited = new HashSet<DepGraphNode>();
        this.decorateSanit(root, deco, visited, depGraph, origDepGraph, true);
        FSAAutomaton rootDeco = deco.get(root).clone();
        // BEWARE: minimization can lead to an automaton that is *less* human-readable
        //rootDeco.minimize(); 
        return rootDeco;
    }

//  ********************************************************************************
    
    // decorates the given node (and all its successors) with an automaton
    private final void decorateSanit(DepGraphNode node, Map<DepGraphNode,FSAAutomaton> deco,
            Set<DepGraphNode> visited, DepGraph depGraph, DepGraph origDepGraph,
            boolean trimAllowed) {
        
        visited.add(node);
        
        TrimInfo trimInfo;
        if (trimAllowed) {
            trimInfo = this.checkTrim(node);
        } else {
            trimInfo = new TrimInfo();
            trimInfo.setDefaultTrim(false);
        }
        
        // if this node has successors, decorate them first (if not done yet)
        List<DepGraphNode> successors = depGraph.getSuccessors(node);
        if (successors != null && !successors.isEmpty()) {
            int i = 0;
            for (DepGraphNode succ : successors) {
                if (!visited.contains(succ) && deco.get(succ) == null) {
                    decorateSanit(succ, deco, visited, depGraph, origDepGraph, trimInfo.mayTrim(i));
                }
                i++;
            }
        }
        
        // now that all successors are decorated, we can decorate this node
        
        FSAAutomaton auto = null;
        if (node instanceof DepGraphNormalNode) {
            DepGraphNormalNode normalNode = (DepGraphNormalNode) node;
            if (successors == null || successors.isEmpty()) {
                // this should be a string leaf node
                TacPlace place = normalNode.getPlace();
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
                for (DepGraphNode succ : successors) {
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
            
        } else if (node instanceof DepGraphOpNode) {
            auto = this.makeAutoForOp((DepGraphOpNode) node, deco, depGraph, trimAllowed);
            
        } else if (node instanceof DepGraphSccNode) {
            
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
             * <uninit> nodes, because <uninit> nodes are always leaf nodes in the dep
             * graph (since they have no successors);
             * under the above assumptions and observations, it is valid to
             * say that the taint value of an SCC node solely depends on
             * the taint values of its successors, and that is exactly what we
             * do here  
             * 
             */
            
            if (trimUntainted && trimAllowed) {
                
                auto = FSAAutomaton.makeString("");
                
                for (DepGraphNode succ : successors) {
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
            
        } else if (node instanceof DepGraphUninitNode) {
            
            // retrieve predecessor
            Set<DepGraphNode> preds = depGraph.getPredecessors(node);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }
            DepGraphNode pre = preds.iterator().next();
            
            if (pre instanceof DepGraphNormalNode) {
                DepGraphNormalNode preNormal = (DepGraphNormalNode) pre;
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
                
            } else if (pre instanceof DepGraphSccNode) {
                // this case can really happen (e.g.: dcpportal: advertiser.php, forums.php);
                
                // take a look at the "real" predecessors (i.e., take a look "into"
                // the SCC node): if there is exactly one predecessor, namely a
                // DepGraphNormalNode, and if the contained place is initially untainted,
                // there is no danger from here; else: we will have to set it to tainted 
                Set<DepGraphNode> origPreds = origDepGraph.getPredecessors(node);
                if (origPreds.size() == 1) {
                    DepGraphNode origPre = origPreds.iterator().next();
                    if (origPre instanceof DepGraphNormalNode) {
                        DepGraphNormalNode origPreNormal = (DepGraphNormalNode) origPre;
                        
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

//  ********************************************************************************
    
    // returns an automaton for the given operation node
    private final FSAAutomaton makeAutoForOp(DepGraphOpNode node, Map<DepGraphNode,FSAAutomaton> deco,
            DepGraph depGraph, boolean trimAllowed) {
        
        List<DepGraphNode> successors = depGraph.getSuccessors(node);
        if (successors == null) {
            successors = new LinkedList<DepGraphNode>();
        }
        
        FSAAutomaton retMe = null;
        
        String opName = node.getName();
        
        List<Integer> multiList = new LinkedList<Integer>();
        
        if (!node.isBuiltin()) {
            
            // call to function or method for which no definition
            // could be found

            CfgNode cfgNodeX = node.getCfgNode();
            if (cfgNodeX instanceof CfgNodeCallUnknown) {
                CfgNodeCallUnknown cfgNode = (CfgNodeCallUnknown) cfgNodeX;
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
            
            // CONCAT
            for (DepGraphNode succ : successors) {
                FSAAutomaton succAuto = deco.get(succ);
                if (retMe == null) {
                    retMe = succAuto;
                } else {
                    retMe = retMe.concatenate(succAuto);
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
            
        } else if (isWeakSanit(opName, multiList)) {
            
            if (trimUntainted && trimAllowed) {
                retMe = FSAAutomaton.makeString("");
            } else {
                retMe = FSAAutomaton.makeAnyString();
            }

        // STRONG SANITIZATION FUNCTIONS *******************************
        // e.g., ops that return numeric values
            
        } else if (isStrongSanit(opName)) {
            
            if (trimUntainted && trimAllowed) {
                retMe = FSAAutomaton.makeString("");
            } else {
                retMe = FSAAutomaton.makeAnyString();
            }
        
        // EVIL FUNCTIONS ***************************************
        // take care: if you define evil functions, you must adjust
        // the treatment of SCC nodes in decorate()
            
        // MULTI-OR-DEPENDENCY **********************************
            
        } else if (isMulti(opName, multiList)) {
            
            retMe = this.multiDependencyAutoSanit(successors, deco, multiList, false);
            
        } else if (isInverseMulti(opName, multiList)) {

            retMe = this.multiDependencyAutoSanit(successors, deco, multiList, true);

        // CATCH-ALL ********************************************
            
        } else {
            System.out.println("Unmodeled builtin function (SQL-Sanit): " + opName);
            
            // conservative decision for operations that have not been
            // modeled yet: .*
            retMe = FSAAutomaton.makeAnyString();
        }
        
        return retMe;
    }

//  ********************************************************************************
    
    // if trimUntainted == false: always returns .*
    // else:
    // - if all successors are empty: returns empty
    // - else: returns .*
    private FSAAutomaton multiDependencyAutoSanit(List<DepGraphNode> succs, 
            Map<DepGraphNode,FSAAutomaton> deco, List<Integer> indices, boolean inverse) {
        
        if (!trimUntainted) {
            return FSAAutomaton.makeAnyString();
        }

        Set<Integer> indexSet = new HashSet<Integer>(indices);
        
        int count = -1;
        for (DepGraphNode succ : succs) {
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

//  ********************************************************************************

    protected void dumpDotAuto(FSAAutomaton auto, String graphName, String path) {
        
        String baseFileName = path + "/" + graphName;
        
        (new File(path)).mkdir();

        String dotFileName = baseFileName + ".dot";
        Utils.writeToFile(auto.toDot(), dotFileName);
        
        // txt representation only required for debugging
        //String txtFileName = baseFileName + ".txt";
        //Utils.writeToFile(auto.getString(), txtFileName);
    }

//  ********************************************************************************
    
    // checks if the given node is a custom sanitization node
    public static boolean isCustomSanit(DepGraphNode node) {
        
        if (node instanceof DepGraphNormalNode) {
            return false;
        } else if (node instanceof DepGraphUninitNode) {
            return false;
        } else if (node instanceof DepGraphOpNode) {
            // check if this operation could be used for custom sanitization
            DepGraphOpNode opNode = (DepGraphOpNode) node;
            if (opNode.isBuiltin()) {
                CfgNode cfgNode = opNode.getCfgNode();
                if (cfgNode instanceof CfgNodeCallBuiltin) {
                    CfgNodeCallBuiltin callBuiltin = (CfgNodeCallBuiltin) cfgNode;
                    String funcName = callBuiltin.getFunctionName();
                    
                    // here is the list of custom sanitization functions
                    if (
                            funcName.equals("ereg_replace") ||
                            funcName.equals("preg_replace") ||
                            funcName.equals("str_replace")
                            ) {
                        
                        // found it!
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
        } else if (node instanceof DepGraphSccNode) {
            throw new RuntimeException("SNH");
        } else {
            throw new RuntimeException("SNH");
        }
    }

//  ********************************************************************************
    
    public boolean isIneffective(DepGraphNode customSanit, 
            Map<DepGraphNode,FSAAutomaton> deco) {
        
        FSAAutomaton auto = deco.get(customSanit);
        if (auto == null) {
            // no decoration for this node: be conservative
            return true;
        }
        
        // intersect!
        FSAAutomaton intersection = auto.intersect(this.undesir);
        if (!intersection.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

//  ********************************************************************************
    
    // locates custom sanitization nodes in the given depgraph and returns them
    public static List<DepGraphNode> findCustomSanit(DepGraph depGraph) {
        List<DepGraphNode> retMe = new LinkedList<DepGraphNode>();
        for (DepGraphNode node : depGraph.getNodes()) {
            if (isCustomSanit(node)) {
                retMe.add(node);
            }
        }
        return retMe;
    }
    
//  ********************************************************************************
    
    // take care: if trimAllowed == false, no need to call this method...
    private TrimInfo checkTrim(DepGraphNode node) {
        
        // start with default triminfo: everything can be trimmed
        TrimInfo retMe = new TrimInfo();
        
        // handle cases where trimming is not allowed
        if (node instanceof DepGraphOpNode) {
            DepGraphOpNode opNode = (DepGraphOpNode) node;
            if (opNode.isBuiltin()) {
                String opName = opNode.getName();
                if (opName.equals("preg_replace") ||
                        opName.equals("ereg_replace") ||
                        opName.equals("str_replace")) {
                    retMe.addNoTrim(0);
                }
            }
        }
        
        return retMe;
    }
    
//  ********************************************************************************
    
    // helper class for exchanging information on whether to allow trimming
    private class TrimInfo {
        
        // these indices must be trimmed
        private List<Integer> trim;
        // these indices must not be trimmed
        private List<Integer> noTrim;
        // what to do with all remaining indices
        private boolean defaultTrim;
        
        TrimInfo() {
            this.defaultTrim = true;
            this.trim = new LinkedList<Integer>();
            this.noTrim = new LinkedList<Integer>();
        }
        
        void setDefaultTrim(boolean defaultTrim) {
            this.defaultTrim = defaultTrim;
        }
        
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
