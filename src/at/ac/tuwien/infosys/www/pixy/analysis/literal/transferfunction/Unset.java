package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for unset nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Unset extends AbstractTransferFunction {
    private Variable operand;
    private boolean supported;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Unset(AbstractTacPlace operand) {

        // only variables can be unset
        if (!operand.isVariable()) {
            throw new RuntimeException("Trying to unset a non-variable.");
        }

        this.operand = (Variable) operand;
        this.supported = AliasAnalysis.isSupported(this.operand);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        // if this statement is not supported by our alias analysis,
        // we simply ignore it
        if (!supported) {
            return inX;
        }

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // unsetting a variable means setting it to null for literal analysis
        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(operand);
        Set<Variable> mayAliases = Collections.emptySet();
        out.assignSimple(operand, Literal.NULL, mustAliases, mayAliases);

        return out;
    }
}