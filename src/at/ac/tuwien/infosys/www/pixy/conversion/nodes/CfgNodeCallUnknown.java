package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.*;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.*;

// *********************************************************************************
// CfgNodeCall *********************************************************************
// *********************************************************************************

// a function call
public class CfgNodeCallUnknown
extends CfgNode {

    // name of the called unknown function
    private String functionName;

    // parameter list
    private List<TacActualParam> paramList;

    // temporary variable to hold the return value
    private Variable tempVar;

    // is this a call to an unknown method?
    private boolean isMethod;


// CONSTRUCTORS ********************************************************************

    public CfgNodeCallUnknown(String functionName, List<TacActualParam> paramList,
            TacPlace tempPlace, ParseNode node, boolean isMethod) {

        super(node);
        this.functionName = functionName.toLowerCase();
        this.paramList = paramList;
        this.tempVar = (Variable) tempPlace;
        this.isMethod = isMethod;
    }

// GET *****************************************************************************

    public String getFunctionName() {
        return this.functionName;
    }

    public List<TacActualParam> getParamList() {
        return this.paramList;
    }

    public Variable getTempVar() {
        return this.tempVar;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
        for (Iterator iter = this.paramList.iterator(); iter.hasNext();) {
            TacActualParam param = (TacActualParam) iter.next();
            TacPlace paramPlace = param.getPlace();
            if (paramPlace instanceof Variable) {
                retMe.add((Variable) paramPlace);
            } else {
                retMe.add(null);
            }
        }
        return retMe;
    }

    public boolean isMethod() {
        return this.isMethod;
    }

// SET *****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        TacActualParam param = (TacActualParam) this.paramList.get(index);
        param.setPlace(replacement);
    }
}