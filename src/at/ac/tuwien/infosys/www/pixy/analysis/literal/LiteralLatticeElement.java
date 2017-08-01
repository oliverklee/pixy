package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class LiteralLatticeElement extends AbstractLatticeElement {

	private Map<AbstractTacPlace, Literal> placeToLit;
	private Map<AbstractTacPlace, Literal> origPlaceToLit;
	public static LiteralLatticeElement DEFAULT;

	public LiteralLatticeElement() {
		this.placeToLit = new HashMap<AbstractTacPlace, Literal>();
	}

	public LiteralLatticeElement(LiteralLatticeElement cloneMe) {
		this.placeToLit = new HashMap<AbstractTacPlace, Literal>(cloneMe.getPlaceToLit());
	}

	public AbstractLatticeElement cloneMe() {
		return new LiteralLatticeElement(this);
	}

	private LiteralLatticeElement(List<?> places, ConstantsTable constantsTable, List<?> functions,
			SymbolTable superSymbolTable) {

		this.placeToLit = new HashMap<AbstractTacPlace, Literal>();
		for (Iterator<?> iter = places.iterator(); iter.hasNext();) {
			AbstractTacPlace place = (AbstractTacPlace) iter.next();
			this.placeToLit.put(place, Literal.TOP);
		}

		for (Iterator<?> iter = places.iterator(); iter.hasNext();) {
			AbstractTacPlace place = (AbstractTacPlace) iter.next();
			if ((place instanceof Variable) && (place.getVariable().isReturnVariable())) {
				this.placeToLit.put(place, Literal.NULL);
			}
		}

		Map<?, ?> constants = constantsTable.getConstants();
		for (Iterator<?> iter = constants.values().iterator(); iter.hasNext();) {
			Constant constant = (Constant) iter.next();
			if (constant.isSpecial()) {
				this.placeToLit.put(constant, constant.getSpecialLiteral());
			} else {
				this.placeToLit.put(constant, new Literal(constant.toString()));
			}
		}

		for (Iterator<?> iter = functions.iterator(); iter.hasNext();) {
			TacFunction function = (TacFunction) iter.next();
			if (function.isMain() && MyOptions.optionG) {
				continue;
			}
			SymbolTable symtab = function.getSymbolTable();
			Map<?, ?> variables = symtab.getVariables();
			for (Iterator<?> varIter = variables.values().iterator(); varIter.hasNext();) {
				Variable variable = (Variable) varIter.next();
				if (variable.isArrayElement()) {
					continue;
				}
				this.initTree(variable, Literal.NULL);
			}
		}
	}

	static void initDefault(List<?> places, ConstantsTable constantsTable, List<?> functions,
			SymbolTable superSymbolTable) {

		LiteralLatticeElement.DEFAULT = new LiteralLatticeElement(places, constantsTable, functions, superSymbolTable);

	}

	public Map<AbstractTacPlace, Literal> getPlaceToLit() {
		return this.placeToLit;
	}

	public Literal getLiteral(AbstractTacPlace place) {
		return this.getLiteralFrom(place, this.placeToLit);
	}

	private Literal getOrigLiteral(AbstractTacPlace place) {
		return this.getLiteralFrom(place, this.origPlaceToLit);
	}

	private Literal getLiteralFrom(AbstractTacPlace place, Map<AbstractTacPlace, Literal> readFrom) {

		if (place instanceof Literal) {
			return (Literal) place;
		}

		Literal nonDefaultLit = (Literal) readFrom.get(place);
		if (nonDefaultLit != null) {
			return nonDefaultLit;
		}

		Literal defaultLit = LiteralLatticeElement.getDefaultLiteral(place);
		if (defaultLit == null) {
			throw new RuntimeException("SNH: " + place);
		} else {
			return defaultLit;
		}
	}

	private static Literal getDefaultLiteral(AbstractTacPlace place) {
		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		return (Literal) LiteralLatticeElement.DEFAULT.getPlaceToLit().get(place);
	}

	private Literal getNonDefaultLiteral(AbstractTacPlace place) {
		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		return this.placeToLit.get(place);
	}

	private void setLiteral(AbstractTacPlace place, Literal literal) {

		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		if (place instanceof Variable && place.getVariable().isMember()) {
			return;
		}
		if (getDefaultLiteral(place).equals(literal)) {
			this.placeToLit.remove(place);
		} else {
			this.placeToLit.put(place, literal);
		}
	}

	private void lubLiteral(AbstractTacPlace place, Literal literal) {

		if (place instanceof Literal) {
			throw new RuntimeException("SNH");
		}
		if (place instanceof Variable && place.getVariable().isMember()) {
			return;
		}

		Literal oldLiteral = this.getLiteral(place);
		if (!oldLiteral.equals(literal)) {
			if (getDefaultLiteral(place) == Literal.TOP) {
				this.placeToLit.remove(place);
			} else {
				this.placeToLit.put(place, Literal.TOP);
			}
		}
	}

	private void setSubTree(Variable root, Literal lit) {
		if (!root.isArray()) {
			return;
		}
		for (Iterator<?> iter = root.getLiteralElements().iterator(); iter.hasNext();) {
			Variable element = (Variable) iter.next();
			this.setWholeTree(element, lit);
		}
	}

	private void setWholeTree(Variable root, Literal lit) {

		this.setLiteral(root, lit);
		if (!root.isArray()) {
			return;
		}
		for (Iterator<?> iter = root.getLiteralElements().iterator(); iter.hasNext();) {
			Variable element = (Variable) iter.next();
			this.setWholeTree(element, lit);
		}
	}

	private void lubSubTree(Variable root, Literal lit) {
		if (!root.isArray()) {
			return;
		}
		for (Iterator<?> iter = root.getLiteralElements().iterator(); iter.hasNext();) {
			Variable element = (Variable) iter.next();
			this.lubWholeTree(element, lit);
		}
	}

	private void lubWholeTree(Variable root, Literal lit) {

		this.lubLiteral(root, lit);
		if (!root.isArray()) {
			return;
		}
		for (Iterator<?> iter = root.getLiteralElements().iterator(); iter.hasNext();) {
			Variable element = (Variable) iter.next();
			this.lubWholeTree(element, lit);
		}
	}

	@SuppressWarnings("rawtypes")
	public void lub(AbstractLatticeElement foreignX) {
		LiteralLatticeElement foreign = (LiteralLatticeElement) foreignX;
		for (Iterator<?> iter = this.placeToLit.entrySet().iterator(); iter.hasNext();) {
			Map.Entry myEntry = (Map.Entry) iter.next();
			AbstractTacPlace myPlace = (AbstractTacPlace) myEntry.getKey();
			Literal myLiteral = (Literal) myEntry.getValue();

			Literal foreignLiteral = foreign.getLiteral(myPlace);
			if (!foreignLiteral.equals(myLiteral)) {
				this.placeToLit.put(myPlace, Literal.TOP);
			}
		}
		Map<?, ?> foreignPlaceToLit = foreign.getPlaceToLit();
		for (Iterator<?> iter = foreignPlaceToLit.entrySet().iterator(); iter.hasNext();) {
			Map.Entry foreignEntry = (Map.Entry) iter.next();
			AbstractTacPlace foreignPlace = (AbstractTacPlace) foreignEntry.getKey();
			Literal foreignLiteral = (Literal) foreignEntry.getValue();

			Literal myLiteral = this.getNonDefaultLiteral(foreignPlace);
			if (myLiteral == null) {
				myLiteral = getDefaultLiteral(foreignPlace);
				if (!foreignLiteral.equals(myLiteral)) {
					this.placeToLit.put(foreignPlace, Literal.TOP);
				} else {
					this.placeToLit.put(foreignPlace, foreignLiteral);
				}
			}
		}

		for (Iterator<?> iter = this.placeToLit.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();
			Literal lit = (Literal) entry.getValue();
			if (getDefaultLiteral(place).equals(lit)) {
				iter.remove();
			}
		}
	}

	public static Literal lub(Literal lit1, Literal lit2) {
		if (lit1.equals(lit2)) {
			return lit1;
		} else {
			return Literal.TOP;
		}
	}

	private void initTree(Variable root, Literal lit) {
		this.placeToLit.put(root, lit);
		if (!root.isArray()) {
			return;
		}
		for (Iterator<?> iter = root.getLiteralElements().iterator(); iter.hasNext();) {
			Variable element = (Variable) iter.next();
			this.initTree(element, lit);
		}
	}

	public void assignSimple(Variable left, AbstractTacPlace right, Set<?> mustAliases, Set<?> mayAliases) {

		this.origPlaceToLit = new HashMap<AbstractTacPlace, Literal>(this.placeToLit);

		int leftCase;
		if (left == null) {
			return;
		}

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
		case 1:
			Literal rightLit = this.getLiteral(right);
			for (Iterator<?> iter = mustAliases.iterator(); iter.hasNext();) {
				Variable mustAlias = (Variable) iter.next();
				this.setLiteral(mustAlias, rightLit);
			}
			for (Iterator<?> iter = mayAliases.iterator(); iter.hasNext();) {
				Variable mayAlias = (Variable) iter.next();
				this.lubLiteral(mayAlias, rightLit);
			}
			break;
		case 2:
			this.strongOverlap(left, right);
			break;
		case 3:
			this.strongOverlap(left, right);
			break;
		case 4:
			for (Iterator<?> iter = this.getMiList(left).iterator(); iter.hasNext();) {
				Variable miVar = (Variable) iter.next();
				this.weakOverlap(miVar, right);
			}
			break;

		default:
			throw new RuntimeException("SNH");
		}

		this.origPlaceToLit = null;
	}

	public void assignUnary(Variable left, AbstractTacPlace right, int op, Set<?> mustAliases, Set<?> mayAliases) {

		Literal baseLit = this.getLiteral(right);
		Literal effectiveRightLit;
		if (baseLit == Literal.TOP) {
			effectiveRightLit = Literal.TOP;
		} else {
			switch (op) {
			case TacOperators.PLUS: {
				effectiveRightLit = baseLit.getFloatValueLiteral();
				break;
			}
			case TacOperators.MINUS: {
				float setToFloat = -1 * baseLit.getFloatValue();
				effectiveRightLit = new Literal(Literal.numberToString(setToFloat));
				break;
			}
			case TacOperators.NOT: {
				Literal baseBool = baseLit.getBoolValueLiteral();
				if (baseBool == Literal.TOP) {
					effectiveRightLit = Literal.TOP;
				} else if (baseBool == Literal.TRUE) {
					effectiveRightLit = Literal.FALSE;
				} else if (baseBool == Literal.FALSE) {
					effectiveRightLit = Literal.TRUE;
				} else {
					throw new RuntimeException("SNH");
				}
				break;
			}
			case TacOperators.BITWISE_NOT: {
				effectiveRightLit = Literal.TOP;
				break;
			}
			case TacOperators.INT_CAST: {
				effectiveRightLit = baseLit.getIntValueLiteral();
				break;
			}
			case TacOperators.DOUBLE_CAST: {
				effectiveRightLit = baseLit.getFloatValueLiteral();
				break;
			}
			case TacOperators.STRING_CAST: {
				effectiveRightLit = baseLit.getStringValueLiteral();
				break;
			}
			case TacOperators.ARRAY_CAST: {
				effectiveRightLit = Literal.TOP;
				break;
			}
			case TacOperators.OBJECT_CAST: {
				effectiveRightLit = Literal.TOP;
				break;
			}
			case TacOperators.BOOL_CAST: {
				effectiveRightLit = baseLit.getBoolValueLiteral();
				break;
			}
			case TacOperators.UNSET_CAST: {
				effectiveRightLit = Literal.NULL;
				break;
			}
			default: {
				throw new RuntimeException("Unsupported operator");
			}
			}
		}
		this.assignSimple(left, effectiveRightLit, mustAliases, mayAliases);

	}

	public void assignBinary(Variable left, AbstractTacPlace leftOperand, AbstractTacPlace rightOperand, int op,
			Set<?> mustAliases, Set<?> mayAliases, AbstractCfgNode cfgNode) {

		Literal baseLeftLit = this.getLiteral(leftOperand);
		Literal baseRightLit = this.getLiteral(rightOperand);

		Literal effectiveLit;

		if (baseLeftLit == Literal.TOP || baseRightLit == Literal.TOP) {
			effectiveLit = Literal.TOP;
		} else {
			switch (op) {
			case TacOperators.PLUS: {
				effectiveLit = new Literal(
						Literal.numberToString(baseLeftLit.getFloatValue() + baseRightLit.getFloatValue()));
				break;
			}
			case TacOperators.MINUS: {
				effectiveLit = new Literal(
						Literal.numberToString(baseLeftLit.getFloatValue() - baseRightLit.getFloatValue()));
				break;
			}
			case TacOperators.MULT: {
				effectiveLit = new Literal(
						Literal.numberToString(baseLeftLit.getFloatValue() * baseRightLit.getFloatValue()));
				break;
			}
			case TacOperators.DIV: {
				float rightFloat = baseRightLit.getFloatValue();
				if (rightFloat == 0) {
					System.out.println("Warning: might be division by zero");
					System.out.println("Line: " + cfgNode.getOriginalLineNumber());
					effectiveLit = Literal.FALSE;
				} else {
					effectiveLit = new Literal(Literal.numberToString(baseLeftLit.getFloatValue() / rightFloat));
				}
				break;
			}
			case TacOperators.MODULO: {
				effectiveLit = new Literal(
						Literal.numberToString(baseLeftLit.getFloatValue() % baseRightLit.getFloatValue()));
				break;
			}
			case TacOperators.CONCAT: {
				effectiveLit = new Literal(baseLeftLit.getStringValue() + baseRightLit.getStringValue());
				break;
			}
			case TacOperators.BOOLEAN_AND: {
				Literal boolLeft = baseLeftLit.getBoolValueLiteral();
				Literal boolRight = baseRightLit.getBoolValueLiteral();
				if (boolLeft == Literal.TOP || boolRight == Literal.TOP) {
					effectiveLit = Literal.TOP;
				} else if (boolLeft == Literal.TRUE && boolRight == Literal.TRUE) {
					effectiveLit = Literal.TRUE;
				} else {
					effectiveLit = Literal.FALSE;
				}
				break;
			}
			case TacOperators.IS_EQUAL: {
				effectiveLit = Literal.isEqualLiteral(baseLeftLit, baseRightLit);
				break;
			}
			case TacOperators.IS_NOT_EQUAL: {
				effectiveLit = Literal.invert(Literal.isEqualLiteral(baseLeftLit, baseRightLit));
				break;
			}
			case TacOperators.IS_SMALLER: {
				effectiveLit = Literal.isSmallerLiteral(baseLeftLit, baseRightLit, cfgNode);
				break;
			}
			case TacOperators.IS_GREATER: {
				effectiveLit = Literal.isGreaterLiteral(baseLeftLit, baseRightLit, cfgNode);
				break;
			}
			case TacOperators.IS_SMALLER_OR_EQUAL: {
				effectiveLit = Literal.invert(Literal.isGreaterLiteral(baseLeftLit, baseRightLit, cfgNode));
				break;
			}
			case TacOperators.IS_GREATER_OR_EQUAL: {
				effectiveLit = Literal.invert(Literal.isSmallerLiteral(baseLeftLit, baseRightLit, cfgNode));
				break;
			}
			case TacOperators.IS_IDENTICAL: {
				effectiveLit = Literal.isIdenticalLiteral(baseLeftLit, baseRightLit);
				break;
			}
			case TacOperators.IS_NOT_IDENTICAL: {
				effectiveLit = Literal.invert(Literal.isIdenticalLiteral(baseLeftLit, baseRightLit));
				break;
			}
			case TacOperators.SL: {
				effectiveLit = Literal.TOP;
				break;
			}
			case TacOperators.SR: {
				effectiveLit = Literal.TOP;
				break;
			}
			case TacOperators.BITWISE_OR: {
				effectiveLit = Literal.TOP;
				break;
			}
			case TacOperators.BITWISE_AND: {
				effectiveLit = Literal.TOP;
				break;
			}
			case TacOperators.BITWISE_XOR: {
				effectiveLit = Literal.TOP;
				break;
			}

			default: {
				throw new RuntimeException("Unsupported operator");
			}
			}
		}
		this.assignSimple(left, effectiveLit, mustAliases, mayAliases);

	}

	public void assignArray(Variable left) {
		this.setWholeTree(left, Literal.NULL);
	}

	public void defineConstant(Constant c, Literal lit) {
		this.setLiteral(c, lit);
	}

	public void defineConstantWeak(Constant c, Literal lit) {
		this.lubLiteral(c, lit);
	}

	public List<Variable> getMiList(Variable var) {

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

	private void strongOverlap(Variable target, AbstractTacPlace source) {

		Literal sourceLit = this.getOrigLiteral(source);

		if (source.isVariable() && ((Variable) source).isArray()) {
			Variable sourceArray = (Variable) source;
			if (target.isArray()) {
				List<?> targetElements = target.getLiteralElements();
				for (Iterator<?> iter = targetElements.iterator(); iter.hasNext();) {
					Variable targetElem = (Variable) iter.next();
					Variable sourceElem = sourceArray.getElement(targetElem.getIndex());
					if (sourceElem != null) {
						this.strongOverlap(targetElem, sourceElem);
					} else {
						this.setWholeTree(targetElem, Literal.TOP);
					}
				}
			}
		} else if (!source.isVariable()) {
			this.setSubTree(target, Literal.NULL);
		} else {
			this.setSubTree(target, Literal.TOP);
		}

		this.setLiteral(target, sourceLit);
	}

	private void weakOverlap(Variable target, AbstractTacPlace source) {

		Literal sourceLit = this.getOrigLiteral(source);

		if (source.isVariable() && ((Variable) source).isArray()) {
			Variable sourceArray = (Variable) source;
			if (target.isArray()) {
				List<?> targetElements = target.getLiteralElements();
				for (Iterator<?> iter = targetElements.iterator(); iter.hasNext();) {
					Variable targetElem = (Variable) iter.next();
					Variable sourceElem = sourceArray.getElement(targetElem.getIndex());
					if (sourceElem != null) {
						this.weakOverlap(targetElem, sourceElem);
					} else {
						this.lubWholeTree(targetElem, Literal.TOP);
					}
				}
			}
		} else if (!source.isVariable()) {
			this.lubSubTree(target, Literal.NULL);
		} else {
			this.lubSubTree(target, Literal.TOP);
		}
		this.lubLiteral(target, sourceLit);
	}

	@SuppressWarnings("rawtypes")
	public void resetVariables(SymbolTable symTab) {

		for (Iterator<?> iter = this.placeToLit.entrySet().iterator(); iter.hasNext();) {
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

	public void setFormal(TacFormalParameter formalParam, AbstractTacPlace place) {

		this.origPlaceToLit = new HashMap<AbstractTacPlace, Literal>(this.placeToLit);

		Variable formalVar = formalParam.getVariable();
		this.strongOverlap(formalVar, place);

		this.origPlaceToLit = null;
	}

	public void setShadow(Variable shadow, Variable original) {
		this.setLiteral(shadow, this.getLiteral(original));
	}

	@SuppressWarnings("rawtypes")
	public void copyGlobalLike(LiteralLatticeElement origElement) {

		Map<?, ?> origMap = origElement.getPlaceToLit();
		for (Iterator<?> iter = origMap.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			Literal origLit = (Literal) entry.getValue();

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
				this.setLiteral(origPlace, origLit);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void copyLocals(LiteralLatticeElement origElement) {

		Map<?, ?> origMap = origElement.getPlaceToLit();
		for (Iterator<?> iter = origMap.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			AbstractTacPlace origPlace = (AbstractTacPlace) entry.getKey();
			Literal origLit = (Literal) entry.getValue();

			if (!(origPlace instanceof Variable)) {
				continue;
			}
			Variable origVar = (Variable) origPlace;
			if (!origVar.isLocal()) {
				continue;
			}

			this.setLiteral(origVar, origLit);
		}
	}

	public void setLocal(Variable local, Literal lit) {
		this.setLiteral(local, lit);
	}

	public void handleReturnValue(Variable tempVar, Literal lit, Variable retVar) {
		this.setLiteral(tempVar, lit);
		this.placeToLit.remove(retVar);
	}

	public void handleReturnValue(Variable tempVar,
			Literal lit/* , Variable retVar */) {
		this.setLiteral(tempVar, lit);
	}

	public void setRetVar(Variable retVar, Literal lit) {
		this.setLiteral(retVar, lit);
	}

	public void handleReturnValueBuiltin(Variable tempVar) {
		this.setWholeTree(tempVar, Literal.TOP);
	}

	public void handleReturnValueUnknown(Variable tempVar) {
		this.setWholeTree(tempVar, Literal.TOP);
	}

	public boolean structureEquals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LiteralLatticeElement)) {
			return false;
		}
		LiteralLatticeElement comp = (LiteralLatticeElement) obj;
		if (!this.placeToLit.equals(comp.getPlaceToLit())) {
			return false;
		}
		return true;
	}

	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LiteralLatticeElement)) {
			return false;
		}
		LiteralLatticeElement comp = (LiteralLatticeElement) obj;

		if (!this.placeToLit.equals(comp.getPlaceToLit())) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return this.placeToLit.hashCode();
	}

	public boolean structureEquals(Recyclable compX) {
		LiteralLatticeElement comp = (LiteralLatticeElement) compX;
		return this.placeToLit.equals(comp.placeToLit);
	}

	public int structureHashCode() {
		return this.placeToLit.hashCode();
	}

	public void dump() {
		System.out.println("LiteralLatticeElement.dump(): not yet");
	}
}