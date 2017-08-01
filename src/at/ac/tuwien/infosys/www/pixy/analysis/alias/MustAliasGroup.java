package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class MustAliasGroup {

	Set<Variable> group;

	public MustAliasGroup(Variable a, Variable b) {
		this.group = new HashSet<Variable>();
		this.group.add(a);
		this.group.add(b);
	}

	public MustAliasGroup(Set<Variable> group) {
		this.group = group;
	}

	public MustAliasGroup(MustAliasGroup cloneMe) {
		this.group = new HashSet<Variable>(cloneMe.getVariables());
	}

	public MustAliasGroup(MustAliasGroup x, MustAliasGroup y) {
		this.group = new HashSet<Variable>();
		this.group.addAll(x.getVariables());
		this.group.addAll(y.getVariables());
	}

	public Set<Variable> getVariables() {
		return this.group;
	}

	public int size() {
		return this.group.size();
	}

	public boolean contains(Variable var) {
		return this.group.contains(var);
	}

	public Variable getArbitraryGlobal() {
		for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isGlobal()) {
				return var;
			}
		}
		return null;
	}

	public Set<Variable> getLocals() {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isLocal()) {
				retMe.add(var);
			}
		}
		return retMe;
	}

	public Set<Variable> getGlobals() {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isGlobal()) {
				retMe.add(var);
			}
		}
		return retMe;
	}

	public void removeLocals() {
		for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof Variable)) {
				throw new RuntimeException("unexpected class: " + obj.getClass());
			}
			Variable var = (Variable) obj;
			if (var.isLocal()) {
				iter.remove();
			}
		}
	}

	public void removeGlobals() {
		for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isGlobal()) {
				iter.remove();
			}
		}
	}

	public void removeVariables(SymbolTable symTab) {
		for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}
	}

	public boolean remove(Variable var) {
		return this.group.remove(var);
	}

	public void add(Variable var) {
		this.group.add(var);
	}

	@SuppressWarnings("rawtypes")
	public void replace(Map<?, ?> replacements) {

		for (Iterator<?> iter = replacements.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Variable replaceMe = (Variable) entry.getKey();
			Variable replaceBy = (Variable) entry.getValue();
			if (this.group.contains(replaceMe)) {
				this.group.remove(replaceMe);
				this.group.add(replaceBy);
			}
		}
	}

	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (!(obj instanceof MustAliasGroup)) {
			return false;
		}
		MustAliasGroup comp = (MustAliasGroup) obj;
		return this.group.equals(comp.getVariables());
	}

	public int hashCode() {
		return this.group.hashCode();
	}

}
