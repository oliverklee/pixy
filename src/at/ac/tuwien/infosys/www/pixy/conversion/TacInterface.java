package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class TacInterface {

	private ParseNode parseNode;

	private String name;

	private Map<String, TacFunction> methods;

	private Map<String, TacInterface> ImplementedInterfaces;

	private Map<String, TacMember> members;

	TacInterface(String name, ParseNode parseNode) {
		this.name = name;
		this.methods = new HashMap<String, TacFunction>();
		this.members = new HashMap<String, TacMember>();
		this.parseNode = parseNode;
		this.ImplementedInterfaces = new HashMap<String, TacInterface>();
	}

	public boolean addImplmentedInterface(String name, TacInterface ImplmentedInterface) {
		if (this.getImplementedInterfaces().get(name) == null) {
			this.getImplementedInterfaces().put(name, ImplmentedInterface);
			return true;
		} else {
			return false;
		}
	}

	public Map<String, TacInterface> getImplementedInterfaces() {
		return ImplementedInterfaces;
	}

	boolean addMethod(String name, TacFunction function) {
		if (this.methods.get(name) == null) {
			this.methods.put(name, function);
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

	public void addMember(String name, ControlFlowGraph cfg, AbstractTacPlace place) {
		TacMember member = new TacMember(name, cfg, place);
		this.members.put(name, member);
	}

	public String dump() {
		StringBuilder b = new StringBuilder();
		b.append("Interface ");
		b.append(this.name);
		b.append("\n");
		b.append("Functions:\n");
		for (String methodName : this.methods.keySet()) {
			b.append(methodName);
			b.append("\n");
		}
		b.append("Members:\n");
		for (TacMember member : this.members.values()) {
			b.append(member.dump());
		}
		b.append("\n");

		return b.toString();
	}

	private class TacMember {

		private String name;

		private ControlFlowGraph cfg;

		private AbstractTacPlace place;

		TacMember(String name, ControlFlowGraph cfg, AbstractTacPlace place) {
			this.name = name;
			this.cfg = cfg;
			this.place = place;
		}

		@SuppressWarnings("unused")
		public String getName() {
			return this.name;
		}

		@SuppressWarnings("unused")
		public ControlFlowGraph getCfg() {
			return this.cfg;
		}

		@SuppressWarnings("unused")
		public AbstractTacPlace getPlace() {
			return this.place;
		}

		String dump() {
			StringBuilder b = new StringBuilder();
			b.append(this.name);
			b.append("\n");
			return b.toString();
		}
	}
}
