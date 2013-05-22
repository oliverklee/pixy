package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DependencyLatticeElement extends AbstractLatticeElement {
    // contains only non-default mappings;
    // does not contain mappings for non-literal array elements, because:
    // the dependency value of such elements solely depends on the array label
    // of their root array;
    // also: contains only mappings for Variables and Constants, not for Literals
    private Map<AbstractTacPlace, DependencySet> placeToDep;

    // "array labels";
    // remotely resemble "clean array flags" (caFlags) from XSS taint analysis;
    // gives an upper bound for the label of non-literal array elements;
    // contains only non-default mappings;
    // only defined for non-array-elements
    private Map<Variable, DependencySet> arrayLabels;

    // the default lattice element; IT MUST NOT BE USED DIRECTLY BY THE ANALYSIS!
    // can be seen as "grounding", "fall-back" for normal lattice elements
    public static DependencyLatticeElement DEFAULT;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  DependencyLatticeElement() ********************************************************

    // a lattice element that doesn't modify the information of the
    // default lattice element
    public DependencyLatticeElement() {
        this.placeToDep = new HashMap<>();
        this.arrayLabels = new HashMap<>();
    }

//  DependencyLatticeElement(DependencyLatticeElement) ***********************************

    // clones the given element
    public DependencyLatticeElement(DependencyLatticeElement element) {
        this.placeToDep =
            new HashMap<>(element.getPlaceToDep());
        this.arrayLabels =
            new HashMap<>(element.getArrayLabels());
    }

//  cloneMe ************************************************************************

    public AbstractLatticeElement cloneMe() {
        // uses the cloning constructor
        return new DependencyLatticeElement(this);
    }

//  DependencyLatticeElement(<for initDefault>) *****************************************

    // constructor for default element (to be called by "initDefault"):
    // basically initializes everything to UNINIT;
    // TODO: the code for this can probably be simplified dramatically ;
    // also, the comments inside this function are partly obsolete
    private DependencyLatticeElement(
        List<AbstractTacPlace> places,
        ConstantsTable constantsTable,
        List<TacFunction> functions,
        SymbolTable superSymbolTable,
        Variable memberPlace) {

        // initialize base mapping for variables: UNINIT
        // (note: array elements have no explicit array label: their label is
        // that of their top enclosing array)
        this.placeToDep = new HashMap<>();
        this.arrayLabels = new HashMap<>();
        for (AbstractTacPlace place : places) {
            if ((place instanceof Variable) &&
                place.getVariable().isArrayElement() &&
                place.getVariable().hasNonLiteralIndices()) {
                // if this is a non-literal array element:
                // don't add a mapping
            } else {
                this.placeToDep.put(place, DependencySet.UNINIT);
            }

            // arrayLabels are not defined for variables that are array-elements
            if ((place instanceof Variable) && !place.getVariable().isArrayElement()) {
                this.arrayLabels.put((Variable) place, DependencySet.UNINIT);
            }
        }

        // initialize function return values:
        // if a function has no return statement, the return variable is not touched;
        // since the real-world return value is then "NULL", the default mapping
        // for return variables has to have the same properties as the default
        // mapping for NULL (i.e., harmless);
        for (AbstractTacPlace place : places) {
            if ((place instanceof Variable) && place.getVariable().isReturnVariable()) {
                this.placeToDep.put(place, DependencySet.UNINIT);
                this.arrayLabels.put((Variable) place, DependencySet.UNINIT);
            }
        }

        // special member variable
        this.placeToDep.put(memberPlace, DependencySet.UNINIT);
        this.arrayLabels.put(memberPlace, DependencySet.UNINIT);

        // initialize constants
        for (Constant constant : constantsTable.getConstants().values()) {
            this.placeToDep.put(constant, DependencySet.UNINIT);
        }

        // initialize local function variables & parameters to HARMLESS;
        // locals of main function == globals of the program:
        // their initialization depends on the register_globals setting:
        // true: UNINIT
        // false: like other locals
        for (TacFunction function : functions) {
            if (function.isMain()) {
                if (MyOptions.optionG) {
                    // if register_globals is active, we don't change the conservative
                    // base mapping (tainted/dirty) for the locals of the main function;
                    // NOTE: this is also quite useless, as most of the rest of this function;
                    // should be cleaned up some day...
                    continue;
                }
            }
            SymbolTable symbolTable = function.getSymbolTable();
            for (Variable variable : symbolTable.getVariables().values()) {
                // array elements are handled along with their root
                if (variable.isArrayElement()) {
                    continue;
                }
                // init the whole tree below this variable to untainted
                // (note that using setTree wouldn't work here)
                this.initTree(variable, DependencySet.UNINIT);
                this.arrayLabels.put(variable, DependencySet.UNINIT);
            }
        }

        // untaint the whole $_SESSION array
        Variable sess = superSymbolTable.getVariable("$_SESSION");
        this.initTree(sess, DependencySet.UNINIT);
        this.arrayLabels.put(sess, DependencySet.UNINIT);

        // untaint harmless superglobals;
        // fill this list with harmless superglobals that must be set to
        // harmless;
        List<Variable> harmlessSuperGlobals = new LinkedList<>();

        // NOTE: WHEN ADDING A HARMLESS SUPERGLOBAL HERE, YOU SHOULD ALSO ADD IT TO
        // TacConverter.addSuperGlobalElements()

        // case-sensitive, and that's OK
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

        // same for 'argv'
        Variable argv = superSymbolTable.getVariable("$_SERVER[argv]");
        if (argv != null) {
            for (Variable argvElement : argv.getElements()) {
                harmlessSuperGlobals.add(argvElement);
            }
        }
        argv = superSymbolTable.getVariable("$HTTP_SERVER_VARS[argv]");
        if (argv != null) {
            for (Variable argvElement : argv.getElements()) {
                harmlessSuperGlobals.add(argvElement);
            }
        }

        // collection of harmless variables finished, now make them harmless
        for (Variable harmlessSuperGlobal : harmlessSuperGlobals) {
            // if it is null, it hasn't been used in the php script
            if (harmlessSuperGlobal != null) {
                this.placeToDep.put(harmlessSuperGlobal, DependencySet.UNINIT);
            }
        }
    }

//  ********************************************************************************

    private void addHarmlessServerVar(List<Variable> harmlessSuperGlobals,
                                      SymbolTable superSymbolTable, String name) {

        Variable v1 = superSymbolTable.getVariable("$_SERVER[" + name + "]");
        Variable v2 = superSymbolTable.getVariable("$HTTP_SERVER_VARS[" + name + "]");
        if (v1 == null || v2 == null) {
            // you must add the variable to TacConverter.addSuperGlobalElements()
            throw new RuntimeException("SNH: " + name);
        }
        harmlessSuperGlobals.add(v1);
        harmlessSuperGlobals.add(v2);
    }

//  initDefault ********************************************************************

    // initializes the default lattice element
    static void initDefault(
        List<AbstractTacPlace> places, ConstantsTable constantsTable, List<TacFunction> functions, SymbolTable superSymbolTable,
        Variable memberPlace
    ) {
        DependencyLatticeElement.DEFAULT
            = new DependencyLatticeElement(places, constantsTable, functions, superSymbolTable, memberPlace);
    }

//  *********************************************************************************
//  GET *****************************************************************************
//  *********************************************************************************

//  getPlaceToDep *****************************************************************

    public Map<AbstractTacPlace, DependencySet> getPlaceToDep() {
        return this.placeToDep;
    }

//  *********************************************************************************

    public Map<Variable, DependencySet> getArrayLabels() {
        return this.arrayLabels;
    }

//  ********************************************************************************

    public DependencySet getDep(AbstractTacPlace place) {
        return this.getDepFrom(place, this.placeToDep);
    }

//  ********************************************************************************

    // returns the non-default dependency for this place if that mapping exists,
    // or the default dependency otherwise
    private DependencySet getDepFrom(
        AbstractTacPlace place, Map<? extends AbstractTacPlace, DependencySet> readFrom) {

        if (place instanceof Literal) {
            throw new RuntimeException("SNH any longer");
        }

        // if we encounter a non-literal array element:
        // dependency depends solely on the array label of the enclosing array
        if (place instanceof Variable) {
            Variable var = (Variable) place;
            if (var.isArrayElement() && var.hasNonLiteralIndices()) {
                return getArrayLabel(var.getTopEnclosingArray());
            }
        }

        // return the non-default mapping (if there is one)
        DependencySet nonDefaultDep = readFrom.get(place);
        if (nonDefaultDep != null) {
            return nonDefaultDep;
        }

        // return the default mapping if there is no non-default one
        DependencySet defaultDep = getDefaultDep(place);
        if (defaultDep == null) {
            throw new RuntimeException("SNH: " + place);
        } else {
            return defaultDep;
        }
    }

//  ********************************************************************************

    private static DependencySet getDefaultDep(AbstractTacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return DependencyLatticeElement.DEFAULT.getPlaceToDep().get(place);
    }

//  ********************************************************************************

    // returns the non-default dependency for the given place;
    // null if there is no non-default dependency for it
    private DependencySet getNonDefaultDep(AbstractTacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return this.placeToDep.get(place);
    }

//  ********************************************************************************

    // returns the non-default array label for the given variable if that mapping
    // exists, or the default label otherwise;
    public DependencySet getArrayLabel(AbstractTacPlace place) {
        if ((place instanceof Literal) || (place instanceof Constant)) {
            throw new RuntimeException("SNH any longer");
        }

        Variable var = (Variable) place;

        // redirect request to tree root if necessary
        if (var.isArrayElement()) {
            var = var.getTopEnclosingArray();
        }

        // returns the default mapping if there is one
        DependencySet nonDefaultArrayLabel = getNonDefaultArrayLabel(var);
        if (nonDefaultArrayLabel != null) {
            return nonDefaultArrayLabel;
        }

        // return the default mapping if there is no non-default one
        return getDefaultArrayLabel(var);
    }

//  ********************************************************************************

    private DependencySet getDefaultArrayLabel(Variable var) {
        return DEFAULT.arrayLabels.get(var);
    }

//  ********************************************************************************

    private DependencySet getNonDefaultArrayLabel(Variable var) {
        return this.arrayLabels.get(var);
    }

//  ********************************************************************************
//  SET ****************************************************************************
//  ********************************************************************************

//  ***********************************************************************

    private void setDep(AbstractTacPlace place, DependencySet dependencySet) {

        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        if (place instanceof Variable && place.getVariable().isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        if (getDefaultDep(place).equals(dependencySet)) {
            // if the target dependency is the default, we simply remove the mapping
            this.placeToDep.remove(place);
        } else {
            this.placeToDep.put(place, dependencySet);
        }
    }

//  ***********************************************************************

    private void lubDep(AbstractTacPlace place, DependencySet dependencySet) {

        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        if (place instanceof Variable && place.getVariable().isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        DependencySet oldDep = this.getDep(place);
        DependencySet resultDep = DependencySet.lub(oldDep, dependencySet);
        if (getDefaultDep(place).equals(resultDep)) {
            // if the target dependency is the default, we simply remove the mapping
            this.placeToDep.remove(place);
        } else {
            this.placeToDep.put(place, resultDep);
        }
    }

//  ********************************************************************************

    // expects a non-array-element!
    private void setArrayLabel(Variable var, DependencySet dependencySet) {
        if (var.isArrayElement()) {
            throw new RuntimeException("SNH: " + var);
        }
        if (var.isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        // if the target dependency is the default, we simply remove the mapping
        if (getDefaultArrayLabel(var).equals(dependencySet)) {
            this.arrayLabels.remove(var);
        } else {
            this.arrayLabels.put(var, dependencySet);
        }
    }

//  ********************************************************************************

    private void lubArrayLabel(Variable var, DependencySet dependencySet) {
        if (var.isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        DependencySet oldDep = this.getArrayLabel(var);
        DependencySet resultDep = DependencySet.lub(dependencySet, oldDep);
        if (resultDep.equals(getDefaultArrayLabel(var))) {
            // if the resulting dependency is equal to this variable's default
            // dependency, we simply have to remove the default mapping
            this.arrayLabels.remove(var);
        } else {
            // replace the non-default mapping
            this.arrayLabels.put(var, resultDep);
        }
    }

// setWholeTree ********************************************************************

    // sets the dependency for all literal array elements in the
    // tree sepcified by the given root, INCLUDING THE ROOT
    private void setWholeTree(Variable root, DependencySet dependencySet) {
        this.setDep(root, dependencySet);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.setWholeTree(element, dependencySet);
        }
    }

//  lubWholeTree ************************************************************************

    // analogous to setWholeTree
    private void lubWholeTree(Variable root, DependencySet dependencySet) {
        this.lubDep(root, dependencySet);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.lubWholeTree(element, dependencySet);
        }
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  lub ****************************************************************************

    // lubs the given lattice element over <<this>> lattice element
    public void lub(AbstractLatticeElement foreignX) {

        DependencyLatticeElement foreign = (DependencyLatticeElement) foreignX;

        // DEPS ***

        // lub over my non-default mappings;
        // this one is necessary because we must not modify a map while
        // iterating over it
        Map<AbstractTacPlace, DependencySet> newPlaceToDep =
            new HashMap<>(this.placeToDep);
        for (Map.Entry<AbstractTacPlace, DependencySet> myEntry : this.placeToDep.entrySet()) {
            AbstractTacPlace myPlace = myEntry.getKey();
            DependencySet myDep = myEntry.getValue();
            DependencySet foreignDep = foreign.getDep(myPlace);
            newPlaceToDep.put(myPlace, DependencySet.lub(myDep, foreignDep));
        }
        this.placeToDep = newPlaceToDep;

        // lub the remaining non-default mappings of "foreign" over my
        // default mappings
        for (Map.Entry<AbstractTacPlace, DependencySet> foreignEntry : foreign.getPlaceToDep().entrySet()) {
            AbstractTacPlace foreignPlace = foreignEntry.getKey();
            DependencySet foreignDep = foreignEntry.getValue();

            DependencySet myDep = this.getNonDefaultDep(foreignPlace);
            // make sure that we handle my default mappings now
            if (myDep == null) {
                myDep = getDefaultDep(foreignPlace);
                this.placeToDep.put(foreignPlace, DependencySet.lub(foreignDep, myDep));
            }
        }

        // cleaning pass: remove defaults
        for (Iterator<Map.Entry<AbstractTacPlace, DependencySet>> iter = this.placeToDep.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<AbstractTacPlace, DependencySet> entry = iter.next();
            AbstractTacPlace place = entry.getKey();
            DependencySet dep = entry.getValue();
            if (getDefaultDep(place).equals(dep)) {
                iter.remove();
            }
        }

        // ARRAY LABELS ***

        // lub over my non-default mappings
        Map<Variable, DependencySet> newArrayLabels = new HashMap<>(this.arrayLabels);
        for (Map.Entry<Variable, DependencySet> myEntry : this.arrayLabels.entrySet()) {
            Variable myVar = myEntry.getKey();
            DependencySet myArrayLabel = myEntry.getValue();
            DependencySet foreignArrayLabel = foreign.getArrayLabel(myVar);
            newArrayLabels.put(myVar, DependencySet.lub(myArrayLabel, foreignArrayLabel));
        }
        this.arrayLabels = newArrayLabels;

        // lub the remaining non-default mappings of "foreign" over my
        // default mappings
        for (Map.Entry<Variable, DependencySet> foreignEntry : foreign.getArrayLabels().entrySet()) {
            Variable foreignVar = foreignEntry.getKey();
            DependencySet foreignArrayLabel = foreignEntry.getValue();

            // try non-default mapping:
            DependencySet myArrayLabel = getNonDefaultArrayLabel(foreignVar);
            if (myArrayLabel == null) {
                // fetch default mapping:
                myArrayLabel = getDefaultArrayLabel(foreignVar);
                this.arrayLabels.put(foreignVar, DependencySet.lub(myArrayLabel, foreignArrayLabel));
            }
        }

        // cleaning pass: remove defaults
        for (Iterator<Map.Entry<Variable, DependencySet>> iter = this.arrayLabels.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Variable, DependencySet> entry = iter.next();
            Variable var = entry.getKey();
            DependencySet arrayLabel = entry.getValue();
            if (getDefaultArrayLabel(var).equals(arrayLabel)) {
                iter.remove();
            }
        }
    }

//  lub (static) *******************************************************************

    // returns the lub of the given deps (the first dependency might be reused)
    public static DependencySet lub(DependencySet dep1, DependencySet dep2) {
        return DependencySet.lub(dep1, dep2);
    }

//  initTree ***********************************************************************

    // inits the deps for all literal array elements in the
    // tree specified by the given root;
    // difference to setTree: doesn't call setDep (which tries
    // to reduce defaults, which don't exist during initialization),
    // but modifies placeToDep directly
    void initTree(Variable root, DependencySet dep) {
        this.placeToDep.put(root, dep);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.initTree(element, dep);
        }
    }

//  ********************************************************************************

    // mustAliases and mayAliases: of left; mustAliases always have to
    // include left itself
    public void assign(Variable left, Set<Variable> mustAliases, Set<Variable> mayAliases, AbstractCfgNode cfgNode) {
        // dependency to be assigned to the left side
        DependencySet dep = DependencySet.create(DependencyLabel.create(cfgNode));

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
            case 1: {
                // strong update for must-aliases (including left itself)
                for (Variable mustAlias : mustAliases) {
                    this.setDep(mustAlias, dep);
                    this.setArrayLabel(mustAlias, dep);
                }

                // weak update for may-aliases
                for (Variable mayAlias : mayAliases) {
                    this.lubDep(mayAlias, dep);
                    this.lubArrayLabel(mayAlias, dep);
                }

                break;
            }

            // array, but not an array element
            case 2: {
                // set target.caFlag
                this.setArrayLabel(left, dep);

                // no strong overlap here, but: set the whole subtree to resultDep
                this.setWholeTree(left, dep);
                break;
            }

            // array element (and maybe an array) without non-literal indices
            case 3: {
                // lub target.root.caFlag
                this.lubArrayLabel(left.getTopEnclosingArray(), dep);

                // no strong overlap here, but: set the whole subtree to resultDep
                this.setWholeTree(left, dep);
                break;
            }

            // array element (and maybe an array) with non-literal indices
            case 4: {
                // lub target.root.caFlag
                this.lubArrayLabel(left.getTopEnclosingArray(), dep);

                // "weak overlap for all MI variables of left"
                // here: lub the whole subtrees of all MI variables of left
                for (Variable miVar : this.getMiList(left)) {
                    this.lubWholeTree(miVar, dep);
                }
                break;
            }

            default:
                throw new RuntimeException("SNH");
        }
    }

//  assignArray ********************************************************************

    public void assignArray(Variable left, AbstractCfgNode cfgNode) {
        // set the whole tree of left
        this.setWholeTree(left, DependencySet.create(DependencyLabel.create(cfgNode)));
        // if left is not an array element, set its arrayLabel
        if (!left.isArrayElement()) {
            this.setArrayLabel(left, DependencySet.create(DependencyLabel.create(cfgNode)));
        }
    }

//  defineConstant *****************************************************************

    // sets the dependency of the given constant
    public void defineConstant(Constant c, AbstractCfgNode cfgNode) {
        this.setDep(c, DependencySet.create(DependencyLabel.create(cfgNode)));
    }

//  defineConstantWeak *************************************************************

    // lubs the taint of the given constant
    public void defineConstantWeak(Constant c, AbstractCfgNode cfgNode) {
        this.lubDep(c, DependencySet.create(DependencyLabel.create(cfgNode)));
    }

// getMiList ***********************************************************************

    // returns a list of array elements that are maybe identical
    // to the given array element; only array elements without
    // non-literal indices are returned
    List<Variable> getMiList(Variable var) {
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

//  resetVariables *****************************************************************

    // resets all variables that belong to the given symbol table
    // (by removing their non-default mapping)
    public void resetVariables(SymbolTable symTab) {

        // reset deps
        for (Iterator<Map.Entry<AbstractTacPlace, DependencySet>> iter = this.placeToDep.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<AbstractTacPlace, DependencySet> entry = iter.next();
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

        // reset array labels
        for (Iterator<Map.Entry<Variable, DependencySet>> iter = this.arrayLabels.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Variable, DependencySet> entry = iter.next();
            AbstractTacPlace place = entry.getKey();

            if (!(place != null)) {
                // nothing to do for non-variables (i.e., constants)
                // Note: Is this possible here at all as of the Map contents data types?
                continue;
            }

            Variable var = (Variable) place;
            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }
    }

//  resetTemporaries ***************************************************************

    // resets all temporaries that belong to the given symbol table
    // (by removing their non-default mapping)
    public void resetTemporaries(SymbolTable symTab) {

        // reset deps
        for (Iterator<Map.Entry<AbstractTacPlace, DependencySet>> iter = this.placeToDep.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<AbstractTacPlace, DependencySet> entry = iter.next();
            AbstractTacPlace place = entry.getKey();

            if (!(place instanceof Variable)) {
                // nothing to do for non-variables (i.e., constants)
                continue;
            }

            Variable var = (Variable) place;
            if (!var.isTemp()) {
                // nothing to do for non-temporaries
                continue;
            }

            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }

        // reset array labels
        for (Iterator<Map.Entry<Variable, DependencySet>>  iter = this.arrayLabels.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Variable, DependencySet> entry = iter.next();
            AbstractTacPlace place = entry.getKey();

            if (!(place != null)) {
                // nothing to do for non-variables (i.e., constants)
                // Note: Is this possible here at all as of the Map contents data types?
                continue;
            }

            Variable var = (Variable) place;
            if (!var.isTemp()) {
                // nothing to do for non-temporaries
                continue;
            }

            if (var.belongsTo(symTab)) {
                iter.remove();
            }
        }
    }

//  setFormal **********************************************************************

    // sets the dependency and array label of the given formal
    public void setFormal(TacFormalParameter formalParam, AbstractCfgNode cfgNode) {
        Variable formalVar = formalParam.getVariable();
        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(formalVar);

        Set<Variable> mayAliases = Collections.emptySet();

        this.assign(formalVar, mustAliases, mayAliases, cfgNode);
    }

//  setShadow **********************************************************************

    // sets a shadow's dependency/label to the dependency/label of its original
    public void setShadow(Variable shadow, Variable original) {
        // TODO: might be causing problems in some cases?
        this.setDep(shadow, this.getDep(original));
        this.setArrayLabel(shadow, this.getArrayLabel(original));
    }

//  copyGlobalLike *****************************************************************

    // copies the non-default dependency/label mappings for "global-like" places
    // from interIn (i.e., global variables, superglobal variables, and
    // constants)
    public void copyGlobalLike(DependencyLatticeElement interIn) {

        // dependency mappings
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : interIn.getPlaceToDep().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origDep = entry.getValue();

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
                this.setDep(origPlace, origDep);
            }
        }

        // array label mappings
        for (Map.Entry<Variable, DependencySet> entry : interIn.getArrayLabels().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origArrayLabel = entry.getValue();

            // decide whether to copy this place
            boolean copyMe = false;
            if (origPlace != null) {
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

//  copyGlobalLike *****************************************************************

    // analogous to the one-parameter copyGlobalLike, but also takes into account
    // MOD-info; currently, constants are not considered as global-like for this
    public void copyGlobalLike(DependencyLatticeElement interIn, DependencyLatticeElement intraIn,
                               Set<AbstractTacPlace> calleeMod) {

        // dependency mappings
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : interIn.getPlaceToDep().entrySet()) {
            AbstractTacPlace interPlace = entry.getKey();
            DependencySet interDep = entry.getValue();

            // decide whether to copy this place
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
                // ok, we have decided that this place shall be copied;
                // now we have to decide from where we want to copy it;
                // NOTE: here we exclude constants from special treatment
                if (interPlace instanceof Constant || calleeMod.contains(interPlace)) {
                    this.setDep(interPlace, interDep);
                } else {
                    this.setDep(interPlace, intraIn.getDep(interPlace));
                }
            }
        }

        // array label mappings
        for (Map.Entry<Variable, DependencySet> entry : interIn.getArrayLabels().entrySet()) {
            AbstractTacPlace interPlace = entry.getKey();
            DependencySet interArrayLabel = entry.getValue();

            // decide whether to copy this place
            boolean copyMe = false;
            if (interPlace != null) {
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

//  copyMainTemporaries ****************************************************************

    // copies the dependency/label mappings for local temporaries of the main function
    public void copyMainTemporaries(DependencyLatticeElement origElement) {

        // dependency mappings
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : origElement.getPlaceToDep().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origDep = entry.getValue();

            // nothing to do for non-variables, non-main's, and non-temporaries
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

            //System.out.println("setting dependency for " + origVar);
            this.setDep(origVar, origDep);
        }

        // array label mappings
        for (Map.Entry<Variable, DependencySet> entry : origElement.getArrayLabels().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origArrayLabel = entry.getValue();

            // nothing to do for non-variables, non-main's, and non-temporaries
            if (!(origPlace != null)) {
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

//  copyMainVariables **************************************************************

    // copies the dependency/label mappings for all variables of the main function
    public void copyMainVariables(DependencyLatticeElement origElement) {

        // dependency mappings
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : origElement.getPlaceToDep().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origDep = entry.getValue();

            // nothing to do for non-variables and non-main's
            if (!(origPlace instanceof Variable)) {
                continue;
            }
            Variable origVar = (Variable) origPlace;
            SymbolTable symTab = origVar.getSymbolTable();
            if (!symTab.isMain()) {
                continue;
            }

            //System.out.println("setting dependency for " + origVar);
            this.setDep(origVar, origDep);
        }

        // array label mappings
        for (Map.Entry<Variable, DependencySet> entry : origElement.getArrayLabels().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origArrayLabel = entry.getValue();

            // nothing to do for non-variables and non-main's
            if (!(origPlace != null)) {
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

//  copyLocals ********************************************************************

    // copies the non-default dependency/label mappings for local variables from origElement
    public void copyLocals(DependencyLatticeElement origElement) {

        // dependency mappings
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : origElement.getPlaceToDep().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origDep = entry.getValue();

            // nothing to do for non-variables and non-locals
            if (!(origPlace instanceof Variable)) {
                continue;
            }
            Variable origVar = (Variable) origPlace;
            if (!origVar.isLocal()) {
                continue;
            }

            this.setDep(origVar, origDep);
        }

        // array label mappings
        for (Map.Entry<Variable, DependencySet> entry : origElement.getArrayLabels().entrySet()) {
            AbstractTacPlace origPlace = entry.getKey();
            DependencySet origArrayLabel = entry.getValue();

            // nothing to do for non-variables and non-locals
            if (!(origPlace != null)) {
                continue;
            }
            Variable origVar = (Variable) origPlace;
            if (!origVar.isLocal()) {
                continue;
            }

            this.setArrayLabel(origVar, origArrayLabel);
        }
    }

//  setLocal ***********************************************************************

    // sets the dependency/label of the given local variable to the given dependency/label
    public void setLocal(Variable local, DependencySet dep, DependencySet arrayLabel) {
        this.setDep(local, dep);
        this.setArrayLabel(local, arrayLabel);
    }

//  handleReturnValue **************************************************************

    // sets the temporary responsible for catching the return value
    // to the retNode and does NOT reset the return variable
    public void handleReturnValue(CallReturn retNode/*, DependencyLatticeElement calleeIn*/) {

        Variable tempVar = retNode.getTempVar();
        DependencyLabel dependencyLabel = DependencyLabel.create(retNode);
        DependencySet dependencySet = DependencySet.create(dependencyLabel);
        this.setWholeTree(tempVar, dependencySet);
        this.setArrayLabel(tempVar, dependencySet);
    }

//  handleReturnValueUnknown *******************************************************

    public void handleReturnValueUnknown(Variable tempVar, DependencySet dep,
                                         DependencySet arrayLabel, Variable retVar) {

        //System.out.println("callretunknown: " + tempVar + " -> " + dependency);

        this.setWholeTree(tempVar, dep);
        this.setArrayLabel(tempVar, arrayLabel);
        this.placeToDep.remove(retVar);
        this.arrayLabels.remove(retVar);
    }

//  handleReturnValueBuiltin *******************************************************

    public void handleReturnValueBuiltin(Variable tempVar, DependencySet dep,
                                         DependencySet arrayLabel) {

        this.setWholeTree(tempVar, dep);
        this.setArrayLabel(tempVar, arrayLabel);
    }

//  setRetVar **********************************************************************

    // sets the dependency/label of the given return variable to the given dependency/label
    public void setRetVar(Variable retVar, DependencySet dep, DependencySet arrayLabel) {
        this.setDep(retVar, dep);
        this.setArrayLabel(retVar, arrayLabel);
    }

//  equals *************************************************************************

    public boolean equals(Object obj) {
        return this.structureEquals(obj);
    }

//  hashCode ***********************************************************************

    public int hashCode() {
        return this.structureHashCode();
    }

//  ********************************************************************************

    public boolean structureEquals(Object compX) {

        if (compX == this) {
            return true;
        }
        if (!(compX instanceof DependencyLatticeElement)) {
            return false;
        }
        DependencyLatticeElement comp = (DependencyLatticeElement) compX;

        // the dependency and CA maps have to be equal
        return this.placeToDep.equals(comp.getPlaceToDep()) && this.arrayLabels.equals(comp.getArrayLabels());
    }

//  ********************************************************************************

    public int structureHashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.placeToDep.hashCode();
        hashCode = 37 * hashCode + this.arrayLabels.hashCode();
        return hashCode;
    }

//  ********************************************************************************

    public void dump() {
        System.out.println(this.placeToDep);
    }
}