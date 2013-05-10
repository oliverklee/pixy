package at.ac.tuwien.infosys.www.pixy.analysis.type;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeLatticeElement
    extends LatticeElement {

    private Map<Variable, Set<Type>> var2Type;

    // an empty lattice element (the analysis starts with this one)
    public TypeLatticeElement() {
        this.var2Type = new HashMap<Variable, Set<Type>>();
    }

    // clones the given element
    public TypeLatticeElement(TypeLatticeElement element) {
        this.var2Type = new HashMap<Variable, Set<Type>>(element.var2Type);
    }

    // lubs the given lattice element over <<this>> lattice element
    public void lub(LatticeElement foreignX) {
        TypeLatticeElement foreign = (TypeLatticeElement) foreignX;
        // for all foreign mappings...
        for (Map.Entry<Variable, Set<Type>> entry : foreign.var2Type.entrySet()) {
            Variable foreignVar = entry.getKey();
            Set<Type> foreignTypes = entry.getValue();
            Set<Type> myTypes = this.var2Type.get(foreignVar);
            if (this.var2Type.containsKey(foreignVar)) {
                // if we already have a mapping for this variable:
                // union over the types
                myTypes.addAll(foreignTypes);
            } else {
                // if we don't have a mapping for this variable yet:
                // add this mapping
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
            // we don't want to modify the special member variable
            return;
        }

        if (types == null) {
            // simply remove the mapping
            this.var2Type.remove(var);
        } else {
            this.var2Type.put(var, types);
        }
    }

    // can also return null
    public Set<Type> getType(Variable var) {
        return this.var2Type.get(var);
    }

    public void assign(Variable left, TacPlace right) {
        if (!(right instanceof Variable)) {
            // we are only tracking object types at the moment
            this.var2Type.remove(left);
            return;
        }
        Variable rightVar = (Variable) right;
        Set<Type> rightTypes = this.var2Type.get(rightVar);
        this.setType(left, rightTypes);
    }

    public void assignUnary(Variable left) {
        // simply delete all info about left
        this.setType(left, null);
    }

    public void assignBinary(Variable left) {
        // simply delete all info about left
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

    // sets the dep and array label of the given formal
    public void setFormal(TacFormalParam formalParam, TacPlace setTo) {
        Variable formalVar = formalParam.getVariable();
        this.assign(formalVar, setTo);
    }

    // resets all variables that belong to the given symbol table
    public void resetVariables(SymbolTable symTab) {
        for (Iterator iter = this.var2Type.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable var = (Variable) entry.getKey();

            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }
    }

    // resets all temporaries that belong to the given symbol table
    public void resetTemporaries(SymbolTable symTab) {

        for (Iterator iter = this.var2Type.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable var = (Variable) entry.getKey();

            if (!var.isTemp()) {
                // nothing to do for non-temporaries
                continue;
            }

            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }
    }

    // copies the mappings for "global-like" places
    // from interIn (i.e., global variables, superglobal variables, and
    // constants)
    public void copyGlobalLike(TypeLatticeElement interIn) {

        for (Map.Entry<Variable, Set<Type>> entry : interIn.var2Type.entrySet()) {

            Variable origVar = entry.getKey();
            Set<Type> origTypes = entry.getValue();

            if (origVar.isGlobal() || origVar.isSuperGlobal()) {
                this.setType(origVar, origTypes);
            }
        }
    }

    // copies the mappings for local temporaries of the main function
    public void copyMainTemporaries(TypeLatticeElement origElement) {

        for (Map.Entry<Variable, Set<Type>> entry : origElement.var2Type.entrySet()) {

            Variable origVar = entry.getKey();
            Set<Type> origTypes = entry.getValue();

            // nothing to do for non-main's and non-temporaries
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

    // sets the temporary responsible for catching the return value
    // and resets the return variable
    public void handleReturnValue(CfgNodeCall callNode,
                                  TypeLatticeElement calleeIn, TacFunction callee) {

        Variable tempVar = callNode.getTempVar();
        Set<Type> types = calleeIn.getType(callNode.getRetVar());
        Variable retVar = callNode.getRetVar();

        // if the callee is a constructor, return the appropriate class as type
        if (callee.isConstructor()) {
            types = new HashSet<Type>();
            types.add(Type.getTypeForClass(callee.getClassName()));
        }

        this.setType(tempVar, types);
        this.var2Type.remove(retVar);
    }

    // copies the mappings for local variables from origElement
    public void copyLocals(TypeLatticeElement origElement) {

        for (Map.Entry<Variable, Set<Type>> entry : origElement.var2Type.entrySet()) {

            Variable origVar = entry.getKey();
            Set<Type> origTypes = entry.getValue();

            // nothing to do for non-locals
            if (!origVar.isLocal()) {
                continue;
            }

            this.setType(origVar, origTypes);
        }
    }

    public void handleReturnValueUnknown(Variable tempVar) {
        this.setType(tempVar, null);
    }

    public LatticeElement cloneMe() {
        // uses the cloning constructor
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