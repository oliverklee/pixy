package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.analysis.*;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.*;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignArray;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignReference;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallPreperation;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Define;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Isset;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Tester;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Unset;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.*;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CSAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.mod.ModAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;

import java.util.*;

/**
 * Dependency analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepAnalysis extends InterAnalysis {
    private TacConverter tac;
    private List<TacPlace> places;
    private ConstantsTable constantsTable;
    private SymbolTable superSymbolTable;
    private Variable memberPlace;
    private GenericRepository<LatticeElement> repos;

    private AliasAnalysis aliasAnalysis;
    private LiteralAnalysis literalAnalysis;
    private ModAnalysis modAnalysis;

    // has detectVulns() already been called?
    private boolean finishedDetection;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ExTaintAnalysis ****************************************************************

    public DepAnalysis(TacConverter tac,
                       AliasAnalysis aliasAnalysis,
                       LiteralAnalysis literalAnalysis,
                       AnalysisType analysisType,
                       InterWorkList workList,
                       ModAnalysis modAnalysis) {

        this.tac = tac;
        this.places = tac.getPlacesList();
        this.constantsTable = tac.getConstantsTable();
        this.superSymbolTable = tac.getSuperSymbolTable();
        this.memberPlace = tac.getMemberPlace();
        this.repos = new GenericRepository<>();

        this.aliasAnalysis = aliasAnalysis;
        this.literalAnalysis = literalAnalysis;
        this.modAnalysis = modAnalysis;

        this.finishedDetection = false;

        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(),
            analysisType, workList);
    }

//  initLattice ********************************************************************

    protected void initLattice() {

        // initialize lattice
        this.lattice = new DepLattice(
            this.places, this.constantsTable, this.functions,
            this.superSymbolTable, this.memberPlace);

        // initialize start value: a lattice element that adds no information to
        // the default lattice element
        this.startValue = new DepLatticeElement();

        // initialize initial value
        this.initialValue = this.lattice.getBottom();
    }

//  applyInsideBasicBlock **********************************************************

    // takes the given invalue and applies transfer functions from the start of the
    // basic block until the beginning of "untilHere" (i.e., the transfer function
    // of this node is NOT applied);
    // only required by DepGraph
    public DepLatticeElement applyInsideBasicBlock(
        BasicBlock basicBlock, AbstractCfgNode untilHere, DepLatticeElement invalue) {

        DepLatticeElement outValue = new DepLatticeElement(invalue);
        List<AbstractCfgNode> containedNodes = basicBlock.getContainedNodes();
        CompositeTransferFunction ctf = (CompositeTransferFunction) this.getTransferFunction(basicBlock);

        Iterator<AbstractCfgNode> nodesIter = containedNodes.iterator();
        Iterator<TransferFunction> tfIter = ctf.iterator();

        while (nodesIter.hasNext() && tfIter.hasNext()) {
            AbstractCfgNode node = nodesIter.next();
            TransferFunction tf = tfIter.next();
            if (node == untilHere) {
                break;
            }
            outValue = (DepLatticeElement) tf.transfer(outValue);
        }

        return outValue;
    }

//  applyInsideDefaultCfg **********************************************************

    // takes the given invalue and applies transfer functions from the start of the
    // default cfg (given by "defaultNode") until the beginning of "untilHere"
    // (i.e., the transfer function of this node is NOT applied);
    // only required by DepGraph
    public DepLatticeElement applyInsideDefaultCfg(AbstractCfgNode defaultNode,
                                                   AbstractCfgNode untilHere, DepLatticeElement invalue) {
        DepLatticeElement out = new DepLatticeElement(invalue);

        while (defaultNode != untilHere) {
            TransferFunction tf = this.getTransferFunction(defaultNode);
            out = (DepLatticeElement) tf.transfer(out);
            defaultNode = defaultNode.getSuccessor(0);
        }

        return out;
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

    // returns a transfer function for an AssignSimple cfg node;
    // aliasInNode:
    // - if cfgNodeX is not inside a basic block: the same node
    // - else: the basic block
    protected TransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple) cfgNodeX;
        Variable left = cfgNode.getLeft();
        Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new AssignSimple(
            left,
            cfgNode.getRight(),
            mustAliases,
            mayAliases,
            cfgNode);
    }

    protected TransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary) cfgNodeX;
        Variable left = cfgNode.getLeft();
        Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new AssignUnary(
            left,
            cfgNode.getRight(),
            cfgNode.getOperator(),
            mustAliases,
            mayAliases,
            cfgNode);
    }

    protected TransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary) cfgNodeX;
        Variable left = cfgNode.getLeft();
        Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new AssignBinary(
            left,
            cfgNode.getLeftOperand(),
            cfgNode.getRightOperand(),
            cfgNode.getOperator(),
            mustAliases,
            mayAliases,
            cfgNode);
    }

    protected TransferFunction assignRef(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference) cfgNodeX;
        return new AssignReference(
            cfgNode.getLeft(),
            cfgNode.getRight(),
            cfgNode);
    }

    protected TransferFunction unset(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset) cfgNodeX;
        return new Unset(cfgNode.getOperand(), cfgNode);
    }

    protected TransferFunction assignArray(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray) cfgNodeX;
        return new AssignArray(cfgNode.getLeft(), cfgNode);
    }

    protected TransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreperation cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreperation) cfgNodeX;
        TacFunction calledFunction = cfgNode.getCallee();
        TacFunction callingFunction = traversedFunction;

        // call to an unknown function;
        // should be prevented in practice (all functions should be
        // modeled in the builtin functions file), but if
        // it happens: assume that it doesn't do anything to
        // taint information;
        // NOTE: in the extended taint analysis, all builtin functions
        // are unknown functions! modeling is done only at the point where
        // a taint graph is constructed
        // NOTE: in the actual version of Pixy, builtin functions are
        // represented by their own special cfg node (see callBuiltin())
        if (calledFunction == null) {

            // at this point, unknown functions should already be represented
            // by their own special cfg node
            System.out.println(cfgNodeX.getFileName() + ", " + cfgNodeX.getOrigLineno());
            System.out.println(cfgNode.getFunctionNamePlace());
            throw new RuntimeException("SNH");

            // how this works:
            // - propagate with ID transfer function to Call
            // - the analysis algorithm propagates from Call
            //   to CallReturn with ID transfer function
            // - we propagate from CallReturn to the following node
            //   with a special transfer function that only assigns the
            //   taint of the unknown function's return variable to
            //   the temporary catching the function's return value
            //System.out.println("unknown function: " + cfgNode.getFunctionNamePlace());
            //return TransferFunctionId.INSTANCE;
        }

        // extract actual and formal params
        List<TacActualParameter> actualParams = cfgNode.getParamList();
        List<TacFormalParameter> formalParams = calledFunction.getParams();

        // the transfer function to be assigned to this node
        TransferFunction tf = null;

        if (actualParams.size() > formalParams.size()) {
            // more actual than formal params; either a bug or a varargs
            // occurrence;
            // note that cfgNode.getFunctionNamePlace() returns a different
            // result than function.getName() if "function" is
            // the unknown function
            throw new RuntimeException(
                "More actual than formal params for function " +
                    cfgNode.getFunctionNamePlace().toString() + " on line " + cfgNode.getOrigLineno());
        } else {
            tf = new CallPreperation(actualParams, formalParams,
                callingFunction, calledFunction, this, cfgNode);
        }

        return tf;
    }

    protected TransferFunction entry(TacFunction traversedFunction) {
        return new FunctionEntry(traversedFunction);
    }

    protected TransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        CallReturn cfgNodeRet = (CallReturn) cfgNodeX;
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreperation cfgNodePrep = cfgNodeRet.getCallPrepNode();
        TacFunction callingFunction = traversedFunction;
        TacFunction calledFunction = cfgNodePrep.getCallee();

        // call to an unknown function;
        // for explanations see above (handling CallPreperation)
        TransferFunction tf;
        if (calledFunction == null) {
            throw new RuntimeException("SNH");
        } else {
            Set<TacPlace> modSet = null;
            if (this.modAnalysis != null) {
                modSet = this.modAnalysis.getMod(calledFunction);
            }

            // quite powerful transfer function, does many things
            tf = new at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallReturn(
                this.interAnalysisInfo.getAnalysisNode(cfgNodePrep),
                callingFunction,
                calledFunction,
                cfgNodePrep,
                cfgNodeRet,
                this.aliasAnalysis,
                modSet);
        }

        return tf;
    }

    protected TransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
        return new at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallBuiltinFunction(cfgNode);
    }

    protected TransferFunction callUnknown(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;
        return new CallUnknown(cfgNode);
    }

    protected TransferFunction global(AbstractCfgNode cfgNodeX) {

        // "global <var>";
        // equivalent to: creating a reference to the variable in the main function with
        // the same name
        Global cfgNode = (Global) cfgNodeX;

        // operand variable of the "global" statement
        Variable globalOp = cfgNode.getOperand();

        // retrieve the variable from the main function with the same name
        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();
        Variable realGlobal = mainSymTab.getVariable(globalOp.getName());

        // trying to declare something global that doesn't occur in the main function?
        if (realGlobal == null) {
            // we must not simply ignore this case, since the corresponding
            // local's taint/label would remain harmless/harmless (the default value for locals);
            // => approximate by assigning unknown/unknown to the operand
            // LATER: this, on the other hand, is unsound, but it reduces the false positive rate:
            return TransferFunctionId.INSTANCE;
        } else {
            // found existent global
            return new AssignReference(globalOp, realGlobal, cfgNode);
        }
    }

    protected TransferFunction isset(AbstractCfgNode cfgNodeX) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset) cfgNodeX;
        return new Isset(
            cfgNode.getLeft(),
            cfgNode.getRight(),
            cfgNode);
    }

    protected TransferFunction define(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define) cfgNodeX;
        return new Define(this.constantsTable, this.literalAnalysis, cfgNode);
    }

    protected TransferFunction tester(AbstractCfgNode cfgNodeX) {
        // special node that only occurs inside the builtin functions file
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester) cfgNodeX;
        return new Tester(cfgNode);
    }

    protected TransferFunction echo(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  evalIf *************************************************************************

    protected Boolean evalIf(If ifNode, LatticeElement inValue) {
        return this.literalAnalysis.evalIf(ifNode);
    }

//  getDepGraph ********************************************************************

    // returns the dependency graphs for the given sink
    public List<DepGraph> getDepGraph(Sink sink) {

        List<DepGraph> retMe = new LinkedList<>();

        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();

        // get vulnerabilities for this sink
        List<SinkProblem> problems;
        problems = sink.getSinkProblems();

        // if this sink has no problems: done!
        if (problems.isEmpty()) {
            return retMe;
        }

        // create dependency graphs for all sensitive places in this sink
        for (SinkProblem problem : problems) {

            DepGraph depGraph = DepGraph.create(problem.getPlace(),
                sink.getNode(), this.interAnalysisInfo, mainSymTab, this);

            // a null depGraph is returned if this sink is unreachable
            if (depGraph == null) {
                continue;
            }

            retMe.add(depGraph);
        }

        return retMe;
    }

//  *********************************************************************************

    public TacConverter getTac() {
        return this.tac;
    }

// getDepGraphs ********************************************************************

    // returns the dependency graphs for all given sinks;
    // this (older) function collects some nice statistics
    public List<DepGraph> getDepGraphs(List<Sink> sinks) {

        List<DepGraph> retMe = new LinkedList<>();

        // sort sinks by line
        Collections.sort(sinks);

        // whether to collect & print statistics
        // (can be quite time-consuming for large programs)
        boolean statistics = false;

        // graph shape counters
        int treeCount = 0;
        int dagCount = 0;
        int cycleCount = 0;

        // leaf stringness counters
        // note: this is only counted for trees and dags
        int stringLeafs = 0;
        int nonStringLeafs = 0;

        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();

        // map operation name -> number of occurrences
        Map<String, Integer> opMap = new HashMap<>();

        // for each sink...
        int i = 0;
        for (Sink sink : sinks) {
            i++;

            // get vulnerabilities for this sink
            List<SinkProblem> problems;
            problems = sink.getSinkProblems();

            // if this sink has no problems: continue with the next one
            // (probably happens if a function's sensitive parameter is optional)
            if (problems.isEmpty()) {
                continue;
            }

            // create dependency graphs for all sensitive places in this sink
            for (SinkProblem problem : problems) {
                DepGraph depGraph = DepGraph.create(problem.getPlace(),
                    sink.getNode(), this.interAnalysisInfo, mainSymTab, this);

                // a null depGraph is returned if this sink is unreachable
                if (depGraph == null) {
                    continue;
                }

                retMe.add(depGraph);
           }
        }

        this.finishedDetection = true;
        System.out.println("skipping clean-up");
        System.out.println();

        return retMe;
    }

//  detectVulns *********************************************************************

    // call this method when the analysis is finished to detect vulnerabilities
    public void detectVulns() {
        throw new RuntimeException("dummy method");
    }

//  recycle ************************************************************************

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }

//  getMainFunction ****************************************************************

    public TacFunction getMainFunction() {
        return this.tac.getMainFunction();
    }

//  clean **************************************************************************

    // performs post-analysis cleanup operations to save memory
    public void clean() {
        // although we don't perform recycling during the analysis, we
        // do perform recycling for folding & cleaning; otherwise, cleaning
        // could result in bigger memory consumption than before
        if (this.finishedDetection) {
            this.interAnalysisInfo.foldRecycledAndClean(this);
        } else {
            throw new RuntimeException("Refusing to clean extaint analysis: no detection yet");
        }
    }

//  ********************************************************************************

    // checks if the callgraph contains unreachable code (i.e., nodes that have not
    // been associated with analysis information)
    public void checkReachability() {

        if (!(this.analysisType instanceof CSAnalysis)) {
            // in this case, we do not have a callgraph, and can't check for
            // unreachable code
            System.out.println("Warning: Can't check for unreachable code");
            return;
        }

        ConnectorComputation cc = ((CSAnalysis) this.analysisType).getConnectorComputation();
        CallGraph callGraph = cc.getCallGraph();

        // for each function in the call graph...
        for (TacFunction f : callGraph.getFunctions()) {
            AbstractCfgNode head = f.getControlFlowGraph().getHead();

            // make a depth-first traversal

            // the number of contexts for this function
            int numContexts = cc.getNumContexts(f);

            // auxiliary stack and visited set
            LinkedList<AbstractCfgNode> stack = new LinkedList<>();
            Set<AbstractCfgNode> visited = new HashSet<>();

            // visit head
            AbstractCfgNode current = head;
            visited.add(current);
            if (!this.isReachable(head, numContexts)) {
                this.warnUnreachable(current);
            } else {
                stack.add(current);
            }

            // how it works:
            // while there is something on the stack:
            // - mark the top stack element as visited
            // - try to get an unvisited successor of this element
            // - if there is such a successor: push it on the stack and continue
            // - else: pop the stack
            while (!stack.isEmpty()) {

                // inspect the top stack element
                current = stack.getLast();

                // we will try to get an unvisited successor element
                AbstractCfgNode next = null;
                for (int i = 0; (i < 2) && (next == null); i++) {
                    CfgEdge outEdge = current.getOutEdge(i);
                    if (outEdge != null) {
                        next = outEdge.getDest();
                        if (visited.contains(next)) {
                            // try another one
                            next = null;
                        } else {
                            // found it!
                        }
                    }
                }

                if (next == null) {
                    // pop from stack
                    stack.removeLast();
                } else {
                    // visit next
                    visited.add(next);

                    if (!this.isReachable(next, numContexts)) {
                        this.warnUnreachable(next);
                    } else {
                        stack.add(next);
                    }
                }
            }
        }
    }

    private boolean isReachable(AbstractCfgNode cfgNode, int numContexts) {
        Map<Context, LatticeElement> phi = this.interAnalysisInfo.getAnalysisNode(cfgNode).getPhi();
        if (phi.size() == 0) {
            // there is not a single context for this node
            return false;
        }
        for (LatticeElement elem : phi.values()) {
            if (elem == null) {
                // a null lattice element?
                throw new RuntimeException("SNH");
                //return false;
            }
        }
        return true;
    }

    private void warnUnreachable(AbstractCfgNode cfgNode) {
        System.out.println("Warning: Unreachable code");
        System.out.println("- " + cfgNode.getLoc());
        if (cfgNode instanceof CallReturn) {
            CallReturn callRet = (CallReturn) cfgNode;
            System.out.println("- return from: " + callRet.getCallNode().getFunctionNamePlace());
        } else {
            System.out.println("- " + cfgNode);
        }
    }
}