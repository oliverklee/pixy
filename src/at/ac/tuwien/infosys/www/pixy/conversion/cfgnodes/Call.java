package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A function call.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Call extends AbstractCfgNode {
    // can also be a variable
    private AbstractTacPlace functionNamePlace;

    private TacFunction callee;

    // the return variable of the called function
    private Variable retVar;
    // temporary variable to hold the return value
    private Variable tempVar;

    // a list of actual params (TacActualParameter objects)
    private List<TacActualParameter> paramList;

    // list of cbr params; description see getCbrParams()
    private List<List<Variable>> cbrParamList;

    // if this is a method call, this field CAN contain the name of the
    // class that contains the callee method; is null if the name could
    // not be resolved during tac conversion
    private String calleeClassName;

    // object upon which this method was invoked;
    // is null if
    // - this is not a method invocation
    // - or if it is a static one
    // - of it it is a constructor invocation ("new")
    private Variable object;

// CONSTRUCTORS ********************************************************************

    // if you pass "null" for "function", don't forget to call "setFunction" later
    public Call(
        AbstractTacPlace functionNamePlace, TacFunction calledFunction, ParseNode node,
        TacFunction enclosingFunction, Variable retVar, AbstractTacPlace tempPlace,
        List<TacActualParameter> paramList, Variable object) {

        super(node);
        this.functionNamePlace = functionNamePlace;
        //this.callee = calledFunction;
        if (calledFunction != null) {
            calledFunction.addCalledFrom(this);
        }
        this.setEnclosingFunction(enclosingFunction);

        this.retVar = retVar;
        this.tempVar = (Variable) tempPlace;    // must be a variable

        this.paramList = paramList;
        this.cbrParamList = null;

        this.calleeClassName = null;
        this.object = object;
    }

// GET *****************************************************************************

    public TacFunction getCallee() {
        return this.callee;
    }

    public AbstractTacPlace getFunctionNamePlace() {
        return this.functionNamePlace;
    }

    public List<Variable> getVariables() {
        // only the params are relevant for globals replacement
        List<Variable> retMe = new LinkedList<>();
        for (TacActualParameter param : this.paramList) {
            AbstractTacPlace paramPlace = param.getPlace();
            if (paramPlace instanceof Variable) {
                retMe.add((Variable) paramPlace);
            } else {
                retMe.add(null);
            }
        }
        return retMe;
    }

    public Variable getRetVar() {
        return this.retVar;
    }

    public Variable getTempVar() {
        return this.tempVar;
    }

    public List<TacActualParameter> getParamList() {
        return this.paramList;
    }

    // returns a list consisting of two-element-lists consisting of
    // (actual cbr-param, formal cbr-param) (Variable objects)
    public List<List<Variable>> getCbrParams() {

        if (this.cbrParamList != null) {
            return this.cbrParamList;
        }

        List<TacActualParameter> actualParams = this.paramList;
        List<TacFormalParameter> formalParams = this.getCallee().getParams();

        this.cbrParamList = new LinkedList<>();

        Iterator<TacActualParameter> actualIter = actualParams.iterator();
        Iterator<TacFormalParameter> formalIter = formalParams.iterator();

        while (actualIter.hasNext()) {
            TacActualParameter actualParam = actualIter.next();
            TacFormalParameter formalParam = formalIter.next();

            // if this is a cbr-param...
            if (actualParam.isReference() || formalParam.isReference()) {

                // the actual part of a cbr-param must always be a variable
                if (!(actualParam.getPlace() instanceof Variable)) {
                    throw new RuntimeException("Error in the PHP file!");
                }

                Variable actualVar = (Variable) actualParam.getPlace();
                Variable formalVar = formalParam.getVariable();

                // check for unsupported features;
                // none of the variables must be an array or etc.;
                // in such a case, ignore it and continue with the next cbr-param
                boolean supported = AliasAnalysis.isSupported(
                    formalVar, actualVar, true, this.getOrigLineno());

                if (!supported) {
                    continue;
                }

                List<Variable> pairList = new LinkedList<>();
                pairList.add(actualVar);
                pairList.add(formalVar);
                cbrParamList.add(pairList);
            }
        }

        return cbrParamList;
    }

    public String getCalleeClassName() {
        return this.calleeClassName;
    }

    public Variable getObject() {
        return this.object;
    }

// SET *****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        TacActualParameter param = this.paramList.get(index);
        param.setPlace(replacement);
    }

    public void setCallee(TacFunction function) {
        this.callee = function;
        function.addCalledFrom(this);
    }

    public void setRetVar(Variable retVar) {
        this.retVar = retVar;
    }

    public void setCalleeClassName(String s) {
        this.calleeClassName = s;
    }
}