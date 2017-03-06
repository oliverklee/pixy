package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class CallReturn extends AbstractTransferFunction {

	private AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep;
	private TacFunction caller;
	private TacFunction callee;
	private Call callNode;

	public CallReturn(AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep, TacFunction caller,
			TacFunction callee, Call retNode) {

		this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
		this.caller = caller;
		this.callee = callee;

		this.callNode = retNode;

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {

		TypeLatticeElement origInfo = (TypeLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);

		TypeLatticeElement calleeIn = (TypeLatticeElement) inX;

		TypeLatticeElement outInfo = new TypeLatticeElement();

		outInfo.copyGlobalLike(calleeIn);

		if (this.caller.isMain()) {

			outInfo.copyMainTemporaries(origInfo);
			outInfo.handleReturnValue(this.callNode, calleeIn, callee);

		} else {

			outInfo.copyLocals(origInfo);
			outInfo.handleReturnValue(this.callNode, calleeIn, callee);

		}

		Variable invocObject = this.callNode.getObject();
		if (invocObject != null) {
			outInfo.setTypeString(invocObject, callee.getClassName());
		}

		return outInfo;

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		throw new RuntimeException("SNH");
	}

}
