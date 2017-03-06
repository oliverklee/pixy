package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class AssignReference extends AbstractTransferFunction {

	private Variable left;
	private Variable right;
	private boolean supported;
	private AbstractCfgNode cfgNode;

	public AssignReference(AbstractTacPlace left, AbstractTacPlace right, AbstractCfgNode cfgNode) {

		this.left = (Variable) left;
		this.right = (Variable) right;
		this.cfgNode = cfgNode;

		this.supported = AliasAnalysis.isSupported(this.left, this.right, false, -1);

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!supported) {
			return inX;
		}

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(left);
		Set<Variable> mayAliases = Collections.emptySet();

		out.assign(left, mustAliases, mayAliases, cfgNode);

		return out;
	}
}
