package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// a set of may-alias-pairs
// EFF: a number of things could be done faster with more effort
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class MayAliases {

    // contains MayAliasPair's
    private Set<MayAliasPair> pairs;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates empty may alias information
    public MayAliases() {
        this.pairs = new HashSet<MayAliasPair>();
    }

    // clones the given object
    // (but no need to clone the underlying pairs)
    public MayAliases(MayAliases cloneMe) {
        this.pairs = new HashSet<MayAliasPair>(cloneMe.getPairs());
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public Set<MayAliasPair> getPairs() {
        return this.pairs;
    }

    // returns the global variables that are may-aliases of the given variable
    // (a set of Variables)
    public Set<Variable> getGlobalAliases(Variable var) {
        Set<Variable> retMe = new HashSet<Variable>();
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            Variable globalMayAlias = pair.getGlobalMayAlias(var);
            if (globalMayAlias != null) {
                retMe.add(globalMayAlias);
            }
        }
        return retMe;
    }

    // returns the local variables that are may-aliases of the given variable
    // (a set of Variables)
    public Set<Variable> getLocalAliases(Variable var) {
        Set<Variable> retMe = new HashSet<Variable>();
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            Variable localMayAlias = pair.getLocalMayAlias(var);
            if (localMayAlias != null) {
                retMe.add(localMayAlias);
            }
        }
        return retMe;
    }

    // returns a set of variables that are may-aliases of the given variable
    public Set<Variable> getAliases(Variable var) {
        Set<Variable> retMe = new HashSet<Variable>();
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            Variable mayAlias = pair.getMayAlias(var);
            if (mayAlias != null) {
                retMe.add(mayAlias);
            }
        }
        return retMe;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // adds the given may-alias-pairs to the own pairs
    // (without generating duplicate pairs)
    public void add(MayAliases addUs) {
        this.pairs.addAll(addUs.getPairs());
    }

    public void add(MayAliasPair pair) {
        this.pairs.add(pair);
    }

    // copies all pairs in which "right" appears and replaces "right"
    // through "left" in these copies
    public void addAliasFor(Variable left, Variable right) {
        HashSet<MayAliasPair> newPairs = new HashSet<MayAliasPair>();
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            if (pair.contains(right)) {
                MayAliasPair newPair = new MayAliasPair(pair);
                newPair.replaceBy(right, left);
                newPairs.add(newPair);
            }
        }
        this.pairs.addAll(newPairs);
    }

    // removes all pairs that contain the given variable
    public void removePairsWith(Variable var) {
        // EFF: it would be faster to locate the pairs to be
        // removed by maintaining a map from variables to sets
        // of pairs
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            if (pair.contains(var)) {
                iter.remove();
            }
        }
    }

    public void removeLocals() {
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            // if the pair contains locals, it can be removed as a whole
            if (pair.containsLocals()) {
                iter.remove();
            }
        }
    }

    public void removeGlobals() {
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            // if the pair contains locals, it can be removed as a whole
            if (pair.containsGlobals()) {
                iter.remove();
            }
        }
    }

    public void removeVariables(SymbolTable symTab) {
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();
            // if the pair contains one or two variables from the given
            // symbol table, it can be removed as a whole
            if (pair.containsVariables(symTab)) {
                iter.remove();
            }
        }
    }

    // for all may-alias-pairs that contain "findMe":
    // a copy of this pair is added, and "findMe" is replaced by "replacer" in
    // the copy
    public void createAdjustedPairCopies(Variable findMe, Variable replacer) {

        // EFF: there must be faster ways to do this

        // start by cloning the pair set; we will add adjusted copies to the
        // new pair set and in the end, put it in place of the old pair set
        Set<MayAliasPair> newPairs = new HashSet<MayAliasPair>(this.pairs);

        // for each pair...
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
            MayAliasPair pair = (MayAliasPair) iter.next();

            // if it contains the searched variable...
            if (pair.contains(findMe)) {

                // create a copy of this set to work on
                Set<Variable> pairSet = new HashSet<Variable>(pair.getPair());

                // we are only interested in the other member of the pair
                pairSet.remove(findMe);
                if (pairSet.size() != 1) {
                    throw new RuntimeException("SNH");
                }

                // create the adjusted copy and add it to the new set of pairs
                MayAliasPair newPair =
                    new MayAliasPair(replacer, (Variable) pairSet.iterator().next());
                newPairs.add(newPair);
            }
        }

        this.pairs = newPairs;
    }

    public void replace(Map replacements) {
        for (Iterator iter = this.pairs.iterator(); iter.hasNext(); ) {
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