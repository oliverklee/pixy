package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

public class ConstantsTable {

    // constant's label (String) -> Constant;
    // works in a case-sensitive way (i.e. nothing unusual to be done)
    private Map<String,Constant> constants;

    // lowercase Constant label -> list of Constants;
    // each list contains Constants whose labels only differ in case
    private Map<String,List<Constant>> insensitiveGroups;
    
// *********************************************************************************
// CONSTRUCTORS ********************************************************************    
// *********************************************************************************
    
    ConstantsTable() {
        this.constants = new LinkedHashMap<String,Constant>();
        this.insensitiveGroups = new HashMap<String,List<Constant>>();
    }

// *********************************************************************************
// GET *****************************************************************************    
// *********************************************************************************
    
    public Constant getConstant(String label) {
        return ((Constant) this.constants.get(label));
    }

    List getInsensitiveGroup(String label) {
        return ((List) this.insensitiveGroups.get(label.toLowerCase()));
    }

    public Map<String,Constant> getConstants() {
        return this.constants;
    }

    public Map getInsensitiveGroups() {
        return this.insensitiveGroups;
    }

    // returns a list of Constant's whose names are equal (case-insensitive)
    // to the given literal
    // might be NULL if there are no such constants
    public List getInsensitiveGroup(Literal name) {
        return (List) this.insensitiveGroups.get(name.toString().toLowerCase());
    }
    
    public int size() {
        return this.constants.size();
    }
 
// *********************************************************************************
// OTHER ***************************************************************************    
// *********************************************************************************
    
    // don't call this method if the constant to be put is already in the table
    void add(Constant newConst) {
        this.constants.put(newConst.getLabel(), newConst);
        // update insensitiveGroups
        String lowLabel = newConst.getLabel().toLowerCase();
        List<Constant> oldList = this.insensitiveGroups.get(lowLabel);
        if (oldList == null) {
            // this constant is the first one in its insensitivity group:
            // new list has to be created
            List<Constant> newList = new LinkedList<Constant>();
            newList.add(newConst);
            this.insensitiveGroups.put(lowLabel, newList);
        } else {
            // there are already other constants in this constant's insensitivity group
            oldList.add(newConst);
        }
    }
    
    // adds all constants from the given constants table to this table (leaving out
    // duplicates)
    void addAll(ConstantsTable sourceTable) {
        Map sourceConstants = sourceTable.getConstants();
        for (Iterator iter = sourceConstants.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String sourceLabel = (String) entry.getKey();
            Constant sourceConst = (Constant) entry.getValue();
            if (!this.constants.containsKey(sourceLabel)) {
                this.add(sourceConst);
            }
        }
    }
}

