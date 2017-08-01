package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.io.File;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class IncludeEnd extends AbstractCfgNode {

	private File file;
	private IncludeStart peer;

	public IncludeEnd(IncludeStart start) {
		super(start.getParseNode());
		start.setPeer(this);
		this.file = start.getContainingFile();
		this.peer = start;
	}

	public File getFile() {
		return this.file;
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public IncludeStart getPeer() {
		return this.peer;
	}

	public boolean isPeer(AbstractCfgNode node) {
		if (node == this.peer) {
			return true;
		} else {
			return false;
		}
	}

	public void replaceVariable(int index, Variable replacement) {
	}

	public void setPeer(IncludeStart peer) {
		this.peer = peer;
	}
}