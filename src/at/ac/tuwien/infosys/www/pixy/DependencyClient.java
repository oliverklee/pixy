package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.dependency.*;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;

import java.util.*;

/**
 * If you want to create a new depclient:
 * 1. inherit from this class (see existing examples)
 * 2. add info to MyOptions.analyses
 * 3. add model and sink config files
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class DependencyClient {
    protected DepAnalysis depAnalysis;
    protected DependencyClientInformation dci;

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

    protected DependencyClient(DepAnalysis depAnalysis) {
        this.depAnalysis = depAnalysis;
        this.dci = MyOptions.getDepClientInfo(this.getClass().getName());
    }

//  ********************************************************************************
//  abstract methods

    // returns a list with the line numbers of the detected vulns
    // (the return value is used for testing)
    public abstract List<Integer> detectVulns();

    public abstract VulnerabilityInformation detectAlternative();

    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected abstract void checkForSink(AbstractCfgNode cfgNodeX, TacFunction traversedFunction,
                                         List<Sink> sinks);

//  ********************************************************************************

    // returns a list of sinks for this analysis
    public List<Sink> collectSinks() {
        List<Sink> sinks = new LinkedList<>();
        for (TacFunction function : this.depAnalysis.getFunctions()) {
            for (AbstractCfgNode cfgNodeX : function.getControlFlowGraph().dfPreOrder()) {
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

    protected DependencyClient.InitialTaint initiallyTainted(TacPlace place) {

        if (place instanceof Variable) {
            Variable var = place.getVariable();
            String varName = var.getName();

            // harmless superglobals?
            if (var.isSuperGlobal()) {

                // return variables
                if (var.isReturnVariable()) {
                    return DependencyClient.InitialTaint.NEVER;
                } else if (MyOptions.isHarmlessServerVar(varName) ||
                    varName.equals("$_SERVER")) {
                    // harmless member of the SERVER array,
                    // or the SERVER array itself
                    return DependencyClient.InitialTaint.NEVER;
                } else if (varName.startsWith("$_SESSION[")) {
                    // the whole session array
                    return DependencyClient.InitialTaint.NEVER;
                } else if (varName.equals("$_ENV") ||
                    varName.equals("$_HTTP_ENV_VARS") ||
                    varName.startsWith("$_ENV[") ||
                    varName.startsWith("$HTTP_ENV_VARS[")) {
                    // the whole env array
                    return DependencyClient.InitialTaint.NEVER;
                } else if (!this.getIsTainted && varName.startsWith("$_GET[")) {
                    // the whole GET array, if it shall be considered
                    // as not tainted
                    return DependencyClient.InitialTaint.NEVER;
                } else {
                    // non-harmless superglobal
                    return DependencyClient.InitialTaint.ALWAYS;
                }

                // non-superglobals
            } else {

                if (var.getSymbolTable().getName().equals("_special")) {
                    if (varName.equals(InternalStrings.memberName)) {
                        return DependencyClient.InitialTaint.NEVER;
                    }
                } else if (varName.equals("$PHPSESSID")) {
                    // the special php session id variable is harmless
                    return DependencyClient.InitialTaint.NEVER;
                } else if (MyOptions.harmlessServerIndices.contains(varName.substring(1))) {
                    // something like $SERVER_NAME etc.
                    // (i.e. harmless indices of the SERVER array that have been
                    // exported into main's scope due to register_globals
                    return DependencyClient.InitialTaint.NEVER;
                } else if (!var.getSymbolTable().isMain()) {
                    // local function variables are untainted
                    return DependencyClient.InitialTaint.NEVER;
                } else {
                    // a global variable
                    if (!MyOptions.optionG) {
                        // if the user decided to disable register_globals,
                        // ignore these cases
                        return DependencyClient.InitialTaint.NEVER;
                    } else {
                        return DependencyClient.InitialTaint.IFRG;
                    }
                }
            }
        } else if (place instanceof Constant) {
            // uninitialized constants are untainted
            return DependencyClient.InitialTaint.NEVER;
        }

        // did we miss something? everything else is tainted
        return DependencyClient.InitialTaint.ALWAYS;
    }

//  ********************************************************************************

    // extracts the "relevant subgraph", using models for builtin functions;
    // here is how it works:
    // - for operation nodes representing sanitization functions, the top-down algorithm
    //   doesn't follow its successors; instead, a single new successor ("<sanitation>")
    //   is created
    // - evil functions: a single <uninit> successor is created
    // - multi-dependency: the algorithm only follows those successors that are
    //   defined as relevant for XSS
    // - unmodeled functions are treated as if they were evil functions
    protected DependencyGraph getRelevant(DependencyGraph dependencyGraph) {
        // start with a one-element graph
        DependencyGraph relevant = new DependencyGraph(dependencyGraph.getRoot());
        this.getRelevantHelper(relevant.getRoot(), relevant, dependencyGraph);
        return relevant;
    }

//  ********************************************************************************

    protected void getRelevantHelper(AbstractNode node, DependencyGraph relevant, DependencyGraph orig) {

        if (node instanceof NormalNode) {

            for (AbstractNode succ : orig.getSuccessors(node)) {

                // if this node has already been added to the relevant graph...
                if (relevant.containsNode(succ)) {
                    relevant.addEdge(node, succ);
                    continue;
                }

                relevant.addNode(succ);
                relevant.addEdge(node, succ);
                getRelevantHelper(succ, relevant, orig);
            }
        } else if (node instanceof BuiltinFunctionNode) {

            BuiltinFunctionNode builtinFunctionNode = (BuiltinFunctionNode) node;
            String opName = builtinFunctionNode.getName();
            // list for indices of multi-dependency functions
            List<Integer> multiList = new LinkedList<>();

            if (!builtinFunctionNode.isBuiltin()) {

                // call to function or method for which no definition
                // could be found

                AbstractCfgNode cfgNodeX = builtinFunctionNode.getCfgNode();
                if (cfgNodeX instanceof CallUnknownFunction) {
                    CallUnknownFunction callUnknown = (CallUnknownFunction) cfgNodeX;
                    if (callUnknown.isMethod()) {
                        AbstractNode sanitNode = new NormalNode(
                            new Literal("<method-call>"), builtinFunctionNode.getCfgNode());
                        relevant.addNode(sanitNode);
                        relevant.addEdge(builtinFunctionNode, sanitNode);
                    } else {
                        AbstractNode uninitNode = new UninitializedNode();
                        relevant.addNode(uninitNode);
                        relevant.addEdge(builtinFunctionNode, uninitNode);
                    }
                } else {
                    throw new RuntimeException("SNH");
                }
                // end of recursion

                // STRONG SANITIZATION FUNCTIONS ************************

            } else if (isStrongSanit(opName)) {

                AbstractNode sanitNode = new NormalNode(
                    new Literal("<sanitization>"), builtinFunctionNode.getCfgNode());
                relevant.addNode(sanitNode);
                relevant.addEdge(builtinFunctionNode, sanitNode);
                // end of recursion

                // WEAK SANITIZATION FUNCTIONS ************************

            } else if (isWeakSanit(opName, multiList)) {

                multiDependencyRelevant(builtinFunctionNode, relevant, orig, multiList, false);

                // EVIL FUNCTIONS ***************************************

            } else if (isEvil(opName)) {

                AbstractNode uninitNode = new UninitializedNode();
                relevant.addNode(uninitNode);
                relevant.addEdge(builtinFunctionNode, uninitNode);
                // end of recursion

                // MULTI-OR-DEPENDENCY **********************************

                // TODO: generic value flows should better be modeled during
                // depgraph construction, and not here
            } else if (isMulti(opName, multiList)) {

                multiDependencyRelevant(builtinFunctionNode, relevant, orig, multiList, false);

                // INVERSE MULTI-OR-DEPENDENCY **************************

            } else if (isInverseMulti(opName, multiList)) {

                multiDependencyRelevant(builtinFunctionNode, relevant, orig, multiList, true);

                // CATCH-ALL ********************************************

            } else {
                System.out.println("Unmodeled builtin function: " + opName);
                AbstractNode uninitNode = new UninitializedNode();
                relevant.addNode(uninitNode);
                relevant.addEdge(builtinFunctionNode, uninitNode);
                // end of recursion
            }
        } else if (node instanceof UninitializedNode) {
            // end of recursion: this is always a leaf node
        } else {
            throw new RuntimeException("SNH: " + node.getClass());
        }
    }

//  ********************************************************************************

    // helper function for multi-dependency builtin functions
    // (in relevant subgraph construction)
    protected void multiDependencyRelevant(BuiltinFunctionNode builtinFunctionNode, DependencyGraph relevant,
                                           DependencyGraph orig, List<Integer> indices, boolean inverse) {

        List<AbstractNode> succs = orig.getSuccessors(builtinFunctionNode);
        Set<Integer> indexSet = new HashSet<>(indices);

        int count = -1;
        boolean created = false;
        for (AbstractNode succ : succs) {
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
                relevant.addEdge(builtinFunctionNode, succ);
                continue;
            }
            relevant.addNode(succ);
            relevant.addEdge(builtinFunctionNode, succ);
            this.getRelevantHelper(succ, relevant, orig);
        }

        if (!created) {
            // if no successors have been created: make a harmless one
            AbstractNode sanitNode = new NormalNode(
                new Literal("<no-dep>"), builtinFunctionNode.getCfgNode());
            relevant.addNode(sanitNode);
            relevant.addEdge(builtinFunctionNode, sanitNode);
        }
    }

//  ********************************************************************************

    // finds those uninit nodes in the given *relevant* depgraph that are dangerous
    protected Map<UninitializedNode, InitialTaint> findDangerousUninit(DependencyGraph relevant) {

        Set<UninitializedNode> uninitializedNodes = relevant.getUninitNodes();

        Map<UninitializedNode, InitialTaint> retMe = new HashMap<>();

        for (UninitializedNode uninitializedNode : uninitializedNodes) {
            Set<AbstractNode> preds = relevant.getPredecessors(uninitializedNode);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }
            AbstractNode pre = preds.iterator().next();
            if (pre instanceof NormalNode) {
                NormalNode preNormal = (NormalNode) pre;
                switch (this.initiallyTainted(preNormal.getPlace())) {
                    case ALWAYS:
                        retMe.put(uninitializedNode, InitialTaint.ALWAYS);
                        break;
                    case IFRG:
                        retMe.put(uninitializedNode, InitialTaint.IFRG);
                        break;
                    case NEVER:
                        // nothing to do here
                        break;
                    default:
                        throw new RuntimeException("SNH");
                }
            } else if (pre instanceof BuiltinFunctionNode) {
                // evil function, don't remove
                retMe.put(uninitializedNode, InitialTaint.ALWAYS);
            } else {
                throw new RuntimeException("SNH");
            }
        }

        return retMe;
    }

//  ********************************************************************************

    protected List<NormalNode> findDangerousSources(DependencyGraph relevant) {

        List<NormalNode> retMe = new LinkedList<>();

        // get dangerous uninit nodes, and then inspect their predecessors
        Set<UninitializedNode> uninitializedNodes = this.findDangerousUninit(relevant).keySet();
        for (UninitializedNode uninitializedNode : uninitializedNodes) {

            Set<AbstractNode> preds = relevant.getPredecessors(uninitializedNode);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }
            AbstractNode pre = preds.iterator().next();
            if (pre instanceof NormalNode) {
                NormalNode preNormal = (NormalNode) pre;
                retMe.add(preNormal);
            } else if (pre instanceof BuiltinFunctionNode) {
                // evil function, ignore

            } else {
                throw new RuntimeException("SNH");
            }
        }

        return retMe;
    }
}