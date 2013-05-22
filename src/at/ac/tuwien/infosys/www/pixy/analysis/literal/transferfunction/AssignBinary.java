package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Set;

/**
 * Transfer function for binary assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignBinary extends AbstractTransferFunction {
    private Variable left;
    private AbstractTacPlace leftOperand;
    private AbstractTacPlace rightOperand;
    private int op;
    private Set<Variable> mustAliases;
    private Set<Variable> mayAliases;
    private AbstractCfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignBinary(
        AbstractTacPlace left, AbstractTacPlace leftOperand, AbstractTacPlace rightOperand, int op, Set<Variable> mustAliases,
        Set<Variable> mayAliases, AbstractCfgNode cfgNode
    ) {
        this.left = (Variable) left;  // must be a variable
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.op = op;
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // let the lattice element handle the details
        out.assignBinary(left, leftOperand, rightOperand, op,
            mustAliases, mayAliases, cfgNode);

        return out;
    }
}