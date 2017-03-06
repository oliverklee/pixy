package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class TacNamespace {

	private ParseNode parseNode;

	private String name;

	private Map<String, TacClass> classes;

	TacNamespace(String name, ParseNode parseNode) {
		this.name = name;
		this.classes = new HashMap<String, TacClass>();
		this.parseNode = parseNode;
	}

	boolean addClass(String name, TacClass newclass) {
		if (this.classes.get(name) == null) {
			this.classes.put(name, newclass);
			return true;
		} else {
			return false;
		}
	}

	public String getName() {
		return this.name;
	}

	public String getFileName() {
		return this.parseNode.getFileName();
	}

	public int getLine() {
		return this.parseNode.getLinenoLeft();
	}

	public String getLoc() {
		if (!MyOptions.optionB) {
			return this.parseNode.getFileName() + ":" + this.parseNode.getLinenoLeft();
		} else {
			return Utils.basename(this.parseNode.getFileName()) + ":" + this.parseNode.getLinenoLeft();
		}
	}

	public String dump() {
		StringBuilder b = new StringBuilder();
		b.append("Namespace ");
		b.append(this.name);
		b.append("\n");
		b.append("Classes:\n");
		for (String className : this.classes.keySet()) {
			b.append(className);
			b.append("\n");
		}
		b.append("\n");

		return b.toString();
	}
}