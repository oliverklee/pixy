package at.ac.tuwien.infosys.www.pixy.analysis.type;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkList;
import at.ac.tuwien.infosys.www.pixy.analysis.type.tf.*;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// quite rough analysis that tries to determine the type (class) of objects;
// can be used for resolving ambiguous method calls (i.e., calls to methods
// that are defined in more than one class)
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeAnalysis
    extends InterAnalysis {

    private GenericRepos<LatticeElement> repos;
    private Collection<String> classNames;

//  ********************************************************************************

    public TypeAnalysis(TacConverter tac,
                        AnalysisType analysisType,
                        InterWorkList workList) {

        this.repos = new GenericRepos<LatticeElement>();
        this.classNames = tac.getUserClasses().keySet();
        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(),
            analysisType, workList);
    }

//  ********************************************************************************

    public Set<Type> getType(Variable var, CfgNode cfgNode) {
        InterAnalysisNode ian = this.interAnalysisInfo.getAnalysisNode(cfgNode);
        if (ian == null) {
            // this means that this cfg node was not assigned an analysis node
            // (should never happen)
            System.out.println(var);
            System.out.println(cfgNode);
            System.out.println(cfgNode.getLoc());
            throw new RuntimeException("SNH");
        }
        TypeLatticeElement elem = (TypeLatticeElement) ian.computeFoldedValue();
        if (elem == null) {
            // this cfg node has no associated analysis info,
            // which means that it is unreachable
            // (e.g., because it is inside a function that is never called)
            return null;
        }
        return elem.getType(var);
    }

//  ********************************************************************************

    protected Boolean evalIf(CfgNodeIf ifNode, LatticeElement inValue) {
        return null;
    }

    protected void initLattice() {

        this.lattice = new TypeLattice(this.classNames);

        // initialize start value: a lattice element that adds no information to
        // the default lattice element
        this.startValue = new TypeLatticeElement();

        // initialize initial value
        this.initialValue = this.lattice.getBottom();
    }

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
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
        Variable left = (Variable) cfgNode.getLeft();

        return new TypeTfAssignSimple(left, cfgNode.getRight());
    }

    protected TransferFunction assignUnary(CfgNode cfgNodeX, CfgNode aliasInNode) {

        CfgNodeAssignUnary cfgNode = (CfgNodeAssignUnary) cfgNodeX;
        Variable left = (Variable) cfgNode.getLeft();

        return new TypeTfAssignUnary(left);
    }

    protected TransferFunction assignBinary(CfgNode cfgNodeX, CfgNode aliasInNode) {

        CfgNodeAssignBinary cfgNode = (CfgNodeAssignBinary) cfgNodeX;
        Variable left = (Variable) cfgNode.getLeft();

        return new TypeTfAssignBinary(left);
    }

    protected TransferFunction assignRef(CfgNode cfgNodeX) {

        CfgNodeAssignRef cfgNode = (CfgNodeAssignRef) cfgNodeX;
        Variable left = (Variable) cfgNode.getLeft();

        return new TypeTfAssignRef(left, cfgNode.getRight());
    }

    protected TransferFunction unset(CfgNode cfgNodeX) {
        CfgNodeUnset cfgNode = (CfgNodeUnset) cfgNodeX;
        return new TypeTfUnset(cfgNode.getOperand());
    }

    protected TransferFunction assignArray(CfgNode cfgNodeX) {
        CfgNodeAssignArray cfgNode = (CfgNodeAssignArray) cfgNodeX;
        return new TypeTfAssignArray(cfgNode.getLeft());
    }

    protected TransferFunction callPrep(CfgNode cfgNodeX, TacFunction traversedFunction) {

        CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
        TacFunction calledFunction = cfgNode.getCallee();
        TacFunction callingFunction = traversedFunction;

        // call to an unknown function;
        // should be prevented in practice (all functions should be
        // modeled in the builtin functions file), but if
        // it happens: assume that it doesn't do anything;
        if (calledFunction == null) {

            // how this works:
            // - propagate with ID transfer function to CfgNodeCall
            // - the analysis algorithm propagates from CfgNodeCall
            //   to CfgNodeCallRet with ID transfer function
            // - CfgNodeCallRet does the rest
            //System.out.println("unknown function: " + cfgNode.getFunctionNamePlace());
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
                    cfgNode.getFunctionNamePlace().toString() + " on line " + cfgNode.getOrigLineno());
        } else {
            tf = new TypeTfCallPrep(actualParams, formalParams,
                callingFunction, calledFunction, this);
        }

        return tf;
    }

    protected TransferFunction callRet(CfgNode cfgNodeX, TacFunction traversedFunction) {

        CfgNodeCallRet cfgNodeRet = (CfgNodeCallRet) cfgNodeX;
        CfgNodeCall cfgNodeCall = cfgNodeRet.getCallNode();
        CfgNodeCallPrep cfgNodePrep = cfgNodeRet.getCallPrepNode();

        TacFunction callingFunction = traversedFunction;
        TacFunction calledFunction = cfgNodeCall.getCallee();

        // call to an unknown function;
        // for explanations see above (handling CfgNodeCallPrep)
        TransferFunction tf;
        if (calledFunction == null) {

            tf = new TypeTfCallRetUnknown(cfgNodeRet);
        } else {

            tf = new TypeTfCallRet(
                this.interAnalysisInfo.getAnalysisNode(cfgNodePrep),
                callingFunction,
                calledFunction,
                cfgNodeCall);
        }

        return tf;
    }

    protected TransferFunction callBuiltin(CfgNode cfgNodeX, TacFunction traversedFunction) {
        CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) cfgNodeX;
        return new TypeTfCallBuiltin(cfgNode);
    }

    protected TransferFunction isset(CfgNode cfgNodeX) {
        CfgNodeIsset cfgNode = (CfgNodeIsset) cfgNodeX;
        return new TypeTfIsset((Variable) cfgNode.getLeft());
    }
}