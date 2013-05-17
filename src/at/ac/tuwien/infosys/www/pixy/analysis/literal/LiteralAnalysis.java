package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkList;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.tf.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralAnalysis
    extends InterAnalysis {

    private TacConverter tac;

    private GenericRepos<LatticeElement> repos;

    // preceding alias analysis (required for correct results)
    private AliasAnalysis aliasAnalysis;

    // list of CfgNodeInclude's
    private List<CfgNodeInclude> includeNodes;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  LiteralAnalysis ****************************************************************

    public LiteralAnalysis(
        TacConverter tac,
        AliasAnalysis aliasAnalysis, AnalysisType analysisType,
        InterWorkList workList) {

        this.tac = tac;
        this.repos = new GenericRepos<LatticeElement>();
        this.aliasAnalysis = aliasAnalysis;
        this.includeNodes = new LinkedList<CfgNodeInclude>();

        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(),
            analysisType, workList);
    }

    // dummy constructor
    public LiteralAnalysis() {
    }

//  initLattice ********************************************************************

    protected void initLattice() {
        this.lattice = new LiteralLattice(
            this.tac.getPlacesList(), this.tac.getConstantsTable(), this.functions,
            this.tac.getSuperSymbolTable());
        // start value for literal analysis:
        // a lattice element that adds no information to the default lattice element
        this.startValue = new LiteralLatticeElement();
        this.initialValue = this.lattice.getBottom();
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

    // returns a transfer function for an AssignSimple cfg node;
    // aliasInNode:
    // - if cfgNodeX is not inside a basic block: the same node
    // - else: the basic block
    protected TransferFunction assignSimple(CfgNode cfgNodeX, CfgNode aliasInNode) {

        CfgNodeAssignSimple cfgNode = (CfgNodeAssignSimple) cfgNodeX;
        Variable left = cfgNode.getLeft();
        Set mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new LiteralTfAssignSimple(
            left,
            cfgNode.getRight(),
            mustAliases,
            mayAliases);
    }

    protected TransferFunction assignUnary(CfgNode cfgNodeX, CfgNode aliasInNode) {

        CfgNodeAssignUnary cfgNode = (CfgNodeAssignUnary) cfgNodeX;
        Variable left = cfgNode.getLeft();
        Set mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new LiteralTfAssignUnary(
            left,
            cfgNode.getRight(),
            cfgNode.getOperator(),
            mustAliases,
            mayAliases);
    }

    protected TransferFunction assignBinary(CfgNode cfgNodeX, CfgNode aliasInNode) {

        CfgNodeAssignBinary cfgNode = (CfgNodeAssignBinary) cfgNodeX;
        Variable left = cfgNode.getLeft();
        Set mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new LiteralTfAssignBinary(
            left,
            cfgNode.getLeftOperand(),
            cfgNode.getRightOperand(),
            cfgNode.getOperator(),
            mustAliases,
            mayAliases,
            cfgNode);
    }

    protected TransferFunction assignRef(CfgNode cfgNodeX) {
        CfgNodeAssignRef cfgNode = (CfgNodeAssignRef) cfgNodeX;
        return new LiteralTfAssignRef(
            cfgNode.getLeft(),
            cfgNode.getRight());
    }

    protected TransferFunction unset(CfgNode cfgNodeX) {
        CfgNodeUnset cfgNode = (CfgNodeUnset) cfgNodeX;
        return new LiteralTfUnset(cfgNode.getOperand());
    }

    protected TransferFunction assignArray(CfgNode cfgNodeX) {
        CfgNodeAssignArray cfgNode = (CfgNodeAssignArray) cfgNodeX;
        return new LiteralTfAssignArray(cfgNode.getLeft());
    }

    protected TransferFunction callPrep(CfgNode cfgNodeX, TacFunction traversedFunction) {

        CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
        TacFunction calledFunction = cfgNode.getCallee();
        TacFunction callingFunction = traversedFunction;

        // call to an unknown function;
        // should be prevented in practice (all functions should be
        // modeled in the builtin functions file), but if
        // it happens: assume that it doesn't do anything to
        // literal information
        if (calledFunction == null) {
            // how this works:
            // - propagate with ID transfer function to CfgNodeCall
            // - the analysis algorithm propagates from CfgNodeCall
            //   to CfgNodeCallRet with ID transfer function
            // - we propagate from CfgNodeCallRet to the following node
            //   with a special transfer function that only assigns the
            //   literal of the unknown function's return variable to
            //   the temporary catching the function's return value
            return TransferFunctionId.INSTANCE;
        }

        // extract actual and formal params
        List actualParams = cfgNode.getParamList();
        List formalParams = calledFunction.getParams();

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
                    cfgNode.getFunctionNamePlace().toString() + " in file " +
                    cfgNode.getFileName() + ", line " + cfgNode.getOrigLineno());
        } else {
            tf = new LiteralTfCallPrep(actualParams, formalParams,
                callingFunction, calledFunction, this, cfgNode);
        }

        return tf;
    }

    protected TransferFunction entry(TacFunction traversedFunction) {
        return new LiteralTfEntry(traversedFunction);
    }

    protected TransferFunction callRet(CfgNode cfgNodeX, TacFunction traversedFunction) {

        CfgNodeCallRet cfgNodeRet = (CfgNodeCallRet) cfgNodeX;
        CfgNodeCallPrep cfgNodePrep = cfgNodeRet.getCallPrepNode();
        TacFunction callingFunction = traversedFunction;
        TacFunction calledFunction = cfgNodePrep.getCallee();

        //System.out.println("rcall: " + callingFunction.getName() + " <- " + calledFunction.getName());

        // call to an unknown function;
        // for explanations see above (handling CfgNodeCallPrep)
        TransferFunction tf;
        if (calledFunction == null) {
            tf = new LiteralTfCallRetUnknown(cfgNodeRet);
        } else {

            // quite powerful transfer function, does many things
            tf = new LiteralTfCallRet(
                this.interAnalysisInfo.getAnalysisNode(cfgNodePrep),
                callingFunction,
                calledFunction,
                cfgNodePrep,
                cfgNodeRet,
                this.aliasAnalysis,
                this.lattice.getBottom());
        }

        return tf;
    }

    protected TransferFunction callBuiltin(CfgNode cfgNodeX, TacFunction traversedFunction) {
        CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) cfgNodeX;
        return new LiteralTfCallBuiltin(cfgNode);
    }

    protected TransferFunction callUnknown(CfgNode cfgNodeX, TacFunction traversedFunction) {
        CfgNodeCallUnknown cfgNode = (CfgNodeCallUnknown) cfgNodeX;
        return new LiteralTfCallUnknown(cfgNode);
    }

    protected TransferFunction global(CfgNode cfgNodeX) {

        // "global <var>";
        // equivalent to: creating a reference to the variable in the main function with
        // the same name
        CfgNodeGlobal cfgNode = (CfgNodeGlobal) cfgNodeX;

        // operand variable of the "global" statement
        Variable globalOp = cfgNode.getOperand();

        // retrieve the variable from the main function with the same name
        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();
        Variable realGlobal = mainSymTab.getVariable(globalOp.getName());

        // trying to declare something global that doesn't occur in the main function?
        if (realGlobal == null) {
            // we must not simply ignore this case, since the corresponding
            // local's literal would remain "NULL" (the default value for locals);
            // => approximate by assigning TOP to the operand

            Set mustAliases = this.aliasAnalysis.getMustAliases(globalOp, cfgNode);
            Set mayAliases = this.aliasAnalysis.getMayAliases(globalOp, cfgNode);

            return new LiteralTfAssignSimple(
                globalOp,
                Literal.TOP,
                mustAliases,
                mayAliases);
        } else {
            return new LiteralTfAssignRef(globalOp, realGlobal);
        }
    }

    protected TransferFunction isset(CfgNode cfgNodeX) {

        CfgNodeIsset cfgNode = (CfgNodeIsset) cfgNodeX;
        return new LiteralTfIsset(
            cfgNode.getLeft(),
            cfgNode.getRight());
    }

    protected TransferFunction define(CfgNode cfgNodeX) {
        CfgNodeDefine cfgNode = (CfgNodeDefine) cfgNodeX;
        return new LiteralTfDefine(this.tac.getConstantsTable(), cfgNode);
    }

    protected TransferFunction tester(CfgNode cfgNodeX) {
        // special node that only occurs inside the builtin functions file
        CfgNodeTester cfgNode = (CfgNodeTester) cfgNodeX;
        return new LiteralTfTester(cfgNode);
    }

    protected TransferFunction include(CfgNode cfgNodeX) {
        this.includeNodes.add((CfgNodeInclude) cfgNodeX);
        return TransferFunctionId.INSTANCE;
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // returns the (folded) literal of the given place coming in to the given node;
    // TOP if the cfgNode is unreachable (and hence can't return a folded value)
    public Literal getLiteral(TacPlace place, CfgNode cfgNode) {

        LiteralLatticeElement element =
            (LiteralLatticeElement) this.interAnalysisInfo.getAnalysisNode(cfgNode).getUnrecycledFoldedValue();

        if (element == null) {
            return Literal.TOP;
        } else {
            return element.getLiteral(place);
        }
    }

    public Literal getLiteral(String varName, CfgNode cfgNode) {
        Variable var = this.tac.getVariable(cfgNode.getEnclosingFunction(), varName);
        if (var == null) {
            // you gave me the name of a variable that does not exist
            return Literal.TOP;
        }
        return this.getLiteral(var, cfgNode);
    }

    public List<CfgNodeInclude> getIncludeNodes() {
        return this.includeNodes;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  evalIf *************************************************************************

    // NOTE: messages about successful evaluation of an "if" expression which
    // obviously can't be evaluated statically is not necessarily an indication
    // of an analysis bug: it only means that under the current analysis state,
    // the condition clearly evaluates to a known boolean; further iterations
    // might change this
    protected Boolean evalIf(CfgNodeIf ifNode, LatticeElement inValueX) {
        return null;
    }

    // evaluates the given if-condition using the folded incoming values
    // (don't call this before literal analysis hasn't finished its work)
    public Boolean evalIf(CfgNodeIf ifNode) {

        // incoming value at if node (folded)
        LiteralLatticeElement folded =
            (LiteralLatticeElement) getAnalysisNode(ifNode).getUnrecycledFoldedValue();
        if (folded == null) {
            // this means that literal analysis never reaches this point;
            // throw new RuntimeException("SNH, line " + ifNode.getOrigLineno());
            return null;
        }

        return this.evalIf(ifNode, folded);
    }

//  recycle ************************************************************************

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }

//  clean **************************************************************************

    // performs post-analysis cleanup operations to save memory
    public void clean() {
        // although we don't perform recycling during the analysis, we
        // do perform recycling for folding & cleaning; otherwise, cleaning
        // could result in bigger memory consumption than before
        this.interAnalysisInfo.foldRecycledAndClean(this);
    }
}