package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class AssignArray extends AbstractTransferFunction {

	private Variable left;
	private boolean supported;
	private AbstractCfgNode cfgNode;

	public AssignArray(AbstractTacPlace left, AbstractCfgNode cfgNode) {

		this.left = (Variable) left;
		this.cfgNode = cfgNode;

		if (this.left.isVariableVariable() || this.left.isMember()) {
			this.supported = false;
		} else {
			this.supported = true;
		}

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!supported) {
			return inX;
		}

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);
		out.assignArray(left, cfgNode);
		return out;
	}
}
