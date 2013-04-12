package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class LiteralLatticeElement
    extends LatticeElement {

    // TacPlace -> Literal
    // contains only non-default mappings;
    private Map<TacPlace, Literal> placeToLit;

    // a copy of placeToLit, must be initialized by methods that need it;
    // they must not forget to null it as soon as they've finished their
    // work (saves memory)
    private Map<TacPlace, Literal> origPlaceToLit;

    // the default lattice element; IT MUST NOT BE USED DIRECTLY BY THE ANALYSIS!
    // can be seen as "grounding", "fall-back" for normal lattice elements
    public static LiteralLatticeElement DEFAULT;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates a lattice element that adds no information to the
    // default lattice element
    public LiteralLatticeElement() {
        this.placeToLit = new HashMap<TacPlace, Literal>();
    }

    // clones the given element
    public LiteralLatticeElement(LiteralLatticeElement cloneMe) {
        this.placeToLit = new HashMap<TacPlace, Literal>(cloneMe.getPlaceToLit());
    }

    public LatticeElement cloneMe() {
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
        List places,
        ConstantsTable constantsTable,
        List functions,
        SymbolTable superSymbolTable) {

        // initialize conservative base mapping for variables & constants: TOP
        this.placeToLit = new HashMap<TacPlace, Literal>();
        for (Iterator iter = places.iterator(); iter.hasNext(); ) {
            TacPlace place = (TacPlace) iter.next();
            this.placeToLit.put(place, Literal.TOP);
        }

        // initialize function return values to NULL, because:
        // if a function has no return statement, the return variable is not touched;
        // since the real-world return value is then "NULL", the default mapping
        // for return variables has to have the same properties as the default
        // mapping for NULL;
        // but skip the return variable of the special unknown function! (see below)
        for (Iterator iter = places.iterator(); iter.hasNext(); ) {
            TacPlace place = (TacPlace) iter.next();
            if ((place instanceof Variable) && (place.getVariable().isReturnVariable())) {
                this.placeToLit.put(place, Literal.NULL);
            }
        }

        // initialize constants
        Map constants = constantsTable.getConstants();
        for (Iterator iter = constants.values().iterator(); iter.hasNext(); ) {
            Constant constant = (Constant) iter.next();
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
        for (Iterator iter = functions.iterator(); iter.hasNext(); ) {
            TacFunction function = (TacFunction) iter.next();
            if (function.isMain() && MyOptions.optionG) {
                continue;
            }
            SymbolTable symtab = function.getSymbolTable();
            Map variables = symtab.getVariables();
            for (Iterator varIter = variables.values().iterator(); varIter.hasNext(); ) {
                Variable variable = (Variable) varIter.next();
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
        List places,
        ConstantsTable constantsTable,
        List functions,
        SymbolTable superSymbolTable) {

        LiteralLatticeElement.DEFAULT =
            new LiteralLatticeElement(places, constantsTable, functions,
                superSymbolTable);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

//  getPlaceToLit ******************************************************************

    public Map<TacPlace, Literal> getPlaceToLit() {
        return this.placeToLit;
    }

// getLiteral **********************************************************************

    public Literal getLiteral(TacPlace place) {
        return this.getLiteralFrom(place, this.placeToLit);
    }

//  getOrigLiteral *****************************************************************

    private Literal getOrigLiteral(TacPlace place) {
        return this.getLiteralFrom(place, this.origPlaceToLit);
    }

// getLiteralFrom ******************************************************************

    // returns the non-default literal for this place if that mapping exists,
    // or the default literal otherwise
    private Literal getLiteralFrom(TacPlace place, Map readFrom) {

        if (place instanceof Literal) {
            return (Literal) place;
        }

        // return the non-default mapping (if there is one)
        Literal nonDefaultLit = (Literal) readFrom.get(place);
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

    private static Literal getDefaultLiteral(TacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return (Literal) LiteralLatticeElement.DEFAULT.getPlaceToLit().get(place);
    }

// getNonDefaultLiteral ************************************************************

    // returns the non-default literal for the given place;
    // null if there is no non-default literal for it
    private Literal getNonDefaultLiteral(TacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return this.placeToLit.get(place);
    }

//  ********************************************************************************
//  SET ****************************************************************************
//  ********************************************************************************

// setLiteral **********************************************************************

    private void setLiteral(TacPlace place, Literal literal) {

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

    private void lubLiteral(TacPlace place, Literal literal) {

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
        for (Iterator iter = root.getLiteralElements().iterator(); iter.hasNext(); ) {
            Variable element = (Variable) iter.next();
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
        for (Iterator iter = root.getLiteralElements().iterator(); iter.hasNext(); ) {
            Variable element = (Variable) iter.next();
            this.setWholeTree(element, lit);
        }
    }

//  lubSubTree ************************************************************************

    // analogous to lubSubTree
    private void lubSubTree(Variable root, Literal lit) {
        if (!root.isArray()) {
            return;
        }
        for (Iterator iter = root.getLiteralElements().iterator(); iter.hasNext(); ) {
            Variable element = (Variable) iter.next();
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
        for (Iterator iter = root.getLiteralElements().iterator(); iter.hasNext(); ) {
            Variable element = (Variable) iter.next();
            this.lubWholeTree(element, lit);
        }
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // lubs the given lattice element over <<this>> lattice element
    public void lub(LatticeElement foreignX) {

        LiteralLatticeElement foreign = (LiteralLatticeElement) foreignX;

        // lub over my non-default mappings
        for (Iterator iter = this.placeToLit.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry myEntry = (Map.Entry) iter.next();
            TacPlace myPlace = (TacPlace) myEntry.getKey();
            Literal myLiteral = (Literal) myEntry.getValue();

            Literal foreignLiteral = foreign.getLiteral(myPlace);
            if (!foreignLiteral.equals(myLiteral)) {
                this.placeToLit.put(myPlace, Literal.TOP);
            }
        }

        // lub the remaining non-default mappings of "foreign" over my
        // default mappings
        Map foreignPlaceToLit = foreign.getPlaceToLit();
        for (Iterator iter = foreignPlaceToLit.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry foreignEntry = (Map.Entry) iter.next();
            TacPlace foreignPlace = (TacPlace) foreignEntry.getKey();
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

        // cleaning pass: remove defaults
        for (Iterator iter = this.placeToLit.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace place = (TacPlace) entry.getKey();
            Literal lit = (Literal) entry.getValue();
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
        for (Iterator iter = root.getLiteralElements().iterator(); iter.hasNext(); ) {
            Variable element = (Variable) iter.next();
            this.initTree(element, lit);
        }
    }

//  assignSimple *******************************************************************

    // mustAliases and mayAliases: of left; mustAliases always have to
    // include left itself
    public void assignSimple(Variable left, TacPlace right, Set mustAliases, Set mayAliases) {

        // initialize state copy (required by strongOverlap)
        this.origPlaceToLit = new HashMap<TacPlace, Literal>(this.placeToLit);

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
                for (Iterator iter = mustAliases.iterator(); iter.hasNext(); ) {
                    Variable mustAlias = (Variable) iter.next();
                    this.setLiteral(mustAlias, rightLit);
                }

                // weak update for may-aliases
                for (Iterator iter = mayAliases.iterator(); iter.hasNext(); ) {
                    Variable mayAlias = (Variable) iter.next();
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
                for (Iterator iter = this.getMiList(left).iterator(); iter.hasNext(); ) {
                    Variable miVar = (Variable) iter.next();
                    this.weakOverlap(miVar, right);
                }
                break;

            default:
                throw new RuntimeException("SNH");
        }

        this.origPlaceToLit = null;
    }

//  assignUnary ********************************************************************

    public void assignUnary(Variable left, TacPlace right, int op,
                            Set mustAliases, Set mayAliases) {

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

    public void assignBinary(Variable left, TacPlace leftOperand,
                             TacPlace rightOperand, int op, Set mustAliases, Set mayAliases,
                             CfgNode cfgNode) {

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
        List<Variable> miList = new LinkedList<Variable>();

        // root of the array tree, indices of the array element
        Variable root = var.getTopEnclosingArray();
        List<TacPlace> indices = var.getIndices();

        this.miRecurse(miList, root, new LinkedList<TacPlace>(indices));
        return miList;
    }

// miRecurse ***********************************************************************

    // CAUTION: the indices list is modified inside this method, so you might
    // want to pass a shallow copy instead of a reference to the list
    private void miRecurse(List<Variable> miList, Variable root, List<TacPlace> indices) {
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

        TacPlace index = (TacPlace) indices.remove(0);
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
                for (Iterator iter = literalElements.iterator(); iter.hasNext(); ) {
                    Variable target = (Variable) iter.next();
                    this.miRecurse(miList, target, new LinkedList<TacPlace>(indices));
                }
            }
        }
    }

//  strongOverlap ******************************************************************

    // before calling this function, don't forget to
    // initialize origPlaceToLit: new HashMap(this.placeToLit)
    private void strongOverlap(Variable target, TacPlace source) {

        Literal sourceLit = this.getOrigLiteral(source);

        if (source.isVariable() && ((Variable) source).isArray()) {
            // a known array on the right side

            Variable sourceArray = (Variable) source;
            if (target.isArray()) {
                // target known as array

                // for all literal direct elements of target...
                List targetElements = target.getLiteralElements();
                for (Iterator iter = targetElements.iterator(); iter.hasNext(); ) {
                    Variable targetElem = (Variable) iter.next();

                    // if there is a direct element of source with the same
                    // direct index...
                    Variable sourceElem = sourceArray.getElement(targetElem.getIndex());
                    if (sourceElem != null) {
                        this.strongOverlap(targetElem, sourceElem);
                    } else {
                        this.setWholeTree(targetElem, Literal.TOP);
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
    private void weakOverlap(Variable target, TacPlace source) {

        Literal sourceLit = this.getOrigLiteral(source);

        if (source.isVariable() && ((Variable) source).isArray()) {
            // a known array on the right side

            Variable sourceArray = (Variable) source;
            if (target.isArray()) {
                // target known as array

                // for all literal direct elements of target...
                List targetElements = target.getLiteralElements();
                for (Iterator iter = targetElements.iterator(); iter.hasNext(); ) {
                    Variable targetElem = (Variable) iter.next();

                    // if there is a direct element of source with the same
                    // direct index...
                    Variable sourceElem = sourceArray.getElement(targetElem.getIndex());
                    if (sourceElem != null) {
                        this.weakOverlap(targetElem, sourceElem);
                    } else {
                        this.lubWholeTree(targetElem, Literal.TOP);
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

        for (Iterator iter = this.placeToLit.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace place = (TacPlace) entry.getKey();

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
    public void setFormal(TacFormalParam formalParam, TacPlace place) {

        // initialize state copy (required by strongOverlap)
        this.origPlaceToLit = new HashMap<TacPlace, Literal>(this.placeToLit);

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

        Map origMap = origElement.getPlaceToLit();
        for (Iterator iter = origMap.entrySet().iterator(); iter.hasNext(); ) {

            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            Literal origLit = (Literal) entry.getValue();

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

        Map origMap = origElement.getPlaceToLit();
        for (Iterator iter = origMap.entrySet().iterator(); iter.hasNext(); ) {

            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            Literal origLit = (Literal) entry.getValue();

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
        if (!this.placeToLit.equals(comp.getPlaceToLit())) {
            return false;
        }

        return true;
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
        if (!this.placeToLit.equals(comp.getPlaceToLit())) {
            return false;
        }

        return true;
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