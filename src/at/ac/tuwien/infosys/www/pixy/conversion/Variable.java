package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Variable extends AbstractTacPlace {
    private String name;
    /** the symbol table this variable belongs to */
    private SymbolTable symbolTable;
    /** member of the superglobals symbol table? */
    private boolean isSuperGlobal = false;
    private boolean isLocal;
    /** local variable of the main function? */
    private boolean isGlobal;
    // (temporaries excluded);
    // hence, globals and superglobals are different

    // a variable is either local or global or superglobal

    /** array properties; array elements can also be arrays, if they have elements themselves */
    private boolean isArray = false;
    /** only direct elements, i.e., who are one dimension deeper */
    private Map<AbstractTacPlace, Variable> elements = null;
    // "shortcut": contains all elements with a literal last index;
    // (only direct elements)
    private List<Variable> literalElements = null;

    // array element properties
    private boolean isArrayElement = false;
    private Variable enclosingArray = null;
    private Variable topEnclosingArray = null;
    /** last index for multi-dimensions */
    private AbstractTacPlace index = null;
    /** all indices */
    private List<AbstractTacPlace> indices = null;
    private boolean hasNonLiteralIndices;

    /** additional information for variable variables */
    private AbstractTacPlace dependsOn = null;

    /** list of array elements whose LAST index is this variable */
    private List<Variable> indexFor = new LinkedList<>();

    /** is this a temporary variable? */
    private boolean isTemp = false;

    /** is this an object member variable? */
    private boolean isMember = false;

    /** is this a function return variable? */
    private boolean isReturnVariable = false;

    public Variable(String name, SymbolTable symbolTable) {
        this.name = name;
        this.symbolTable = symbolTable;
        if (symbolTable.isMain()) {
            this.isLocal = false;
            this.isGlobal = true;
        } else {
            this.isLocal = true;
            this.isGlobal = false;
        }
    }

    public Variable(String name, SymbolTable symbolTable, boolean isTemp) {
        this(name, symbolTable);
        this.isTemp = isTemp;
        if (isTemp) {
            this.isGlobal = false;
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public String getName() {
        return this.name;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public boolean isSuperGlobal() {
        return this.isSuperGlobal;
    }

    public boolean isArray() {
        return this.isArray;
    }

    public List<Variable> getElements() {
        if (this.elements == null) {
            return Collections.emptyList();
        } else {
            return new LinkedList<>(this.elements.values());
        }
    }

    // returns all elements recursively (i.e., the whole array tree, without
    // the root)
    public List<Variable> getElementsRecursive() {
        List<Variable> retMe = new LinkedList<>();
        Collection<Variable> directElements = this.elements.values();
        retMe.addAll(directElements);
        for (Variable directElement : directElements) {
            if (directElement.isArray()) {
                retMe.addAll(directElement.getElementsRecursive());
            }
        }

        return retMe;
    }

    public Variable getElement(AbstractTacPlace index) {
        if (this.elements == null) {
            return null;
        }
        return this.elements.get(index);
    }

    public List<Variable> getLiteralElements() {
        return this.literalElements;
    }

    public boolean isArrayElement() {
        return this.isArrayElement;
    }

    public Variable getEnclosingArray() {
        return this.enclosingArray;
    }

    public Variable getTopEnclosingArray() {
        return this.topEnclosingArray;
    }

    public AbstractTacPlace getIndex() {
        return this.index;
    }

    public List<AbstractTacPlace> getIndices() {
        if (this.indices == null) {
            return new LinkedList<>();
        } else {
            return new LinkedList<>(this.indices);
        }
    }

    // does this array element have non-literal indices?
    public boolean hasNonLiteralIndices() {
        return this.hasNonLiteralIndices;
    }

    public AbstractTacPlace getDependsOn() {
        return this.dependsOn;
    }

    public List<Variable> getIndexFor() {
        return this.indexFor;
    }

    public String toString() {
        return this.symbolTable.getName() + "." + this.name;
    }

    public boolean isTemp() {
        return this.isTemp;
    }

    public boolean isMember() {
        return this.isMember;
    }

    public boolean isReturnVariable() {
        return this.isReturnVariable;
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    public boolean isGlobal() {
        return this.isGlobal;
    }

    public boolean belongsTo(SymbolTable symTab) {
        return (symTab == this.symbolTable);
    }

    // is this a variable variable (such as "$$x")?
    public boolean isVariableVariable() {
        return this.dependsOn != null;
    }

    public boolean isArrayElementOf(Variable array) {
        // if we don't even have an enclosing array: can't be true
        if (this.enclosingArray == null) {
            return false;
        }
        if (this.enclosingArray.equals(array)) {
            // found it!
            return true;
        }
        // walk upwards recursively
        return this.enclosingArray.isArrayElementOf(array);
    }

    void setIsSuperGlobal(boolean isSuperGlobal) {
        this.isSuperGlobal = isSuperGlobal;
        if (isSuperGlobal) {
            this.isLocal = false;
            this.isGlobal = false;
        }
    }

    void setIsArray(boolean isArray) {
        if (isArray && !this.isArray) {
            this.isArray = isArray;
            this.elements = new LinkedHashMap<>();
            this.literalElements = new LinkedList<>();
        } else {
            System.out.println("Warning: unchecked call to Variable.setIsArray()");
        }
    }

    void addElement(Variable variable) {
        if (!variable.isArrayElement()) {
            throw new RuntimeException("SNH");
        }
        this.elements.put(variable.getIndex(), variable);
        if (variable.getIndex() instanceof Literal) {
            this.literalElements.add(variable);
        }
    }

    void setArrayElementAttributes(Variable enclosingArray, AbstractTacPlace index) {
        this.isArrayElement = true;
        this.indices = new LinkedList<>();
        this.hasNonLiteralIndices = enclosingArray.hasNonLiteralIndices();
        this.enclosingArray = enclosingArray;
        if (enclosingArray.isArrayElement()) {
            this.topEnclosingArray = enclosingArray.getTopEnclosingArray();
            this.indices.addAll(enclosingArray.getIndices());
        } else {
            this.topEnclosingArray = enclosingArray;
        }
        this.index = index;
        this.indices.add(index);
        if (!(index instanceof Literal)) {
            this.hasNonLiteralIndices = true;
        }

        if (index.isVariable()) {
            ((Variable) index).addIndexFor(this);
        }

        // if the enclosing array is a temporary, then this
        // one also has to be a temporary
        if (enclosingArray.isTemp()) {
            this.isTemp = true;
        }

        // if the enclosing array is a global, then this
        // one also has to be a global
        if (enclosingArray.isGlobal()) {
            this.isGlobal = true;
        }
    }

    void setDependsOn(AbstractTacPlace dependsOn) {
        this.dependsOn = dependsOn;
    }

    void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    void addIndexFor(Variable var) {
        this.indexFor.add(var);
    }

    void setIsMember(boolean isMember) {
        this.isMember = isMember;
    }

    void setIsReturnVariable(boolean isReturnVariable) {
        this.isReturnVariable = isReturnVariable;
    }

    void setIsGlobal() {
        this.isGlobal = true;
        this.isLocal = false;
    }

    void setIsLocal() {
        this.isLocal = true;
        this.isGlobal = false;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Variable)) {
            return false;
        }
        Variable comp = (Variable) obj;
        return this.symbolTable.equals(comp.getSymbolTable()) && this.name.equals(comp.getName());
    }

    // EFF: hashcode caching
    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.name.hashCode();
        hashCode = 37 * hashCode + this.symbolTable.hashCode();
        return hashCode;
    }
}