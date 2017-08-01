package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class CallPreparation extends AbstractTransferFunction {

	private List<?> actualParams;
	private List<?> formalParams;
	private TacFunction caller;
	private TacFunction callee;
	private DependencyAnalysis depAnalysis;
	private AbstractCfgNode cfgNode;

	public CallPreparation(List<?> actualParams, List<?> formalParams, TacFunction caller, TacFunction callee,
			DependencyAnalysis depAnalysis, AbstractCfgNode cfgNode) {

		this.actualParams = actualParams;
		this.formalParams = formalParams;
		this.caller = caller;
		this.callee = callee;
		this.depAnalysis = depAnalysis;
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		ListIterator<?> formalIter = formalParams.listIterator();
		Iterator<?> actualIter = actualParams.iterator();

		while (formalIter.hasNext()) {

			TacFormalParameter formalParam = (TacFormalParameter) formalIter.next();

			if (actualIter.hasNext()) {

				actualIter.next();

				out.setFormal(formalParam, cfgNode);

			} else {

				formalIter.previous();

				while (formalIter.hasNext()) {

					formalParam = (TacFormalParameter) formalIter.next();

					if (formalParam.hasDefault()) {

						ControlFlowGraph defaultCfg = formalParam.getDefaultCfg();

						AbstractCfgNode defaultNode = defaultCfg.getHead();
						while (defaultNode != null) {
							AbstractTransferFunction tf = this.depAnalysis.getTransferFunction(defaultNode);
							out = (DependencyLatticeElement) tf.transfer(out);
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
