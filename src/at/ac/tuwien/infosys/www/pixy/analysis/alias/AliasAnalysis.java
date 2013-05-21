package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.tf.*;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkListPoor;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AliasAnalysis extends InterAnalysis {
    private GenericRepos<LatticeElement> repos;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  AliasAnalysis ******************************************************************

    public AliasAnalysis(TacConverter tac, AnalysisType analysisType) {
        this.repos = new GenericRepos<>();
        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(),
            analysisType, new InterWorkListPoor());
    }

    // dummy constructor
    protected AliasAnalysis() {
    }

//  initLattice ********************************************************************

    protected void initLattice() {
        this.lattice = new AliasLattice(this);
        this.startValue = this.recycle(new AliasLatticeElement());
        this.initialValue = this.lattice.getBottom();
    }

//  makeBasicBlockTf ***************************************************************

    protected TransferFunction makeBasicBlockTf(BasicBlock basicBlock, TacFunction traversedFunction) {
        // we can override the general method from Analysis with this, because
        // alias information must not change inside basic blocks
        // (all nodes inside a basic block should have an ID transfer function,
        // so we can use this shortcut)
        return TransferFunctionId.INSTANCE;
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

    protected TransferFunction assignRef(AbstractCfgNode cfgNodeX) {
        AssignReference cfgNode = (AssignReference) cfgNodeX;
        return new AliasTfAssignRef(
            cfgNode.getLeft(),
            cfgNode.getRight(),
            this,
            cfgNode);
    }

    protected TransferFunction global(AbstractCfgNode cfgNodeX) {

        // "global <var>";
        // equivalent to creating a reference to the variable in the main function with
        // the same name
        Global cfgNode = (Global) cfgNodeX;

        // operand variable of the "global" statement (= local variable)
        Variable globalOp = cfgNode.getOperand();

        // retrieve the variable from the main function with the same name
        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();
        Variable realGlobal = mainSymTab.getVariable(globalOp.getName());

        // trying to declare something global that doesn't occur in the main function?
        if (realGlobal == null) {
            System.out.println("Warning: access to non-existent global " + globalOp.getName());

            // LATER: ignore this case for now
            return TransferFunctionId.INSTANCE;
        } else {
            return new AliasTfAssignRef(
                globalOp,
                realGlobal,
                this,
                cfgNode);
        }
    }

    protected TransferFunction unset(AbstractCfgNode cfgNodeX) {
        Unset cfgNode = (Unset) cfgNodeX;
        return new AliasTfUnset(cfgNode.getOperand(), this);
    }

    protected TransferFunction assignArray(AbstractCfgNode cfgNodeX) {
        // no effect on alias information
        return TransferFunctionId.INSTANCE;
    }

    protected TransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        CallPreperation cfgNode = (CallPreperation) cfgNodeX;
        TacFunction calledFunction = cfgNode.getCallee();
        TacFunction callingFunction = traversedFunction;

        // call to an unknown function;
        // should be prevented in practice (all functions should be
        // modeled in the builtin functions file), but if
        // it happens: assume that it doesn't do anything to
        // alias information
        if (calledFunction == null) {

            // at this point, unknown functions should already be represented
            // by their own special cfg node
            throw new RuntimeException("SNH");

            // how this works:
            // - propagate with ID transfer function to Call
            // - the analysis algorithm propagates from Call
            //   to ReturnFromCall with ID transfer function
            // - we propagate from ReturnFromCall to the following node
            //   with ID
            //return TransferFunctionId.INSTANCE;
        }

        // extract actual and formal params
        List<TacActualParam> actualParams = cfgNode.getParamList();
        List<TacFormalParam> formalParams = calledFunction.getParams();

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
            tf = new AliasTfCallPrep(callingFunction, this, cfgNode);
        }

        return tf;
    }

    protected TransferFunction entry(TacFunction traversedFunction) {
        return new AliasTfEntry(traversedFunction, this);
    }

    protected TransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

        ReturnFromCall cfgNode = (ReturnFromCall) cfgNodeX;
        CallPreperation cfgNodePrep = cfgNode.getCallPrepNode();
        TacFunction calledFunction = cfgNodePrep.getCallee();

        // call to an unknown function;
        // for explanations see above (handling CallPreperation)
        if (calledFunction == null) {
            throw new RuntimeException("SNH");
            // return TransferFunctionId.INSTANCE;
        }

        // quite powerful transfer function, does many things
        TransferFunction tf = new AliasTfCallRet(
            this.interAnalysisInfo.getAnalysisNode(cfgNodePrep),
            calledFunction,
            this,
            cfgNodePrep);

        return tf;
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    // returns the set of must-aliases (Variable's) for the given variable
    // at the given node (folded over all contexts)
    public Set<Variable> getMustAliases(Variable var, AbstractCfgNode cfgNode) {
        InterAnalysisNode aNode = this.interAnalysisInfo.getAnalysisNode(cfgNode);
        if (aNode == null) {
            System.out.println(cfgNode);
            throw new RuntimeException("gotcha");
        }
        AliasLatticeElement value = this.getFoldedValue(aNode);
        if (value == null) {
            // if we enter this branch, it means that the must-aliases for variable var
            // were requested for a node inside a CFG for default function params;
            // no aliases in there
            Set<Variable> retMe = new HashSet<>();
            retMe.add(var);
            return retMe;
        } else {
            return value.getMustAliases(var);
        }
    }

    // returns the set of may-aliases (Variable's) for the given variable
    // at the given node (folded over all contexts)
    public Set<Variable> getMayAliases(Variable var, AbstractCfgNode cfgNode) {
        InterAnalysisNode aNode = this.interAnalysisInfo.getAnalysisNode(cfgNode);
        AliasLatticeElement value = this.getFoldedValue(aNode);
        if (value == null) {
            // explanations see: getMustAliases
            Set<Variable> retMe = new HashSet<>();
            retMe.add(var);
            return retMe;
        } else {
            return value.getMayAliases(var);
        }
    }

    // returns an arbitrary global must-alias of the given variable at
    // the given node (folded over all contexts); null if there is none
    public Variable getGlobalMustAlias(Variable var, AbstractCfgNode cfgNode) {
        for (Variable mustAlias : this.getMustAliases(var, cfgNode)) {
            if (mustAlias.isGlobal()) {
                return mustAlias;
            }
        }

        return null;
    }

    // returns a set of local must-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set<Variable> getLocalMustAliases(Variable var, AbstractCfgNode cfgNode) {
        Set<Variable> retMe = new HashSet<>();
        for (Variable mustAlias : this.getMustAliases(var, cfgNode)) {
            if (mustAlias.isLocal()) {
                retMe.add(mustAlias);
            }
        }

        return retMe;
    }

    // returns a set of global may-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set<Variable> getGlobalMayAliases(Variable var, AbstractCfgNode cfgNode) {
        Set<Variable> retMe = new HashSet<>();
        for (Variable mayAlias : this.getMayAliases(var, cfgNode)) {
            if (mayAlias.isGlobal()) {
                retMe.add(mayAlias);
            }
        }

        return retMe;
    }

    // returns a set of local may-aliases of the given variable at
    // the given node (folded over all contexts); empty set if there
    // are none
    public Set<Variable> getLocalMayAliases(Variable var, AbstractCfgNode cfgNode) {
        Set<Variable> retMe = new HashSet<>();
        for (Variable mayAlias : this.getMayAliases(var, cfgNode)) {
            if (mayAlias.isLocal()) {
                retMe.add(mayAlias);
            }
        }

        return retMe;
    }

    // says whether a reference assignment "left =& right" is supported by this
    // analysis; the verbose flag determines whether you want warning messages
    // in case of unsupported features (in this case, also supply the correct line
    // number)
    public static boolean isSupported(Variable left, Variable right,
                                      boolean verbose, int lineno) {

        // - none of the variables must be an array or an array element
        // - none of the variables must be a variable variable
        // - none of the variables must be a member variable
        StringBuilder message = new StringBuilder();
        String description = left + " = & " + right;
        boolean supported = true;
        if (left.isArray()) {
            message.append("Warning: Rereferencing of arrays not supported: " +
                description + "\nLine: " + lineno);
            supported = false;
        } else if (right.isArray()) {
            message.append("Warning: Rereferencing to arrays not supported: " +
                description + "\nLine: " + lineno);
            supported = false;
        } else if (left.isArrayElement()) {
            message.append("Warning: Rereferencing of array elements not supported: " +
                description + "\nLine: " + lineno);
            supported = false;
        } else if (right.isArrayElement()) {
            message.append("Warning: Rereferencing to array elements not supported: " +
                description + "\nLine: " + lineno);
            supported = false;
        } else if (left.isVariableVariable()) {
            message.append("Warning: Rereferencing of variable variables not supported: " +
                description + "\nLine: " + lineno);
            supported = false;
        } else if (right.isVariableVariable()) {
            message.append("Warning: Rereferencing to variable variables not supported: " +
                description + "\nLine: " + lineno);
            supported = false;
        } else if (left.isMember()) {
            // stay silent
            supported = false;
        } else if (right.isMember()) {
            // stay silent
            supported = false;
        }

        return supported;
    }

    // analogous to the previous method, but only for one variable and
    // always silent
    public static boolean isSupported(Variable var) {

        boolean supported = true;

        if (var.isArray()) {
            supported = false;
        } else if (var.isArrayElement()) {
            supported = false;
        } else if (var.isVariableVariable()) {
            supported = false;
        } else if (var.isMember()) {
            supported = false;
        }

        return supported;
    }

    public AliasLatticeElement getFoldedValue(InterAnalysisNode node) {

        // no need to recompute it if we already have it
        if (node.hasFoldedValue()) {
            return (AliasLatticeElement) node.getRecycledFoldedValue();
        }

        // compute...
        AliasLatticeElement foldedValue = (AliasLatticeElement) node.computeFoldedValue();
        if (foldedValue == null) {
            return foldedValue;
        }

        // recycle!
        foldedValue = (AliasLatticeElement) this.recycle(foldedValue);

        // set!
        node.setFoldedValue(foldedValue);

        return foldedValue;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  evalIf *************************************************************************

    protected Boolean evalIf(If ifNode, LatticeElement inValue) {
        // alias analysis doesn't have the necessary information for
        // evaluating conditions; hence, it must always return "I don't know"
        return null;
    }

//  recycle ************************************************************************

    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }

//  clean **************************************************************************

    // performs post-analysis cleanup operations to save memory
    public void clean() {
        this.interAnalysisInfo.foldRecycledAndClean(this);
    }
}