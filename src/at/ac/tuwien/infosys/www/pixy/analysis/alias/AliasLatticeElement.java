package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph.Edge;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph.Graph;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AliasLatticeElement extends LatticeElement implements Recyclable {
    private MustAliases mustAliases;
    private MayAliases mayAliases;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates an alias lattice element without any aliases
    public AliasLatticeElement() {
        this.mustAliases = new MustAliases();
        this.mayAliases = new MayAliases();
    }

    public AliasLatticeElement(MustAliases mustAliases, MayAliases mayAliases) {
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
    }

    // clones the given element
    public AliasLatticeElement(AliasLatticeElement cloneMe) {
        this.mustAliases = new MustAliases(cloneMe.getMustAliases());
        this.mayAliases = new MayAliases(cloneMe.getMayAliases());
    }

    public LatticeElement cloneMe() {
        // uses the cloning constructor
        return new AliasLatticeElement(this);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public MustAliases getMustAliases() {
        return this.mustAliases;
    }

    public MayAliases getMayAliases() {
        return this.mayAliases;
    }

    // returns a set of must-aliases of variable x
    // (including x itself); hence, the returned set always has
    // at least one element
    public Set<Variable> getMustAliases(Variable x) {
        Set<Variable> retMe;
        MustAliasGroup group = this.mustAliases.getMustAliasGroup(x);
        if (group == null) {
            // there is no explicit group
            retMe = new HashSet<>();
            retMe.add(x);
        } else {
            retMe = group.getVariables();
        }
        return retMe;
    }

    // returns a set of may-aliases of variable x; might be empty (but never null)
    public Set<Variable> getMayAliases(Variable x) {
        return this.mayAliases.getAliases(x);
    }

    // returns the MustAliasGroup that contains the given
    // variable, or NULL if there is no such explicit group
    public MustAliasGroup getMustAliasGroup(Variable x) {
        return this.mustAliases.getMustAliasGroup(x);
    }

    // returns the global variables that are may-aliases of the given variable
    // (a set of Variables)
    public Set<Variable> getGlobalMayAliases(Variable var) {
        return this.mayAliases.getGlobalAliases(var);
    }

    // returns the global variables that are must-aliases of the given variable
    // (a set of Variables)
    public Set<Variable> getGlobalMustAliases(Variable var) {
        return this.mustAliases.getGlobalAliases(var);
    }

    // returns the local variables that are may-aliases of the given variable
    // (a set of Variables)
    public Set<Variable> getLocalMayAliases(Variable var) {
        return this.mayAliases.getLocalAliases(var);
    }

    // returns the global variables that are aliases (must / may) of the
    // given variable (a set of Variables)
    public Set<Variable> getGlobalAliases(Variable var) {
        Set<Variable> retMe = new HashSet<>();
        retMe.addAll(this.mustAliases.getGlobalAliases(var));
        retMe.addAll(this.mayAliases.getGlobalAliases(var));
        return retMe;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // removes alias-pairs that "conflict" with must-alias information
    public void removeConflictingPairs() {

        // we will create a new MayAlias object as replacement for our current one
        MayAliases newMayAliases = new MayAliases();

        for (MayAliasPair pair : this.mayAliases.getPairs()) {
            Iterator<Variable> pairIter = pair.getPair().iterator();
            Variable firstElement = pairIter.next();
            Variable secondElement = pairIter.next();
            // we only keep this pair if it doesn't conflict with must-information
            if (!this.mustAliases.isMustAlias(firstElement, secondElement)) {
                newMayAliases.add(pair);
            }
        }
        this.mayAliases = newMayAliases;
    }

    // merges the must-alias-groups that the given variables belong to (also
    // considering implicit one-element groups)
    public void merge(Variable x, Variable y) {
        this.mustAliases.merge(x, y);
    }

    // adds the info from "source" to *this* alias information
    // (without deep copy);
    // the caller has to make sure that the information from source
    // is disjoint from *this* information
    public void add(AliasLatticeElement source) {
        this.mustAliases.add(source.getMustAliases());
        this.mayAliases.add(source.getMayAliases());
    }

    // adds all pairs (variable from varSet, var) to the may-aliases
    public void addMayAliasPairs(Set<Variable> variables, Variable variable) {
        for (Variable variableFromSet : variables) {
            this.add(new MayAliasPair(variableFromSet, variable));
        }
    }

    public void add(MayAliasPair pair) {
        this.mayAliases.add(pair);
    }

    // removes all local variables
    public void removeLocals() {
        this.mustAliases.removeLocals();
        this.mayAliases.removeLocals();
    }

    // removes all global variables
    public void removeGlobals() {
        this.mustAliases.removeGlobals();
        this.mayAliases.removeGlobals();
    }

    // removes all variables that belong to the given symbol table
    public void removeVariables(SymbolTable symTab) {
        this.mustAliases.removeVariables(symTab);
        this.mayAliases.removeVariables(symTab);
    }

    // lubs the given element over *this* element
    public void lub(LatticeElement element) {
        if (!(element instanceof AliasLatticeElement)) {
            throw new RuntimeException("SNH");
        }
        this.lub((AliasLatticeElement) element);
    }

    // lubs the given element over *this* element
    public void lub(AliasLatticeElement element) {

        // easy: union of may-aliases
        this.mayAliases.add(element.getMayAliases());

        // graph-theoretic approach for "intersection" of must-aliases

        MustAliases foreignMustAliases = element.getMustAliases();

        // collect variables from both must-alias-group sets
        // to create an Graph with them
        Set<Variable> variablesInMust = new HashSet<>();
        variablesInMust.addAll(this.mustAliases.getVariables());
        variablesInMust.addAll(foreignMustAliases.getVariables());

        Graph graph = new Graph(variablesInMust);

        // for each of my own must-alias-groups: draw SCC
        for (MustAliasGroup group : this.mustAliases.getGroups()) {
            graph.drawFirstScc(group.getVariables());
        }

        // for each of the foreign must-alias-groups: draw SCC
        for (MustAliasGroup group : foreignMustAliases.getGroups()) {
            graph.drawSecondScc(group.getVariables());
        }

        // single edges in the resulting graph correspond to may-alias pairs
        for (Edge singleEdge : graph.getSingleEdges()) {
            this.mayAliases.add(new MayAliasPair(singleEdge.getN1().getLabel(), singleEdge.getN2().getLabel()));
        }

        // SCC's formed by double edges in the resulting graph correspond
        // to must-alias groups
        MustAliases myNewMustAliases = new MustAliases();
        Set<Set<Variable>> sccs = graph.getDoubleSccs();
        for (Set<Variable> scc : sccs) {
            myNewMustAliases.add(new MustAliasGroup(scc));
        }
        this.mustAliases = myNewMustAliases;
    }

    // responsible for redirection "left =& right"
    public void redirect(Variable left, Variable right) {
        this.mayAliases.removePairsWith(left);
        this.mustAliases.remove(left);
        this.mustAliases.addToGroup(left, right);
        this.mayAliases.addAliasFor(left, right);
    }

    // responsible for "unset(var)"
    public void unset(Variable var) {
        this.mayAliases.removePairsWith(var);
        this.mustAliases.remove(var);
    }

    // wrapper around MustAliases.addToGroup
    public void addToGroup(Variable addMe, Variable host) {
        this.mustAliases.addToGroup(addMe, host);
    }

    public void createAdjustedPairCopies(Variable findMe, Variable replacer) {
        this.mayAliases.createAdjustedPairCopies(findMe, replacer);
    }

    // expects a map from Variable -> Variable (replaceMe -> replaceBy)
    public void replace(Map<Variable, Variable> replacements) {
        this.mustAliases.replace(replacements);
        this.mayAliases.replace(replacements);
    }

    // thorough (and slower) structural comparison required by the repository
    public boolean structureEquals(Object compX) {
        AliasLatticeElement comp = (AliasLatticeElement) compX;
        return this.mustAliases.structureEquals(comp.getMustAliases()) &&
            this.mayAliases.structureEquals(comp.getMayAliases());
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