package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class SymbolTable {

    // map variable object -> Variable object
    private Map<Variable, Variable> variables;
    private String name;    // usually corresponds to a function name (lowercase)
    private boolean isSuperSymTab;
    private boolean isMain;

    // auxiliary map for g-shadows: global -> g-shadow
    private Map<Variable, Variable> globals2GShadows;

    // auxiliary map for f-shadows: formal -> f-shadow
    private Map<Variable, Variable> formals2FShadows;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public SymbolTable(String name, boolean isSuperSymTab) {
        if (name == null) {
            throw new RuntimeException("SNH");
        }
        this.variables = new LinkedHashMap<Variable, Variable>();
        this.name = name;
        this.isSuperSymTab = isSuperSymTab;
        this.isMain = false;
        this.globals2GShadows = null;
        this.formals2FShadows = null;
    }

    // use this constructor for non-supersymtabs
    public SymbolTable(String name) {
        this(name, false);
        if (name.equals(InternalStrings.mainFunctionName)) {
            this.isMain = true;
        }
        this.globals2GShadows = new HashMap<Variable, Variable>();
        this.formals2FShadows = new HashMap<Variable, Variable>();
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    Variable getVariable(Variable variable) {
        return ((Variable) this.variables.get(variable));
    }

    public Variable getVariable(String varName) {
        return ((Variable) this.variables.get(new Variable(varName, this)));
    }

    public Map<Variable, Variable> getVariables() {
        return this.variables;
    }

    Collection<Variable> getVariablesColl() {
        return this.variables.values();
    }

    public String getName() {
        return this.name;
    }

    boolean isSuperSymTab() {
        return this.isSuperSymTab;
    }

    int size() {
        return this.variables.size();
    }

    public String toString() {
        return this.name;
    }

    public boolean isMain() {
        return this.isMain;
    }

    // returns the g-shadow of the given global variable (which must
    // be contained in this symbol table)
    public Variable getGShadow(Variable global) {
        return this.globals2GShadows.get(global);
    }

    // returns the f-shadow of the given formal variable (which must
    // be contained in this symbol table)
    public Variable getFShadow(Variable formal) {
        return this.formals2FShadows.get(formal);
    }

    public Map<Variable, Variable> getGlobals2GShadows() {
        return this.globals2GShadows;
    }

    public Map<Variable, Variable> getFormals2FShadows() {
        return this.formals2FShadows;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    // if there is already such a variable, the old one is replaced!
    void add(Variable newVar) {
        this.variables.put(newVar, newVar);
    }

    // creates and adds a g-shadow for the given global variable
    void addGShadow(Variable global) {
        Variable gShadow = new Variable(
            global.getName() + InternalStrings.gShadowSuffix, this);
        this.variables.put(gShadow, gShadow);
        this.globals2GShadows.put(global, gShadow);
    }

    // creates and adds an f-shadow for the given variable (formal param)
    void addFShadow(Variable formal) {
        Variable fShadow = new Variable(
            formal.getName() + InternalStrings.fShadowSuffix, this);
        this.variables.put(fShadow, fShadow);
        this.formals2FShadows.put(formal, fShadow);
    }

    // adds the variables of the given symbol table, changing their symbolTable
    // member variable;
    // WARNING: the current implementation of this method doesn't care about
    // handling duplicates: it simply overwrites the mappings in this table;
    // note that this doesn't affect the references to the overwritten variables
    // from other places: in this case, they point to a variable that is no longer
    // part of the symbol table! [but probably, this doesn't matter, since
    // we don't use the == operator in connection with variables]
    // LATER: for tackling this issue, you could use a separate variable
    // repository for each ProgramConverter
    // WARNING: the given symbol table must be trashed afterwards, since
    // its contents are modified! if you need the original symbol table
    // after this operation, you have to use a method that copies the original
    // variables and adds the modified clones to the current symbol table
    void addAll(SymbolTable table) {

        // the Variable objects' member field pointing at the enclosing symbol
        // table has to be modified;
        // this means that the following tempting line is not enough:
        // this.variables.putAll(table.getVariables());

        // we can iterate over the keys since they are identical to the values
        for (Iterator iter = table.getVariables().keySet().iterator(); iter.hasNext(); ) {
            Variable variable = (Variable) iter.next();
            variable.setSymbolTable(this);
            // adjust "local" and "global" attributes
            if (this.isMain) {
                variable.setIsGlobal();
            } else {
                variable.setIsLocal();
            }
            this.variables.put(variable, variable);
        }
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SymbolTable)) {
            return false;
        }
        SymbolTable comp = (SymbolTable) obj;
        return this.name.equals(comp.name);
    }
}