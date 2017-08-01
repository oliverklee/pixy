package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class FunctionEntry extends AbstractTransferFunction {

	private TacFunction function;

	public FunctionEntry(TacFunction function) {
		this.function = function;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		Map<Variable, Variable> globals2GShadows = this.function.getSymbolTable().getGlobals2GShadows();

		for (Map.Entry<Variable, Variable> entry : globals2GShadows.entrySet()) {
			Variable global = entry.getKey();
			Variable gShadow = entry.getValue();
			out.setShadow(gShadow, global);
		}

		Map<Variable, Variable> formals2FShadows = this.function.getSymbolTable().getFormals2FShadows();

		for (Map.Entry<Variable, Variable> entry : formals2FShadows.entrySet()) {
			Variable formal = entry.getKey();
			Variable fShadow = entry.getValue();
			out.setShadow(fShadow, formal);
		}
		return out;
	}
}