package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

public class Variable extends AbstractTacPlace {

	private String name;
	private SymbolTable symbolTable;
	private boolean isSuperGlobal;
	private boolean isLocal;
	private boolean isGlobal;
	private boolean isAbstract;
	private boolean isStatic;
	private boolean isFinal;
	private String accessModifier;
	private boolean isCustomObject;
	private String CustomClass;
	private boolean isArray;
	private Map<AbstractTacPlace, Variable> elements;
	private List<Variable> literalElements;
	private boolean isArrayElement;
	private Variable enclosingArray;
	private Variable topEnclosingArray;
	private AbstractTacPlace index;
	private List<AbstractTacPlace> indices;
	private boolean hasNonLiteralIndices;
	private AbstractTacPlace dependsOn;
	private List<Variable> indexFor;
	private boolean isTemp;
	private boolean isMember;
	private boolean isReturnVariable;

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
			return new LinkedList<Variable>(this.elements.values());
		}
	}

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

	public Variable getElement(AbstractTacPlace index) {
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

	public AbstractTacPlace getIndex() {
		return this.index;
	}

	public List<AbstractTacPlace> getIndices() {
		if (this.indices == null) {
			return new LinkedList<AbstractTacPlace>();
		} else {
			return new LinkedList<AbstractTacPlace>(this.indices);
		}
	}

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

	public boolean isVariableVariable() {
		if (this.dependsOn == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isArrayElementOf(Variable array) {
		if (this.enclosingArray == null) {
			return false;
		}
		if (this.enclosingArray.equals(array)) {
			return true;
		}
		return this.enclosingArray.isArrayElementOf(array);
	}

	public boolean GetisCustomObject() {
		return this.isCustomObject;
	}

	public String GetCustomClass() {
		return this.CustomClass;
	}

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
			this.elements = new LinkedHashMap<AbstractTacPlace, Variable>();
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

	void setArrayElementAttributes(Variable enclosingArray, AbstractTacPlace index) {

		this.isArrayElement = true;
		this.indices = new LinkedList<AbstractTacPlace>();
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

		if (enclosingArray.isTemp()) {
			this.isTemp = true;
		}

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

	void SetisCustomObject(boolean value) {
		this.isCustomObject = value;
	}

	void SetCustomClass(String value) {
		this.CustomClass = value;
	}

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

	public int hashCode() {

		int hashCode = 17;
		hashCode = 37 * hashCode + this.name.hashCode();
		hashCode = 37 * hashCode + this.symbolTable.hashCode();
		return hashCode;
	}

	public boolean IsAbstract() {
		return isAbstract;
	}

	public void SetAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean IsStatic() {
		return isStatic;
	}

	public void SetStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public String getAccessModifier() {
		return accessModifier;
	}

	public void setAccessModifier(String accessModifier) {
		this.accessModifier = accessModifier;
	}

	public boolean IsFinal() {
		return isFinal;
	}

	public void SetFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

}
