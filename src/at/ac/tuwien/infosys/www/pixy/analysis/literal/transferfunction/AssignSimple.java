package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class AssignSimple extends AbstractTransferFunction {

	private Variable left;
	private AbstractTacPlace right;
	private Set<Variable> mustAliases;
	private Set<Variable> mayAliases;

	public AssignSimple(AbstractTacPlace left, AbstractTacPlace right, Set<Variable> mustAliases,
			Set<Variable> mayAliases) {

		this.left = (Variable) left;
		this.right = right;
		this.mustAliases = mustAliases;
		this.mayAliases = mayAliases;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		out.assignSimple(left, right, mustAliases, mayAliases);

		return out;
	}
}
