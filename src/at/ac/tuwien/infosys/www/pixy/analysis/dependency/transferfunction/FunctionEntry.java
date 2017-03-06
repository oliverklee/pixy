package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class FunctionEntry extends AbstractTransferFunction {

	private TacFunction function;

	public FunctionEntry(TacFunction function) {
		this.function = function;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		for (Map.Entry<Variable, Variable> entry : this.function.getSymbolTable().getGlobals2GShadows().entrySet()) {
			Variable global = entry.getKey();
			Variable gShadow = entry.getValue();

			out.setShadow(gShadow, global);
		}

		for (Map.Entry<Variable, Variable> entry : this.function.getSymbolTable().getFormals2FShadows().entrySet()) {
			Variable formal = entry.getKey();
			Variable fShadow = entry.getValue();
			out.setShadow(fShadow, formal);
		}

		return out;
	}
}
