package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * A function call.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallOfBuiltinFunction extends AbstractCfgNode {
    // name of the called builtin function
    private String functionName;

    // parameter list
    private List<TacActualParameter> paramList;

    // temporary variable to hold the return value
    private Variable tempVar;

// CONSTRUCTORS ********************************************************************

    public CallOfBuiltinFunction(String functionName,
                                 List<TacActualParameter> paramList, TacPlace tempPlace, ParseNode node) {

        super(node);
        this.functionName = functionName.toLowerCase();
        this.paramList = paramList;
        this.tempVar = (Variable) tempPlace;
    }

// GET *****************************************************************************

    public String getFunctionName() {
        return this.functionName;
    }

    public List<TacActualParameter> getParamList() {
        return this.paramList;
    }

    public Variable getTempVar() {
        return this.tempVar;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
        for (TacActualParameter param : this.paramList) {
            TacPlace paramPlace = param.getPlace();
            if (paramPlace instanceof Variable) {
                retMe.add((Variable) paramPlace);
            } else {
                retMe.add(null);
            }
        }
        return retMe;
    }

// SET *****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        TacActualParameter param = this.paramList.get(index);
        param.setPlace(replacement);
    }
}