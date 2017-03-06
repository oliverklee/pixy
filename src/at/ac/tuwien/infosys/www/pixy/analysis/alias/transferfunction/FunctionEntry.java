package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class FunctionEntry extends AbstractTransferFunction {

	private TacFunction function;
	private AliasAnalysis aliasAnalysis;

	public FunctionEntry(TacFunction function, AliasAnalysis aliasRepos) {
		this.function = function;
		this.aliasAnalysis = aliasRepos;
	}

	@SuppressWarnings("rawtypes")
	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		AliasLatticeElement in = (AliasLatticeElement) inX;
		AliasLatticeElement out = new AliasLatticeElement(in);

		Map<?, ?> globals2GShadows = this.function.getSymbolTable().getGlobals2GShadows();

		for (Iterator<?> iter = globals2GShadows.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Variable global = (Variable) entry.getKey();
			Variable gShadow = (Variable) entry.getValue();

			out.redirect(gShadow, global);
		}

		Map<?, ?> formals2FShadows = this.function.getSymbolTable().getFormals2FShadows();

		for (Iterator<?> iter = formals2FShadows.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Variable formal = (Variable) entry.getKey();
			Variable fShadow = (Variable) entry.getValue();

			out.redirect(fShadow, formal);
		}

		out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

		return out;

	}
}
