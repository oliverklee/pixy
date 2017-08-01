package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class MustAliases {

	private Set<MustAliasGroup> groups;

	public MustAliases() {
		this.groups = new HashSet<MustAliasGroup>();
	}

	public MustAliases(MustAliases cloneMe) {
		this.groups = new HashSet<MustAliasGroup>();
		Set<MustAliasGroup> origGroup = cloneMe.getGroups();
		for (Iterator<MustAliasGroup> iter = origGroup.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			this.groups.add(new MustAliasGroup(group));
		}
	}

	public Set<MustAliasGroup> getGroups() {
		return this.groups;
	}

	public Set<Variable> getVariables() {
		Set<Variable> variables = new HashSet<Variable>();
		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			variables.addAll(group.getVariables());
		}
		return variables;
	}

	public MustAliasGroup getMustAliasGroup(Variable x) {
		boolean searchGroup = true;
		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext() && searchGroup;) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			if (group.contains(x)) {
				return group;
			}
		}
		return null;
	}

	public Set<Variable> getGlobalAliases(Variable var) {
		Set<Variable> retMe = new HashSet<Variable>();
		MustAliasGroup group = this.getMustAliasGroup(var);
		if (group != null) {
			retMe.addAll(group.getGlobals());
		}
		return retMe;
	}

	public boolean isMustAlias(Variable x, Variable y) {

		MustAliasGroup groupX = this.getMustAliasGroup(x);

		if (groupX == null) {
			return false;
		}

		if (groupX.contains(y)) {
			return true;
		} else {
			return false;
		}
	}

	public void merge(Variable x, Variable y) {

		MustAliasGroup groupX = this.getMustAliasGroup(x);
		MustAliasGroup groupY = this.getMustAliasGroup(y);

		if (groupX == null) {
			if (groupY == null) {
				this.add(new MustAliasGroup(x, y));
			} else {
				this.addToGroup(x, groupY);
			}
		} else {
			if (groupY == null) {
				this.addToGroup(y, groupX);
			} else {
				MustAliasGroup mergedGroup = new MustAliasGroup(groupX, groupY);
				this.groups.remove(groupX);
				this.groups.remove(groupY);
				this.groups.add(mergedGroup);
			}
		}
	}

	public void removeLocals() {

		List<MustAliasGroup> keepUs = new LinkedList<MustAliasGroup>();
		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			group.removeLocals();

			if (group.size() > 1) {
				keepUs.add(group);
			}
		}
		this.groups = new HashSet<MustAliasGroup>(keepUs);
	}

	public void removeGlobals() {

		List<MustAliasGroup> keepUs = new LinkedList<MustAliasGroup>();
		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			group.removeGlobals();

			if (group.size() > 1) {
				keepUs.add(group);
			}
		}
		this.groups = new HashSet<MustAliasGroup>(keepUs);
	}

	public void removeVariables(SymbolTable symTab) {

		List<MustAliasGroup> keepUs = new LinkedList<MustAliasGroup>();
		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			group.removeVariables(symTab);

			if (group.size() > 1) {
				keepUs.add(group);
			}
		}
		this.groups = new HashSet<MustAliasGroup>(keepUs);
	}

	public void remove(Variable var) {
		MustAliasGroup group = this.getMustAliasGroup(var);
		if (group != null) {
			MustAliasGroup groupCopy = new MustAliasGroup(group);
			groupCopy.remove(var);
			if (groupCopy.size() < 2) {
				this.groups.remove(group);
			} else {
				group.remove(var);
			}
		}
	}

	public void addToGroup(Variable addMe, Variable host) {
		MustAliasGroup hostGroup = this.getMustAliasGroup(host);

		if (hostGroup == null) {
			hostGroup = new MustAliasGroup(addMe, host);
			this.groups.add(hostGroup);
		} else {
			hostGroup.add(addMe);
		}
	}

	public void addToGroup(Variable addMe, MustAliasGroup hostGroup) {
		hostGroup.add(addMe);
	}

	public void add(MustAliases source) {
		Set<MustAliasGroup> sourceGroups = source.getGroups();
		this.groups.addAll(sourceGroups);
	}

	public void add(MustAliasGroup group) {
		this.groups.add(group);
	}

	public void replace(Map<?, ?> replacements) {
		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			group.replace(replacements);
		}
	}

	public boolean structureEquals(MustAliases comp) {

		Set<?> compGroupSet = comp.getGroups();

		if (this.groups.size() != compGroupSet.size()) {
			return false;
		}

		for (Iterator<MustAliasGroup> iter = this.groups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();

			for (Iterator<?> iterator = compGroupSet.iterator(); iterator.hasNext();) {
				MustAliasGroup compGroup = (MustAliasGroup) iterator.next();
				if (compGroup.equals(group)) {
					return true;
				}
			}
			return false;
		}

		return true;

	}

	public int structureHashCode() {
		return this.groups.hashCode();
	}

}