package at.ac.tuwien.infosys.www.pixy.analysis.type;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterWorkList;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.*;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignArray;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignReference;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.CallPreperation;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.CallReturn;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.Isset;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.Unset;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Quite rough analysis that tries to determine the type (class) of objects.
 *
 * Can be used for resolving ambiguous method calls (i.e., calls to methods that are defined in more than one class).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeAnalysis extends InterAnalysis {
    private GenericRepository<LatticeElement> repos;
    private Collection<String> classNames;

//  ********************************************************************************

    public TypeAnalysis(TacConverter tac,
                        AnalysisType analysisType,
                        InterWorkList workList) {

        this.repos = new GenericRepository<>();
        this.classNames = tac.getUserClasses().keySet();
        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(),
            analysisType, workList);
    }

//  ********************************************************************************

    public Set<Type> getType(Variable var, AbstractCfgNode cfgNode) {
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

    protected Boolean evalIf(If ifNode, LatticeElement inValue) {
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
    protected TransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignSimple(left, cfgNode.getRight());
    }

    protected TransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignUnary(left);
    }

    protected TransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignBinary(left);
    }

    protected TransferFunction assignRef(AbstractCfgNode cfgNodeX) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignReference(left, cfgNode.getRight());
    }

    protected TransferFunction unset(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset) cfgNodeX;
        return new Unset(cfgNode.getOperand());
    }

    protected TransferFunction assignArray(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray) cfgNodeX;
        return new AssignArray(cfgNode.getLeft());
    }

    protected TransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreperation cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreperation) cfgNodeX;
        TacFunction calledFunction = cfgNode.getCallee();
        TacFunction callingFunction = traversedFunction;

        // call to an unknown function;
        // should be prevented in practice (all functions should be
        // modeled in the builtin functions file), but if
        // it happens: assume that it doesn't do anything;
        if (calledFunction == null) {

            // how this works:
            // - propagate with ID transfer function to Call
            // - the analysis algorithm propagates from Call
            //   to CallReturn with ID transfer function
            // - CallReturn does the rest
            //System.out.println("unknown function: " + cfgNode.getFunctionNamePlace());
            return TransferFunctionId.INSTANCE;
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
                callingFunction, calledFunction, this);
        }

        return tf;
    }

    protected TransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn cfgNodeRet = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn) cfgNodeX;
        Call cfgNodeCall = cfgNodeRet.getCallNode();
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreperation cfgNodePrep = cfgNodeRet.getCallPrepNode();

        TacFunction callingFunction = traversedFunction;
        TacFunction calledFunction = cfgNodeCall.getCallee();

        // call to an unknown function;
        // for explanations see above (handling CallPreperation)
        TransferFunction tf;
        if (calledFunction == null) {

            tf = new CallReturnUnknown(cfgNodeRet);
        } else {

            tf = new CallReturn(
                this.interAnalysisInfo.getAnalysisNode(cfgNodePrep),
                callingFunction,
                calledFunction,
                cfgNodeCall);
        }

        return tf;
    }

    protected TransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
        return new CallBuiltin(cfgNode);
    }

    protected TransferFunction isset(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset) cfgNodeX;
        return new Isset((Variable) cfgNode.getLeft());
    }
}