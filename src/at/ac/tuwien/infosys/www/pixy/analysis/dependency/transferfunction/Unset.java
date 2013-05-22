package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for unset nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Unset extends TransferFunction {
    private Variable operand;
    private AbstractCfgNode cfgNode;
    private boolean supported;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Unset(TacPlace operand, AbstractCfgNode cfgNode) {

        // only variables can be unset
        if (!operand.isVariable()) {
            throw new RuntimeException("Trying to unset a non-variable.");
        }

        this.operand = (Variable) operand;
        this.cfgNode = cfgNode;
        this.supported = true;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // if this statement is not supported by our alias analysis,
        // we simply ignore it
        if (!supported) {
            return inX;
        }

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // unsetting a variable means setting it to NULL (untainted/clean)
        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(operand);
        Set<Variable> mayAliases = Collections.emptySet();
        out.assign(operand, mustAliases, mayAliases, cfgNode);

        return out;
    }
}