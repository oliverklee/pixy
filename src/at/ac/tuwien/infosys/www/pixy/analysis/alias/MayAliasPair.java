package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class MayAliasPair {

	private Set<Variable> pair;

	public MayAliasPair(Variable a, Variable b) {
		this.pair = new HashSet<Variable>();
		this.pair.add(a);
		this.pair.add(b);
	}

	public MayAliasPair(MayAliasPair orig) {
		this.pair = new HashSet<Variable>();
		Set<?> origPair = orig.getPair();
		for (Iterator<?> iter = origPair.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			this.pair.add(var);
		}
	}

	public Set<Variable> getPair() {
		return this.pair;
	}

	public int size() {
		return this.pair.size();
	}

	public Variable getGlobalMayAlias(Variable var) {
		Iterator<Variable> iter = this.pair.iterator();
		Variable firstElement = (Variable) iter.next();
		Variable secondElement = (Variable) iter.next();

		if (firstElement == var) {
			if (secondElement.isGlobal()) {
				return secondElement;
			} else {
				return null;
			}
		} else if (secondElement == var) {
			if (firstElement.isGlobal()) {
				return firstElement;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public Variable getLocalMayAlias(Variable var) {
		Iterator<Variable> iter = this.pair.iterator();
		Variable firstElement = (Variable) iter.next();
		Variable secondElement = (Variable) iter.next();

		if (firstElement == var) {
			if (secondElement.isLocal()) {
				return secondElement;
			} else {
				return null;
			}
		} else if (secondElement == var) {
			if (firstElement.isLocal()) {
				return firstElement;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public Variable getMayAlias(Variable var) {

		Iterator<Variable> iter = this.pair.iterator();
		Variable firstElement = (Variable) iter.next();
		Variable secondElement = (Variable) iter.next();

		if (firstElement == var) {
			return secondElement;
		} else if (secondElement == var) {
			return firstElement;
		} else {
			return null;
		}

	}

	public Variable[] getLocalGlobal() {

		Iterator<Variable> iter = this.pair.iterator();
		Variable firstElement = (Variable) iter.next();
		Variable secondElement = (Variable) iter.next();

		if (firstElement.isLocal()) {
			if (secondElement.isGlobal()) {
				Variable[] retMe = { firstElement, secondElement };
				return retMe;
			} else {
				return null;
			}
		} else if (firstElement.isGlobal()) {
			if (secondElement.isLocal()) {
				Variable[] retMe = { secondElement, firstElement };
				return retMe;
			} else {
				return null;
			}
		} else {
			throw new RuntimeException("SNH");
		}

	}

	public boolean containsLocalAndGlobal() {
		Iterator<Variable> iter = this.pair.iterator();
		Variable firstVar = (Variable) iter.next();
		Variable secondVar = (Variable) iter.next();
		if (firstVar.isLocal()) {
			if (secondVar.isGlobal()) {
				return true;
			} else {
				return false;
			}
		} else if (firstVar.isGlobal()) {
			if (secondVar.isLocal()) {
				return true;
			} else {
				return false;
			}
		} else {
			throw new RuntimeException("SNH");
		}
	}

	public boolean containsLocals() {
		for (Iterator<Variable> iter = this.pair.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isLocal()) {
				return true;
			}
		}
		return false;
	}

	public boolean containsGlobals() {
		for (Iterator<Variable> iter = this.pair.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isGlobal()) {
				return true;
			}
		}
		return false;
	}

	public boolean containsVariables(SymbolTable symTab) {
		for (Iterator<Variable> iter = this.pair.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.belongsTo(symTab)) {
				return true;
			}
		}
		return false;
	}

	public void replaceBy(Variable replaceMe, Variable replaceBy) {
		if (!this.pair.remove(replaceMe)) {
			throw new RuntimeException("SNH");
		}
		this.pair.add(replaceBy);
	}

	public void replace(Map<?, ?> replacements) {
		Set<Variable> newPair = new HashSet<Variable>(this.pair);
		for (Iterator<Variable> iter = this.pair.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			Variable replaceBy = (Variable) replacements.get(var);
			if (replaceBy != null) {
				newPair.remove(var);
				newPair.add(replaceBy);
			}
		}
		this.pair = newPair;
	}

	public boolean contains(Variable var) {
		if (this.pair.contains(var)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (!(obj instanceof MayAliasPair)) {
			return false;
		}
		MayAliasPair comp = (MayAliasPair) obj;
		return this.pair.equals(comp.getPair());
	}

	public int hashCode() {
		return this.pair.hashCode();
	}

}
