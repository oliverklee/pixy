package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class AssignReference extends AbstractTransferFunction {

	private Variable left;
	private Variable right;
	private boolean supported;

	public AssignReference(AbstractTacPlace left, AbstractTacPlace right) {

		this.left = (Variable) left;
		this.right = (Variable) right;

		this.supported = AliasAnalysis.isSupported(this.left, this.right, false, -1);

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!supported) {
			return inX;
		}
		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);
		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(left);
		Set<Variable> mayAliases = Collections.emptySet();
		out.assignSimple(left, right, mustAliases, mayAliases);
		return out;
	}
}