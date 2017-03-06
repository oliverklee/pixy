package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Isset extends AbstractTransferFunction {

	private Variable setMe;
	@SuppressWarnings("unused")
	private AbstractTacPlace testMe;
	private AbstractCfgNode cfgNode;

	public Isset(AbstractTacPlace setMe, AbstractTacPlace testMe, AbstractCfgNode cfgNode) {
		this.setMe = (Variable) setMe;
		this.testMe = testMe;
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		if (!setMe.isTemp()) {
			throw new RuntimeException("SNH");
		}
		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(setMe);
		Set<Variable> mayAliases = Collections.emptySet();
		out.assign(setMe, mustAliases, mayAliases, cfgNode);
		return out;
	}
}
