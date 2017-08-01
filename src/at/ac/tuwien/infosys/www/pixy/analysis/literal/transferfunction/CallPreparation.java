package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
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
	@SuppressWarnings("unused")
	private TacFunction callee;
	private LiteralAnalysis literalAnalysis;
	@SuppressWarnings("unused")
	private AbstractCfgNode cfgNode;

	public CallPreparation(List<TacActualParameter> actualParams, List<TacFormalParameter> formalParams,
			TacFunction caller, TacFunction callee, LiteralAnalysis literalAnalysis, AbstractCfgNode cfgNode) {

		this.actualParams = actualParams;
		this.formalParams = formalParams;
		this.caller = caller;
		this.callee = callee;
		this.literalAnalysis = literalAnalysis;
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		ListIterator<TacFormalParameter> formalIter = formalParams.listIterator();
		Iterator<TacActualParameter> actualIter = actualParams.iterator();

		while (formalIter.hasNext()) {

			TacFormalParameter formalParam = (TacFormalParameter) formalIter.next();

			if (actualIter.hasNext()) {
				TacActualParameter actualParam = (TacActualParameter) actualIter.next();
				AbstractTacPlace actualPlace = actualParam.getPlace();
				out.setFormal(formalParam, actualPlace);
			} else {
				formalIter.previous();

				while (formalIter.hasNext()) {

					formalParam = (TacFormalParameter) formalIter.next();

					if (formalParam.hasDefault()) {

						ControlFlowGraph defaultCfg = formalParam.getDefaultCfg();

						AbstractCfgNode defaultNode = defaultCfg.getHead();
						while (defaultNode != null) {
							AbstractTransferFunction tf = this.literalAnalysis.getTransferFunction(defaultNode);
							out = (LiteralLatticeElement) tf.transfer(out);
							defaultNode = defaultNode.getSuccessor(0);
						}

					} else {

					}
				}
			}
		}
		SymbolTable callerSymTab = this.caller.getSymbolTable();
		if (!callerSymTab.isMain()) {
			out.resetVariables(callerSymTab);
		}

		return out;
	}

}
