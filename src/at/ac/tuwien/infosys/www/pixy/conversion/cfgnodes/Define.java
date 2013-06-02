package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the CFG node of a constant definition using define(key, value).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Define extends AbstractCfgNode {
    // the first parameter (the name of the constant to be set)
    private AbstractTacPlace setMe;

    // the second parameter (the value that the constant shall be set to)
    private AbstractTacPlace setTo;

    // the third parameter
    private AbstractTacPlace caseInsensitive;

// CONSTRUCTORS ********************************************************************

    public Define(AbstractTacPlace setMe, AbstractTacPlace setTo, AbstractTacPlace caseInsensitive,
                  ParseNode node) {

        super(node);
        this.setMe = setMe;
        this.setTo = setTo;
        this.caseInsensitive = caseInsensitive;
    }

// GET *****************************************************************************

    public AbstractTacPlace getSetMe() {
        return this.setMe;
    }

    public AbstractTacPlace getSetTo() {
        return this.setTo;
    }

    public AbstractTacPlace getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<>();
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