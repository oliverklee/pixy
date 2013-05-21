package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import at.ac.tuwien.infosys.www.pixy.DepClientInfo;
import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisInfo;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.ReverseTarget;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;
import at.ac.tuwien.infosys.www.pixy.sanitation.FSAAutomaton;
import at.ac.tuwien.infosys.www.pixy.sanitation.SanitAnalysis;

import java.io.*;
import java.util.*;


/**
 * Graph that displays the data dependencies for a variable at some point in the program.
 * Very useful for better understanding vulnerability reports.
 *
 * OVERVIEW FOR BETTER UNDERSTANDING
 *
 * If you want a dependency graph to be created, call create(),
 *
 * Passing the TacPlace and the CfgNode that will form the root of the dependency graph.
 * create() then calls makeDepGraph() with these parameters (will return the root of the created DepGraph).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepGraph {
    // map from a node to *the same* node;
    // necessary due to the usual limitation of java.util.Set
    private Map<DepGraphNode, DepGraphNode> nodes;

    // the root node (is also contained in "nodes")
    private DepGraphNormalNode root;

    // edges (from -> to)
    private Map<DepGraphNode, List<DepGraphNode>> edges;

    // required for building the graph
    private InterAnalysisInfo analysisInfo;

    // symbol table of the main function
    private SymbolTable mainSymTab;

    private DepAnalysis depAnalysis;

    // just a helper for SCC computation
    private int n;

    // set to true if reduceWithLeaves() is called
    private boolean leavesReduced = false;

//  *********************************************************************************

    private DepGraph() {
    }

//  *********************************************************************************

    // creates a single-element graph
    public DepGraph(DepGraphNormalNode root) {
        this.nodes = new LinkedHashMap<>();
        this.addNode(root);
        this.root = root;
        this.edges = new LinkedHashMap<>();
        this.analysisInfo = null;
        this.mainSymTab = null;
        this.depAnalysis = null;
    }

//  *********************************************************************************

    // place: this will be the root of the dependency graph
    // start: point in the program for which the graph shall be generated
    //        (necessary since we are flow-sensitive, of course)
    // analysisInfo: we need the results of data flow analysis before we can
    //          draw a data dependency graph
    // returns null if the start node is not reachable

    /**
     * Creates a dependency graph.
     *
     * Passing the TacPlace and the CfgNode that will form the root of the dependency graph.
     * This function then calls makeDepGraph() with these parameters (will return the root of the created DepGraph).
     *
     * @param place
     * @param start
     * @param analysisInfo
     * @param mainSymTab
     * @param depAnalysis
     *
     * @return
     */
    public static DepGraph create(TacPlace place, CfgNode start, InterAnalysisInfo analysisInfo,
                                  SymbolTable mainSymTab, DepAnalysis depAnalysis) {

        DepGraph depGraph = new DepGraph();
        depGraph.nodes = new LinkedHashMap<>();
        depGraph.edges = new LinkedHashMap<>();
        depGraph.analysisInfo = analysisInfo;
        depGraph.mainSymTab = mainSymTab;
        depGraph.depAnalysis = depAnalysis;

        List<TacPlace> indices = new LinkedList<>();

        try {
            // start with all contexts of the start node
            Set<Context> allC = analysisInfo.getAnalysisNode(start).getContexts();
            depGraph.root = (DepGraphNormalNode) depGraph.makeDepGraph(
                place, start, Cfg.getFunction(start), indices, allC);
        } catch (NotReachableException ex) {
            debug("not reachable!!!");
            return null;
        }

        return depGraph;
    }

//  *********************************************************************************

    // clones the given DepGraph
    // (nodes are reused)
    public DepGraph(DepGraph orig) {
        this.nodes = new LinkedHashMap<>(orig.nodes);
        this.root = orig.root;
        // this would not be real cloning due to list reuse:
        //this.edges = new LinkedHashMap<DepGraphNode, List<DepGraphNode>>(orig.edges);
        this.edges = new LinkedHashMap<>();
        for (Map.Entry<DepGraphNode, List<DepGraphNode>> origEntry : orig.edges.entrySet()) {
            DepGraphNode origFrom = origEntry.getKey();
            List<DepGraphNode> origTos = origEntry.getValue();

            List<DepGraphNode> myTos = new LinkedList<>(origTos);

            // we can reuse the nodes from the original graph, but we must
            // not reuse its lists
            this.edges.put(origFrom, myTos);
        }

        this.analysisInfo = orig.analysisInfo;
        this.mainSymTab = orig.mainSymTab;
        this.depAnalysis = orig.depAnalysis;
    }

//  ********************************************************************************

    private static void debug(String s) {
    }

//  *********************************************************************************

    // draws a dependency graph for the given input
    // and returns the root of the graph;
    // - funcName: name the function that contains "current"

    /**
     * Creates a dependency graph node "dgn" for the given place and cfg node.
     *
     * If there already is such a node in the graph, this function reuses the already existing node by returning it,
     * ending recursion.
     *
     * Adds dgn to the graph.
     *
     * Iff the given place is a literal: Returns dgn (a literal has no further dependencies), ends recursion.
     *
     * Gets the dependency value of the given place at the given cfg node. The dependency value is basically a set of
     * cfg nodes at which the value of the place was modified.
     *
     * For each of these dependencies:
     * - If it is an uninit dependency: Creates a new uninit node and connect it with dgn.
     * - Else: Determines the places that were USED at the cfg node represented by this dependency
     *   (see "getUsedPlaces()" below).
     * - For each of these places: Recursively calls makeDepGraph() and connect dgn with the returned node.
     *
     * @param place
     * @param current
     * @param function
     * @param indices
     * @param contexts
     * @return
     * @throws NotReachableException
     */
    private DepGraphNode makeDepGraph(TacPlace place, CfgNode current, TacFunction function,
                                      List<TacPlace> indices, Set<Context> contexts) throws NotReachableException {

        debug("  visiting: " + current.getClass() + ", " + current.getOrigLineno() + ", " + place);
        debug(current.toString());
        debug("in function : " + function.getName());
        debug("under contexts: " + contexts);

        // create a graph node for the combination of (current cfg node, place)
        DepGraphNode dgn = new DepGraphNormalNode(place, current);

        // if there already is such a graph node: reuse it and end recursion
        if (this.nodes.containsKey(dgn)) {
            debug("loop!");
            return this.nodes.get(dgn);
        }

        // now that we know that there is no such graph node yet, we can add
        // it to the graph
        addNode(dgn);

        // if this place is a simple literal, we don't have to continue here
        // (a literal has no further data dependencies)
        if (place instanceof Literal) {
            debug("literal!");
            return dgn;
        }

        // get the dep value of the given place for the current cfg node under
        // the given contexts
        DepSet depSet = this.getDepSet(current, place, contexts);

        // a dep set is basically nothing but a set of cfg nodes at which
        // a variable's value has been modified; now we go and visit these cfg nodes...
        debug("start going to nodes...");
        for (Dep dep : depSet.getDepSet()) {
            if (dep == Dep.UNINIT) {
                // end of recursion
                debug("uninit!");
                DepGraphUninitNode uninitNode = new DepGraphUninitNode();
                addNode(uninitNode);
                addEdge(dgn, uninitNode);
            } else {
                debug("getting used places for " + dep.getCfgNode().getOrigLineno());

                // we retrieve the places that were "used" (read) at the given cfg node
                // and recursively continue the graph construction algorithm

                // the node we will have to visit next
                CfgNode targetNode = dep.getCfgNode();

                // tweak to add appropriate operation nodes
                DepGraphNode connectWith = this.checkOp(targetNode);
                if (connectWith == null) {
                    // the target node is not an operation node, so there is nothing
                    // we have to do
                    connectWith = dgn;
                } else if (this.nodes.containsKey(connectWith)) {
                    // the target node is an operation node which already exists
                    connectWith = this.nodes.get(connectWith);
                    addEdge(dgn, connectWith);
                    continue;
                } else {
                    // the target node is a new operation node
                    addNode(connectWith);
                    addEdge(dgn, connectWith);
                }

                // used for our increased array-awareness
                List<TacPlace> newIndices = new LinkedList<>();

                // context and function switching!
                ContextSwitch cswitch = this.switchContexts(function, contexts, current, targetNode);
                TacFunction targetFunction = cswitch.targetFunction;
                Set<Context> targetContexts = cswitch.targetContexts;

                // for every used place...
                for (TacPlace used : this.getUsedPlaces(targetNode, place, indices, newIndices)) {

                    addEdge(connectWith,
                        makeDepGraph(used, targetNode, targetFunction,
                            newIndices, targetContexts));
                }
            }
        }
        debug("...end going to nodes");

        return dgn;
    }

//  ********************************************************************************

    // returns the dep set of the given place for the given cfg node under
    // the given contexts,
    // considering basic blocks and function default cfg's (in these cases,
    // the cfg node has no directly associated analysis info)
    private DepSet getDepSet(CfgNode cfgNode, TacPlace place, Set<Context> contexts)
        throws NotReachableException {

        DepSet depSet = null;
        CfgNode enclosingX = cfgNode.getSpecial();
        if (enclosingX instanceof CfgNodeBasicBlock) {
            // the current node is enclosed by a basic block

            // we have to apply the transfer functions inside this basic block
            // until we reach the current node

            // note: in theory, it should not matter whether we fold first and then
            // apply until we reach the node, or vice versa (apply for the whole
            // table, and fold afterwards)

            Map<Context, LatticeElement> bbPhi = this.analysisInfo.getAnalysisNode(enclosingX).getPhi();

            if (bbPhi.isEmpty()) {
                throw new NotReachableException();
            }

            DepLatticeElement latticeElement = this.newFold(bbPhi, contexts);

            DepLatticeElement propagated = this.depAnalysis.applyInsideBasicBlock(
                (CfgNodeBasicBlock) enclosingX, cfgNode, latticeElement);
            depSet = propagated.getDep(place);
        } else if (enclosingX instanceof CfgNodeEntry) {
            // the current node is inside a function's default cfg
            // (i.e., we want to retrieve the value of a default parameter)

            if (place.isConstant()) {

                // if this is a constant, we simply look up the analysis
                // info in the function's entry node

                Map<Context, LatticeElement> phi = this.analysisInfo.getAnalysisNode(enclosingX).getPhi();
                if (phi.isEmpty()) {
                    throw new NotReachableException();
                }

                depSet = this.newFold(phi, place, contexts);
            } else {

                // this happens if the default parameter is assigned
                // some static array (e.g., $p = array('1', '2')) in the function's head;
                // in analogy to a basic block, we have to apply the transfer
                // functions inside this default cfg

                // start at the default cfg's head
                CfgNode defaultHead = Cfg.getHead(cfgNode);

                // use the value at the function's entry node as start value
                // (since we only have static stuff inside default cfgs, this is
                // a valid method)
                Map<Context, LatticeElement> bbPhi = this.analysisInfo.getAnalysisNode(enclosingX).getPhi();
                DepLatticeElement latticeElement = this.newFold(bbPhi, contexts);

                DepLatticeElement propagated = this.depAnalysis.applyInsideDefaultCfg(
                    defaultHead, cfgNode, latticeElement);
                depSet = propagated.getDep(place);
            }
        } else {
            // none of the above applies, so the current node has
            // directly associated analysis info

            Map<Context, LatticeElement> phi = this.analysisInfo.getAnalysisNode(cfgNode).getPhi();

            // if the map is empty, it means that the function containing
            // this node is never called;
            if (phi.isEmpty()) {
                throw new NotReachableException();
            }

            try {
                depSet = this.newFold(phi, place, contexts);
            } catch (NullPointerException e) {
                // was a bug
                System.out.println(cfgNode.getLoc());
                System.out.println(place);
                throw e;
            }
        }

        return depSet;
    }

//  ********************************************************************************

    // performs context and function switching, making the
    // depgraph construction algorithm context-sensitive;
    // the target function and the target contexts are returned
    private ContextSwitch switchContexts(TacFunction function, Set<Context> contexts,
                                         CfgNode current, CfgNode targetNode) {

        ContextSwitch retMe = new ContextSwitch();

        // function and contexts of the target cfg node;
        // at the beginning, assume that we are remaining in the same function
        TacFunction targetFunction = function;
        Set<Context> targetContexts = contexts;

        // check whether we are jumping from caller to callee
        // (i.e., since we are creating the depgraph backwards, we are
        // jumping from the function call to the end of the callee);
        // adjust targetFunction and targetContexts accordingly

        if (current instanceof CfgNodeCallRet) {

            // determine callee and context inside the callee;

            CfgNodeCallRet callRet = (CfgNodeCallRet) current;
            CfgNodeCall callNode = callRet.getCallNode();
            targetFunction = callNode.getCallee();
            debug("jumping from caller to end of callee: " + function.getName() + " -> " + targetFunction.getName());
            targetContexts = new HashSet<>();
            for (Context c : contexts) {
                targetContexts.add(this.depAnalysis.getPropagationContext(callNode, c));
            }
            debug("target contexts: " + targetContexts);
        }

        // check whether we are jumping from callee to caller
        // (i.e., from the callee's start to the call node)
        else if (targetNode instanceof CfgNodeCallPrep) {
            debug("jumping from start of callee to caller!");
            CfgNodeCallPrep prep = (CfgNodeCallPrep) targetNode;
            CfgNodeCall callNode = (CfgNodeCall) prep.getSuccessor(0);
            targetFunction = prep.getCaller();
            debug("caller: " + targetFunction.getName());
            targetContexts = new HashSet<>();
            for (Context c : contexts) {
                List<ReverseTarget> revs = this.depAnalysis.getReverseTargets(function, c);
                for (ReverseTarget rev : revs) {
                    if (!rev.getCallNode().equals(callNode)) {
                        continue;
                    }
                    targetContexts.addAll(rev.getContexts());
                }
            }
            if (targetContexts.isEmpty()) {
                throw new RuntimeException("SNH");
            }
            debug("target contexts: " + targetContexts);
        }

        // if the target node is a define node, we cannot be sure
        // about the target contexts; the constant that we are looking
        // up might have been defined in a completely different function;
        // here, we make a reasonable conservative decision, and use
        // *all* available contexts
        else if (targetNode instanceof CfgNodeDefine) {
            targetFunction = Cfg.getFunction(targetNode);
            targetContexts = this.analysisInfo.getAnalysisNode(targetNode.getSpecial()).getContexts();
            if (targetContexts.isEmpty()) {
                throw new RuntimeException("SNH");
            }
        }

        // check if the target cfg node is located in a different function
        // than the current cfg node; this case can happen, for instance, if
        // a global variable is written in some function, and later read in some
        // other function; the read dep directly leads us to the function where
        // the global variable was written, without warning
        else if (!Cfg.getFunction(targetNode).equals(function)) {
            debug("Unexpected function change: " + function.getName() + " -> " + Cfg.getFunction(targetNode).getName());
            targetFunction = Cfg.getFunction(targetNode);
            targetContexts = this.analysisInfo.getAnalysisNode(targetNode.getSpecial()).getContexts();
            if (targetContexts.isEmpty()) {
                throw new RuntimeException("SNH");
            }
        }

        retMe.targetFunction = targetFunction;
        retMe.targetContexts = targetContexts;
        return retMe;
    }

//  *********************************************************************************

    // checks if the given targetNode is an operation node;
    // if it is, it creates a corresponding node and returns it; otherwise,
    // it returns null
    private DepGraphNode checkOp(CfgNode targetNode) {

        if (targetNode instanceof CfgNodeAssignBinary) {
            CfgNodeAssignBinary inspectMe = (CfgNodeAssignBinary) targetNode;
            return new DepGraphOpNode(targetNode, TacOperators.opToName(inspectMe.getOperator()), true);
        } else if (targetNode instanceof CfgNodeAssignUnary) {
            CfgNodeAssignUnary inspectMe = (CfgNodeAssignUnary) targetNode;
            return new DepGraphOpNode(targetNode, TacOperators.opToName(inspectMe.getOperator()), true);
        } else if (targetNode instanceof CfgNodeCallRet) {

            CfgNodeCallRet inspectMe = (CfgNodeCallRet) targetNode;
            CfgNodeCallPrep prepNode = inspectMe.getCallPrepNode();

            if (prepNode.getCallee() == null) {
                throw new RuntimeException("SNH");
            } else {
                // a user-defined function; we don't want to clutter
                // the depgraph with nodes for these calls
                return null;
            }
        } else if (targetNode instanceof CfgNodeCallBuiltin) {
            // a builtin function
            CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) targetNode;
            String functionName = cfgNode.getFunctionName();
            return new DepGraphOpNode(targetNode, functionName, true);
        } else if (targetNode instanceof CfgNodeCallUnknown) {
            // a function / method for which no definition could be found
            CfgNodeCallUnknown callNode = (CfgNodeCallUnknown) targetNode;
            String functionName = callNode.getFunctionName();
            boolean builtin = false;
            return new DepGraphOpNode(targetNode, functionName, builtin);
        }

        return null;
    }

//  *********************************************************************************

    // never add an already existing node
    public DepGraphNode addNode(DepGraphNode node) {
        if (this.nodes.containsKey(node)) {
            throw new RuntimeException("SNH");
        }
        this.nodes.put(node, node);
        return node;
    }

//  *********************************************************************************

    public boolean containsNode(DepGraphNode node) {
        return this.nodes.containsKey(node);
    }

//  *********************************************************************************

    // you must only draw edges between already existing nodes in the graph
    public void addEdge(DepGraphNode from, DepGraphNode to) {
        if (!this.nodes.containsKey(from) || !this.nodes.containsKey(to)) {
            throw new RuntimeException("SNH");
        }
        List<DepGraphNode> toList = this.edges.get(from);
        if (toList == null) {
            toList = new LinkedList<>();
            this.edges.put(from, toList);
        }
        toList.add(to);
    }

//  *********************************************************************************

    // returns the places *used* in this node (e.g., those on the *right*
    // side of an assignment); if there are no such places (e.g., in the case
    // of CfgNodeEmpty), returns a
    // set containing an appropriate dummy placeholder literal (necessary for the
    // graph construction algorithm to work properly);
    // for concat nodes: returns the places in the right order

    /**
     * This function is quite straightforward in most cases, but the handling of arrays might be a bit confusing. For
     * this reason, here is an explanation of what is going on when the inspected cfg node is either AssignSimple or
     * CfgNodeCallPrep.
     *
     * AssignSimple:
     * left = right;
     * ...
     * ...victim...
     * - in the easiest case, victim equals left:
     * $left = $right
     * echo($left);
     * => we simply return right
     * - alternatively, victim could also be an array element of left, e.g.:
     * $left = $right;
     * echo($left[0]);
     * here, we must not return $right, but $right[0]
     * - in the previous case, it might happen that $right[0] does not
     * explicitly appear in the source code (=> a "hidden" array element),
     * so there is no Variable for $right[0] that we can return;
     * we overcome this problem by returning $right, and writing the
     * missing indices (in this case, only "0") into the list "newIndices"
     * for later use
     * => this list will correspond to "oldIndices" for the next call of
     * getUsedPlaces(); if you look at the code of getUsedPlaces()
     * (case AssignSimple) below, you will see that the first check is
     * whether oldIndices is empty or not; if it is not empty, it means
     * that we must take such hidden indices into account, e.g., look at
     * this example from bottom to top:
     * $c[0] = "harmless";
     * $b = $c;        // here, we have an old index "0" hanging around, so we have to get $c[0]
     * $a = $b;        // there is no $b[0], so we return $b (one line up) and memorize index 0
     * echo($a[0]);    // the dependency for $a[0] leads us one line up
     * the method responsible for correctly resolving old indices is called "getCorresponding()";
     * look at its description below to get a better feeling for what is going on
     *
     * CfgNodeCallPrep:
     * * "victim" is a formal param here;
     * we want to return the corresponding actual parameter
     * - for each formal parameter:
     * * let us assume that there are no old indices hanging around, then we
     * have the following cases:
     * - victim equals this formal
     * => return the corresponding actual
     * - victim is an array element of this formal
     * => return the corresponding array element of the corresponding actual
     * - none of the above:
     * continue with the next formal
     * now, let us assume that there ARE old indices hanging around; note that
     * of course, additional indices can only make victim "deeper"
     * a formal param is never indexed in a function's head
     * - victim (without additional indexes) equals this formal
     * => victim (with additional indexes) is an array element of the formal
     * - victim (without a.i.) is an array element of this formal
     * => victim (with a.i.) is an array element of this formal
     * - none of the above:
     * continue with the next formal
     *
     * @param cfgNodeX
     * @param victim
     * @param oldIndices
     * @param newIndices
     *
     * @return
     */
    private List<TacPlace> getUsedPlaces(CfgNode cfgNodeX, TacPlace victim,
                                         List<TacPlace> oldIndices, List<TacPlace> newIndices) {

        List<TacPlace> retMe = new LinkedList<>();

        // the node types for which an exception is raised are those
        // that should not even be visited by the algorithm in the first
        // place, since they don't change the value of a variable

        // LATER: you should treat this analogously to a builtin function
        // without any parameters; also take care that it is correctly modeled
        // by client analyses
        if (cfgNodeX instanceof CfgNodeAssignArray) {
            retMe.add(new Literal(""));
        } else if (cfgNodeX instanceof CfgNodeAssignBinary) {
            CfgNodeAssignBinary cfgNode = (CfgNodeAssignBinary) cfgNodeX;
            retMe.add(cfgNode.getLeftOperand());
            retMe.add(cfgNode.getRightOperand());
        } else if (cfgNodeX instanceof CfgNodeAssignRef) {
            CfgNodeAssignRef cfgNode = (CfgNodeAssignRef) cfgNodeX;
            retMe.add(cfgNode.getRight());
        } else if (cfgNodeX instanceof CfgNodeAssignSimple) {

            CfgNodeAssignSimple cfgNode = (CfgNodeAssignSimple) cfgNodeX;
            Variable left = cfgNode.getLeft();
            TacPlace right = cfgNode.getRight();

            // this is a bit more complicated because we are array-aware;
            // if there are some old indices hanging around
            if (!oldIndices.isEmpty() && right.isVariable()) {
                retMe.add(getCorresponding(left,
                    victim.getVariable(),
                    right.getVariable(),
                    oldIndices, newIndices));

                // if the victim is an array element of left
            } else if (victim.isVariable() && victim.getVariable().isArrayElement() &&
                victim.getVariable().isArrayElementOf(left)) {

                if (!right.isVariable()) {
                    // approx
                    retMe.add(right);

                    // the above happens in cases such as this one;
                    // here, "right" == FALSE (a constant)
                    /*
                    $x = false;
                    if ($cond) {
                        $x = array();
                        $x[0] = 7;
                    }
                    if ($cond) {
                        echo $x[0];
                    }
                    */

                } else {
                    retMe.add(getCorresponding(left,
                        victim.getVariable(),
                        right.getVariable(),
                        oldIndices, newIndices));
                }

                // else: no need for array awareness
            } else {
                retMe.add(right);
            }
        } else if (cfgNodeX instanceof CfgNodeAssignUnary) {
            CfgNodeAssignUnary cfgNode = (CfgNodeAssignUnary) cfgNodeX;
            retMe.add(cfgNode.getRight());
        } else if (cfgNodeX instanceof CfgNodeBasicBlock) {
            // should be handled by the caller
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeCall) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeCallPrep) {

            // return corresponding actual param ("victim" is the formal param here)
            CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
            List<TacActualParam> actualParams = cfgNode.getParamList();
            List<TacFormalParam> formalParams = cfgNode.getCallee().getParams();
            int index = -1;
            int i = 0;

            // for each formal parameter...
            for (TacFormalParam formalParam : formalParams) {
                TacActualParam actualParam = actualParams.get(i);

                // if the victim equals the formal parameter...
                if (formalParam.getVariable().equals(victim)) {

                    // if there are no old indices hanging around...
                    if (oldIndices.isEmpty()) {
                        retMe.add(actualParam.getPlace());
                    } else {
                        if (!actualParam.getPlace().isVariable()) {
                            // evil case: the actual param is not a variable!
                            retMe.add(actualParam.getPlace());
                        } else {
                            retMe.add(getCorresponding(formalParam.getVariable(),
                                victim.getVariable(),
                                actualParam.getPlace().getVariable(),
                                oldIndices, newIndices));
                        }
                    }

                    index = i;
                    break;
                }

                // if the victim is an array element of the formal param...
                if (victim.isVariable() && victim.getVariable().isArrayElement() &&
                    victim.getVariable().isArrayElementOf(formalParam.getVariable())) {

                    if (!actualParam.getPlace().isVariable()) {
                        // evil case: the actual param is not a variable!
                        retMe.add(actualParam.getPlace());
                    } else {
                        retMe.add(getCorresponding(formalParam.getVariable(),
                            victim.getVariable(),
                            actualParam.getPlace().getVariable(),
                            oldIndices, newIndices));
                    }

                    index = i;
                    break;
                }

                i++;
            }

            // note: even though it is not obvious from the above code, default
            // parameters are also handled

            if (index == -1) {
                // could not find formal parameter
                System.out.println("victim: " + victim);
                throw new RuntimeException("SNH");
            }
        } else if (cfgNodeX instanceof CfgNodeCallRet) {

            // either a call to a user-defined function
            return this.getUsedPlacesForCall((CfgNodeCallRet) cfgNodeX, victim, oldIndices, newIndices);
        } else if (cfgNodeX instanceof CfgNodeCallBuiltin) {
            return this.getUsedPlacesForBuiltin((CfgNodeCallBuiltin) cfgNodeX);
        } else if (cfgNodeX instanceof CfgNodeCallUnknown) {
            // call to an unknown function;
            // simply add all parameters;
            CfgNodeCallUnknown cfgNode = (CfgNodeCallUnknown) cfgNodeX;
            for (TacActualParam param : cfgNode.getParamList()) {
                retMe.add(param.getPlace());
            }
        } else if (cfgNodeX instanceof CfgNodeDefine) {
            CfgNodeDefine cfgNode = (CfgNodeDefine) cfgNodeX;
            retMe.add(cfgNode.getSetTo());
        } else if (cfgNodeX instanceof CfgNodeEcho) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeEmpty) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeExit) {
            throw new RuntimeException("SNH");
            // this belonged to the quick hack in DepLatticeElement.handleReturnValue
            // retMe.add(new Literal("<null>"));
        } else if (cfgNodeX instanceof CfgNodeEmptyTest) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeEval) {
            // still modeled with ID transfer function
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeGlobal) {
            CfgNodeGlobal cfgNode = (CfgNodeGlobal) cfgNodeX;
            // "global($x)" is analogous to "$x =& main.$x";
            // hence, the used variable is main.$x, which we can
            // retrieve like this:
            Variable realGlobal = mainSymTab.getVariable(cfgNode.getOperand().getName());
            if (realGlobal == null) {
                throw new RuntimeException("SNH");
            }
            retMe.add(realGlobal);
        } else if (cfgNodeX instanceof CfgNodeHotspot) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeIf) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeInclude) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeIncludeEnd) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeIncludeStart) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeIsset) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeStatic) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeTester) {
            throw new RuntimeException("SNH");
        } else if (cfgNodeX instanceof CfgNodeUnset) {
            // LATER: you should treat this as a built-in sanitization function
            // (analogously to what you should do with array())
            retMe.add(new Literal("")); // the empty string
        } else {
            throw new RuntimeException("not yet: " + cfgNodeX.getClass());
        }
        return retMe;
    }

//  ********************************************************************************

    // left:   some array (might also be an array element itself),
    //         on the left side of an assignment
    // victim: an element of left; additional, deeper indices might be
    //         specified in oldIndices
    // right:  some variable on the right side of an assignment
    //
    // TODO:
    // ONLY WORKS FINE IF THERE ARE NO NON-LITERAL INDICES INVOLVED
    // to fix this, it would be the best to replace the "array label" mechanism
    // with special array variables that have the same purpose
    //
    // tries to find an element retMe of right such that the relationship
    // "left : victim" corresponds to the relationship "right : retMe",
    // where additional indices of retMe can be written
    // into newIndices (pass an empty list to this method for this purpose);
    // e.g., if left = $a[1][2], $victim = $a[1][2][3], oldIndices = [4], and
    // right = $b[7], then this method tries to return $b[7][3][4]; if this
    // element does not explicitly exist, it tries to return $b[7][3] and writes
    // the additional index [4] into newIndices; if this element also doesn't
    // exist, it returns $b[7] with additional indices [3][4]
    private Variable getCorresponding(Variable left, Variable victim, Variable right,
                                      List<TacPlace> oldIndices, List<TacPlace> newIndices) {
        if (!victim.isArrayElementOf(left)) {
            // can happen for "return" statements (assignments to a
            // superglobal return variable)
            victim = left;
        }

        List<TacPlace> leftIndices = left.getIndices();
        List<TacPlace> victimIndices = victim.getIndices();

        // add oldIndices to victimIndices
        for (TacPlace oldIndex : oldIndices) {
            victimIndices.add(oldIndex);
        }

        //System.out.println("merged old and victim: " + victimIndices);

        // "cut" the start of victimIndices depending on the length of
        // leftIndices
        // -> the resulting indices of victim correspond to the
        // relation between left and victim
        ListIterator<TacPlace> victimIter = victimIndices.listIterator();
        for (TacPlace leftIndice : leftIndices) {
            victimIter.next();
        }

        // the least you can return is right itself;
        // try to descend into it as deeply as possible
        Variable retMe = right;
        while (victimIter.hasNext()) {
            TacPlace victimIndex = victimIter.next();
            Variable newTarget = retMe.getElement(victimIndex);
            if (newTarget == null) {
                // the requested element does not exist, so we have
                // to stop our descent; the remaining indices
                // will be written into newIndices in the following loop
                victimIter.previous();
                break;
            } else {
                retMe = retMe.getElement(victimIndex);
            }
        }

        // write the remaining, unmatched indices into newIndices
        while (victimIter.hasNext()) {
            TacPlace victimIndex = victimIter.next();
            newIndices.add(victimIndex);
        }

        return retMe;
    }

//  ********************************************************************************

    // helper function for retrieving the used places for calls to
    // user-defined functions / methods
    private List<TacPlace> getUsedPlacesForCall(CfgNodeCallRet retNode,
                                                TacPlace victim,
                                                List<TacPlace> oldIndices, List<TacPlace> newIndices) {

        List<TacPlace> retMe = new LinkedList<>();

        CfgNodeCallPrep prepNode = retNode.getCallPrepNode();
        if (prepNode.getCallee() == null) {
            throw new RuntimeException("SNH");
        }

        Variable retVar = retNode.getRetVar();

        if (!oldIndices.isEmpty()) {
            // if there are some old indices hanging around

            retMe.add(getCorresponding(victim.getVariable(),
                victim.getVariable(),
                retVar,
                oldIndices, newIndices));
        } else {
            // else: no need for array-awareness
            retMe.add(retVar);
        }

        return retMe;
    }

//  ********************************************************************************

    // helper function for retrieving the used places for calls to builtin functions
    private List<TacPlace> getUsedPlacesForBuiltin(CfgNodeCallBuiltin cfgNode) {

        List<TacPlace> retMe = new LinkedList<>();
        String functionName = cfgNode.getFunctionName();

        if (functionName.equals("mysql_query")) {
            // do nothing;
            // it is not appropriate to say that the value of the params
            // flows to the return value
        } else {
            // simply add all parameters
            for (TacActualParam param : cfgNode.getParamList()) {
                retMe.add(param.getPlace());
            }
        }

        return retMe;
    }

//  *********************************************************************************

    // determines the "folded" dep set of the given place by lubbing over
    // the given contexts
    private DepSet newFold(Map<Context, LatticeElement> phi, TacPlace place, Set<Context> contexts) {
        DepSet depSet = null;

        for (Context context : contexts) {
            DepLatticeElement element = (DepLatticeElement) phi.get(context);
            if (element == null) {
                // there is no associated analysis information for this context
                // (partly unreachable code)
                continue;
            }
            if (depSet == null) {
                depSet = element.getDep(place);
            } else {
                // EFF: this might be a memory leak, since all intermediate
                // results are automatically stored in the repository; maybe use
                // weak references to fix this
                depSet = DepSet.lub(depSet, element.getDep(place));
            }
        }

        return depSet;
    }

//  *********************************************************************************

    // determines the "folded" lattice element by lubbing over the given contexts
    private DepLatticeElement newFold(Map<Context, LatticeElement> phi, Set<Context> contexts) {
        DepLatticeElement retMe = null;

        for (Context context : contexts) {
            DepLatticeElement element = (DepLatticeElement) phi.get(context);
            if (retMe == null) {
                // EFF: it should also be possible to say "retMe = element"
                retMe = new DepLatticeElement(element);
            } else {
                // EFF: this might be a memory leak, since all intermediate
                // results are automatically stored in the repository; maybe use
                // weak references to fix this
                retMe = (DepLatticeElement) this.depAnalysis.getLattice().lub(element, retMe);
            }
        }

        return retMe;
    }

//  ********************************************************************************

    // checks whether this graph is a tree or not
    public boolean isTree() {
        int edgeCount = 0;
        for (Map.Entry<DepGraphNode, List<DepGraphNode>> entry : this.edges.entrySet()) {
            edgeCount += entry.getValue().size();
        }

        System.out.println("nodes size: " + this.nodes.size());
        System.out.println("edges size: " + edgeCount);

        return this.nodes.size() == edgeCount + 1;
    }

//  ********************************************************************************

    // cycle detection
    public boolean hasCycles() {

        // color map (white 0, grey 1, black 2)
        HashMap<DepGraphNode, Integer> colorMap = new HashMap<>();

        // initialize all nodes with white
        for (DepGraphNode node : this.nodes.keySet()) {
            colorMap.put(node, 0);
        }

        for (DepGraphNode node : this.nodes.keySet()) {
            if (hasCyclesHelper(node, colorMap)) {
                return true;
            }
        }
        return false;
    }

//  ********************************************************************************

    private boolean hasCyclesHelper(DepGraphNode node, HashMap<DepGraphNode, Integer> colorMap) {

        // mark as grey
        colorMap.put(node, 1);

        // visit successors (depth-first)
        List<DepGraphNode> successors = this.edges.get(node);
        if (successors != null) {
            for (DepGraphNode succ : this.edges.get(node)) {
                int color = colorMap.get(succ);
                if (color == 1) {
                    return true;
                } else if (color == 0) {
                    if (hasCyclesHelper(succ, colorMap)) {
                        return true;
                    }
                }
            }
        }

        // all successors visited, mark as black
        colorMap.put(node, 2);

        return false;
    }

//  ********************************************************************************

    // returns a map that gives the number of occurrences for each type of
    // operation node
    public Map<String, Integer> getOpMap() {
        Map<String, Integer> retMe = new HashMap<>();
        for (DepGraphNode node : this.nodes.keySet()) {
            if (!(node instanceof DepGraphOpNode)) {
                continue;
            }
            DepGraphOpNode opNode = (DepGraphOpNode) node;
            String opName = opNode.getName();
            Integer opCount = retMe.get(opName);
            if (opCount == null) {
                opCount = 1;
            } else {
                opCount = opCount + 1;
            }
            retMe.put(opName, opCount);
        }

        return retMe;
    }

//  ********************************************************************************

    // returns true if all leafs contain literal (string) places,
    // and false otherwise
    public boolean leafsAreStrings() {
        for (DepGraphNode node : this.getLeafNodes()) {
            // if one of the leafs is not a string, we're done
            if (!(node instanceof DepGraphNormalNode)) {
                return false;
            }
            if (!((DepGraphNormalNode) node).isString()) {
                return false;
            }
        }
        return true;
    }

//  ********************************************************************************

    // returns all the nodes of this graph
    public List<DepGraphNode> getNodes() {
        // return a copy of our node set
        List<DepGraphNode> retMe = new LinkedList<>(this.nodes.keySet());
        return retMe;
    }

//  ********************************************************************************

    // returns the leaf nodes of this graph
    public Set<DepGraphNode> getLeafNodes() {
        Set<DepGraphNode> leafCandidates = new HashSet<>(this.nodes.keySet());
        Set<DepGraphNode> nonLeafs = this.edges.keySet();
        leafCandidates.removeAll(nonLeafs);
        return leafCandidates;
    }

//  ********************************************************************************

    // returns all uninit nodes
    public Set<DepGraphUninitNode> getUninitNodes() {
        Set<DepGraphUninitNode> uninitNodes = new HashSet<>();
        for (DepGraphNode node : this.nodes.keySet()) {
            if (node instanceof DepGraphUninitNode) {
                uninitNodes.add((DepGraphUninitNode) node);
            }
        }
        return uninitNodes;
    }

//  ********************************************************************************

    public DepGraphNormalNode getRoot() {
        return this.root;
    }

//  ********************************************************************************

    // returns a string representation of this depgraph (in dot syntax)
    public String makeDotUnique(String graphName) {
        try {
            Writer outWriter = new StringWriter();
            this.writeDotUnique(graphName, new HashSet<DepGraphNode>(), true, outWriter);
            String ret = outWriter.toString();
            outWriter.close();
            return ret;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public void dumpDotUnique(String graphName, String path) {
        try {
            (new File(path)).mkdir();
            Writer outWriter = new FileWriter(path + "/" + graphName + ".dot");
            this.writeDotUnique(graphName, new HashSet<DepGraphNode>(), true, outWriter);
            outWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // dumps this depgraph to a dot file with to the given name (extension
    // is added automatically) and path
    public void dumpDot(String graphName, String path, DepClientInfo dci) {
        this.dumpDot(graphName, path, new HashSet<DepGraphNode>(), dci);
    }

    // dumps this depgraph to a dot file with to the given name (extension
    // is added automatically) and path, shading the given nodes
    public void dumpDot(String graphName, String path, Set<? extends DepGraphNode> fillUs, DepClientInfo dci) {
        try {
            (new File(path)).mkdir();
            Writer outWriter = new FileWriter(path + "/" + graphName + ".dot");
            this.writeDot(graphName, fillUs, outWriter, dci);
            outWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

//  ********************************************************************************

    public void writeDot(String graphName, Set<? extends DepGraphNode> fillUs, Writer outWriter, DepClientInfo dci)
        throws IOException {

        // distinguish between verbose and normal output
        if (MyOptions.option_V) {
            writeDotVerbose(graphName, fillUs, outWriter, dci);
        } else {
            writeDotNormal(graphName, fillUs, outWriter);
        }
    }

    // writes a dot representation of this depgraph to the given writer
    public void writeDotVerbose(String graphName, Set<? extends DepGraphNode> fillUs, Writer outWriter, DepClientInfo dci)
        throws IOException {

        outWriter.write("digraph cfg {\n  label=\"");
        outWriter.write(Dumper.escapeDot(graphName, 0));
        outWriter.write("\";\n");
        outWriter.write("  labelloc=t;\n");

        // print nodes
        int idCounter = 0;
        HashMap<DepGraphNode, Integer> node2Int = new HashMap<>();
        for (DepGraphNode tgn : this.nodes.keySet()) {
            node2Int.put(tgn, ++idCounter);

            String styleString = "";
            if (fillUs.contains(tgn)) {
                styleString = ",style=filled";
            }

            boolean isModelled = true;
            String shapeString = "shape=box";
            if (tgn == this.root) {
                shapeString = "shape=doubleoctagon";
            } else if (tgn instanceof DepGraphOpNode) {
                shapeString = "shape=ellipse";
                if (dci != null) {
                    isModelled = dci.isModelled(((DepGraphOpNode) tgn).getName());
                }
            }

            String name = tgn.dotNameVerbose(isModelled);
            outWriter.write("  n" + idCounter + " [" + shapeString + ", label=\"" +
                name + "\"" + styleString + "];\n");
        }

        // print edges
        for (Map.Entry<DepGraphNode, List<DepGraphNode>> entry : this.edges.entrySet()) {
            DepGraphNode from = entry.getKey();
            List<DepGraphNode> toList = entry.getValue();
            int i = 1;
            for (DepGraphNode to : toList) {
                if (from instanceof DepGraphOpNode) {
                    // also add number labels to the edges, but only if leaves
                    // have not been reduced yet
                    if (leavesReduced) {
                        outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
                    } else {
                        outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) +
                            "[label=\"Param #" + i++ + "\"]");
                    }
                    outWriter.write(";\n");
                } else {
                    outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
                    outWriter.write(";\n");
                }
            }
        }

        outWriter.write("}\n");
    }

    // writes a dot representation of this depgraph to the given writer
    public void writeDotNormal(String graphName, Set<? extends DepGraphNode> fillUs, Writer outWriter)
        throws IOException {

        outWriter.write("digraph cfg {\n  label=\"");
        outWriter.write(Dumper.escapeDot(graphName, 0));
        outWriter.write("\";\n");
        outWriter.write("  labelloc=t;\n");

        // print nodes
        int idCounter = 0;
        HashMap<DepGraphNode, Integer> node2Int = new HashMap<>();
        for (DepGraphNode tgn : this.nodes.keySet()) {
            node2Int.put(tgn, ++idCounter);

            String styleString = "";
            if (fillUs.contains(tgn)) {
                styleString = ",style=filled";
            }

            if (tgn instanceof DepGraphOpNode) {
                styleString = ",style=filled,color=lightblue";
            }

            String shapeString = "shape=ellipse";
            if (tgn == this.root) {
                shapeString = "shape=box";
            }

            String name = tgn.dotName();
            outWriter.write("  n" + idCounter + " [" + shapeString + ", label=\"" +
                name + "\"" + styleString + "];\n");
        }

        // print edges
        for (Map.Entry<DepGraphNode, List<DepGraphNode>> entry : this.edges.entrySet()) {
            DepGraphNode from = entry.getKey();
            List<DepGraphNode> toList = entry.getValue();
            int i = 1;
            for (DepGraphNode to : toList) {
                if (from instanceof DepGraphOpNode) {
                    // also add number labels to the edges, but only if leaves
                    // have not been reduced yet
                    if (leavesReduced) {
                        outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
                    } else {
                        outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) +
                            "[label=\"Param #" + i++ + "\"]");
                    }
                    outWriter.write(";\n");
                } else {
                    outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
                    outWriter.write(";\n");
                }
            }
        }

        outWriter.write("}\n");
    }

    // writes a dot representation of this depgraph to the given writer;
    // use this one if you need a UNIQUE representation
    public void writeDotUnique(String graphName, Set<? extends DepGraphNode> fillUs, boolean shortName, Writer outWriter)
        throws IOException {

        outWriter.write("digraph cfg {\n  label=\"");
        outWriter.write(Dumper.escapeDot(graphName, 0));
        outWriter.write("\";\n");
        outWriter.write("  labelloc=t;\n");

        // print nodes
        int idCounter = 0;
        HashMap<DepGraphNode, Integer> node2Int = new HashMap<>();

        for (DepGraphNode tgn : this.bfIterator()) {

            node2Int.put(tgn, ++idCounter);

            String styleString = "";
            if (fillUs.contains(tgn)) {
                styleString = ",style=filled";
            }

            if (tgn instanceof DepGraphOpNode) {
                styleString = ",style=filled,color=lightblue";
            }

            String shapeString = "shape=ellipse";
            if (tgn == this.root) {
                shapeString = "shape=box";
            }

            String name;
            if (shortName) {
                name = tgn.dotNameShort();
            } else {
                name = tgn.dotName();
            }
            outWriter.write("  n" + idCounter + " [" + shapeString + ", label=\"" +
                name + "\"" + styleString + "];\n");
        }

        // print edges
        List<String> lines = new LinkedList<>();
        for (Map.Entry<DepGraphNode, List<DepGraphNode>> entry : this.edges.entrySet()) {
            DepGraphNode from = entry.getKey();
            List<DepGraphNode> toList = entry.getValue();
            int i = 1;
            for (DepGraphNode to : toList) {
                if (from instanceof DepGraphOpNode) {
                    // also add number labels to the edges
                    lines.add("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) +
                        "[label=\"" + i++ + "\"];");
                } else {
                    lines.add("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) + ";");
                }
            }
        }

        Collections.sort(lines);
        for (String line : lines) {
            outWriter.write(line);
            outWriter.write("\n");
        }

        outWriter.write("}\n");
    }

//  ********************************************************************************

    // eliminates cycles (SCCs) from this string graph, replacing them with
    // special DepGraphSccNodes
    public void eliminateCycles() {

        if (!this.hasCycles()) {
            return;
        }

        // for each SCC...
        List<List<DepGraphNode>> sccs = this.getSccs();
        for (List<DepGraphNode> scc : sccs) {

            // one-element sccs are no problem
            if (scc.size() < 2) {
                continue;
            }

            // determine edges pointing into this SCC
            Set<DepGraphNode> sccPredecessors = new HashSet<>();
            for (DepGraphNode sccMember : scc) {
                Set<DepGraphNode> predecessors = this.getPredecessors(sccMember);
                predecessors.removeAll(scc);  // don't take predecessors that are inside the SCC
                sccPredecessors.addAll(predecessors);
            }

            // determine edges going out of this SCC
            Set<DepGraphNode> sccSuccessors = new HashSet<>();
            for (DepGraphNode sccMember : scc) {
                List<DepGraphNode> successors = this.getSuccessors(sccMember);
                successors.removeAll(scc);  // don't take predecessors that are inside the SCC
                sccSuccessors.addAll(successors);
            }

            // remove scc members
            for (DepGraphNode sccMember : scc) {
                //this.remove(sccMember, new HashSet<DepGraphNode>());
                this.nodes.remove(sccMember);
                this.edges.remove(sccMember);
            }

            // the replacement node
            DepGraphSccNode sccNode = new DepGraphSccNode();
            this.addNode(sccNode);

            // adjust nodes coming in to the SCC
            for (DepGraphNode pre : sccPredecessors) {
                // remove stale nodes from the out-list
                List<DepGraphNode> out = this.edges.get(pre);
                for (Iterator<DepGraphNode> iter = out.iterator(); iter.hasNext(); ) {
                    DepGraphNode outNode = iter.next();
                    if (!this.nodes.containsKey(outNode)) {
                        iter.remove();
                    }
                }
                // add new replacement node to the out-list
                out.add(sccNode);
            }

            // adjust nodes going out of the SCC
            this.edges.put(sccNode, new LinkedList<>(sccSuccessors));

            // done!

        }
    }

//  ********************************************************************************

    // returns a list of strongly connected components;
    // uses the algorithm from "The Design and Analysis of Computer Algorithms"
    // (Aho, Hopcroft, Ullman), Chapter 5.5 ("Strong Connectivity")
    public List<List<DepGraphNode>> getSccs() {
        n = 1;
        List<List<DepGraphNode>> sccs = new LinkedList<>();
        List<DepGraphNode> stack = new LinkedList<>();
        Map<DepGraphNode, Integer> dfsnum = new HashMap<>();
        Map<DepGraphNode, Integer> low = new HashMap<>();
        Set<DepGraphNode> old = new HashSet<>();
        sccVisit(this.root, stack, dfsnum, low, old, sccs);
        return sccs;
    }

//  ********************************************************************************

    // helper function for SCC computation
    private void sccVisit(DepGraphNode v, List<DepGraphNode> stack,
                          Map<DepGraphNode, Integer> dfsnum,
                          Map<DepGraphNode, Integer> low,
                          Set<DepGraphNode> old,
                          List<List<DepGraphNode>> sccs) {

        old.add(v);
        dfsnum.put(v, n);
        low.put(v, n);
        n++;
        stack.add(v);

        for (DepGraphNode w : this.getSuccessors(v)) {
            if (!old.contains(w)) {
                sccVisit(w, stack, dfsnum, low, old, sccs);
                int low_v = low.get(v);
                int low_w = low.get(w);
                low.put(v, Math.min(low_v, low_w));
            } else {
                int dfsnum_v = dfsnum.get(v);
                int dfsnum_w = dfsnum.get(w);
                if (dfsnum_w < dfsnum_v && stack.contains(w)) {
                    int low_v = low.get(v);
                    low.put(v, Math.min(dfsnum_w, low_v));
                }
            }
        }

        if (low.get(v).equals(dfsnum.get(v))) {
            List<DepGraphNode> scc = new LinkedList<>();
            DepGraphNode x;
            do {
                x = stack.remove(stack.size() - 1);
                scc.add(x);
            } while (!x.equals(v));
            sccs.add(scc);
        }
    }

//  ********************************************************************************

    public List<DepGraphNode> getSuccessors(DepGraphNode node) {
        List<DepGraphNode> retMe = this.edges.get(node);
        if (retMe == null) {
            retMe = new LinkedList<>();
        }
        return retMe;
    }

//  ********************************************************************************

    // EFF: could be much faster
    public Set<DepGraphNode> getPredecessors(DepGraphNode node) {
        Set<DepGraphNode> retMe = new HashSet<>();
        for (Map.Entry<DepGraphNode, List<DepGraphNode>> entry : this.edges.entrySet()) {
            DepGraphNode from = entry.getKey();
            List<DepGraphNode> toList = entry.getValue();
            if (toList.contains(node)) {
                retMe.add(from);
            }
        }

        return retMe;
    }

// bfIterator **********************************************************************

    // breadth first iterator
    public List<DepGraphNode> bfIterator() {

        // list for the iterator
        LinkedList<DepGraphNode> list = new LinkedList<>();

        // queue for nodes that still have to be visited
        LinkedList<DepGraphNode> queue = new LinkedList<>();

        // already visited
        Set<DepGraphNode> visited = new HashSet<>();

        queue.add(this.root);
        visited.add(this.root);

        Comparator<DepGraphNode> comp = new NodeComparator<>();
        this.bfIteratorHelper(list, queue, visited, comp);

        return list;
    }

// bfIteratorHelper ****************************************************************

    private void bfIteratorHelper(List<DepGraphNode> list,
                                  LinkedList<DepGraphNode> queue, Set<DepGraphNode> visited,
                                  Comparator<DepGraphNode> comp) {

        DepGraphNode node = queue.removeFirst();
        list.add(node);

        // handle successors
        List<DepGraphNode> successors = this.getSuccessors(node);
        if (!(node instanceof DepGraphOpNode)) {
            // only sort for non-operation nodes; for operation nodes,
            // we want to preserve the order of the parameters
            Collections.sort(successors, comp);
        }
        for (DepGraphNode succ : successors) {
            // for all successors that have not been visited yet...
            if (!visited.contains(succ)) {
                // add it to the queue
                queue.add(succ);
                // mark it as visited
                visited.add(succ);
            }
        }

        // if the queue is non-empty: recurse
        if (queue.size() > 0) {
            bfIteratorHelper(list, queue, visited, comp);
        }
    }

//  ********************************************************************************

    public boolean isRoot(DepGraphNode node) {
        return node.equals(this.root);
    }

//  ********************************************************************************

    // makes the depgraph smaller in two ways:
    // - reduces it to those nodes that are on a path from the root
    //   to any of the given leaves
    // - removes unnecessary temporary nodes that precede those nodes that
    //   represent function return variables
    public void reduceWithLeaves(Collection<? extends DepGraphNode> leaves) {

        this.leavesReduced = true;

        // mark reachable nodes
        // and nodes representing return variables
        Set<DepGraphNode> reachable = new HashSet<>();
        Set<DepGraphNormalNode> retVars = new HashSet<>();
        for (DepGraphNode leaf : leaves) {
            reduceWithLeavesHelper(leaf, reachable, retVars);
        }

        // delete all unreachable nodes

        // delete from nodes map
        for (Iterator<Map.Entry<DepGraphNode, DepGraphNode>> iter = this.nodes.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<DepGraphNode, DepGraphNode> entry = iter.next();
            DepGraphNode node = entry.getKey();
            if (!reachable.contains(node)) {
                iter.remove();
            }
        }
        // delete from edges map
        for (Iterator<Map.Entry<DepGraphNode, List<DepGraphNode>>> iter = this.edges.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<DepGraphNode, List<DepGraphNode>> entry = iter.next();
            DepGraphNode node = entry.getKey();
            List<DepGraphNode> successors = entry.getValue();
            if (!reachable.contains(node)) {
                iter.remove();
                continue;
            }
            for (Iterator<DepGraphNode> succIter = successors.iterator(); succIter.hasNext(); ) {
                DepGraphNode succ = succIter.next();
                if (!reachable.contains(succ)) {
                    succIter.remove();
                }
            }
        }

        // collapse return variable nodes with their predecessors;
        // explanation: due to the way how depgraph construction is implemented,
        // every node that represents a function return value has one or more
        // predecessors that represent the temporary that is responsible for
        // catching its value after the completed function invokation;
        // these temporary nodes can be removed from the graph

        // EFF: we are calling "getPredecessors" quite often in this method,
        // it would probably pay to make this call faster
        for (DepGraphNormalNode retVarNode : retVars) {
            Set<DepGraphNode> tempNodes = this.getPredecessors(retVarNode);
            if (tempNodes.size() != 1) {
                // there can be only one
                throw new RuntimeException("SNH");
            }
            DepGraphNode tempNode = tempNodes.iterator().next();

            if (tempNode == this.root) {
                // leave the root alone!
                continue;
            }

            // retrieve the predecessors before you remove the temporary node
            Set<DepGraphNode> preds = this.getPredecessors(tempNode);

            // remove the temporary node
            this.nodes.remove(tempNode);
            this.edges.remove(tempNode);
            for (Map.Entry<DepGraphNode, List<DepGraphNode>> entry : this.edges.entrySet()) {
                List<DepGraphNode> successors = entry.getValue();
                for (Iterator<DepGraphNode> succIter = successors.iterator(); succIter.hasNext(); ) {
                    DepGraphNode succ = succIter.next();
                    if (succ.equals(tempNode)) {
                        succIter.remove();
                    }
                }
            }

            // turn the former predecessors of the temporary node into
            // the predecessors of the retVarNode
            for (DepGraphNode pred : preds) {
                this.addEdge(pred, retVarNode);
            }
        }
    }

//  ********************************************************************************

    private void reduceWithLeavesHelper(DepGraphNode node,
                                        Set<DepGraphNode> reachable, Set<DepGraphNormalNode> retVars) {

        // stop recursion if we were already there
        if (reachable.contains(node)) {
            return;
        }

        // add to reachable set
        reachable.add(node);

        // detect function return variables
        if (node instanceof DepGraphNormalNode) {
            DepGraphNormalNode normalNode = (DepGraphNormalNode) node;
            TacPlace place = normalNode.getPlace();
            if (place.isVariable() && place.getVariable().isReturnVariable()) {
                retVars.add(normalNode);
            }
        }

        // recurse
        for (DepGraphNode pre : this.getPredecessors(node)) {
            reduceWithLeavesHelper(pre, reachable, retVars);
        }
    }

//  ********************************************************************************

    // removes all uninit nodes and returns their predecessors
    public Set<DepGraphNode> removeUninitNodes() {

        Set<DepGraphNode> retme = new HashSet<>();

        Set<DepGraphUninitNode> uninitNodes = getUninitNodes();
        for (DepGraphUninitNode uninitNode : uninitNodes) {

            Set<DepGraphNode> preds = this.getPredecessors(uninitNode);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }

            DepGraphNode pre = preds.iterator().next();

            // add predecessor to return set
            retme.add(pre);

            // remove uninit node
            this.nodes.remove(uninitNode);
            // this.edges.remove(uninitNode);   // unnecessary, because: leaf node
            List<DepGraphNode> outEdges = this.edges.get(pre);
            if (outEdges == null) {
                this.edges.remove(pre);
            } else {
                outEdges.remove(uninitNode);
                if (outEdges.isEmpty()) {
                    this.edges.remove(pre);
                }
            }
        }

        return retme;
    }

//  ********************************************************************************

    // removes all nodes that represent temporary variables and that have
    // exactly 1 predecessor and 1 successor
    public void removeTemporaries() {
        Set<DepGraphNormalNode> temporaries = this.getTemporaries();
        for (DepGraphNormalNode temp : temporaries) {

            Set<DepGraphNode> preds = this.getPredecessors(temp);
            List<DepGraphNode> succs = this.edges.get(temp);

            if (preds == null || succs == null || preds.size() != 1 || succs.size() != 1) {
                continue;
            }

            DepGraphNode pre = preds.iterator().next();
            DepGraphNode succ = succs.iterator().next();

            // redirect incoming edge
            List<DepGraphNode> outEdges = this.edges.get(pre);
            int outIndex = outEdges.indexOf(temp);
            outEdges.remove(outIndex);
            outEdges.add(outIndex, succ);

            this.nodes.remove(temp);
            this.edges.remove(temp);
        }
    }

    // returns all nodes that represent temporary variables
    private Set<DepGraphNormalNode> getTemporaries() {
        Set<DepGraphNormalNode> retme = new HashSet<>();
        for (DepGraphNode node : this.nodes.keySet()) {
            if (node instanceof DepGraphNormalNode) {
                DepGraphNormalNode nn = (DepGraphNormalNode) node;
                if (nn.getPlace().isVariable()) {
                    if (nn.getPlace().getVariable().isTemp()) {
                        retme.add(nn);
                    }
                }
            }
        }
        return retme;
    }

//  ********************************************************************************

    // reduces this depgraph to the ineffective sanitization stuff;
    // returns the number of ineffective border sanitizations
    public int reduceToIneffectiveSanit(Map<DepGraphNode, FSAAutomaton> deco,
                                        SanitAnalysis sanitAnalysis) {

        // get the "custom sanitization border"
        List<DepGraphNode> border = new LinkedList<>();
        Set<DepGraphNode> visited = new HashSet<>();
        this.getCustomSanitBorder(this.root, visited, border);

        // identify ineffective border sanitizations
        List<DepGraphNode> ineffectiveBorder = new LinkedList<>();
        for (DepGraphNode customSanit : border) {
            if (sanitAnalysis.isIneffective(customSanit, deco)) {
                ineffectiveBorder.add(customSanit);
            }
        }

        // reduce this depgraph to these ineffective sanitizations
        this.reduceToInnerNodes(ineffectiveBorder);

        return ineffectiveBorder.size();
    }

    private void getCustomSanitBorder(DepGraphNode node,
                                      Set<DepGraphNode> visited, List<DepGraphNode> border) {

        // stop if we were already there
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);

        // reached the border?
        if (SanitAnalysis.isCustomSanit(node)) {
            border.add(node);
            return;
        }

        // recurse downwards
        for (DepGraphNode succ : this.getSuccessors(node)) {
            getCustomSanitBorder(succ, visited, border);
        }
    }

//  ********************************************************************************

    // makes the depgraph smaller in the following way:
    // - reduces it to those nodes that are on a path that contains
    //   one of the given nodes (may be inner nodes)
    public void reduceToInnerNodes(Collection<? extends DepGraphNode> nodes) {

        // mark reachable nodes (upwards and downwards, starting from
        // the sanitization nodes)
        Set<DepGraphNode> reachable = new HashSet<>();
        for (DepGraphNode sanitNode : nodes) {
            reduceToInnerHelper(sanitNode, reachable);
        }

        // delete all unreachable nodes

        // delete from nodes map
        for (Iterator<Map.Entry<DepGraphNode, DepGraphNode>> iter = this.nodes.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<DepGraphNode, DepGraphNode> entry = iter.next();
            DepGraphNode node = entry.getKey();
            if (!reachable.contains(node)) {
                iter.remove();
            }
        }
        // delete from edges map
        for (Iterator<Map.Entry<DepGraphNode, List<DepGraphNode>>> iter = this.edges.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<DepGraphNode, List<DepGraphNode>> entry = iter.next();
            DepGraphNode node = entry.getKey();
            List<DepGraphNode> successors = entry.getValue();
            if (!reachable.contains(node)) {
                iter.remove();
                continue;
            }
            for (Iterator<DepGraphNode> succIter = successors.iterator(); succIter.hasNext(); ) {
                DepGraphNode succ = succIter.next();
                if (!reachable.contains(succ)) {
                    succIter.remove();
                }
            }
        }
    }

//  ********************************************************************************

    private void reduceToInnerHelper(DepGraphNode node,
                                     Set<DepGraphNode> reachable) {

        // recurse upwards
        for (DepGraphNode pre : this.getPredecessors(node)) {
            reduceToInnerHelperUp(pre, reachable);
        }

        // recurse downwards
        reduceToSanitInnerDown(node, reachable);
    }

//  ********************************************************************************

    // upwards reachability
    private void reduceToInnerHelperUp(DepGraphNode node,
                                       Set<DepGraphNode> reachable) {

        // stop recursion if we were already there
        if (reachable.contains(node)) {
            return;
        }

        // add to reachable set
        reachable.add(node);

        // recurse
        for (DepGraphNode pre : this.getPredecessors(node)) {
            reduceToInnerHelperUp(pre, reachable);
        }
    }

//  ********************************************************************************

    // downwards reachability
    private void reduceToSanitInnerDown(DepGraphNode node,
                                        Set<DepGraphNode> reachable) {

        // stop recursion if we were already there
        if (reachable.contains(node)) {
            return;
        }

        // add to reachable set
        reachable.add(node);

        // recurse
        for (DepGraphNode succ : this.getSuccessors(node)) {
            reduceToSanitInnerDown(succ, reachable);
        }
    }

//  ********************************************************************************

    // counts the number of paths through this dependence graph;
    public int countPaths() {
        // work on a copy
        return (new DepGraph(this).countPathsDestructive());
    }

//  ********************************************************************************

    // BEWARE: eliminates cycles first, so this changes the depgraph
    private int countPathsDestructive() {
        this.eliminateCycles();
        Map<DepGraphNode, Integer> node2p = new HashMap<>();
        pathCounterHelper(this.root, node2p, new HashSet<DepGraphNode>());
        return node2p.get(root);
    }

//  ********************************************************************************

    private void pathCounterHelper(DepGraphNode node, Map<DepGraphNode, Integer> node2p,
                                   Set<DepGraphNode> visited) {

        visited.add(node);

        List<DepGraphNode> successors = this.getSuccessors(node);
        if (successors != null && !successors.isEmpty()) {
            // if this node has successors, decorate them first (if not done yet)
            for (DepGraphNode succ : successors) {
                if (!visited.contains(succ) && node2p.get(succ) == null) {
                    pathCounterHelper(succ, node2p, visited);
                }
            }
            int p = 0;
            for (DepGraphNode succ : successors) {
                p += node2p.get(succ);
            }
            node2p.put(node, p);
        } else {
            // no successors
            node2p.put(node, 1);
        }
    }

//  ********************************************************************************
//  ********************************************************************************

    private class NotReachableException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private class NodeComparator<T>
        implements Comparator<T> {

        public int compare(T o1, T o2) {
            if (!(o1 instanceof DepGraphNode) || !(o2 instanceof DepGraphNode)) {
                throw new RuntimeException("SNH");
            }
            DepGraphNode n1 = (DepGraphNode) o1;
            DepGraphNode n2 = (DepGraphNode) o2;
            return n1.comparableName().compareTo(n2.dotName());
        }
    }

    // just a data storage to allow for an extended return value of the
    // switchContexts() method
    private class ContextSwitch {
        TacFunction targetFunction;
        Set<Context> targetContexts;
    }
}