package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import java.util.*;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class CallPreparation extends AbstractTransferFunction {

	private List<List<Variable>> cbrParams;
	private TacFunction caller;
	private AliasAnalysis aliasAnalysis;
	@SuppressWarnings("unused")
	private AbstractCfgNode cfgNode;

	public CallPreparation(TacFunction caller, AliasAnalysis aliasAnalysis,
			at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNode) {

		this.cfgNode = cfgNode;
		this.cbrParams = cfgNode.getCbrParams();
		this.caller = caller;
		this.aliasAnalysis = aliasAnalysis;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		AliasLatticeElement in = (AliasLatticeElement) inX;
		AliasLatticeElement out = new AliasLatticeElement(in);

		if (!this.cbrParams.isEmpty()) {

			SymbolTable placeHolderSymTab = new SymbolTable("_placeHolder");
			Map<Variable, Variable> replacements = new HashMap<Variable, Variable>();

			for (Iterator<?> iter = this.cbrParams.iterator(); iter.hasNext();) {

				List<?> pairList = (List<?>) iter.next();
				Iterator<?> pairListIter = pairList.iterator();
				Variable actualVar = (Variable) pairListIter.next();
				Variable formalVar = (Variable) pairListIter.next();

				Variable formalPlaceHolder = new Variable(formalVar.getName(), placeHolderSymTab);
				replacements.put(formalPlaceHolder, formalVar);

				out.addToGroup(formalPlaceHolder, (Variable) actualVar);

				out.createAdjustedPairCopies(actualVar, formalPlaceHolder);
			}

			SymbolTable callerSymTab = this.caller.getSymbolTable();
			if (!callerSymTab.isMain()) {
				out.removeVariables(callerSymTab);
			}

			out.replace(replacements);

		} else {

			out.removeLocals();
		}
		out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

		return out;
	}

}
