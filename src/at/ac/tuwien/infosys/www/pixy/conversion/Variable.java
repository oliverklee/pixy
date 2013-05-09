package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Variable
    extends TacPlace {

    // EFF: check for unnecessary fields / methods

    private String name;
    private SymbolTable symbolTable; // the symbol table this variable belongs to;
    private boolean isSuperGlobal;   // member of the superglobals symboltable?
    private boolean isLocal;
    private boolean isGlobal;   // local variable of the main function?
    // (temporaries excluded);
    // hence, globals and superglobals are different

    // a variable is either local or global or superglobal

    // array properties; array elements can also be arrays, if they have
    // elements themselves
    private boolean isArray;
    // TacPlace (=index) -> Variable
    // (only direct elements, i.e., who are one dimension deeper)
    private Map<TacPlace, Variable> elements;
    // "shortcut": contains all elements with a literal last index;
    // (only direct elements)
    private List<Variable> literalElements;

    // array element properties
    private boolean isArrayElement;
    private Variable enclosingArray;
    private Variable topEnclosingArray;
    private TacPlace index;     // last index for multidimensions
    private List<TacPlace> indices;   // all indices
    private boolean hasNonLiteralIndices;

    // additional information for variable variables
    private TacPlace dependsOn;

    // list of array elements whose LAST index is this variable
    private List<Variable> indexFor;

    // is this a temporary variable?
    private boolean isTemp;

    // is this an object member variable?
    private boolean isMember;

    // is this a function return variable?
    private boolean isReturnVariable;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Variable(String name, SymbolTable symbolTable) {
        this.name = name;
        this.symbolTable = symbolTable;
        this.isSuperGlobal = false;
        if (symbolTable.isMain()) {
            this.isLocal = false;
            this.isGlobal = true;
        } else {
            this.isLocal = true;
            this.isGlobal = false;
        }

        this.isArray = false;
        this.elements = null;
        this.literalElements = null;

        this.isArrayElement = false;
        this.enclosingArray = null;
        this.topEnclosingArray = null;
        this.index = null;
        this.indices = null;

        this.dependsOn = null;
        this.indexFor = new LinkedList<Variable>();

        this.isTemp = false;
        this.isMember = false;
        this.isReturnVariable = false;
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
            // System.out.println("WARNING: unchecked call to Variable.getElements()");
            return Collections.emptyList();
        } else {
            return new LinkedList<Variable>(this.elements.values());
        }
    }

    // returns all elements recursively (i.e., the whole array tree, without
    // the root)
    public List<Variable> getElementsRecursive() {
        List<Variable> retMe = new LinkedList<Variable>();
        Collection<Variable> directElements = this.elements.values();
        retMe.addAll(directElements);
        for (Variable directElement : directElements) {
            if (directElement.isArray()) {
                retMe.addAll(directElement.getElementsRecursive());
            }
        }
        return retMe;
    }

    public Variable getElement(TacPlace index) {
        if (this.elements == null) {
            return null;
        }
        return (Variable) this.elements.get(index);
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

    public TacPlace getIndex() {
        return this.index;
    }

    public List<TacPlace> getIndices() {
        if (this.indices == null) {
            return new LinkedList<TacPlace>();
        } else {
            return new LinkedList<TacPlace>(this.indices);
        }
    }

    // does this array element have non-literal indices?
    public boolean hasNonLiteralIndices() {
        return this.hasNonLiteralIndices;
    }

    public TacPlace getDependsOn() {
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
        if (this.dependsOn == null) {
            return false;
        } else {
            return true;
        }
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

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    void setIsSuperGlobal(boolean isSuperGlobal) {
        this.isSuperGlobal = isSuperGlobal;
        if (isSuperGlobal) {
            this.isLocal = false;
            this.isGlobal = false;
        }
    }

    void setIsArray(boolean isArray) {
        if (isArray == true && this.isArray == false) {
            this.isArray = isArray;
            this.elements = new LinkedHashMap<TacPlace, Variable>();
            this.literalElements = new LinkedList<Variable>();
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

    void setArrayElementAttributes(Variable enclosingArray, TacPlace index) {

        this.isArrayElement = true;
        this.indices = new LinkedList<TacPlace>();
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

    void setDependsOn(TacPlace dependsOn) {
        this.dependsOn = dependsOn;
    }

    void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        // this.resetHashCode();
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

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Variable)) {
            return false;
        }
        Variable comp = (Variable) obj;
        if (!this.symbolTable.equals(comp.getSymbolTable())) {
            return false;
        }
        return (this.name.equals(comp.getName()));
    }

    // EFF: hashcode caching
    public int hashCode() {

        int hashCode = 17;
        hashCode = 37 * hashCode + this.name.hashCode();
        hashCode = 37 * hashCode + this.symbolTable.hashCode();
        return hashCode;
    }
}