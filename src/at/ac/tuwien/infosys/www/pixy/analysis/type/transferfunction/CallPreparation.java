package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class CallPreparation extends AbstractTransferFunction {

	private List<TacActualParameter> actualParams;
	private List<TacFormalParameter> formalParams;
	private TacFunction caller;
	private TacFunction callee;
	private TypeAnalysis typeAnalysis;

	public CallPreparation(List<TacActualParameter> actualParams, List<TacFormalParameter> formalParams,
			TacFunction caller, TacFunction callee, TypeAnalysis typeAnalysis) {

		this.actualParams = actualParams;
		this.formalParams = formalParams;
		this.caller = caller;
		this.callee = callee;
		this.typeAnalysis = typeAnalysis;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		TypeLatticeElement in = (TypeLatticeElement) inX;
		TypeLatticeElement out = new TypeLatticeElement(in);

		ListIterator<TacFormalParameter> formalIter = formalParams.listIterator();
		Iterator<TacActualParameter> actualIter = actualParams.iterator();

		while (formalIter.hasNext()) {

			TacFormalParameter formalParam = (TacFormalParameter) formalIter.next();

			if (actualIter.hasNext()) {

				TacActualParameter actualParam = (TacActualParameter) actualIter.next();
				out.assign(formalParam.getVariable(), actualParam.getPlace());

			} else {

				formalIter.previous();

				while (formalIter.hasNext()) {

					formalParam = (TacFormalParameter) formalIter.next();

					if (formalParam.hasDefault()) {

						ControlFlowGraph defaultCfg = formalParam.getDefaultCfg();

						AbstractCfgNode defaultNode = defaultCfg.getHead();
						while (defaultNode != null) {
							AbstractTransferFunction tf = this.typeAnalysis.getTransferFunction(defaultNode);
							out = (TypeLatticeElement) tf.transfer(out);
							defaultNode = defaultNode.getSuccessor(0);
						}
					} else {
					}
				}
			}
		}

		SymbolTable callerSymTab = this.caller.getSymbolTable();
		if (!callerSymTab.isMain()) {
			if (!(callee == caller)) {
				out.resetVariables(callerSymTab);
			}
		} else {
			out.resetTemporaries(callerSymTab);
		}
		return out;
	}
}