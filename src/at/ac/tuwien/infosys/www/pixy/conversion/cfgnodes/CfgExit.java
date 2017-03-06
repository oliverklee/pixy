package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.Collections;
import java.util.List;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class CfgExit extends AbstractCfgNode {

	public CfgExit() {
		super();
	}

	public CfgExit(ParseNode node) {
		super(node);
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public void replaceVariable(int index, Variable replacement) {
	}
}