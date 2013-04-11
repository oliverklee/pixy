package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.*;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

//*********************************************************************************
//CfgNodeDefine ******************************************************************
//*********************************************************************************

public class CfgNodeDefine
extends CfgNode {

    // the first parameter (the name of the constant to be set)
    private TacPlace setMe;

    // the second parameter (the value that the constant shall be set to)
    private TacPlace setTo;

    // the third parameter
    private TacPlace caseInsensitive;

// CONSTRUCTORS ********************************************************************

    public CfgNodeDefine(TacPlace setMe, TacPlace setTo, TacPlace caseInsensitive,
            ParseNode node) {

        super(node);
        this.setMe = setMe;
        this.setTo = setTo;
        this.caseInsensitive = caseInsensitive;
    }

// GET *****************************************************************************

    public TacPlace getSetMe() {
        return this.setMe;
    }

    public TacPlace getSetTo() {
        return this.setTo;
    }

    public TacPlace getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
        if (this.setMe instanceof Variable) {
            retMe.add((Variable) setMe);
        } else {
            retMe.add(null);
        }
        if (this.setTo instanceof Variable) {
            retMe.add((Variable) setTo);
        } else {
            retMe.add(null);
        }
        if (this.caseInsensitive instanceof Variable) {
            retMe.add((Variable) caseInsensitive);
        } else {
            retMe.add(null);
        }

        return retMe;
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
        case 0:
            this.setMe = replacement;
            break;
        case 1:
            this.setTo = replacement;
            break;
        case 2:
            this.caseInsensitive = replacement;
            break;
        default:
            throw new RuntimeException("SNH");
        }
    }
}