package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.Analysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.CSAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;

import java.util.List;

/**
 * Vase class for interprocedural analyses (Sharir and Pnueli). Can be used for the functional and the call-string
 * approach.
 *
 * The different approaches have to implement the following abstract methods:
 * - getPropagationContext
 * - getReverseTargets
 *
 * The concrete analyses derived from these approaches have to implement the remaining abstract methods:
 * - initLattice
 * - evalIf
 * - override those transfer function generators that shall return transfer functions other than the ID transfer function
 * - call initGeneral()
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class InterAnalysis extends Analysis {
    // INPUT ***********************************************************************

    // functional or CS analysis
    protected AnalysisType analysisType;

    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an InterAnalysisNode)
    protected InterAnalysisInfo interAnalysisInfo;

    // OTHER ***********************************************************************

    // the main function
    protected TacFunction mainFunction;

    // context for the main function
    protected Context mainContext;

    // worklist consisting of pairs (ControlFlowGraph node, lattice element)
    InterWorkList workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

// initGeneral *********************************************************************

    // general initialization work; taken out of the constructor to bypass the
    // restriction that superclass constructors have to be called first;
    // the "functions" map has to map function name -> TacFunction object
    protected void initGeneral(List<TacFunction> functions, TacFunction mainFunction,
                               AnalysisType analysisType, InterWorkList workList) {

        this.analysisType = analysisType;
        this.analysisType.setAnalysis(this);
        this.functions = functions;

        // determine ControlFlowGraph of main function: start analysis here
        this.mainFunction = mainFunction;
        ControlFlowGraph mainControlFlowGraph = this.mainFunction.getControlFlowGraph();
        AbstractCfgNode mainHead = mainControlFlowGraph.getHead();

        // initialize carrier lattice
        this.initLattice();

        // initialize main context
        this.mainContext = this.analysisType.initContext(this);

        // initialize worklist
        this.workList = workList;
        this.workList.add(mainHead, this.mainContext);

        // initialize analysis nodes
        this.interAnalysisInfo = new InterAnalysisInfo();
        this.genericAnalysisInfo = interAnalysisInfo;
        // assign transfer functions
        this.initTransferFunctions();

        // initialize PHI map for start node
        InterAnalysisNode startAnalysisNode = this.interAnalysisInfo.getAnalysisNode(mainHead);
        startAnalysisNode.setPhiValue(this.mainContext, this.startValue);
    }

//  initTransferFunctions ***********************************************************

    // controls the assignment of transfer functions to analysis nodes by calling
    // traverseCfg()
    void initTransferFunctions() {

        // handle default CFGs (for default parameters) first;
        // for all functions...
        for (TacFunction function : this.functions) {
            for (TacFormalParam param : function.getParams()) {
                // if this param has a default value, it also has a small CFG;
                // traverse it as well...;
                // NOTE: default CFGs will not be associated with analysis information,
                // see transfer functions for CallPreperation; analogous to the
                // contents of basic blocks
                if (param.hasDefault()) {
                    ControlFlowGraph defaultControlFlowGraph = param.getDefaultControlFlowGraph();
                    this.traverseCfg(defaultControlFlowGraph, function);
                }
            }
        }

        // now handle "normal" CFGs;
        // for all functions...
        for (TacFunction function : this.functions) {
            // extract and traverse CFG
            this.traverseCfg(function.getControlFlowGraph(), function);
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

//  getPropagationContext **********************************************************

    // returns the context to which interprocedural propagation shall
    // be conducted (used at call nodes)
    public Context getPropagationContext(Call callNode, Context context) {
        return this.analysisType.getPropagationContext(callNode, context);
    }

//  getReverseTargets **************************************************************

    // returns a set of ReverseTarget objects to which interprocedural
    // propagation shall be conducted (used at exit nodes)
    public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, Context context) {
        return this.analysisType.getReverseTargets(exitedFunction, context);
    }

//  getTransferFunction ************************************************************

    public TransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        return this.interAnalysisInfo.getTransferFunction(cfgNode);
    }

//  getAnalysisInfo *****************************************************************

    public InterAnalysisInfo getInterAnalysisInfo() {
        return this.interAnalysisInfo;
    }

//  getAnalysisNode ****************************************************************

    public InterAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return this.interAnalysisInfo.getAnalysisNode(cfgNode);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

//  makeAnalysisNode ***************************************************************

    // creates and returns an analysis node for the given parameters that is
    // appropriate for the analysis type (functional / call-string)
    protected AnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, TransferFunction tf) {
        return this.analysisType.makeAnalysisNode(cfgNode, tf);
    }

//  evalIf **************************************************************************

    // returns Boolean.TRUE, Boolean.FALSE, or null if it can't be evaluated
    protected abstract Boolean evalIf(If ifNode, LatticeElement inValue);

//  useSummaries *******************************************************************

    // indicates whether to use function summaries during the analysis or not
    // (works for functional approach, but would lead to wrong results for
    // call string analysis; there is a test case for literal analysis that
    // demonstrates this)
    protected boolean useSummaries() {
        return this.analysisType.useSummaries();
    }

//  ********************************************************************************

    private static void debug(String s) {
    }

//  analyze ************************************************************************

    // this method applies the worklist algorithm
    public void analyze() {

        int steps = 0;

        // for each element in the worklist...
        // (each worklist element is a pair of CFG node & context lattice element)
        while (this.workList.hasNext()) {

            steps++;
            if (steps % 10000 == 0) System.out.println("Steps so far: " + steps);

            // remove the element from the worklist
            InterWorkListElement element = this.workList.removeNext();

            // extract information from the element
            AbstractCfgNode node = element.getCfgNode();
            Context context = element.getContext();

            // get incoming value at node n (you need to understand the PHI table :)
            InterAnalysisNode analysisNode = this.interAnalysisInfo.getAnalysisNode(node);
            LatticeElement inValue = analysisNode.getPhiValue(context);
            if (inValue == null) {
                throw new RuntimeException("SNH");
            }

            try {

                // distinguish between various types of CFG nodes
                if (node instanceof Call) {

                    Call callNode = (Call) node;

                    // get necessary function information (= called function)
                    TacFunction function = callNode.getCallee();
                    ReturnFromCall callRet = (ReturnFromCall) node.getOutEdge(0).getDest();

                    if (function == null) {
                        // callee could not be determined yet;
                        // the search for a function summary doesn't make
                        // sense; simply go on to the return node;
                        // the concrete analysis is responsible for handling
                        // calls to unknown functions in the transfer functions
                        // for CallPrep and CallRet

                        // note: even though calls to unknown functions will be
                        // replaced with a special cfg node at the end of tac conversion,
                        // this case might still occur *during* tac conversion
                        // (especially during include file resolution)

                        propagate(context, inValue, callRet);
                        continue;
                    }

                    ControlFlowGraph functionControlFlowGraph = function.getControlFlowGraph();

                    AbstractCfgNode exitNode = functionControlFlowGraph.getTail();
                    // the tail of the function's CFG has to be an exit node
                    if (!(exitNode instanceof CfgExit)) {
                        throw new RuntimeException("SNH");
                    }

                    Context propagationContext = this.getPropagationContext(callNode, context);

                    // look if the exit node's PHI map has an entry under the context
                    // resulting from this call
                    InterAnalysisNode exitAnalysisNode = this.interAnalysisInfo.getAnalysisNode(exitNode);
                    if (exitAnalysisNode == null) {
                        // this can only mean that there is no way to reach the
                        // function's natural exit node, i.e. there is something like
                        // die() on each path to the natural exit node; in this
                        // case, we simply enter the function; this can lead to
                        // redundant computations, but it is simpler than a
                        // special, more efficient treatment of this rare case
                        AbstractCfgNode entryNode = functionControlFlowGraph.getHead();
                        propagate(propagationContext, inValue, entryNode);
                        continue;
                    }

                    LatticeElement exitInValue = exitAnalysisNode.getPhiValue(propagationContext);

                    if (this.useSummaries() && exitInValue != null) {

                        // previously computed function summary can be used;
                        // determine successor node (unique) of this call node
                        CfgEdge[] outEdges = callNode.getOutEdges();
                        AbstractCfgNode succ = outEdges[0].getDest();
                        propagate(context, exitInValue, succ);
                    } else {

                        // there is no function summary yet (or we don't want to
                        // use summaries)

                        // necessary for call-string analyses
                        // EFF: think about additional conditions to add here
                        if ((this.analysisType instanceof CSAnalysis) && exitInValue != null) {
                            this.workList.add(exitNode, propagationContext);
                        }

                        // there is no function summary yet (or we don't want to
                        // use summaries), so compute it now by entering the function
                        AbstractCfgNode entryNode = functionControlFlowGraph.getHead();
                        propagate(propagationContext, inValue, entryNode);
                    }

                    // calls to a builtin function are simply treated by invoking
                    // the corresponding transfer function; covered by the catch-all below
                    //} else if (node instanceof CallOfBuiltinFunction) {

                } else if (node instanceof CfgExit) {

                    CfgExit exitNode = (CfgExit) node;

                    // the function to this exit node
                    TacFunction function = exitNode.getEnclosingFunction();

                    // no need to proceed if this is the exit node of the
                    // main function
                    if (function == this.mainFunction) {
                        continue;
                    }

                    // the exit node gets a special treatment: pass incoming value
                    // in a lazy manner
                    // LatticeElement outValue = this.analysisInfo[node.getId()].transfer(inValue);
                    LatticeElement outValue = inValue;

                    // get targets that we have to return to
                    for (ReverseTarget reverseTarget : this.getReverseTargets(function, context)) {
                        // extract target call node
                        Call callNode = reverseTarget.getCallNode();

                        // determine successor node (unique) of the call node
                        CfgEdge[] outEdges = callNode.getOutEdges();
                        ReturnFromCall callRetNode = (ReturnFromCall) outEdges[0].getDest();

                        // determine predecessor node (unique) of the call node
                        CallPreperation callPrepNode = callRetNode.getCallPrepNode();

                        for (Context targetContext : reverseTarget.getContexts()) {
                            // if the incoming value at the callprep node is undefined, this means
                            // that the analysis hasn't made the call under this context
                            // (can happen for call-string analysis);
                            // => don't propagate
                            //if (this.analysisInfo[callPrepNode.getId()].getPhiValue(targetContext) == null) {
                            InterAnalysisNode callPrepANode = this.interAnalysisInfo.getAnalysisNode(callPrepNode);
                            if (callPrepANode.getPhiValue(targetContext) == null) {
                                // don't propagate
                            } else {
                                // propagate!
                                propagate(targetContext, outValue, callRetNode);
                            }
                        }
                    }
                } else if (node instanceof If) {

                    If ifNode = (If) node;

                    LatticeElement outValue = this.interAnalysisInfo.getAnalysisNode(node).transfer(inValue);
                    CfgEdge[] outEdges = node.getOutEdges();

                    // try to evaluate the "if" condition
                    Boolean eval = this.evalIf(ifNode, inValue);

                    if (eval == null) {
                        // static evaluation of if condition failed, continue
                        // analysis along both outgoing edges

                        propagate(context, outValue, outEdges[0].getDest());
                        propagate(context, outValue, outEdges[1].getDest());
                    } else if (eval == Boolean.TRUE) {
                        // continue analysis along true edge
                        propagate(context, outValue, outEdges[1].getDest());
                    } else {
                        // continue analysis along false edge
                        propagate(context, outValue, outEdges[0].getDest());
                    }
                } else if (node instanceof ReturnFromCall) {

                    // a call return node is to be handled just as a normal node,
                    // with the exception that it also needs to know about the
                    // current context

                    // apply transfer function to incoming value
                    InterAnalysisNode aNode = this.interAnalysisInfo.getAnalysisNode(node);
                    LatticeElement outValue = aNode.transfer(inValue, context);

                    // for each outgoing edge...
                    CfgEdge[] outEdges = node.getOutEdges();
                    for (CfgEdge outEdge : outEdges) {
                        if (outEdge != null) {

                            // determine the successor
                            AbstractCfgNode succ = outEdge.getDest();

                            // propagate the result of applying the transfer function
                            // to the successor (under the current context)
                            propagate(context, outValue, succ);
                        }
                    }
                } else {

                    // apply transfer function to incoming value
                    LatticeElement outValue;
                    outValue = this.interAnalysisInfo.getAnalysisNode(node).transfer(inValue);

                    // for each outgoing edge...
                    CfgEdge[] outEdges = node.getOutEdges();
                    for (CfgEdge outEdge : outEdges) {
                        if (outEdge != null) {

                            // determine the successor
                            AbstractCfgNode succ = outEdge.getDest();

                            // propagate the result of applying the transfer function
                            // to the successor (under the current context)
                            propagate(context, outValue, succ);
                        }
                    }
                }
            } catch (RuntimeException ex) {
                System.out.println("File:" + node.getFileName() + ", Line: " + node.getOrigLineno());
                throw ex;
            }
        }

        if (!MyOptions.optionB && MyOptions.optionV) {
            System.out.println("Steps total: " + steps);
        }
        // worklist algorithm finished!
    }

// propagate ***********************************************************************

    // helper method for analyze();
    // propagates a value under the given context to the target node
    void propagate(Context context, LatticeElement value, AbstractCfgNode target) {
        // analysis information for the target node
        InterAnalysisNode analysisNode = this.interAnalysisInfo.getAnalysisNode(target);

        if (analysisNode == null) {
            System.out.println(Dumper.makeCfgNodeName(target));
            throw new RuntimeException("SNH: " + target.getClass());
        }

        // determine the target's old PHI value
        LatticeElement oldPhiValue = analysisNode.getPhiValue(context);
        if (oldPhiValue == null) {
            // initial value of this analysis
            oldPhiValue = this.initialValue;
        }

        // speedup: if incoming value and target value are exactly the same
        // object, then the result certainly can't change
        if (value == oldPhiValue) {
            System.out.println("exact match!");
            return;
        }

        // the new PHI value is computed as usual (with lub)
        LatticeElement newPhiValue = this.lattice.lub(value, oldPhiValue);

        // if the PHI value changed...
        if (!oldPhiValue.equals(newPhiValue)) {
            // update analysis information
            analysisNode.setPhiValue(context, newPhiValue);

            // add this node (under the current context) to the worklist
            this.workList.add(target, context);
        }
    }
}