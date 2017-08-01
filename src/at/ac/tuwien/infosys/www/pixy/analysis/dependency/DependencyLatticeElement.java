package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import java.util.*;
import java.util.Map.Entry;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;

public class DependencyLatticeElement extends AbstractLatticeElement {

	private Map<AbstractTacPlace, DependencySet> placeToDep;

	private Map<Variable, DependencySet> arrayLabels;

	public static DependencyLatticeElement DEFAULT;

	public DependencyLatticeElement() {
		this.placeToDep = new HashMap<AbstractTacPlace, DependencySet>();
		this.arrayLabels = new HashMap<Variable, DependencySet>();
	}

	public DependencyLatticeElement(DependencyLatticeElement element) {
		this.placeToDep = new HashMap<AbstractTacPlace, DependencySet>(element.getPlaceToDep());
		this.arrayLabels = new HashMap<Variable, DependencySet>(element.getArrayLabels());
	}

	public DependencyLatticeElement cloneMe() {
		return new DependencyLatticeElement(this);
	}

	@SuppressWarnings("rawtypes")
	private DependencyLatticeElement(List<AbstractTacPlace> places, ConstantsTable constantsTable, List functions,
			SymbolTable superSymbolTable, Variable memberPlace) {

		this.placeToDep = new HashMap<AbstractTacPlace, DependencySet>();
		this.arrayLabels = new HashMap<Variable, DependencySet>();
		for (AbstractTacPlace place : places) {

			if ((place instanceof Variable) && place.getVariable().isArrayElement()
					&& place.getVariable().hasNonLiteralIndices()) {

			} else {
				this.placeToDep.put(place, DependencySet.UNINIT);
			}

			if ((place instanceof Variable) && !(place.getVariable().isArrayElement())) {
				this.arrayLabels.put((Variable) place, DependencySet.UNINIT);
			}
		}

		for (AbstractTacPlace place : places) {
			if ((place instanceof Variable) && (place.getVariable().isReturnVariable())) {
				this.placeToDep.put(place, DependencySet.UNINIT);
				this.arrayLabels.put((Variable) place, DependencySet.UNINIT);
			}
		}

		this.placeToDep.put(memberPlace, DependencySet.UNINIT);
		this.arrayLabels.put(memberPlace, DependencySet.UNINIT);

		Map<String, Constant> constants = constantsTable.getConstants();
		for (Iterator<Constant> iter = constants.values().iterator(); iter.hasNext();) {
			Constant constant = (Constant) iter.next();
			this.placeToDep.put(constant, DependencySet.UNINIT);
		}

		for (Iterator iter = functions.iterator(); iter.hasNext();) {
			TacFunction function = (TacFunction) iter.next();

			if (function.isMain()) {
				if (MyOptions.optionG) {
					continue;
				}
			}
			SymbolTable symtab = function.getSymbolTable();
			Map variables = symtab.getVariables();
			for (Iterator varIter = variables.values().iterator(); varIter.hasNext();) {
				Variable variable = (Variable) varIter.next();

				if (variable.isArrayElement()) {
					continue;
				}
				this.initTree(variable, DependencySet.UNINIT);
				this.arrayLabels.put(variable, DependencySet.UNINIT);
			}
		}

		Variable sess = superSymbolTable.getVariable("$_SESSION");
		this.initTree(sess, DependencySet.UNINIT);
		this.arrayLabels.put(sess, DependencySet.UNINIT);

		List<Variable> harmlessSuperGlobals = new LinkedList<Variable>();
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "SERVER_NAME");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "HTTP_HOST");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "HTTP_ACCEPT_LANGUAGE");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "SERVER_SOFTWARE");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "PHP_AUTH_USER");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "PHP_AUTH_PW");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "PHP_AUTH_TYPE");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "SCRIPT_NAME");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "SCRIPT_FILENAME");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "REQUEST_URI");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "QUERY_STRING");
		addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "SCRIPT_URI");
		Variable argv = superSymbolTable.getVariable("$_SERVER[argv]");
		if (argv != null) {
			List argvElements = argv.getElements();
			for (Iterator iter = argvElements.iterator(); iter.hasNext();) {
				Variable argvElement = (Variable) iter.next();
				harmlessSuperGlobals.add(argvElement);
			}
		}
		argv = superSymbolTable.getVariable("$HTTP_SERVER_VARS[argv]");
		if (argv != null) {
			List argvElements = argv.getElements();
			for (Iterator iter = argvElements.iterator(); iter.hasNext();) {
				Variable argvElement = (Variable) iter.next();
				harmlessSuperGlobals.add(argvElement);
			}
		}
		for (Iterator iter = harmlessSuperGlobals.iterator(); iter.hasNext();) {
			Variable harmlessSuperGlobal = (Variable) iter.next();
			if (harmlessSuperGlobal != null) {
				this.placeToDep.put(harmlessSuperGlobal, DependencySet.UNINIT);
			}
		}
	}

	private void addHarmlessServerVar(List<Variable> harmlessSuperGlobals, SymbolTable superSymbolTable, String name) {

		Variable v1 = superSymbolTable.getVariable("$_SERVER[" + name + "]");
		Variable v2 = superSymbolTable.getVariable("$HTTP_SERVER_VARS[" + name + "]");
		if (v1 == null || v2 == null) {
			throw new RuntimeException("SNH: " + name);
		}
		harmlessSuperGlobals.add(v1);
		harmlessSuperGlobals.add(v2);
	}

	@SuppressWarnings("rawtypes")
	static void initDefault(List<AbstractTacPlace> places, ConstantsTable constantsTable, List functions,
			SymbolTable superSymbolTable, Variable memberPlace) {

		DependencyLatticeElement.DEFAULT = new DependencyLatticeElement(places, constantsTable, functions,
				superSymbolTable, memberPlace);
	}

	public Map<AbstractTacPlace, DependencySet> getPlaceToDep() {
		return this.placeToDep;
	}

	public Map<Variable, DependencySet> getArrayLabels() {
		return this.arrayLabels;
	}

	public DependencySet getDep(AbstractTacPlace place) {
		return this.getDepFrom(place, this.placeToDep);
	}

	private DependencySet getDepFrom(AbstractTacPlace place, Map<? extends AbstractTacPlace, DependencySet> readFrom) {

		if (place instanceof Literal) {
			throw new RuntimeException("SNH any longer");
		}

		if (place instanceof Variable) {
			Variable var = (Variable) place;
			if (var.isArrayElement() && var.hasNonLiteralIndices()) {
				return getArrayLabel(var.getTopEnclosingArray());
			}
		}

		DependencySet nonDefaultDep = readFrom.get(place);
		if (nonDefaultDep != null) {
			return nonDefaultDep;
		}

		DependencySet defaultDep = getDefaultDep(place);
		if (defaultDep == null) {
			throw new RuntimeException("SNH: " + place);
		} else {
			return defaultDep;
		}
	}

	private static DependencySet getDefaultDep(AbstractTacPlace place) {
		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		return (DependencySet) DependencyLatticeElement.DEFAULT.getPlaceToDep().get(place);
	}

	private DependencySet getNonDefaultDep(AbstractTacPlace place) {
		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		return this.placeToDep.get(place);
	}

	public DependencySet getArrayLabel(AbstractTacPlace place) {

		if ((place instanceof Literal) || (place instanceof Constant)) {
			throw new RuntimeException("SNH any longer");
		}

		Variable var = (Variable) place;
		if (var.isArrayElement()) {
			var = var.getTopEnclosingArray();
		}
		DependencySet nonDefaultArrayLabel = getNonDefaultArrayLabel(var);
		if (nonDefaultArrayLabel != null) {
			return nonDefaultArrayLabel;
		}

		return getDefaultArrayLabel(var);
	}

	private DependencySet getDefaultArrayLabel(Variable var) {
		return DEFAULT.arrayLabels.get(var);
	}

	private DependencySet getNonDefaultArrayLabel(Variable var) {
		return this.arrayLabels.get(var);
	}

	private void setDep(AbstractTacPlace place, DependencySet depSet) {

		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		if (place instanceof Variable && place.getVariable().isMember()) {
			return;
		}

		if (getDefaultDep(place).equals(depSet)) {
			this.placeToDep.remove(place);
		} else {
			this.placeToDep.put(place, depSet);
		}
	}

	private void lubDep(AbstractTacPlace place, DependencySet depSet) {

		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		if (place instanceof Variable && place.getVariable().isMember()) {
			return;
		}

		DependencySet oldDep = this.getDep(place);
		DependencySet resultDep = DependencySet.lub(oldDep, depSet);
		if (getDefaultDep(place).equals(resultDep)) {
			this.placeToDep.remove(place);
		} else {
			this.placeToDep.put(place, resultDep);
		}
	}

	private void setArrayLabel(Variable var, DependencySet depSet) {

		if (var.isArrayElement()) {
			throw new RuntimeException("SNH: " + var);
		}
		if (var.isMember()) {
			return;
		}

		if (getDefaultArrayLabel(var).equals(depSet)) {
			this.arrayLabels.remove(var);
		} else {
			this.arrayLabels.put(var, depSet);
		}
	}

	private void lubArrayLabel(Variable var, DependencySet depSet) {

		if (var.isMember()) {
			return;
		}

		DependencySet oldDep = this.getArrayLabel(var);
		DependencySet resultDep = DependencySet.lub(depSet, oldDep);
		if (resultDep.equals(getDefaultArrayLabel(var))) {
			this.arrayLabels.remove(var);
		} else {
			this.arrayLabels.put(var, resultDep);
		}
	}

	private void setWholeTree(Variable root, DependencySet depSet) {

		this.setDep(root, depSet);
		if (!root.isArray()) {
			return;
		}
		for (Variable element : root.getLiteralElements()) {
			this.setWholeTree(element, depSet);
		}
	}

	private void lubWholeTree(Variable root, DependencySet depSet) {

		this.lubDep(root, depSet);
		if (!root.isArray()) {
			return;
		}
		for (Variable element : root.getLiteralElements()) {
			this.lubWholeTree(element, depSet);
		}
	}

	public void lub(AbstractLatticeElement foreignX) {

		DependencyLatticeElement foreign = (DependencyLatticeElement) foreignX;
		Map<AbstractTacPlace, DependencySet> newPlaceToDep = new HashMap<AbstractTacPlace, DependencySet>(
				this.placeToDep);
		for (Map.Entry<AbstractTacPlace, DependencySet> myEntry : this.placeToDep.entrySet()) {
			AbstractTacPlace myPlace = myEntry.getKey();
			DependencySet myDep = myEntry.getValue();
			DependencySet foreignDep = foreign.getDep(myPlace);
			newPlaceToDep.put(myPlace, DependencySet.lub(myDep, foreignDep));
		}
		this.placeToDep = newPlaceToDep;
		Map<AbstractTacPlace, DependencySet> foreignPlaceToDep = foreign.getPlaceToDep();
		for (Map.Entry<AbstractTacPlace, DependencySet> foreignEntry : foreignPlaceToDep.entrySet()) {
			AbstractTacPlace foreignPlace = foreignEntry.getKey();
			DependencySet foreignDep = foreignEntry.getValue();

			DependencySet myDep = this.getNonDefaultDep(foreignPlace);
			if (myDep == null) {
				myDep = getDefaultDep(foreignPlace);
				this.placeToDep.put(foreignPlace, DependencySet.lub(foreignDep, myDep));
			}
		}

		for (Iterator<Map.Entry<AbstractTacPlace, DependencySet>> iter = this.placeToDep.entrySet().iterator(); iter
				.hasNext();) {

			Map.Entry<AbstractTacPlace, DependencySet> entry = iter.next();
			AbstractTacPlace place = entry.getKey();
			DependencySet dep = entry.getValue();
			if (getDefaultDep(place).equals(dep)) {
				iter.remove();
			}
		}
		Map<Variable, DependencySet> newArrayLabels = new HashMap<Variable, DependencySet>(this.arrayLabels);
		for (Map.Entry<Variable, DependencySet> myEntry : this.arrayLabels.entrySet()) {
			Variable myVar = myEntry.getKey();
			DependencySet myArrayLabel = myEntry.getValue();
			DependencySet foreignArrayLabel = foreign.getArrayLabel(myVar);
			newArrayLabels.put(myVar, DependencySet.lub(myArrayLabel, foreignArrayLabel));
		}
		this.arrayLabels = newArrayLabels;
		Map<Variable, DependencySet> foreignArrayLabels = foreign.getArrayLabels();
		for (Map.Entry<Variable, DependencySet> foreignEntry : foreignArrayLabels.entrySet()) {
			Variable foreignVar = foreignEntry.getKey();
			DependencySet foreignArrayLabel = foreignEntry.getValue();
			DependencySet myArrayLabel = getNonDefaultArrayLabel(foreignVar);
			if (myArrayLabel == null) {
				myArrayLabel = getDefaultArrayLabel(foreignVar);
				this.arrayLabels.put(foreignVar, DependencySet.lub(myArrayLabel, foreignArrayLabel));
			}
		}
		for (Iterator<Map.Entry<Variable, DependencySet>> iter = this.arrayLabels.entrySet().iterator(); iter
				.hasNext();) {

			Map.Entry<Variable, DependencySet> entry = iter.next();
			Variable var = entry.getKey();
			DependencySet arrayLabel = entry.getValue();
			if (getDefaultArrayLabel(var).equals(arrayLabel)) {
				iter.remove();
			}
		}
	}

	public static DependencySet lub(DependencySet dep1, DependencySet dep2) {
		return DependencySet.lub(dep1, dep2);
	}

	void initTree(Variable root, DependencySet dep) {
		this.placeToDep.put(root, dep);
		if (!root.isArray()) {
			return;
		}
		for (Variable element : root.getLiteralElements()) {
			this.initTree(element, dep);
		}
	}

	@SuppressWarnings("rawtypes")
	public void assign(Variable left, Set mustAliases, Set mayAliases, AbstractCfgNode cfgNode) {

		DependencySet dep = DependencySet.create(DependencyLabel.create(cfgNode));

		int leftCase;
		if (left == null)
			return;

		if (!left.isArrayElement()) {
			if (!left.isArray()) {
				leftCase = 1;
			} else {
				leftCase = 2;
			}
		} else {
			if (!left.hasNonLiteralIndices()) {
				leftCase = 3;
			} else {
				leftCase = 4;
			}
		}

		switch (leftCase) {
		case 1: {
			for (Iterator iter = mustAliases.iterator(); iter.hasNext();) {
				Variable mustAlias = (Variable) iter.next();

				this.setDep(mustAlias, dep);
				this.setArrayLabel(mustAlias, dep);
			}

			for (Iterator iter = mayAliases.iterator(); iter.hasNext();) {
				Variable mayAlias = (Variable) iter.next();
				this.lubDep(mayAlias, dep);
				this.lubArrayLabel(mayAlias, dep);
			}

			break;
		}

		case 2: {
			this.setArrayLabel(left, dep);
			this.setWholeTree(left, dep);
			break;
		}
		case 3: {
			this.lubArrayLabel(left.getTopEnclosingArray(), dep);
			this.setWholeTree(left, dep);
			break;
		}
		case 4: {
			this.lubArrayLabel(left.getTopEnclosingArray(), dep);
			for (Iterator iter = this.getMiList(left).iterator(); iter.hasNext();) {
				Variable miVar = (Variable) iter.next();
				this.lubWholeTree(miVar, dep);
			}
			break;
		}

		default:
			throw new RuntimeException("SNH");
		}
	}

	public void assignArray(Variable left, AbstractCfgNode cfgNode) {
		this.setWholeTree(left, DependencySet.create(DependencyLabel.create(cfgNode)));
		if (!left.isArrayElement()) {
			this.setArrayLabel(left, DependencySet.create(DependencyLabel.create(cfgNode)));
		}
	}

	public void defineConstant(Constant c, AbstractCfgNode cfgNode) {
		this.setDep(c, DependencySet.create(DependencyLabel.create(cfgNode)));
	}

	public void defineConstantWeak(Constant c, AbstractCfgNode cfgNode) {
		this.lubDep(c, DependencySet.create(DependencyLabel.create(cfgNode)));
	}

	@SuppressWarnings("rawtypes")
	List getMiList(Variable var) {

		if (!var.isArrayElement()) {
			throw new RuntimeException("SNH");
		}
		if (!var.hasNonLiteralIndices()) {
			throw new RuntimeException("SNH");
		}

		List<Variable> miList = new LinkedList<Variable>();

		Variable root = var.getTopEnclosingArray();
		List<AbstractTacPlace> indices = var.getIndices();

		this.miRecurse(miList, root, new LinkedList<AbstractTacPlace>(indices));
		return miList;
	}

	private void miRecurse(List<Variable> miList, Variable root, List<AbstractTacPlace> indices) {

		if (!root.isArray()) {
			return;
		}

		AbstractTacPlace index = (AbstractTacPlace) indices.remove(0);
		if (index instanceof Literal) {

			Variable target = root.getElement(index);
			if (target != null) {
				if (indices.isEmpty()) {
					miList.add(target);
				} else {
					this.miRecurse(miList, target, indices);
				}
			}
		} else {
			List<Variable> literalElements = root.getLiteralElements();
			if (indices.isEmpty()) {
				miList.addAll(literalElements);
			} else {
				for (Iterator<Variable> iter = literalElements.iterator(); iter.hasNext();) {
					Variable target = (Variable) iter.next();
					this.miRecurse(miList, target, new LinkedList<AbstractTacPlace>(indices));
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void resetVariables(SymbolTable symTab) {

		for (Iterator<Entry<AbstractTacPlace, DependencySet>> iter = this.placeToDep.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();

			if (!(place instanceof Variable)) {
				continue;
			}

			Variable var = (Variable) place;
			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}

		for (Iterator iter = this.arrayLabels.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();

			if (!(place instanceof Variable)) {
				continue;
			}

			Variable var = (Variable) place;
			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public void resetTemporaries(SymbolTable symTab) {

		for (Iterator<Entry<AbstractTacPlace, DependencySet>> iter = this.placeToDep.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();

			if (!(place instanceof Variable)) {
				continue;
			}

			Variable var = (Variable) place;
			if (!var.isTemp()) {
				continue;
			}

			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}
		for (Iterator iter = this.arrayLabels.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();

			if (!(place instanceof Variable)) {
				continue;
			}

			Variable var = (Variable) place;
			if (!var.isTemp()) {
				continue;
			}

			if (var.belongsTo(symTab)) {
				iter.remove();
			}
		}

	}

	public void setFormal(TacFormalParameter formalParam, AbstractCfgNode cfgNode) {
		Variable formalVar = formalParam.getVariable();
		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(formalVar);
		this.assign(formalVar, mustAliases, Collections.emptySet(), cfgNode);
	}

	public void setShadow(Variable shadow, Variable original) {

		this.setDep(shadow, this.getDep(original));
		this.setArrayLabel(shadow, this.getArrayLabel(original));
	}

	@SuppressWarnings("rawtypes")
	public void copyGlobalLike(DependencyLatticeElement interIn) {

		Map origPlaceToDep = interIn.getPlaceToDep();
		for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origDep = (DependencySet) entry.getValue();

			boolean copyMe = false;
			if (origPlace instanceof Constant) {
				copyMe = true;
			} else if (origPlace instanceof Variable) {
				Variable origVar = (Variable) origPlace;
				if (origVar.isGlobal() || origVar.isSuperGlobal()) {
					copyMe = true;
				}
			} else {
				throw new RuntimeException("SNH: " + origPlace);
			}

			if (copyMe) {
				this.setDep(origPlace, origDep);
			}
		}

		Map origArrayLabels = interIn.getArrayLabels();
		for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origArrayLabel = (DependencySet) entry.getValue();

			boolean copyMe = false;
			if (origPlace instanceof Variable) {
				Variable origVar = (Variable) origPlace;
				if (origVar.isGlobal() || origVar.isSuperGlobal()) {
					copyMe = true;
				}
			} else {
				throw new RuntimeException("SNH: " + origPlace);
			}

			if (copyMe) {
				this.setArrayLabel((Variable) origPlace, origArrayLabel);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void copyGlobalLike(DependencyLatticeElement interIn, DependencyLatticeElement intraIn,
			Set<AbstractTacPlace> calleeMod) {

		Map origPlaceToDep = interIn.getPlaceToDep();
		for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace interPlace = (AbstractTacPlace) entry.getKey();
			DependencySet interDep = (DependencySet) entry.getValue();

			boolean copyMe = false;
			if (interPlace instanceof Constant) {
				copyMe = true;
			} else if (interPlace instanceof Variable) {
				Variable interVar = (Variable) interPlace;
				if (interVar.isGlobal() || interVar.isSuperGlobal()) {
					copyMe = true;
				}
			} else {
				throw new RuntimeException("SNH: " + interPlace);
			}

			if (copyMe) {
				if (interPlace instanceof Constant || calleeMod.contains(interPlace)) {
					this.setDep(interPlace, interDep);
				} else {
					this.setDep(interPlace, intraIn.getDep(interPlace));
				}
			}
		}

		Map interArrayLabels = interIn.getArrayLabels();
		for (Iterator iter = interArrayLabels.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace interPlace = (AbstractTacPlace) entry.getKey();
			DependencySet interArrayLabel = (DependencySet) entry.getValue();

			boolean copyMe = false;
			if (interPlace instanceof Variable) {
				Variable interVar = (Variable) interPlace;
				if (interVar.isGlobal() || interVar.isSuperGlobal()) {
					copyMe = true;
				}
			} else {
				throw new RuntimeException("SNH: " + interPlace);
			}

			if (copyMe) {
				if (calleeMod.contains(interPlace)) {
					this.setArrayLabel((Variable) interPlace, interArrayLabel);
				} else {
					this.setArrayLabel((Variable) interPlace, intraIn.getArrayLabel(interPlace));
				}
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public void copyMainTemporaries(DependencyLatticeElement origElement) {

		Map origPlaceToDep = origElement.getPlaceToDep();
		for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origDep = (DependencySet) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			SymbolTable symTab = origVar.getSymbolTable();
			if (!symTab.isMain()) {
				continue;
			}
			if (!origVar.isTemp()) {
				continue;
			}

			this.setDep(origVar, origDep);
		}

		Map origArrayLabels = origElement.getArrayLabels();
		for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origArrayLabel = (DependencySet) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			SymbolTable symTab = origVar.getSymbolTable();
			if (!symTab.isMain()) {
				continue;
			}
			if (!origVar.isTemp()) {
				continue;
			}

			this.setArrayLabel(origVar, origArrayLabel);
		}
	}

	@SuppressWarnings("rawtypes")
	public void copyMainVariables(DependencyLatticeElement origElement) {

		Map origPlaceToDep = origElement.getPlaceToDep();
		for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origDep = (DependencySet) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			SymbolTable symTab = origVar.getSymbolTable();
			if (!symTab.isMain()) {
				continue;
			}

			this.setDep(origVar, origDep);
		}

		Map origArrayLabels = origElement.getArrayLabels();
		for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origArrayLabel = (DependencySet) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			SymbolTable symTab = origVar.getSymbolTable();
			if (!symTab.isMain()) {
				continue;
			}

			this.setArrayLabel(origVar, origArrayLabel);
		}
	}

	@SuppressWarnings("rawtypes")
	public void copyLocals(DependencyLatticeElement origElement) {

		Map origPlaceToDep = origElement.getPlaceToDep();
		for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origDep = (DependencySet) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			if (!origVar.isLocal()) {
				continue;
			}

			this.setDep(origVar, origDep);
		}

		Map origArrayLabels = origElement.getArrayLabels();
		for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			DependencySet origArrayLabel = (DependencySet) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			if (!origVar.isLocal()) {
				continue;
			}

			this.setArrayLabel(origVar, origArrayLabel);
		}
	}

	public void setLocal(Variable local, DependencySet dep, DependencySet arrayLabel) {
		this.setDep(local, dep);
		this.setArrayLabel(local, arrayLabel);
	}

	public void handleReturnValue(CallReturn retNode) {

		Variable tempVar = retNode.getTempVar();
		DependencyLabel dep = DependencyLabel.create(retNode);
		DependencySet depSet = DependencySet.create(dep);
		this.setWholeTree(tempVar, depSet);
		this.setArrayLabel(tempVar, depSet);
	}

	public void handleReturnValueUnknown(Variable tempVar, DependencySet dep, DependencySet arrayLabel,
			Variable retVar) {

		this.setWholeTree(tempVar, dep);
		this.setArrayLabel(tempVar, arrayLabel);
		this.placeToDep.remove(retVar);
		this.arrayLabels.remove(retVar);
	}

	public void handleReturnValueBuiltin(Variable tempVar, DependencySet dep, DependencySet arrayLabel) {

		this.setWholeTree(tempVar, dep);
		this.setArrayLabel(tempVar, arrayLabel);
	}

	public void setRetVar(Variable retVar, DependencySet dep, DependencySet arrayLabel) {
		this.setDep(retVar, dep);
		this.setArrayLabel(retVar, arrayLabel);
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
		if (!(compX instanceof DependencyLatticeElement)) {
			return false;
		}
		DependencyLatticeElement comp = (DependencyLatticeElement) compX;

		if (!this.placeToDep.equals(comp.getPlaceToDep())) {
			return false;
		}
		if (!this.arrayLabels.equals(comp.getArrayLabels())) {
			return false;
		}

		return true;
	}

	public int structureHashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.placeToDep.hashCode();
		hashCode = 37 * hashCode + this.arrayLabels.hashCode();
		return hashCode;
	}

	public void dump() {
		System.out.println(this.placeToDep);
	}
}
