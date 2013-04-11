package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.*;

public class MustAliasGroup {

    // contains Variable's
    Set<Variable> group;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // a must-alias-group consists of at least two variables
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

    // creates a must-alias-group that consists of the given groups
    public MustAliasGroup(MustAliasGroup x, MustAliasGroup y) {
        this.group = new HashSet<Variable>();
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

    // returns true if this group contains at least one local variable;
    // false otherwise
    /*
    public boolean containsLocals() {
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isLocal()) {
                return true;
            }
        }
        return false;
    }
    */

    // returns true if this group contains at least one global variable;
    // false otherwise
    /*
    public boolean containsGlobals() {
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isGlobal()) {
                return true;
            }
        }
        return false;
    }
    */

    // returns an arbitrary global variable from this group, or null if there
    // are no global variables in this group
    public Variable getArbitraryGlobal() {
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isGlobal()) {
                return var;
            }
        }
        return null;
    }

    // returns an arbitrary local variable from this group, or null if there
    // are no local variables in this group
    /*
    public Variable getArbitraryLocal() {
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isLocal()) {
                return var;
            }
        }
        return null;
    }
    */

    // returns all local variables in this group
    public Set<Variable> getLocals() {
        Set<Variable> retMe = new HashSet<Variable>();
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isLocal()) {
                retMe.add(var);
            }
        }
        return retMe;
    }

    // returns all global variables in this group
    public Set<Variable> getGlobals() {
        Set<Variable> retMe = new HashSet<Variable>();
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
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
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (!(obj instanceof Variable)) {
                throw new RuntimeException("unexpected class: " + obj.getClass());
            }
            // Variable var = (Variable) iter.next();
            Variable var = (Variable) obj;
            if (var.isLocal()) {
                iter.remove();
            }
        }
    }

    // removes all global variables
    public void removeGlobals() {
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isGlobal()) {
                iter.remove();
            }
        }
    }

    // removes all variables that belong to the given symbol table
    public void removeVariables(SymbolTable symTab) {
        for (Iterator iter = this.group.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
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
    public void replace(Map replacements) {

        for (Iterator iter = replacements.entrySet().iterator(); iter.hasNext(); ) {
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