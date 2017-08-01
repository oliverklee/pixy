package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Unset extends AbstractTransferFunction {

	private Variable operand;
	private AbstractCfgNode cfgNode;
	private boolean supported;

	public Unset(AbstractTacPlace operand, AbstractCfgNode cfgNode) {

		if (!operand.isVariable()) {
			throw new RuntimeException("Trying to unset a non-variable.");
		}

		this.operand = (Variable) operand;
		this.cfgNode = cfgNode;
		this.supported = AliasAnalysis.isSupported(this.operand);

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!supported) {
			return inX;
		}

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(operand);
		Set<Variable> mayAliases = Collections.emptySet();
		out.assign(operand, mustAliases, mayAliases, cfgNode);
		return out;
	}
}
