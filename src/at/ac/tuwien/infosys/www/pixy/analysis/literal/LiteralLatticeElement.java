package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralLatticeElement extends AbstractLatticeElement {
    // AbstractTacPlace -> Literal
    // contains only non-default mappings;
    private Map<AbstractTacPlace, Literal> placeToLit;

    // a copy of placeToLit, must be initialized by methods that need it;
    // they must not forget to null it as soon as they've finished their
    // work (saves memory)
    private Map<AbstractTacPlace, Literal> origPlaceToLit;

    // the default lattice element; IT MUST NOT BE USED DIRECTLY BY THE ANALYSIS!
    // can be seen as "grounding", "fall-back" for normal lattice elements
    public static LiteralLatticeElement DEFAULT;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates a lattice element that adds no information to the
    // default lattice element
    public LiteralLatticeElement() {
        this.placeToLit = new HashMap<>();
    }

    // clones the given element
    public LiteralLatticeElement(LiteralLatticeElement cloneMe) {
        this.placeToLit = new HashMap<>(cloneMe.getPlaceToLit());
    }

    public AbstractLatticeElement cloneMe() {
        // uses the cloning constructor
        return new LiteralLatticeElement(this);
    }

    // constructor for default element (to be called by "initDefault"):
    // literal mappings:
    // - main function variables and superglobals (not including function return
    //   variables): top
    // - a number of "harmless" superglobals: top
    // - return variables: top
    // - other function variables and their parameters: null
    // - constants: their "natural" string
    // - special constant _TAINTED: top
    // - special constant _UNTAINTED: top
    // - and a few other special constants (see below)
    private LiteralLatticeElement(
        List<AbstractTacPlace> places,
        ConstantsTable constantsTable,
        List<TacFunction> functions,
        SymbolTable superSymbolTable) {

        // initialize conservative base mapping for variables & constants: TOP
        this.placeToLit = new HashMap<>();
        for (AbstractTacPlace place : places) {
            this.placeToLit.put(place, Literal.TOP);
        }

        // initialize function return values to NULL, because:
        // if a function has no return statement, the return variable is not touched;
        // since the real-world return value is then "NULL", the default mapping
        // for return variables has to have the same properties as the default
        // mapping for NULL;
        // but skip the return variable of the special unknown function! (see below)
        for (AbstractTacPlace place : places) {
            if ((place instanceof Variable) && (place.getVariable().isReturnVariable())) {
                this.placeToLit.put(place, Literal.NULL);
            }
        }

        // initialize constants
        Map<String, Constant> constants = constantsTable.getConstants();
        for (Constant constant : constants.values()) {
            if (constant.isSpecial()) {
                // retrieve stored values for special constants
                this.placeToLit.put(constant, constant.getSpecialLiteral());
            } else {
                // initialize non-special constants to their 'natural' string
                this.placeToLit.put(constant, new Literal(constant.toString()));
            }
        }

        // initialize local function variables & parameters to NULL;
        // locals of main function == globals of the program:
        // their initialization depends on the register_globals setting:
        // true: top
        // false: like other locals (i.e., null)
        for (TacFunction function : functions) {
            if (function.isMain() && MyOptions.optionG) {
                continue;
            }
            SymbolTable symbolTable = function.getSymbolTable();
            Map<Variable, Variable> variables = symbolTable.getVariables();
            for (Variable variable : variables.values()) {
                // array elements are handled along with their root
                if (variable.isArrayElement()) {
                    continue;
                }
                // init the whole tree below this variable to null
                // (note that using setTree wouldn't work here)
                this.initTree(variable, Literal.NULL);
            }
        }
    }

// initDefault *********************************************************************

    // initializes the default lattice element
    static void initDefault(
        List<AbstractTacPlace> places, ConstantsTable constantsTable, List<TacFunction> functions, SymbolTable superSymbolTable
    ) {
        LiteralLatticeElement.DEFAULT = new LiteralLatticeElement(places, constantsTable, functions, superSymbolTable);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

//  getPlaceToLit ******************************************************************

    public Map<AbstractTacPlace, Literal> getPlaceToLit() {
        return this.placeToLit;
    }

// getLiteral **********************************************************************

    public Literal getLiteral(AbstractTacPlace place) {
        return this.getLiteralFrom(place, this.placeToLit);
    }

//  getOrigLiteral *****************************************************************

    private Literal getOrigLiteral(AbstractTacPlace place) {
        return this.getLiteralFrom(place, this.origPlaceToLit);
    }

// getLiteralFrom ******************************************************************

    // returns the non-default literal for this place if that mapping exists,
    // or the default literal otherwise
    private Literal getLiteralFrom(AbstractTacPlace place, Map<AbstractTacPlace, Literal> readFrom) {
        if (place instanceof Literal) {
            return (Literal) place;
        }

        // return the non-default mapping (if there is one)
        Literal nonDefaultLit = readFrom.get(place);
        if (nonDefaultLit != null) {
            return nonDefaultLit;
        }

        // return the default mapping if there is no non-default one
        Literal defaultLit = LiteralLatticeElement.getDefaultLiteral(place);
        if (defaultLit == null) {
            throw new RuntimeException("SNH: " + place);
        } else {
            return defaultLit;
        }
    }

// getDefaultLiteral ***************************************************************

    private static Literal getDefaultLiteral(AbstractTacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return LiteralLatticeElement.DEFAULT.getPlaceToLit().get(place);
    }

// getNonDefaultLiteral ************************************************************

    // returns the non-default literal for the given place;
    // null if there is no non-default literal for it
    private Literal getNonDefaultLiteral(AbstractTacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return this.placeToLit.get(place);
    }

//  ********************************************************************************
//  SET ****************************************************************************
//  ********************************************************************************

// setLiteral **********************************************************************

    private void setLiteral(AbstractTacPlace place, Literal literal) {

        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        if (place instanceof Variable && place.getVariable().isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        if (getDefaultLiteral(place).equals(literal)) {
            // if the target literal is the default, we simply remove the mapping
            this.placeToLit.remove(place);
        } else {
            this.placeToLit.put(place, literal);
        }
    }

// lubLiteral **********************************************************************

    private void lubLiteral(AbstractTacPlace place, Literal literal) {

        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        if (place instanceof Variable && place.getVariable().isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        Literal oldLiteral = this.getLiteral(place);
        // we only have to do something if the new literal is
        // different from the old literal
        if (!oldLiteral.equals(literal)) {
            // in this case, the literal has to be set to top;
            // if "top" is the default for this place: remove non-default
            if (getDefaultLiteral(place) == Literal.TOP) {
                this.placeToLit.remove(place);
            } else {
                this.placeToLit.put(place, Literal.TOP);
            }
        }
    }

//  setSubTree ************************************************************************

    // sets the literal for all literal array elements in the
    // tree specified by the given root, EXCEPT THE ROOT (which is left
    // untouched)
    private void setSubTree(Variable root, Literal lit) {
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.setWholeTree(element, lit);
        }
    }

// setWholeTree ********************************************************************

    // sets the literal for all literal array elements in the
    // tree sepcified by the given root, INCLUDING THE ROOT
    private void setWholeTree(Variable root, Literal lit) {

        this.setLiteral(root, lit);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.setWholeTree(element, lit);
        }
    }

//  lubSubTree ************************************************************************

    // analogous to lubSubTree
    private void lubSubTree(Variable root, Literal lit) {
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.lubWholeTree(element, lit);
        }
    }

//  lubWholeTree ************************************************************************

    // analogous to setWholeTree
    private void lubWholeTree(Variable root, Literal lit) {

        this.lubLiteral(root, lit);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.lubWholeTree(element, lit);
        }
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // lubs the given lattice element over <<this>> lattice element
    public void lub(AbstractLatticeElement foreignX) {

        LiteralLatticeElement foreign = (LiteralLatticeElement) foreignX;

        // lub over my non-default mappings
        for (Map.Entry<AbstractTacPlace, Literal> tacPlaceLiteralEntry : this.placeToLit.entrySet()) {
            AbstractTacPlace myPlace = tacPlaceLiteralEntry.getKey();
            Literal myLiteral = tacPlaceLiteralEntry.getValue();

            Literal foreignLiteral = foreign.getLiteral(myPlace);
            if (!foreignLiteral.equals(myLiteral)) {
                this.placeToLit.put(myPlace, Literal.TOP);
            }
        }

        // lub the remaining non-default mappings of "foreign" over my
        // default mappings
        Map<AbstractTacPlace, Literal> foreignPlaceToLit = foreign.getPlaceToLit();
        for (Map.Entry<AbstractTacPlace, Literal> foreignEntry : foreignPlaceToLit.entrySet()) {
            AbstractTacPlace foreignPlace = foreignEntry.getKey();
            Literal foreignLiteral = foreignEntry.getValue();

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

        // cleaning pass: remove defaults
        for (Iterator<Map.Entry<AbstractTacPlace, Literal>> iter = this.placeToLit.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<AbstractTacPlace, Literal> entry = iter.next();
            AbstractTacPlace place = entry.getKey();
            Literal lit = entry.getValue();
            if (getDefaultLiteral(place).equals(lit)) {
                iter.remove();
            }
        }
    }

//  lub (static) *******************************************************************

    // returns the lub of the given literals (the first literal might be reused)
    public static Literal lub(Literal lit1, Literal lit2) {
        if (lit1.equals(lit2)) {
            return lit1;
        } else {
            return Literal.TOP;
        }
    }

// initTree *************************************************************************

    // inits the literal for all literal array elements in the
    // tree specified by the given root;
    // difference to setTree: doesn't call setLiteral (which tries
    // to reduce defaults, which don't exist during initialization),
    // but modifies placeToLit directly
    private void initTree(Variable root, Literal lit) {
        this.placeToLit.put(root, lit);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.initTree(element, lit);
        }
    }

//  assignSimple *******************************************************************

    // mustAliases and mayAliases: of left; mustAliases always have to
    // include left itself
    public void assignSimple(Variable left, AbstractTacPlace right, Set<Variable> mustAliases, Set<Variable> mayAliases) {

        // initialize state copy (required by strongOverlap)
        this.origPlaceToLit = new HashMap<>(this.placeToLit);

        // case distinguisher for the left variable
        int leftCase;

        // find out more about the left variable
        if (!left.isArrayElement()) {
            // left: not an array element
            if (!left.isArray()) {
                // left: neither an array nor an array element
                leftCase = 1;
            } else {
                // left: an array that is not an array element
                leftCase = 2;
            }
        } else {
            // left: array element
            if (!left.hasNonLiteralIndices()) {
                // left: literal array element
                leftCase = 3;
            } else {
                // left: non-literal array element
                leftCase = 4;
            }
        }

        // take appropariate actions
        switch (leftCase) {

            // not an array element and not known as array ("normal variable")
            case 1:

                Literal rightLit = this.getLiteral(right);

                // strong update for must-aliases (including left itself)
                for (Variable mustAlias : mustAliases) {
                    this.setLiteral(mustAlias, rightLit);
                }

                // weak update for may-aliases
                for (Variable mayAlias : mayAliases) {
                    this.lubLiteral(mayAlias, rightLit);
                }

                break;

            // array, but not an array element
            case 2:

                // strong overlap
                this.strongOverlap(left, right);
                break;

            // array element (and maybe an array) without non-literal indices
            case 3:

                // strong overlap
                this.strongOverlap(left, right);
                break;

            // array element (and maybe an array) with non-literal indices
            case 4:

                // weak overlap for all MI variables of left
                for (Variable miVar : this.getMiList(left)) {
                    this.weakOverlap(miVar, right);
                }
                break;

            default:
                throw new RuntimeException("SNH");
        }

        this.origPlaceToLit = null;
    }

//  assignUnary ********************************************************************

    public void assignUnary(Variable left, AbstractTacPlace right, int op, Set<Variable> mustAliases, Set<Variable> mayAliases) {
        // base literal; must undergo conversion depending on operator
        Literal baseLit = this.getLiteral(right);

        // effective literal: to be computed
        Literal effectiveRightLit;

        // construct effective literal
        if (baseLit == Literal.TOP) {
            // shortcut: no need to look at the operator if we are dealing with TOP
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

                    // invert the boolean value
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
                    // note: used for special nodes (such as ~_hotspot0)
                    // see: builtin functions file)
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
                    // not documented in the PHP manual: casts to NULL
                    effectiveRightLit = Literal.NULL;
                    break;
                }
                default: {
                    throw new RuntimeException("Unsupported operator");
                }
            }
        }

        // assign
        this.assignSimple(left, effectiveRightLit, mustAliases, mayAliases);
    }

//  assignBinary********************************************************************

    public void assignBinary(
        Variable left, AbstractTacPlace leftOperand, AbstractTacPlace rightOperand, int op, Set<Variable> mustAliases,
        Set<Variable> mayAliases, AbstractCfgNode cfgNode
    ) {
        // base literals; must undergo conversion depending on operator
        Literal baseLeftLit = this.getLiteral(leftOperand);
        Literal baseRightLit = this.getLiteral(rightOperand);

        // effective literal: to be computed
        Literal effectiveLit;

        // construct effective literal
        if (baseLeftLit == Literal.TOP || baseRightLit == Literal.TOP) {
            // shortcut: no need to look at the operator if we are dealing with TOP
            // (short-circuit code is handled by the TacConverter)
            effectiveLit = Literal.TOP;
        } else {

            switch (op) {

                // ARITHMETIC OPERATORS

                case TacOperators.PLUS: {
                    effectiveLit = new Literal(Literal.numberToString(
                        baseLeftLit.getFloatValue() + baseRightLit.getFloatValue()));
                    break;
                }
                case TacOperators.MINUS: {
                    effectiveLit = new Literal(Literal.numberToString(
                        baseLeftLit.getFloatValue() - baseRightLit.getFloatValue()));
                    break;
                }
                case TacOperators.MULT: {
                    effectiveLit = new Literal(Literal.numberToString(
                        baseLeftLit.getFloatValue() * baseRightLit.getFloatValue()));
                    break;
                }
                case TacOperators.DIV: {
                    float rightFloat = baseRightLit.getFloatValue();
                    if (rightFloat == 0) {
                        // handle division by zero;
                        // the warning is not always absolutely true, e.g.:
                        //   $b = 7;
                        //   if ($x) {
                        //     $b = 0;
                        //   }
                        //   $a / $b;
                        // it just says 'there might be a path through the
                        // program leading to a division by zero'
                        System.out.println("Warning: might be division by zero");
                        System.out.println("Line: " + cfgNode.getOrigLineno());
                        effectiveLit = Literal.FALSE;
                    } else {
                        effectiveLit = new Literal(Literal.numberToString(
                            baseLeftLit.getFloatValue() / rightFloat));
                    }
                    break;
                }
                case TacOperators.MODULO: {
                    effectiveLit = new Literal(Literal.numberToString(
                        baseLeftLit.getFloatValue() % baseRightLit.getFloatValue()));
                    break;
                }

                // STRING OPERATORS

                case TacOperators.CONCAT: {
                    effectiveLit = new Literal(
                        baseLeftLit.getStringValue() + baseRightLit.getStringValue());
                    break;
                }

                // LOGICAL OPERATORS

                case TacOperators.BOOLEAN_AND: {
                    // BOOLEAN_AND is used only for the special "isset" case
                    // (see TacConverter), no need for short-circuit code here
                    Literal boolLeft = baseLeftLit.getBoolValueLiteral();
                    Literal boolRight = baseRightLit.getBoolValueLiteral();
                    if (boolLeft == Literal.TOP || boolRight == Literal.TOP) {
                        effectiveLit = Literal.TOP;
                    } else if (boolLeft == Literal.TRUE && boolRight == Literal.TRUE) {
                        effectiveLit = Literal.TRUE;
                    } else {
                        // at least one of the operands is false, at most one operand
                        // is true, none of the operands is TOP
                        effectiveLit = Literal.FALSE;
                    }
                    break;
                }

                // COMPARISON OPERATORS

                case TacOperators.IS_EQUAL: {
                    effectiveLit = Literal.isEqualLiteral(baseLeftLit, baseRightLit);
                    break;
                }
                case TacOperators.IS_NOT_EQUAL: {
                    effectiveLit = Literal.invert(
                        Literal.isEqualLiteral(baseLeftLit, baseRightLit));
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
                    // "<=" is the same as "not >"
                    effectiveLit = Literal.invert(
                        Literal.isGreaterLiteral(baseLeftLit, baseRightLit, cfgNode));
                    break;
                }
                case TacOperators.IS_GREATER_OR_EQUAL: {
                    // ">=" is the same as "not <"
                    effectiveLit = Literal.invert(
                        Literal.isSmallerLiteral(baseLeftLit, baseRightLit, cfgNode));
                    break;
                }
                case TacOperators.IS_IDENTICAL: {
                    effectiveLit = Literal.isIdenticalLiteral(baseLeftLit, baseRightLit);
                    break;
                }
                case TacOperators.IS_NOT_IDENTICAL: {
                    effectiveLit = Literal.invert(
                        Literal.isIdenticalLiteral(baseLeftLit, baseRightLit));
                    break;
                }

                // BITWISE OPERATORS
                // LATER: implement on demand

                case TacOperators.SL: {
                    effectiveLit = Literal.TOP;
                    break;
                }
                case TacOperators.SR: {
                    // LATER: implement on demand
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

        // assign
        this.assignSimple(left, effectiveLit, mustAliases, mayAliases);
    }

//  assignArray ********************************************************************

    public void assignArray(Variable left) {
        // set the whole tree of left to null
        this.setWholeTree(left, Literal.NULL);
    }

//  defineConstant *****************************************************************

    // sets the literal of the given constant
    public void defineConstant(Constant c, Literal lit) {
        this.setLiteral(c, lit);
    }

//  defineConstantWeak *************************************************************

    // lubs over the literal of the given constant
    public void defineConstantWeak(Constant c, Literal lit) {
        this.lubLiteral(c, lit);
    }

// getMiList ***********************************************************************

    // returns a list of array elements that are maybe identical
    // to the given array element; only array elements without
    // non-literal indices are returned
    public List<Variable> getMiList(Variable var) {

        if (!var.isArrayElement()) {
            throw new RuntimeException("SNH");
        }
        if (!var.hasNonLiteralIndices()) {
            throw new RuntimeException("SNH");
        }

        // the list to be returned (contains Variables)
        List<Variable> miList = new LinkedList<>();

        // root of the array tree, indices of the array element
        Variable root = var.getTopEnclosingArray();
        List<AbstractTacPlace> indices = var.getIndices();

        this.miRecurse(miList, root, new LinkedList<>(indices));
        return miList;
    }

// miRecurse ***********************************************************************

    // CAUTION: the indices list is modified inside this method, so you might
    // want to pass a shallow copy instead of a reference to the list
    private void miRecurse(List<Variable> miList, Variable root, List<AbstractTacPlace> indices) {
        /*
         * - separate head from the indices list
         * - if this first index is literal:
         *   - if the remaining indices list is empty:
         *     add the target array element to the miList
         *   - else: recurse with target array element and
         *     the remaining list
         * - else (first index is non-literal):
         *   - if the remaining indices list is empty:
         *     add all literal array elements to the miList
         *   - else: recurse for all literal array elements and
         *     <<a copy>> of the remaining indices list: otherwise,
         *     the different branches would operate on the same
         *     indices list
         *
         */

        // proceeding only makes sense if the considered root has known
        // array elements
        if (!root.isArray()) {
            return;
        }

        AbstractTacPlace index = indices.remove(0);
        if (index instanceof Literal) {

            Variable target = root.getElement(index);
            // it is possible that the considered array doesn't have this index
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
                for (Variable target : literalElements) {
                    this.miRecurse(miList, target, new LinkedList<>(indices));
                }
            }
        }
    }

//  strongOverlap ******************************************************************

    // before calling this function, don't forget to
    // initialize origPlaceToLit: new HashMap(this.placeToLit)
    private void strongOverlap(Variable target, AbstractTacPlace source) {

        Literal sourceLit = this.getOrigLiteral(source);

        if (source.isVariable() && ((Variable) source).isArray()) {
            // a known array on the right side

            Variable sourceArray = (Variable) source;
            if (target.isArray()) {
                // target known as array

                // for all literal direct elements of target...
                List<Variable> targetElements = target.getLiteralElements();
                for (Variable targetElement : targetElements) {
                    // if there is a direct element of source with the same
                    // direct index...
                    Variable sourceElement = sourceArray.getElement(targetElement.getIndex());
                    if (sourceElement != null) {
                        this.strongOverlap(targetElement, sourceElement);
                    } else {
                        this.setWholeTree(targetElement, Literal.TOP);
                    }
                }
            }
        } else if (!source.isVariable()) {
            // the right side is literal or constant
            this.setSubTree(target, Literal.NULL);
        } else {
            this.setSubTree(target, Literal.TOP);
        }

        this.setLiteral(target, sourceLit);
    }

//  weakOverlap ********************************************************************

    // before calling this function, don't forget to
    // initialize origPlaceToLit: new HashMap(this.placeToLit)
    private void weakOverlap(Variable target, AbstractTacPlace source) {

        Literal sourceLit = this.getOrigLiteral(source);

        if (source.isVariable() && ((Variable) source).isArray()) {
            // a known array on the right side

            Variable sourceArray = (Variable) source;
            if (target.isArray()) {
                // target known as array

                // for all literal direct elements of target...
                List<Variable> targetElements = target.getLiteralElements();
                for (Variable targetElement : targetElements) {
                    // if there is a direct element of source with the same
                    // direct index...
                    Variable sourceElement = sourceArray.getElement(targetElement.getIndex());
                    if (sourceElement != null) {
                        this.weakOverlap(targetElement, sourceElement);
                    } else {
                        this.lubWholeTree(targetElement, Literal.TOP);
                    }
                }
            }
        } else if (!source.isVariable()) {
            // the right side is literal or constant
            this.lubSubTree(target, Literal.NULL);
        } else {
            this.lubSubTree(target, Literal.TOP);
        }

        this.lubLiteral(target, sourceLit);
    }

//  resetVariables *****************************************************************

    // resets all variables that belong to the given symbol table
    // (by removing their non-default mapping)
    public void resetVariables(SymbolTable symTab) {

        for (Iterator<Map.Entry<AbstractTacPlace, Literal>> iter = this.placeToLit.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<AbstractTacPlace, Literal> entry = iter.next();
            AbstractTacPlace place = entry.getKey();

            if (!(place instanceof Variable)) {
                // nothing to do for non-variables (i.e., constants)
                continue;
            }

            Variable var = (Variable) place;
            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }
    }

//  setFormal **********************************************************************

    // overlaps the given formal with the given place
    // (no need to consider aliases here)
    public void setFormal(TacFormalParameter formalParam, AbstractTacPlace place) {

        // initialize state copy (required by strongOverlap)
        this.origPlaceToLit = new HashMap<>(this.placeToLit);

        Variable formalVar = formalParam.getVariable();
        this.strongOverlap(formalVar, place);

        this.origPlaceToLit = null;
    }

//  setShadow **********************************************************************

    // sets a shadow's literal to the literal of its original
    public void setShadow(Variable shadow, Variable original) {
        this.setLiteral(shadow, this.getLiteral(original));
    }

//  copyGlobalLike *****************************************************************

    // copies the non-default literal mappings for "global-like" places
    // from origElement (i.e., global variables, superglobal variables, and
    // constants)
    public void copyGlobalLike(LiteralLatticeElement origElement) {

        Map<AbstractTacPlace, Literal> origMap = origElement.getPlaceToLit();
        for (Map.Entry<AbstractTacPlace, Literal> entry : origMap.entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            Literal origLit = entry.getValue();

            // decide whether to copy this place
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

//  copyLocals *********************************************************************

    // copies the non-default literal mappings for global variables from origElement
    public void copyLocals(LiteralLatticeElement origElement) {

        Map<AbstractTacPlace, Literal> origMap = origElement.getPlaceToLit();
        for (Map.Entry<AbstractTacPlace, Literal> entry : origMap.entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            Literal origLit = entry.getValue();

            // nothing to do for non-variables and non-locals
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

//  setLocal ***********************************************************************

    // sets the literal of the given local variable to the given literal
    public void setLocal(Variable local, Literal lit) {
        this.setLiteral(local, lit);
    }

//  handleReturnValue **************************************************************

    // sets the temporary to the given literal and resets retVar
    public void handleReturnValue(Variable tempVar, Literal lit, Variable retVar) {
        this.setLiteral(tempVar, lit);
        this.placeToLit.remove(retVar);
    }

    // sets the temporary to the given literal and DOES NOT reset retVar
    public void handleReturnValue(Variable tempVar, Literal lit/*, Variable retVar*/) {
        this.setLiteral(tempVar, lit);
    }

//  setRetVar **********************************************************************

    // sets the literal of the given return variable to the given literal
    public void setRetVar(Variable retVar, Literal lit) {
        this.setLiteral(retVar, lit);
    }

//  ********************************************************************************

    public void handleReturnValueBuiltin(Variable tempVar) {
        this.setWholeTree(tempVar, Literal.TOP);
    }

//  ********************************************************************************

    public void handleReturnValueUnknown(Variable tempVar) {
        this.setWholeTree(tempVar, Literal.TOP);
    }

//  equals *************************************************************************

    public boolean structureEquals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LiteralLatticeElement)) {
            return false;
        }
        LiteralLatticeElement comp = (LiteralLatticeElement) obj;

        // the literal maps have to be equal
        return this.placeToLit.equals(comp.getPlaceToLit());
    }

//  equals *************************************************************************

    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LiteralLatticeElement)) {
            return false;
        }
        LiteralLatticeElement comp = (LiteralLatticeElement) obj;

        // the literal maps have to be equal
        return this.placeToLit.equals(comp.getPlaceToLit());
    }

//  hashCode ***********************************************************************

    public int hashCode() {
        // EFF: perform hashCode caching
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