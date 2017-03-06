package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.transferfunction.Add;
import at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural.AbstractIntraproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeStart;

public class InclusionDominatorAnalysis extends AbstractIntraproceduralAnalysis {

	private GenericRepository<AbstractLatticeElement> repos;;

	public InclusionDominatorAnalysis(TacFunction function) {
		this.repos = new GenericRepository<AbstractLatticeElement>();
		this.initGeneral(function);
	}

	protected void initLattice() {
		this.lattice = new InclusionDominatorLattice(this);
		this.startValue = this.recycle(new InclusionDominatorLatticeElement());
		this.initialValue = this.lattice.getBottom();
	}

	protected AbstractTransferFunction makeBasicBlockTf(BasicBlock basicBlock, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction includeStart(AbstractCfgNode cfgNodeX) {
		return new Add(cfgNodeX, this);
	}

	protected AbstractTransferFunction includeEnd(AbstractCfgNode cfgNodeX) {
		return includeStart(cfgNodeX);
	}

	public List<AbstractCfgNode> getIncludeChain(AbstractCfgNode cfgNode) {

		InclusionDominatorLatticeElement latElem = (InclusionDominatorLatticeElement) this.getAnalysisNode(cfgNode)
				.getInValue();
		List<?> dominators = latElem.getDominators();

		LinkedList<AbstractCfgNode> chain = new LinkedList<AbstractCfgNode>();
		for (Iterator<?> iterator = dominators.iterator(); iterator.hasNext();) {
			AbstractCfgNode dom = (AbstractCfgNode) iterator.next();
			if (dom instanceof IncludeStart) {
				chain.add(dom);
			} else if (dom instanceof IncludeEnd) {
				IncludeEnd incEnd = (IncludeEnd) dom;
				if (incEnd.isPeer((AbstractCfgNode) chain.getLast())) {
					chain.removeLast();
				} else {
					throw new RuntimeException("SNH");
				}
			} else {
				throw new RuntimeException("SNH");
			}
		}
		return chain;

	}

	public static List<AbstractCfgNode> computeChain(TacFunction function, AbstractCfgNode cfgNode) {
		InclusionDominatorAnalysis incDomAnalysis = new InclusionDominatorAnalysis(function);
		incDomAnalysis.analyze();
		return incDomAnalysis.getIncludeChain(cfgNode);
	}

	public AbstractLatticeElement recycle(AbstractLatticeElement recycleMe) {
		return this.repos.recycle(recycleMe);
	}

}
