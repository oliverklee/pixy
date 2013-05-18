package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class MustAliasGroup {

    // contains Variable's
    Set<Variable> group;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // a must-alias-group consists of at least two variables
    public MustAliasGroup(Variable a, Variable b) {
        this.group = new HashSet<>();
        this.group.add(a);
        this.group.add(b);
    }

    public MustAliasGroup(Set<Variable> group) {
        this.group = group;
    }

    public MustAliasGroup(MustAliasGroup cloneMe) {
        this.group = new HashSet<>(cloneMe.getVariables());
    }

    // creates a must-alias-group that consists of the given groups
    public MustAliasGroup(MustAliasGroup x, MustAliasGroup y) {
        this.group = new HashSet<>();
        this.group.addAll(x.getVariables());
        this.group.addAll(y.getVariables());
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public Set<Variable> getVariables() {
        return this.group;
    }

    public int size() {
        return this.group.size();
    }

    public boolean contains(Variable var) {
        return this.group.contains(var);
    }

    // returns an arbitrary global variable from this group, or null if there
    // are no global variables in this group
    public Variable getArbitraryGlobal() {
        for (Variable var : this.group) {
            if (var.isGlobal()) {
                return var;
            }
        }
        return null;
    }

    // returns all local variables in this group
    public Set<Variable> getLocals() {
        Set<Variable> retMe = new HashSet<>();
        for (Variable var : this.group) {
            if (var.isLocal()) {
                retMe.add(var);
            }
        }
        return retMe;
    }

    // returns all global variables in this group
    public Set<Variable> getGlobals() {
        Set<Variable> retMe = new HashSet<>();
        for (Variable var : this.group) {
            if (var.isGlobal()) {
                retMe.add(var);
            }
        }
        return retMe;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // removes all local variables
    public void removeLocals() {
        for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext(); ) {
            Variable var = iter.next();
            if (var.isLocal()) {
                iter.remove();
            }
        }
    }

    // removes all global variables
    public void removeGlobals() {
        for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext(); ) {
            Variable var = iter.next();
            if (var.isGlobal()) {
                iter.remove();
            }
        }
    }

    // removes all variables that belong to the given symbol table
    public void removeVariables(SymbolTable symTab) {
        for (Iterator<Variable> iter = this.group.iterator(); iter.hasNext(); ) {
            Variable var = iter.next();
            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }
    }

    // if the given variable is contained in this group, it
    // is removed, and true is returned; else: false is returned
    public boolean remove(Variable var) {
        return this.group.remove(var);
    }

    public void add(Variable var) {
        this.group.add(var); // uniqueness of members is already guaranteed by
        // the underlying HashSet
    }

    // expects a map Variable -> Variable (replaceMe -> replaceBy)
    public void replace(Map<Variable, Variable> replacements) {
        for (Map.Entry<Variable, Variable> entry : replacements.entrySet()) {
            Variable replaceMe = entry.getKey();
            Variable replaceBy = entry.getValue();
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