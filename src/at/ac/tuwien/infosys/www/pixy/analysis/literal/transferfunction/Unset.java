package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Unset extends AbstractTransferFunction {

	private Variable operand;
	private boolean supported;

	public Unset(AbstractTacPlace operand) {

		if (!operand.isVariable()) {
			throw new RuntimeException("Trying to unset a non-variable.");
		}

		this.operand = (Variable) operand;
		this.supported = AliasAnalysis.isSupported(this.operand);

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!supported) {
			return inX;
		}
		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(operand);
		Set<Variable> mayAliases = Collections.emptySet();
		out.assignSimple(operand, Literal.NULL, mustAliases, mayAliases);

		return out;
	}
}