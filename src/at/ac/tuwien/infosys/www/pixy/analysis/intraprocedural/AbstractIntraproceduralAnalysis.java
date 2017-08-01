package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public abstract class AbstractIntraproceduralAnalysis extends AbstractAnalysis {

	protected IntraproceduralAnalysisInformation analysisInfo;

	IntraproceduralWorklist workList;

	protected void initGeneral(TacFunction function) {

		if (this.functions == null) {
			this.functions = new LinkedList<TacFunction>();
			this.initLattice();
		}

		this.functions.add(function);
		this.workList = new IntraproceduralWorklist();
		this.workList.add(function.getCfg().getHead());
		this.analysisInfo = new IntraproceduralAnalysisInformation();
		this.genericAnalysisInfo = analysisInfo;
		this.traverseCfg(function.getCfg(), function);
		IntraproceduralAnalysisNode startAnalysisNode = (IntraproceduralAnalysisNode) this.analysisInfo
				.getAnalysisNode(function.getCfg().getHead());
		startAnalysisNode.setInValue(this.startValue);
	}

	public AbstractTransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
		return this.analysisInfo.getAnalysisNode(cfgNode).getTransferFunction();
	}

	public IntraproceduralAnalysisInformation getAnalysisInfo() {
		return this.analysisInfo;
	}

	public IntraproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
		return (IntraproceduralAnalysisNode) this.analysisInfo.getAnalysisNode(cfgNode);
	}

	protected AbstractAnalysisNode makeAnalysisNode(AbstractCfgNode node, AbstractTransferFunction tf) {
		return new IntraproceduralAnalysisNode(tf);
	}

	public abstract AbstractLatticeElement recycle(AbstractLatticeElement recycleMe);

	public void analyze() {

		while (this.workList.hasNext()) {
			AbstractCfgNode node = this.workList.removeNext();
			IntraproceduralAnalysisNode analysisNode = (IntraproceduralAnalysisNode) this.analysisInfo
					.getAnalysisNode(node);
			AbstractLatticeElement inValue = analysisNode.getInValue();
			if (inValue == null) {
				throw new RuntimeException("SNH");
			}
			try {
				AbstractLatticeElement outValue;
				outValue = this.analysisInfo.getAnalysisNode(node).transfer(inValue);
				CfgEdge[] outEdges = node.getOutEdges();
				for (int i = 0; i < outEdges.length; i++) {
					if (outEdges[i] != null) {
						AbstractCfgNode succ = outEdges[i].getDest();
						propagate(outValue, succ);
					}
				}
			} catch (RuntimeException ex) {
				System.out.println("File:" + node.getFileName() + ", Line: " + node.getOriginalLineNumber());
				throw ex;
			}
		}
	}

	void propagate(AbstractLatticeElement value, AbstractCfgNode target) {

		IntraproceduralAnalysisNode analysisNode = this.analysisInfo.getAnalysisNode(target);
		if (analysisNode == null) {
			System.out.println(Dumper.makeCfgNodeName(target));
			throw new RuntimeException("SNH: " + target.getClass());
		}
		AbstractLatticeElement oldInValue = analysisNode.getInValue();
		if (oldInValue == null) {
			oldInValue = this.initialValue;
		}
		if (value == oldInValue) {
			return;
		}
		AbstractLatticeElement newInValue = this.lattice.lub(value, oldInValue);

		if (!oldInValue.equals(newInValue)) {

			analysisNode.setInValue(newInValue);

			this.workList.add(target);
		}
	}

}
