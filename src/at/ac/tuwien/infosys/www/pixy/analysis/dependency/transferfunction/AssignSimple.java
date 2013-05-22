package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Set;

/**
 * Transfer function for simple assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignSimple extends AbstractTransferFunction {
    private Variable left;
    private Set<Variable> mustAliases;
    private Set<Variable> mayAliases;
    private AbstractCfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public AssignSimple(
        AbstractTacPlace left, AbstractTacPlace right, Set<Variable> mustAliases, Set<Variable> mayAliases, AbstractCfgNode cfgNode
    ) {
        this.left = (Variable) left;  // must be a variable
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        DependencyLatticeElement in = (DependencyLatticeElement) inX;
        DependencyLatticeElement out = new DependencyLatticeElement(in);

        // let the lattice element handle the details
        out.assign(left, mustAliases, mayAliases, cfgNode);

        return out;
    }
}