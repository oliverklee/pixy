package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class AssignUnary extends AbstractTransferFunction {

	private Variable left;
	@SuppressWarnings("unused")
	private AbstractTacPlace right;
	@SuppressWarnings("unused")
	private int op;
	private Set<Variable> mustAliases;
	private Set<Variable> mayAliases;
	private AbstractCfgNode cfgNode;

	public AssignUnary(AbstractTacPlace left, AbstractTacPlace right, int op, Set<Variable> mustAliases,
			Set<Variable> mayAliases, AbstractCfgNode cfgNode) {

		this.left = (Variable) left;
		this.right = right;
		this.op = op;
		this.mustAliases = mustAliases;
		this.mayAliases = mayAliases;
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		out.assign(left, mustAliases, mayAliases, cfgNode);

		return out;
	}
}
