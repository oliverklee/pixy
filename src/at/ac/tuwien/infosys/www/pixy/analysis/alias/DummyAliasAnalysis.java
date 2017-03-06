package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class DummyAliasAnalysis extends AliasAnalysis {

	public DummyAliasAnalysis() {
		super();
	}

	public Set<Variable> getMustAliases(Variable var, AbstractCfgNode cfgNode) {
		Set<Variable> retMe = new HashSet<Variable>();
		retMe.add(var);
		return retMe;
	}

	public Set<Variable> getMayAliases(Variable var, AbstractCfgNode cfgNode) {
		return Collections.emptySet();
	}

	public Variable getGlobalMustAlias(Variable var, AbstractCfgNode cfgNode) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getLocalMustAliases(Variable var, AbstractCfgNode cfgNode) {
		return Collections.EMPTY_SET;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getGlobalMayAliases(Variable var, AbstractCfgNode cfgNode) {
		return Collections.EMPTY_SET;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getLocalMayAliases(Variable var, AbstractCfgNode cfgNode) {
		return Collections.EMPTY_SET;
	}

}
