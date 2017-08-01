package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

public class SymbolTable {

	private Map<Variable, Variable> variables;
	private String name;
	private boolean isSuperSymTab;
	private boolean isMain;

	private Map<Variable, Variable> globals2GShadows;

	private Map<Variable, Variable> formals2FShadows;

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

	public SymbolTable(String name) {
		this(name, false);
		if (name.equals(InternalStrings.mainFunctionName)) {
			this.isMain = true;
		}
		this.globals2GShadows = new HashMap<Variable, Variable>();
		this.formals2FShadows = new HashMap<Variable, Variable>();
	}

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

	public Variable getGShadow(Variable global) {
		return this.globals2GShadows.get(global);
	}

	public Variable getFShadow(Variable formal) {
		return this.formals2FShadows.get(formal);
	}

	public Map<Variable, Variable> getGlobals2GShadows() {
		return this.globals2GShadows;
	}

	public Map<Variable, Variable> getFormals2FShadows() {
		return this.formals2FShadows;
	}

	void add(Variable newVar) {
		this.variables.put(newVar, newVar);
	}

	void addGShadow(Variable global) {
		Variable gShadow = new Variable(global.getName() + InternalStrings.gShadowSuffix, this);
		this.variables.put(gShadow, gShadow);
		this.globals2GShadows.put(global, gShadow);
	}

	void addFShadow(Variable formal) {
		Variable fShadow = new Variable(formal.getName() + InternalStrings.fShadowSuffix, this);
		this.variables.put(fShadow, fShadow);
		this.formals2FShadows.put(formal, fShadow);
	}

	void addAll(SymbolTable table) {

		for (Iterator<Variable> iter = table.getVariables().keySet().iterator(); iter.hasNext();) {
			Variable variable = (Variable) iter.next();
			variable.setSymbolTable(this);
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
