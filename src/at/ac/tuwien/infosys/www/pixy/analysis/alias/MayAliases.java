package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class MayAliases {

	private Set<MayAliasPair> pairs;

	public MayAliases() {
		this.pairs = new HashSet<MayAliasPair>();
	}

	public MayAliases(MayAliases cloneMe) {
		this.pairs = new HashSet<MayAliasPair>(cloneMe.getPairs());
	}

	public Set<MayAliasPair> getPairs() {
		return this.pairs;
	}

	public Set<Variable> getGlobalAliases(Variable var) {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			Variable globalMayAlias = pair.getGlobalMayAlias(var);
			if (globalMayAlias != null) {
				retMe.add(globalMayAlias);
			}
		}
		return retMe;
	}

	public Set<Variable> getLocalAliases(Variable var) {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			Variable localMayAlias = pair.getLocalMayAlias(var);
			if (localMayAlias != null) {
				retMe.add(localMayAlias);
			}
		}
		return retMe;
	}

	public Set<Variable> getAliases(Variable var) {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			Variable mayAlias = pair.getMayAlias(var);
			if (mayAlias != null) {
				retMe.add(mayAlias);
			}
		}
		return retMe;
	}

	public void add(MayAliases addUs) {
		this.pairs.addAll(addUs.getPairs());
	}

	public void add(MayAliasPair pair) {
		this.pairs.add(pair);
	}

	public void addAliasFor(Variable left, Variable right) {
		HashSet<MayAliasPair> newPairs = new HashSet<MayAliasPair>();
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			if (pair.contains(right)) {
				MayAliasPair newPair = new MayAliasPair(pair);
				newPair.replaceBy(right, left);
				newPairs.add(newPair);
			}
		}
		this.pairs.addAll(newPairs);
	}

	public void removePairsWith(Variable var) {

		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			if (pair.contains(var)) {
				iter.remove();
			}
		}
	}

	public void removeLocals() {
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			if (pair.containsLocals()) {
				iter.remove();
			}
		}
	}

	public void removeGlobals() {
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			if (pair.containsGlobals()) {
				iter.remove();
			}
		}
	}

	public void removeVariables(SymbolTable symTab) {
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			if (pair.containsVariables(symTab)) {
				iter.remove();
			}
		}
	}

	public void createAdjustedPairCopies(Variable findMe, Variable replacer) {

		Set<MayAliasPair> newPairs = new HashSet<MayAliasPair>(this.pairs);

		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();

			if (pair.contains(findMe)) {

				Set<Variable> pairSet = new HashSet<Variable>(pair.getPair());
				pairSet.remove(findMe);
				if (pairSet.size() != 1) {
					throw new RuntimeException("SNH");
				}
				MayAliasPair newPair = new MayAliasPair(replacer, (Variable) pairSet.iterator().next());
				newPairs.add(newPair);
			}
		}
		this.pairs = newPairs;
	}

	public void replace(Map<?, ?> replacements) {
		for (Iterator<MayAliasPair> iter = this.pairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			pair.replace(replacements);
		}
	}

	public boolean structureEquals(MayAliases comp) {
		if (this.pairs.equals(comp.getPairs())) {
			return true;
		} else {
			return false;
		}
	}

	public int structureHashCode() {
		return this.pairs.hashCode();
	}

}
