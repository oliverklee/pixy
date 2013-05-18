package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.dep.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallUnknown;

import java.util.*;

/**
 * If you want to create a new depclient:
 * 1. inherit from this class (see existing examples)
 * 2. add info to MyOptions.analyses
 * 3. add model and sink config files
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class DepClient {

    protected DepAnalysis depAnalysis;
    protected DepClientInfo dci;

    // flags returned by initiallyTainted():
    // - always tainted
    // - tainted if register_globals is active
    public enum InitialTaint {
        ALWAYS, IFRG, NEVER
    }

    // should members of the $_GET array be considered as initially tainted?
    // defaults to true, of course
    protected boolean getIsTainted = true;

//  ********************************************************************************

    protected DepClient(DepAnalysis depAnalysis) {
        this.depAnalysis = depAnalysis;
        this.dci = MyOptions.getDepClientInfo(this.getClass().getName());
    }

//  ********************************************************************************
//  abstract methods

    // returns a list with the line numbers of the detected vulns
    // (the return value is used for testing)
    public abstract List<Integer> detectVulns();

    public abstract VulnInfo detectAlternative();

    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected abstract void checkForSink(CfgNode cfgNodeX, TacFunction traversedFunction,
                                         List<Sink> sinks);

//  ********************************************************************************

    // returns a list of sinks for this analysis
    public List<Sink> collectSinks() {
        List<Sink> sinks = new LinkedList<>();
        for (TacFunction function : this.depAnalysis.getFunctions()) {
            for (CfgNode cfgNodeX : function.getCfg().dfPreOrder()) {
                checkForSink(cfgNodeX, function, sinks);
            }
        }
        return sinks;
    }

//  ********************************************************************************

    protected boolean isStrongSanit(String opName) {
        return this.dci.getFunctionModels().getF_strongSanit().contains(opName);
    }

//  ********************************************************************************

    protected boolean isWeakSanit(String opName, List<Integer> indices) {
        Set<Integer> i = this.dci.getFunctionModels().getF_weakSanit().get(opName);
        if (i == null) {
            return false;
        }
        indices.addAll(i);
        return true;
    }

//  ********************************************************************************

    protected boolean isEvil(String opName) {
        return this.dci.getFunctionModels().getF_evil().contains(opName);
    }

//  ********************************************************************************

    // if the given operation is a multi-dependency operation, it returns true
    // and fills the given indices list with the appropriate index numbers
    protected boolean isMulti(String opName, List<Integer> indices) {
        Set<Integer> i = this.dci.getFunctionModels().getF_multi().get(opName);
        if (i == null) {
            return false;
        }
        indices.addAll(i);
        return true;
    }

//  ********************************************************************************

    // analogous to isMulti, but inverse: e.g., if some function is an inverse
    // multi-dependency with a returned index "2", then all its parameters are
    // relevant, except for parameter #2
    protected boolean isInverseMulti(String opName, List<Integer> indices) {
        Set<Integer> i = this.dci.getFunctionModels().getF_invMulti().get(opName);
        if (i == null) {
            return false;
        }
        indices.addAll(i);
        return true;
    }

//  ********************************************************************************

    protected DepClient.InitialTaint initiallyTainted(TacPlace place) {

        if (place instanceof Variable) {
            Variable var = place.getVariable();
            String varName = var.getName();

            // harmless superglobals?
            if (var.isSuperGlobal()) {

                // return variables
                if (var.isReturnVariable()) {
                    return DepClient.InitialTaint.NEVER;
                } else if (MyOptions.isHarmlessServerVar(varName) ||
                    varName.equals("$_SERVER")) {
                    // harmless member of the SERVER array,
                    // or the SERVER array itself
                    return DepClient.InitialTaint.NEVER;
                } else if (varName.startsWith("$_SESSION[")) {
                    // the whole session array
                    return DepClient.InitialTaint.NEVER;
                } else if (varName.equals("$_ENV") ||
                    varName.equals("$_HTTP_ENV_VARS") ||
                    varName.startsWith("$_ENV[") ||
                    varName.startsWith("$HTTP_ENV_VARS[")) {
                    // the whole env array
                    return DepClient.InitialTaint.NEVER;
                } else if (!this.getIsTainted && varName.startsWith("$_GET[")) {
                    // the whole GET array, if it shall be considered
                    // as not tainted
                    return DepClient.InitialTaint.NEVER;
                } else {
                    // non-harmless superglobal
                    return DepClient.InitialTaint.ALWAYS;
                }

                // non-superglobals
            } else {

                if (var.getSymbolTable().getName().equals("_special")) {
                    if (varName.equals(InternalStrings.memberName)) {
                        return DepClient.InitialTaint.NEVER;
                    }
                } else if (varName.equals("$PHPSESSID")) {
                    // the special php session id variable is harmless
                    return DepClient.InitialTaint.NEVER;
                } else if (MyOptions.harmlessServerIndices.contains(varName.substring(1))) {
                    // something like $SERVER_NAME etc.
                    // (i.e. harmless indices of the SERVER array that have been
                    // exported into main's scope due to register_globals
                    return DepClient.InitialTaint.NEVER;
                } else if (!var.getSymbolTable().isMain()) {
                    // local function variables are untainted
                    return DepClient.InitialTaint.NEVER;
                } else {
                    // a global variable
                    if (!MyOptions.optionG) {
                        // if the user decided to disable register_globals,
                        // ignore these cases
                        return DepClient.InitialTaint.NEVER;
                    } else {
                        return DepClient.InitialTaint.IFRG;
                    }
                }
            }
        } else if (place instanceof Constant) {
            // uninitialized constants are untainted
            return DepClient.InitialTaint.NEVER;
        }

        // did we miss something? everything else is tainted
        return DepClient.InitialTaint.ALWAYS;
    }

//  ********************************************************************************

    // extracts the "relevant subgraph", using models for builtin functions;
    // here is how it works:
    // - for operation nodes representing sanitization functions, the top-down algorithm
    //   doesn't follow its successors; instead, a single new successor ("<sanit>")
    //   is created
    // - evil functions: a single <uninit> successor is created
    // - multi-dependency: the algorithm only follows those successors that are
    //   defined as relevant for XSS
    // - unmodeled functions are treated as if they were evil functions
    protected DepGraph getRelevant(DepGraph depGraph) {
        // start with a one-element graph
        DepGraph relevant = new DepGraph(depGraph.getRoot());
        this.getRelevantHelper(relevant.getRoot(), relevant, depGraph);
        return relevant;
    }

//  ********************************************************************************

    protected void getRelevantHelper(DepGraphNode node, DepGraph relevant, DepGraph orig) {

        if (node instanceof DepGraphNormalNode) {

            for (DepGraphNode succ : orig.getSuccessors(node)) {

                // if this node has already been added to the relevant graph...
                if (relevant.containsNode(succ)) {
                    relevant.addEdge(node, succ);
                    continue;
                }

                relevant.addNode(succ);
                relevant.addEdge(node, succ);
                getRelevantHelper(succ, relevant, orig);
            }
        } else if (node instanceof DepGraphOpNode) {

            DepGraphOpNode opNode = (DepGraphOpNode) node;
            String opName = opNode.getName();
            // list for indices of multi-dependency functions
            List<Integer> multiList = new LinkedList<>();

            if (!opNode.isBuiltin()) {

                // call to function or method for which no definition
                // could be found

                CfgNode cfgNodeX = opNode.getCfgNode();
                if (cfgNodeX instanceof CfgNodeCallUnknown) {
                    CfgNodeCallUnknown callUnknown = (CfgNodeCallUnknown) cfgNodeX;
                    if (callUnknown.isMethod()) {
                        DepGraphNode sanitNode = new DepGraphNormalNode(
                            new Literal("<method-call>"), opNode.getCfgNode());
                        relevant.addNode(sanitNode);
                        relevant.addEdge(opNode, sanitNode);
                    } else {
                        DepGraphNode uninitNode = new DepGraphUninitNode();
                        relevant.addNode(uninitNode);
                        relevant.addEdge(opNode, uninitNode);
                    }
                } else {
                    throw new RuntimeException("SNH");
                }
                // end of recursion

                // STRONG SANITIZATION FUNCTIONS ************************

            } else if (isStrongSanit(opName)) {

                DepGraphNode sanitNode = new DepGraphNormalNode(
                    new Literal("<sanitization>"), opNode.getCfgNode());
                relevant.addNode(sanitNode);
                relevant.addEdge(opNode, sanitNode);
                // end of recursion

                // WEAK SANITIZATION FUNCTIONS ************************

            } else if (isWeakSanit(opName, multiList)) {

                multiDependencyRelevant(opNode, relevant, orig, multiList, false);

                // EVIL FUNCTIONS ***************************************

            } else if (isEvil(opName)) {

                DepGraphNode uninitNode = new DepGraphUninitNode();
                relevant.addNode(uninitNode);
                relevant.addEdge(opNode, uninitNode);
                // end of recursion

                // MULTI-OR-DEPENDENCY **********************************

                // TODO: generic value flows should better be modeled during
                // depgraph construction, and not here
            } else if (isMulti(opName, multiList)) {

                multiDependencyRelevant(opNode, relevant, orig, multiList, false);

                // INVERSE MULTI-OR-DEPENDENCY **************************

            } else if (isInverseMulti(opName, multiList)) {

                multiDependencyRelevant(opNode, relevant, orig, multiList, true);

                // CATCH-ALL ********************************************

            } else {
                System.out.println("Unmodeled builtin function: " + opName);
                DepGraphNode uninitNode = new DepGraphUninitNode();
                relevant.addNode(uninitNode);
                relevant.addEdge(opNode, uninitNode);
                // end of recursion
            }
        } else if (node instanceof DepGraphUninitNode) {
            // end of recursion: this is always a leaf node
        } else {
            throw new RuntimeException("SNH: " + node.getClass());
        }
    }

//  ********************************************************************************

    // helper function for multi-dependency builtin functions
    // (in relevant subgraph construction)
    protected void multiDependencyRelevant(DepGraphOpNode opNode, DepGraph relevant,
                                           DepGraph orig, List<Integer> indices, boolean inverse) {

        List<DepGraphNode> succs = orig.getSuccessors(opNode);
        Set<Integer> indexSet = new HashSet<>(indices);

        int count = -1;
        boolean created = false;
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

            created = true;

            if (relevant.containsNode(succ)) {
                relevant.addEdge(opNode, succ);
                continue;
            }
            relevant.addNode(succ);
            relevant.addEdge(opNode, succ);
            this.getRelevantHelper(succ, relevant, orig);
        }

        if (!created) {
            // if no successors have been created: make a harmless one
            DepGraphNode sanitNode = new DepGraphNormalNode(
                new Literal("<no-dep>"), opNode.getCfgNode());
            relevant.addNode(sanitNode);
            relevant.addEdge(opNode, sanitNode);
        }
    }

//  ********************************************************************************

    // finds those uninit nodes in the given *relevant* depgraph that are dangerous
    protected Map<DepGraphUninitNode, InitialTaint> findDangerousUninit(DepGraph relevant) {

        Set<DepGraphUninitNode> uninitNodes = relevant.getUninitNodes();

        Map<DepGraphUninitNode, InitialTaint> retMe = new HashMap<>();

        for (DepGraphUninitNode uninitNode : uninitNodes) {
            Set<DepGraphNode> preds = relevant.getPredecessors(uninitNode);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }
            DepGraphNode pre = preds.iterator().next();
            if (pre instanceof DepGraphNormalNode) {
                DepGraphNormalNode preNormal = (DepGraphNormalNode) pre;
                switch (this.initiallyTainted(preNormal.getPlace())) {
                    case ALWAYS:
                        retMe.put(uninitNode, InitialTaint.ALWAYS);
                        break;
                    case IFRG:
                        retMe.put(uninitNode, InitialTaint.IFRG);
                        break;
                    case NEVER:
                        // nothing to do here
                        break;
                    default:
                        throw new RuntimeException("SNH");
                }
            } else if (pre instanceof DepGraphOpNode) {
                // evil function, don't remove
                retMe.put(uninitNode, InitialTaint.ALWAYS);
            } else {
                throw new RuntimeException("SNH");
            }
        }

        return retMe;
    }

//  ********************************************************************************

    protected List<DepGraphNormalNode> findDangerousSources(DepGraph relevant) {

        List<DepGraphNormalNode> retMe = new LinkedList<>();

        // get dangerous uninit nodes, and then inspect their predecessors
        Set<DepGraphUninitNode> uninitNodes = this.findDangerousUninit(relevant).keySet();
        for (DepGraphUninitNode uninitNode : uninitNodes) {

            Set<DepGraphNode> preds = relevant.getPredecessors(uninitNode);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }
            DepGraphNode pre = preds.iterator().next();
            if (pre instanceof DepGraphNormalNode) {
                DepGraphNormalNode preNormal = (DepGraphNormalNode) pre;
                retMe.add(preNormal);
            } else if (pre instanceof DepGraphOpNode) {
                // evil function, ignore

            } else {
                throw new RuntimeException("SNH");
            }
        }

        return retMe;
    }
}