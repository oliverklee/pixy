package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class CfgEntry extends AbstractCfgNode {

	public CfgEntry() {
		super();
	}

	public CfgEntry(ParseNode node) {
		super(node);
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public void replaceVariable(int index, Variable replacement) {
	}
}