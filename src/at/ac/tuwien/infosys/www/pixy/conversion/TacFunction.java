package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class TacFunction {

	private String name;
	private ControlFlowGraph cfg;
	private boolean isReference;
	private List<TacFormalParameter> params;
	private Variable retVar;

	private SymbolTable symbolTable;

	private List<Call> calledFrom;

	private boolean isMain;

	private String className;
	private boolean isConstructor;

	private boolean isAbstract;
	private boolean isStatic;
	private boolean isFinal;
	private String accessModifier;

	private ParseNode parseNode;

	TacFunction(String name, ControlFlowGraph cfg, Variable retVar, boolean isReference, ParseNode parseNode,
			String className) {

		this.name = name;
		this.cfg = cfg;
		this.retVar = retVar;
		this.isReference = isReference;
		this.parseNode = parseNode;
		this.className = className;
		this.isConstructor = false;
		if (!className.isEmpty()) {
			String name2 = name.substring(0, name.length() - InternalStrings.methodSuffix.length());
			if (name2.equals(className)) {
				this.isConstructor = true;
			}
		}

		this.params = Collections.emptyList();
		this.symbolTable = new SymbolTable(name);
		this.calledFrom = new LinkedList<Call>();
		this.isMain = false;

		this.cfg.getTail().setEnclosingFunction(this);
	}

	public String getName() {
		return this.name;
	}

	public ControlFlowGraph getCfg() {
		return this.cfg;
	}

	public boolean isReference() {
		return this.isReference;
	}

	public List<TacFormalParameter> getParams() {
		return this.params;
	}

	public TacFormalParameter getParam(int index) {
		return this.params.get(index);
	}

	public Variable getRetVar() {
		return this.retVar;
	}

	public SymbolTable getSymbolTable() {
		return this.symbolTable;
	}

	Variable getVariable(String varName) {
		return this.symbolTable.getVariable(varName);
	}

	public List<Call> getCalledFrom() {
		return this.calledFrom;
	}

	public List<Call> getContainedCalls() {
		return this.cfg.getContainedCalls();
	}

	public boolean isMain() {
		return this.isMain;
	}

	public boolean isConstructor() {
		return this.isConstructor;
	}

	boolean isEmpty() {
		if (this.cfg.size() == 2) {
			return true;
		} else {
			return false;
		}
	}

	public Collection<Variable> getLocals() {
		return this.symbolTable.getVariablesColl();
	}

	public long size() {
		return this.cfg.size();
	}

	public String getFileName() {
		return this.parseNode.getFileName();
	}

	public String getLoc() {
		if (this.parseNode.isToken()) {
			return "Column: ((" + this.parseNode.column() + ")) , Line Number:  ((" + this.parseNode.getLineno()
					+ ")).";
		}
		return "unknown";
	}

	public int getLine() {
		return this.cfg.getHead().getOriginalLineNumber();
	}

	public String getClassName() {
		return this.className;
	}

	void setParams(List<TacFormalParameter> params) {
		this.params = params;
	}

	void setIsMain(boolean isMain) {
		this.isMain = isMain;
	}

	public void addCalledFrom(Call callNode) {
		this.calledFrom.add(callNode);
	}

	public void assignReversePostOrder() {
		this.cfg.assignReversePostOrder();
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof TacFunction)) {
			return false;
		}
		TacFunction comp = (TacFunction) obj;
		if (!this.name.equals(comp.name)) {
			return false;
		}
		if (!this.className.equals(comp.className)) {
			return false;
		}
		return true;
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