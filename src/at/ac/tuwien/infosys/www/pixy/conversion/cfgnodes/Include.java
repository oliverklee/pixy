package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.io.File;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class Include extends AbstractCfgNode implements Comparable<Include> {

	private Variable temp;
	private AbstractTacPlace includeMe;
	private File file;
	private TacFunction includeFunction;

	public Include(AbstractTacPlace temp, AbstractTacPlace includeMe, File file, TacFunction includeFunction,
			ParseNode parseNode) {
		super(parseNode);
		this.temp = (Variable) temp;
		this.includeMe = includeMe;
		this.file = file;
		this.includeFunction = includeFunction;
	}

	public AbstractTacPlace getTemp() {
		return this.temp;
	}

	public AbstractTacPlace getIncludeMe() {
		return this.includeMe;
	}

	public File getFile() {
		return this.file;
	}

	public TacFunction getIncludeFunction() {
		return this.includeFunction;
	}

	public boolean isLiteral() {
		return this.includeMe.isLiteral();
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		retMe.add(this.temp);
		if (this.includeMe instanceof Variable) {
			retMe.add((Variable) this.includeMe);
		} else {
			retMe.add(null);
		}
		return retMe;
	}

	public void setIncludeFunction(TacFunction function) {
		this.includeFunction = function;
	}

	public void replaceVariable(int index, Variable replacement) {
		switch (index) {
		case 0:
			this.temp = replacement;
			break;
		case 1:
			this.includeMe = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}

	public int compareTo(Include o) {
		if (o == this) {
			return 0;
		}
		Include comp = (Include) o;
		int fileComp = this.file.compareTo(comp.file);
		if (fileComp != 0) {
			return fileComp;
		} else {
			return new Integer(this.getOriginalLineNumber()).compareTo(comp.getOriginalLineNumber());
		}
	}
}