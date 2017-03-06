package at.ac.tuwien.infosys.www.pixy.conversion;

public class TacFormalParameter {

	private Variable variable;
	private boolean isReference;
	private boolean hasDefault;
	private ControlFlowGraph defaultCfg;

	TacFormalParameter(Variable variable) {
		this.variable = variable;
		this.isReference = false;
		this.hasDefault = false;
		this.defaultCfg = null;
	}

	TacFormalParameter(Variable variable, boolean isReference) {
		this.variable = variable;
		this.isReference = isReference;
		this.hasDefault = false;
		this.defaultCfg = null;
	}

	TacFormalParameter(Variable variable, boolean hasDefault, ControlFlowGraph defaultCfg) {
		this.variable = variable;
		this.isReference = false;
		this.hasDefault = hasDefault;
		this.defaultCfg = defaultCfg;
	}

	public Variable getVariable() {
		return this.variable;
	}

	public boolean isReference() {
		return this.isReference;
	}

	public boolean hasDefault() {
		return this.hasDefault;
	}

	public ControlFlowGraph getDefaultCfg() {
		return this.defaultCfg;
	}

	public void setIsReference(boolean value) {
		this.isReference = value;
	}
}