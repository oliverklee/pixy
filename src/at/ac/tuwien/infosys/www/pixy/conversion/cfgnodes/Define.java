package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class Define extends AbstractCfgNode {

	private AbstractTacPlace setMe;

	private AbstractTacPlace setTo;

	private AbstractTacPlace caseInsensitive;

	public Define(AbstractTacPlace setMe, AbstractTacPlace setTo, AbstractTacPlace caseInsensitive, ParseNode node) {

		super(node);
		this.setMe = setMe;
		this.setTo = setTo;
		this.caseInsensitive = caseInsensitive;
	}

	public AbstractTacPlace getSetMe() {
		return this.setMe;
	}

	public AbstractTacPlace getSetTo() {
		return this.setTo;
	}

	public AbstractTacPlace getCaseInsensitive() {
		return this.caseInsensitive;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		if (this.setMe instanceof Variable) {
			retMe.add((Variable) setMe);
		} else {
			retMe.add(null);
		}
		if (this.setTo instanceof Variable) {
			retMe.add((Variable) setTo);
		} else {
			retMe.add(null);
		}
		if (this.caseInsensitive instanceof Variable) {
			retMe.add((Variable) caseInsensitive);
		} else {
			retMe.add(null);
		}

		return retMe;
	}

	public void replaceVariable(int index, Variable replacement) {
		switch (index) {
		case 0:
			this.setMe = replacement;
			break;
		case 1:
			this.setTo = replacement;
			break;
		case 2:
			this.caseInsensitive = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}

}