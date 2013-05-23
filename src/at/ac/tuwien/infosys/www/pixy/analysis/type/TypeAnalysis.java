package at.ac.tuwien.infosys.www.pixy.analysis.type;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;

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
public class TypeAnalysis extends AbstractInterproceduralAnalysis {
    private GenericRepository<AbstractLatticeElement> repos;
    private Collection<String> classNames;

//  ********************************************************************************

    public TypeAnalysis(TacConverter tac,
                        AbstractAnalysisType analysisType,
                        InterproceduralWorklist workList) {

        this.repos = new GenericRepository<>();
        this.classNames = tac.getUserClasses().keySet();
        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(),
            analysisType, workList);
    }

//  ********************************************************************************

    public Set<Type> getType(Variable var, AbstractCfgNode cfgNode) {
        AbstractInterproceduralAnalysisNode ian = this.interproceduralAnalysisInformation.getAnalysisNode(cfgNode);
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

    protected Boolean evalIf(If ifNode, AbstractLatticeElement inValue) {
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

    public AbstractLatticeElement recycle(AbstractLatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

    // returns a transfer function for an AssignSimple cfg node;
    // aliasInNode:
    // - if cfgNodeX is not inside a basic block: the same node
    // - else: the basic block
    protected AbstractTransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignSimple(left, cfgNode.getRight());
    }

    protected AbstractTransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignUnary(left);
    }

    protected AbstractTransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignBinary(left);
    }

    protected AbstractTransferFunction assignRef(AbstractCfgNode cfgNodeX) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference) cfgNodeX;
        Variable left = cfgNode.getLeft();

        return new AssignReference(left, cfgNode.getRight());
    }

    protected AbstractTransferFunction unset(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset) cfgNodeX;
        return new Unset(cfgNode.getOperand());
    }

    protected AbstractTransferFunction assignArray(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray) cfgNodeX;
        return new AssignArray(cfgNode.getLeft());
    }

    protected AbstractTransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation) cfgNodeX;
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
        AbstractTransferFunction tf = null;

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
            tf = new CallPreparation(actualParams, formalParams,
                callingFunction, calledFunction, this);
        }

        return tf;
    }

    protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn cfgNodeRet = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn) cfgNodeX;
        Call cfgNodeCall = cfgNodeRet.getCallNode();
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNodePrep = cfgNodeRet.getCallPrepNode();

        TacFunction callingFunction = traversedFunction;
        TacFunction calledFunction = cfgNodeCall.getCallee();

        // call to an unknown function;
        // for explanations see above (handling CallPreparation)
        AbstractTransferFunction tf;
        if (calledFunction == null) {

            tf = new CallReturnUnknown(cfgNodeRet);
        } else {

            tf = new CallReturn(
                this.interproceduralAnalysisInformation.getAnalysisNode(cfgNodePrep),
                callingFunction,
                calledFunction,
                cfgNodeCall);
        }

        return tf;
    }

    protected AbstractTransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
        CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
        return new CallBuiltin(cfgNode);
    }

    protected AbstractTransferFunction isset(AbstractCfgNode cfgNodeX) {
        at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset) cfgNodeX;
        return new Isset((Variable) cfgNode.getLeft());
    }
}