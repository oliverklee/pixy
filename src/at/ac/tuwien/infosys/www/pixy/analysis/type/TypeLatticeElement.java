package at.ac.tuwien.infosys.www.pixy.analysis.type;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class TypeLatticeElement extends AbstractLatticeElement {

	private Map<Variable, Set<Type>> var2Type;

	public TypeLatticeElement() {
		this.var2Type = new HashMap<Variable, Set<Type>>();
	}

	public TypeLatticeElement(TypeLatticeElement element) {
		this.var2Type = new HashMap<Variable, Set<Type>>(element.var2Type);
	}

	public void lub(AbstractLatticeElement foreignX) {
		TypeLatticeElement foreign = (TypeLatticeElement) foreignX;
		for (Map.Entry<Variable, Set<Type>> entry : foreign.var2Type.entrySet()) {
			Variable foreignVar = entry.getKey();
			Set<Type> foreignTypes = entry.getValue();
			Set<Type> myTypes = this.var2Type.get(foreignVar);
			if (this.var2Type.containsKey(foreignVar)) {
				myTypes.addAll(foreignTypes);
			} else {
				this.var2Type.put(foreignVar, foreignTypes);
			}
		}
	}

	public void setTypeString(Variable var, String className) {
		Set<Type> types = new HashSet<Type>();
		types.add(Type.getTypeForClass(className));
		this.setType(var, types);
	}

	private void setType(Variable var, Set<Type> types) {
		if (var.isMember()) {
			return;
		}
		if (types == null) {
			this.var2Type.remove(var);
		} else {
			this.var2Type.put(var, types);
		}
	}

	public Set<Type> getType(Variable var) {
		return this.var2Type.get(var);
	}

	public void assign(Variable left, AbstractTacPlace right) {
		if (left == null) {
			return;
		}
		if (!(right instanceof Variable)) {
			this.var2Type.remove(left);
			return;
		}
		Variable rightVar = (Variable) right;
		Set<Type> rightTypes = this.var2Type.get(rightVar);
		this.setType(left, rightTypes);
	}

	public void assignUnary(Variable left) {
		this.setType(left, null);
	}

	public void assignBinary(Variable left) {
		this.setType(left, null);
	}

	public void unset(Variable var) {
		this.setType(var, null);
	}

	public void assignArray(Variable var) {
		this.setType(var, null);
	}

	public void handleReturnValueBuiltin(Variable tempVar) {
		this.setType(tempVar, null);
	}

	public void setFormal(TacFormalParameter formalParam, AbstractTacPlace setTo) {
		Variable formalVar = formalParam.getVariable();
		this.assign(formalVar, setTo);
	}

	@SuppressWarnings("rawtypes")
	public void resetVariables(SymbolTable symTab) {
		for (Iterator<?> iter = this.var2Type.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Variable var = (Variable) entry.getKey();
			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void resetTemporaries(SymbolTable symTab) {

		for (Iterator<?> iter = this.var2Type.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Variable var = (Variable) entry.getKey();

			if (!var.isTemp()) {
				continue;
			}
			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}
	}

	public void copyGlobalLike(TypeLatticeElement interIn) {

		for (Map.Entry<Variable, Set<Type>> entry : interIn.var2Type.entrySet()) {

			Variable origVar = entry.getKey();
			Set<Type> origTypes = entry.getValue();

			if (origVar.isGlobal() || origVar.isSuperGlobal()) {
				this.setType(origVar, origTypes);
			}
		}
	}

	public void copyMainTemporaries(TypeLatticeElement origElement) {

		for (Map.Entry<Variable, Set<Type>> entry : origElement.var2Type.entrySet()) {

			Variable origVar = entry.getKey();
			Set<Type> origTypes = entry.getValue();

			SymbolTable symTab = origVar.getSymbolTable();
			if (!symTab.isMain()) {
				continue;
			}
			if (!origVar.isTemp()) {
				continue;
			}

			this.setType(origVar, origTypes);
		}
	}

	public void handleReturnValue(Call callNode, TypeLatticeElement calleeIn, TacFunction callee) {

		Variable tempVar = callNode.getTempVar();
		Set<Type> types = calleeIn.getType(callNode.getRetVar());
		Variable retVar = callNode.getRetVar();

		if (callee.isConstructor()) {
			types = new HashSet<Type>();
			types.add(Type.getTypeForClass(callee.getClassName()));
		}

		this.setType(tempVar, types);
		this.var2Type.remove(retVar);

	}

	public void copyLocals(TypeLatticeElement origElement) {

		for (Map.Entry<Variable, Set<Type>> entry : origElement.var2Type.entrySet()) {

			Variable origVar = entry.getKey();
			Set<Type> origTypes = entry.getValue();

			if (!origVar.isLocal()) {
				continue;
			}

			this.setType(origVar, origTypes);
		}
	}

	public void handleReturnValueUnknown(Variable tempVar) {
		this.setType(tempVar, null);
	}

	public AbstractLatticeElement cloneMe() {
		return new TypeLatticeElement(this);
	}

	public boolean equals(Object obj) {
		return this.structureEquals(obj);
	}

	public int hashCode() {
		return this.structureHashCode();
	}

	public boolean structureEquals(Object compX) {
		if (compX == this) {
			return true;
		}
		if (!(compX instanceof TypeLatticeElement)) {
			return false;
		}
		TypeLatticeElement comp = (TypeLatticeElement) compX;

		if (!this.var2Type.equals(comp.var2Type)) {
			return false;
		}

		return true;
	}

	public int structureHashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.var2Type.hashCode();
		return hashCode;
	}

	public void dump() {
		System.out.println(this.var2Type);
	}

}