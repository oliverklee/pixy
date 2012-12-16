package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.*;

// a set of disjoint must-alias-groups
// EFF: a number of things could be done faster with more effort
public class MustAliases {

    // contains MustAliasGroup's
    private Set<MustAliasGroup> groups; 
    
//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates empty must alias information
    public MustAliases() {
        this.groups = new HashSet<MustAliasGroup>();
    }
    
    // clones the given object
    public MustAliases(MustAliases cloneMe) {
        this.groups = new HashSet<MustAliasGroup>();
        Set<MustAliasGroup> origGroup = cloneMe.getGroups();
        for (Iterator iter = origGroup.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            this.groups.add(new MustAliasGroup(group));
        }
    }
    
//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public Set<MustAliasGroup> getGroups() {
        return this.groups;
    }
    
    // returns a set of all variables that occur in the contained groups 
    public Set<Variable> getVariables() {
        Set<Variable> variables = new HashSet<Variable>();
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            variables.addAll(group.getVariables());
        }
        return variables;
    }
    
    // returns the MustAliasGroup that contains the given
    // variable, or NULL if there is no such explicit group
    public MustAliasGroup getMustAliasGroup(Variable x) {
        // EFF: there are faster ways to do this
        boolean searchGroup = true;
        for (Iterator iter = this.groups.iterator(); iter.hasNext() && searchGroup; ) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            if (group.contains(x)) {
                return group;
            }
        }
        return null;
    }
    
    // returns all global variables that are must-aliases of the given variable
    // (a set of Variables)
    public Set<Variable> getGlobalAliases(Variable var) {
        Set<Variable> retMe = new HashSet<Variable>();
        MustAliasGroup group = this.getMustAliasGroup(var);
        if (group != null) {
            retMe.addAll(group.getGlobals());
        }
        return retMe;
    }
    
    // returns true if the given variables are must-aliases, and false otherwise
    public boolean isMustAlias(Variable x, Variable y) {
        
        MustAliasGroup groupX = this.getMustAliasGroup(x);
        
        // if x's group is implicit, x isn't a must-alias of anyone
        if (groupX == null) {
            return false;
        }
        
        if (groupX.contains(y)) {
            return true;
        } else {
            return false;
        }
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // merges the must-alias-groups that the given variables belong to (also 
    // considering implicit one-element groups)
    public void merge(Variable x, Variable y) {
        
        MustAliasGroup groupX = this.getMustAliasGroup(x);
        MustAliasGroup groupY = this.getMustAliasGroup(y);
        
        if (groupX == null) {
            if (groupY == null) {
                // both variables only have implicit groups;
                // add a new two-element group containing the variables
                this.add(new MustAliasGroup(x, y));
            } else {
                // add x to y's existing group
                this.addToGroup(x, groupY);
            }
        } else {
            if (groupY == null) {
                // add y to x's existing group
                this.addToGroup(y, groupX);
            } else {
                // both groups exist and must be merged
                MustAliasGroup mergedGroup = new MustAliasGroup(groupX, groupY);
                this.groups.remove(groupX);
                this.groups.remove(groupY);
                this.groups.add(mergedGroup);
            }
        }
    }
    
    public void removeLocals() {
        
        List<MustAliasGroup> keepUs = new LinkedList<MustAliasGroup>();
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            group.removeLocals();
            
            /* very strange: doesn't work
            // if the group now has less than two members: remove it
            if (group.size() < 2) {
                System.out.println("removing group!");
                iter.remove();
            }
            */
            // alternative:
            if (group.size() > 1) {
                keepUs.add(group);
            }
        }
        this.groups = new HashSet<MustAliasGroup>(keepUs);
    }
    
    public void removeGlobals() {
        
        List<MustAliasGroup> keepUs = new LinkedList<MustAliasGroup>();
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            group.removeGlobals();
            
            /* very strange: doesn't work
            // if the group now has less than two members: remove it
            if (group.size() < 2) {
                iter.remove();
            }
            */
            // alternative:
            if (group.size() > 1) {
                keepUs.add(group);
            }
        }
        this.groups = new HashSet<MustAliasGroup>(keepUs);
    }

    public void removeVariables(SymbolTable symTab) {
        
        List<MustAliasGroup> keepUs = new LinkedList<MustAliasGroup>();
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            group.removeVariables(symTab);
            
            if (group.size() > 1) {
                keepUs.add(group);
            }
        }
        this.groups = new HashSet<MustAliasGroup>(keepUs);
    }

    // removes the given variable from its group (if there
    // is such a group); if this leads to a group having only
    // one member, this group is removed completely
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
    
    // adds "addMe" to the group of "host"; if the host has no
    // group (i.e., it is contained in an implicit one-element group),
    // a new two-element group with addMe and host as members is created
    public void addToGroup(Variable addMe, Variable host) {
        MustAliasGroup hostGroup = this.getMustAliasGroup(host);
        
        if (hostGroup == null) {
            hostGroup = new MustAliasGroup(addMe, host);
            this.groups.add(hostGroup);
        } else {
            hostGroup.add(addMe);
        }
    }

    // adds "addMe" to the given (non-null) group
    public void addToGroup(Variable addMe, MustAliasGroup hostGroup) {
        hostGroup.add(addMe);
    }

    // adds the groups of "source" to this.groups
    // (without deep copy);
    // the caller has to make sure that they are disjoint 
    public void add(MustAliases source) {
        Set<MustAliasGroup> sourceGroups = source.getGroups();
        this.groups.addAll(sourceGroups);
    }
    
    public void add(MustAliasGroup group) {
        this.groups.add(group);
    }
    
    public void replace(Map replacements) {
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            group.replace(replacements);
        }
    }

    public boolean structureEquals(MustAliases comp) {

        Set compGroupSet = comp.getGroups();
        
        if (this.groups.size() != compGroupSet.size()) {
            // System.out.println("Sizes don't match");
            return false;
        }

        // for each of my own groups: check if it is contained in the other group set
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            
            // linear, slow implementation; but unfortunately, contains() doesn't
            // do what it should; for instance, uncomment the debug output inside
            // the "if" construct and run the analysis on the
            // PHP program given (far) below: sometimes contains() returns true,
            // sometimes it returns false (although it should always return true)
            for (Iterator iterator = compGroupSet.iterator(); iterator.hasNext();) {
                MustAliasGroup compGroup = (MustAliasGroup) iterator.next();
                if (compGroup.equals(group)) {
                    /*
                    System.out.println("found equal guy; hashCodes: ");
                    System.out.println(compGroup.hashCode());
                    System.out.println(group.hashCode());
                    System.out.println("symmetry check: " + group.equals(compGroup));
                    System.out.println(compGroupSet.contains(group));
                    */
                    return true;
                }
            }
            return false;
        }
        
        return true;    // we come here if both sizes are 0

        
        // here is another implementation that doesn't work...
        // the "contains" method is responsible for that
        /*
        Set compGroupSet = comp.getGroups();
        if (this.groups.size() != compGroupSet.size()) {
            System.out.println("Sizes don't match");
            return false;
        }
        for (Iterator iter = this.groups.iterator(); iter.hasNext();) {
            MustAliasGroup group = (MustAliasGroup) iter.next();
            
            if (!compGroupSet.contains(group)) {
                return false;
            }
        }
        return true;
        */

        
        // for unknown reasons, the following implementation doesn't work
        // properly; it sometimes returns false although it should
        // return true; according to the API, it should do exactly the
        // same thing as above
        /*
        if (this.groups.equals(comp.getGroups())) {
            return true;
        } else {
            return false;
        }
        */
    }
    
    public int structureHashCode() {
        return this.groups.hashCode();
    }
    
}

/* Example PHP Program

a();

function a($ap1, $ap2) {
    if ($u) {
        a(&$ap1, &$ap1);
    }
}
     
*/
