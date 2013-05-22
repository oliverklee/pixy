package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for "isset" tests.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Isset extends AbstractTransferFunction {
    private Variable setMe;
    private AbstractTacPlace testMe;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Isset(AbstractTacPlace setMe, AbstractTacPlace testMe) {
        this.setMe = (Variable) setMe;  // must be a variable
        this.testMe = testMe;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        // System.out.println("transfer method: " + setMe + " = " + setTo);
        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        if (!setMe.isTemp()) {
            throw new RuntimeException("SNH");
        }

        // not so intelligent, but sound;
        // "setMe" is a temporary variable, which has no aliases
        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(setMe);
        Set<Variable> mayAliases = Collections.emptySet();
        out.assignSimple(setMe, Literal.TOP, mustAliases, mayAliases);

        return out;
    }
}