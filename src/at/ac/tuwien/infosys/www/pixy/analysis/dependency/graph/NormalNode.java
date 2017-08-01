package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class NormalNode extends AbstractNode {

	private AbstractTacPlace place;
	private AbstractCfgNode cfgNode;
	private boolean isTainted;

	public NormalNode(AbstractTacPlace place, AbstractCfgNode cfgNode) {
		this.place = place;
		this.cfgNode = cfgNode;
		this.isTainted = false;
	}

	public String dotName() {
		return Dumper.escapeDot(this.place.toString(), 0) + " (" + this.cfgNode.getOriginalLineNumber() + ")" + "\\n"
				+ this.cfgNode.getFileName();
	}

	public String comparableName() {
		return Dumper.escapeDot(this.place.toString(), 0) + " (" + this.cfgNode.getOriginalLineNumber() + ")" + "\\n"
				+ this.cfgNode.getFileName();
	}

	public String dotNameShort() {
		String fileName = this.cfgNode.getFileName();
		return Dumper.escapeDot(this.place.toString(), 0) + " (" + this.cfgNode.getOriginalLineNumber() + ")" + "\\n"
				+ fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	public String dotNameVerbose(boolean isModelled) {

		String retme = "";

		if (!MyOptions.optionW) {
			retme += this.cfgNode.getFileName() + " : " + this.cfgNode.getOriginalLineNumber() + "\\n";
		} else {
			retme += "Line " + this.cfgNode.getOriginalLineNumber() + "\\n";
		}

		if (this.place instanceof Variable) {
			Variable var = (Variable) this.place;
			retme += "Var: " + Dumper.escapeDot(var.getName(), 0) + "\\n";
			retme += "Func: " + Dumper.escapeDot(var.getSymbolTable().getName(), 0) + "\\n";
		} else if (this.place instanceof Constant) {
			retme += "Const: " + Dumper.escapeDot(this.place.toString(), 0) + "\\n";
		} else if (this.place instanceof Literal) {
			retme += "Lit: " + Dumper.escapeDot(this.place.toString(), 0) + "\\n";
		} else {
			throw new RuntimeException("SNH");
		}

		return retme;
	}

	public void setTainted(boolean isTainted) {
		this.isTainted = isTainted;
	}

	public boolean isTainted() {
		return this.isTainted;
	}

	public boolean isString() {
		if (this.place.isLiteral()) {
			return true;
		} else {
			return false;
		}
	}

	public AbstractTacPlace getPlace() {
		return this.place;
	}

	public AbstractCfgNode getCfgNode() {
		return this.cfgNode;
	}

	public int getLine() {
		return this.cfgNode.getOriginalLineNumber();
	}

	public boolean equals(Object compX) {

		if (compX == this) {
			return true;
		}
		if (!(compX instanceof NormalNode)) {
			return false;
		}
		NormalNode comp = (NormalNode) compX;

		if (!this.place.equals(comp.place)) {
			return false;
		}
		if (!this.cfgNode.equals(comp.cfgNode)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.place.hashCode();
		hashCode = 37 * hashCode + this.cfgNode.hashCode();
		return hashCode;
	}

	public String toString() {
		return this.place.toString() + " (" + this.cfgNode.getOriginalLineNumber() + ") " + this.cfgNode.getFileName();
	}

}
