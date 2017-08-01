package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.io.File;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class IncludeStart extends AbstractCfgNode {

	private File containingFile;
	private IncludeEnd peer;

	public IncludeStart(File file, ParseNode parseNode) {
		super(parseNode);
		this.containingFile = file;
		this.peer = null;
	}

	public File getContainingFile() {
		return this.containingFile;
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public IncludeEnd getPeer() {
		return this.peer;
	}

	public void replaceVariable(int index, Variable replacement) {
	}

	public void setPeer(IncludeEnd peer) {
		this.peer = peer;
	}
}