package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for reference assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignReference extends TransferFunction {
    private Variable left;
    private Variable right;
    private boolean supported;
    private AbstractCfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignReference(TacPlace left, TacPlace right, AbstractCfgNode cfgNode) {

        this.left = (Variable) left;    // must be a variable
        this.right = (Variable) right;  // must be a variable
        this.cfgNode = cfgNode;

        // check for unsupported features
        this.supported = AliasAnalysis.isSupported(this.left, this.right, false, -1);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // if this reference assignment is not supported by our alias analysis,
        // we simply ignore it
        if (!supported) {
            return inX;
        }

        DependencyLatticeElement in = (DependencyLatticeElement) inX;
        DependencyLatticeElement out = new DependencyLatticeElement(in);

        // "left =& right" means that left is redirected to right;
        // for the taint mapping, this means that nothing changes
        // except that left receives the taint of right;
        // array label mapping: the same;
        // we achieve this through the following actions:

        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(left);
        Set<Variable> mayAliases = Collections.emptySet();

        // let the lattice element handle the details
        out.assign(left, mustAliases, mayAliases, cfgNode);

        return out;
    }
}