package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Unset extends AbstractTransferFunction {

	private Variable operand;
	private AliasAnalysis aliasAnalysis;
	private boolean supported;

	public Unset(AbstractTacPlace operand, AliasAnalysis aliasAnalysis) {

		if (!operand.isVariable()) {
			throw new RuntimeException("Trying to unset a non-variable.");
		}

		this.operand = operand.getVariable();
		this.aliasAnalysis = aliasAnalysis;
		this.supported = AliasAnalysis.isSupported(this.operand);

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		AliasLatticeElement in = (AliasLatticeElement) inX;

		if (!this.supported) {
			return in;
		}

		AliasLatticeElement out = new AliasLatticeElement(in);

		out.unset(this.operand);

		out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

		return out;
	}
}
