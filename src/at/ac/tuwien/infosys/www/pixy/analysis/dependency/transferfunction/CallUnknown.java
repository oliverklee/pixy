package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLabel;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;

public class CallUnknown extends AbstractTransferFunction {

	private CallUnknownFunction cfgNode;

	public CallUnknown(CallUnknownFunction cfgNode) {
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		Set<DependencyLabel> ets = new HashSet<DependencyLabel>();
		ets.add(DependencyLabel.create(this.cfgNode));
		DependencySet retDepSet = DependencySet.create(ets);
		DependencySet retArrayLabel = retDepSet;

		out.handleReturnValueBuiltin(this.cfgNode.getTempVar(), retDepSet, retArrayLabel);

		return out;

	}

}
