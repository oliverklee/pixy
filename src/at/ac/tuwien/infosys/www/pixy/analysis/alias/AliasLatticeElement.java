package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph.Edge;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph.Graph;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.*;

public class AliasLatticeElement extends AbstractLatticeElement implements Recyclable {

	private MustAliases mustAliases;
	private MayAliases mayAliases;

	public AliasLatticeElement() {
		this.mustAliases = new MustAliases();
		this.mayAliases = new MayAliases();
	}

	public AliasLatticeElement(MustAliases mustAliases, MayAliases mayAliases) {
		this.mustAliases = mustAliases;
		this.mayAliases = mayAliases;
	}

	public AliasLatticeElement(AliasLatticeElement cloneMe) {
		this.mustAliases = new MustAliases(cloneMe.getMustAliases());
		this.mayAliases = new MayAliases(cloneMe.getMayAliases());
	}

	public AbstractLatticeElement cloneMe() {
		return new AliasLatticeElement(this);
	}

	public MustAliases getMustAliases() {
		return this.mustAliases;
	}

	public MayAliases getMayAliases() {
		return this.mayAliases;
	}

	public Set<Variable> getMustAliases(Variable x) {
		Set<Variable> retMe;
		MustAliasGroup group = this.mustAliases.getMustAliasGroup(x);
		if (group == null) {
			retMe = new HashSet<Variable>();
			retMe.add(x);
		} else {
			retMe = group.getVariables();
		}
		return retMe;
	}

	public Set<Variable> getMayAliases(Variable x) {
		return this.mayAliases.getAliases(x);
	}

	public MustAliasGroup getMustAliasGroup(Variable x) {
		return this.mustAliases.getMustAliasGroup(x);
	}

	public Set<?> getGlobalMayAliases(Variable var) {
		return this.mayAliases.getGlobalAliases(var);
	}

	public Set<?> getGlobalMustAliases(Variable var) {
		return this.mustAliases.getGlobalAliases(var);
	}

	public Set<?> getLocalMayAliases(Variable var) {
		return this.mayAliases.getLocalAliases(var);
	}

	public Set<Variable> getGlobalAliases(Variable var) {
		Set<Variable> retMe = new HashSet<Variable>();
		retMe.addAll(this.mustAliases.getGlobalAliases(var));
		retMe.addAll(this.mayAliases.getGlobalAliases(var));
		return retMe;
	}

	public void removeConflictingPairs() {

		MayAliases newMayAliases = new MayAliases();
		for (Iterator<MayAliasPair> iter = this.mayAliases.getPairs().iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			Iterator<?> pairIter = pair.getPair().iterator();
			Variable firstElement = (Variable) pairIter.next();
			Variable secondElement = (Variable) pairIter.next();
			if (!this.mustAliases.isMustAlias(firstElement, secondElement)) {
				newMayAliases.add(pair);
			}
		}
		this.mayAliases = newMayAliases;
	}

	public void merge(Variable x, Variable y) {
		this.mustAliases.merge(x, y);
	}

	public void add(AliasLatticeElement source) {
		this.mustAliases.add(source.getMustAliases());
		this.mayAliases.add(source.getMayAliases());
	}

	public void addMayAliasPairs(Set<?> varSet, Variable var) {
		for (Iterator<?> iter = varSet.iterator(); iter.hasNext();) {
			Variable varFromSet = (Variable) iter.next();
			this.add(new MayAliasPair(varFromSet, var));
		}
	}

	public void add(MayAliasPair pair) {
		this.mayAliases.add(pair);
	}

	public void removeLocals() {
		this.mustAliases.removeLocals();
		this.mayAliases.removeLocals();
	}

	public void removeGlobals() {
		this.mustAliases.removeGlobals();
		this.mayAliases.removeGlobals();
	}

	public void removeVariables(SymbolTable symTab) {
		this.mustAliases.removeVariables(symTab);
		this.mayAliases.removeVariables(symTab);
	}

	public void lub(AbstractLatticeElement element) {
		if (!(element instanceof AliasLatticeElement)) {
			throw new RuntimeException("SNH");
		}
		this.lub((AliasLatticeElement) element);
	}

	public void lub(AliasLatticeElement element) {

		this.mayAliases.add(element.getMayAliases());

		MustAliases foreignMustAliases = element.getMustAliases();

		Set<Variable> variablesInMust = new HashSet<Variable>();
		variablesInMust.addAll(this.mustAliases.getVariables());
		variablesInMust.addAll(foreignMustAliases.getVariables());

		Graph sccGraph = new Graph(variablesInMust);

		for (Iterator<MustAliasGroup> iter = this.mustAliases.getGroups().iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			sccGraph.drawFirstScc(group.getVariables());
		}

		for (Iterator<?> iter = foreignMustAliases.getGroups().iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			sccGraph.drawSecondScc(group.getVariables());
		}

		Set<?> singleEdges = sccGraph.getSingleEdges();
		for (Iterator<?> iter = singleEdges.iterator(); iter.hasNext();) {
			Edge singleEdge = (Edge) iter.next();
			this.mayAliases.add(new MayAliasPair(singleEdge.getN1().getLabel(), singleEdge.getN2().getLabel()));
		}

		MustAliases myNewMustAliases = new MustAliases();
		Set<Set<Variable>> sccs = sccGraph.getDoubleSccs();
		for (Set<Variable> scc : sccs) {
			myNewMustAliases.add(new MustAliasGroup(scc));
		}
		this.mustAliases = myNewMustAliases;
	}

	public void redirect(Variable left, Variable right) {
		this.mayAliases.removePairsWith(left);
		this.mustAliases.remove(left);
		this.mustAliases.addToGroup(left, right);
		this.mayAliases.addAliasFor(left, right);
	}

	public void unset(Variable var) {
		this.mayAliases.removePairsWith(var);
		this.mustAliases.remove(var);
	}

	public void addToGroup(Variable addMe, Variable host) {
		this.mustAliases.addToGroup(addMe, host);
	}

	public void createAdjustedPairCopies(Variable findMe, Variable replacer) {
		this.mayAliases.createAdjustedPairCopies(findMe, replacer);
	}

	public void replace(Map<?, ?> replacements) {
		this.mustAliases.replace(replacements);
		this.mayAliases.replace(replacements);
	}

	public boolean structureEquals(Object compX) {
		AliasLatticeElement comp = (AliasLatticeElement) compX;
		if (this.mustAliases.structureEquals(comp.getMustAliases())
				&& this.mayAliases.structureEquals(comp.getMayAliases())) {
			return true;
		} else {
			return false;
		}
	}

	public int structureHashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.mustAliases.structureHashCode();
		hashCode = 37 * hashCode + this.mayAliases.structureHashCode();
		return hashCode;
	}

	public void dump() {
		System.out.println("<AliasLatticeElement.dump(): not yet>");
	}

}
