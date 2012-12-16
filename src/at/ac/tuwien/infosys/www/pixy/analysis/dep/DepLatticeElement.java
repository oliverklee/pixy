package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallRet;

public class DepLatticeElement 
extends LatticeElement {

    // contains only non-default mappings;
    // does not contain mappings for non-literal array elements, because:
    // the dep value of such elements solely depends on the array label
    // of their root array;
    // also: contains only mappings for Variables and Constants, not for Literals
    private Map<TacPlace,DepSet> placeToDep;
    
    // "array labels";
    // remotely resemble "clean array flags" (caFlags) from XSS taint analysis;
    // gives an upper bound for the label of non-literal array elements;
    // contains only non-default mappings;
    // only defined for non-array-elements
    private Map<Variable,DepSet> arrayLabels;

    // the default lattice element; IT MUST NOT BE USED DIRECTLY BY THE ANALYSIS!
    // can be seen as "grounding", "fall-back" for normal lattice elements
    public static DepLatticeElement DEFAULT;

//  ********************************************************************************    
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  DepLatticeElement() ********************************************************
    
    // a lattice element that doesn't modify the information of the 
    // default lattice element
    public DepLatticeElement() {
        this.placeToDep = new HashMap<TacPlace,DepSet>();
        this.arrayLabels = new HashMap<Variable,DepSet>();
    }

//  DepLatticeElement(DepLatticeElement) ***********************************
    
    // clones the given element
    public DepLatticeElement(DepLatticeElement element) {
        this.placeToDep = 
            new HashMap<TacPlace,DepSet>(element.getPlaceToDep());
        this.arrayLabels = 
            new HashMap<Variable,DepSet>(element.getArrayLabels());
    }

//  cloneMe ************************************************************************
    
    public LatticeElement cloneMe() {
        // uses the cloning constructor
        return new DepLatticeElement(this);
    }

//  DepLatticeElement(<for initDefault>) *****************************************
    
    // constructor for default element (to be called by "initDefault"):
    // basically initializes everything to UNINIT;
    // TODO: the code for this can probably be simplified dramatically ;
    // also, the comments inside this function are partly obsolete
    private DepLatticeElement(
            List<TacPlace> places,
            ConstantsTable constantsTable,
            List functions,
            SymbolTable superSymbolTable,
            Variable memberPlace) {
        
        // initialize base mapping for variables: UNINIT
        // (note: array elements have no explicit array label: their label is
        // that of their top enclosing array)
        this.placeToDep = new HashMap<TacPlace,DepSet>();
        this.arrayLabels = new HashMap<Variable,DepSet>();
        for (TacPlace place : places) {
            
            if ((place instanceof Variable) && 
                    place.getVariable().isArrayElement() && 
                    place.getVariable().hasNonLiteralIndices()) {
                // if this is a non-literal array element:
                // don't add a mapping
            } else {
                this.placeToDep.put(place, DepSet.UNINIT);
            }
            
            // arrayLabels are not defined for variables that are array-elements
            if ((place instanceof Variable) && !(place.getVariable().isArrayElement())) {
                this.arrayLabels.put((Variable) place, DepSet.UNINIT);
            }
        }
        
        // initialize function return values:
        // if a function has no return statement, the return variable is not touched;
        // since the real-world return value is then "NULL", the default mapping
        // for return variables has to have the same properties as the default
        // mapping for NULL (i.e., harmless);
        for (TacPlace place : places) {
            if ((place instanceof Variable) && (place.getVariable().isReturnVariable())) {
                this.placeToDep.put(place, DepSet.UNINIT);
                this.arrayLabels.put((Variable) place, DepSet.UNINIT);
            }
        }
        
        // here you can determine the attributes for the return variable
        // of the unknown function
        /*
        Variable unknownFunctionRetVar = superSymbolTable.getVariable(
                InternalStrings.returnPrefix + InternalStrings.unknownFunctionName);
        this.placeToDep.put(unknownFunctionRetVar, DepSet.UNINIT);
        this.arrayLabels.put(unknownFunctionRetVar, DepSet.UNINIT);
        */

        // here you can determine the attributes for the return variable
        // of method calls [not effective any longer; see DepGraph instead]
        /*
        Variable methodRetVar = superSymbolTable.getVariable(
                InternalStrings.returnPrefix + InternalStrings.unknownMethodName);
        this.placeToDep.put(methodRetVar, DepSet.UNINIT);
        this.arrayLabels.put(methodRetVar, DepSet.UNINIT);
        */
        
        // special member variable
        this.placeToDep.put(memberPlace, DepSet.UNINIT);
        this.arrayLabels.put(memberPlace, DepSet.UNINIT);
        
        // initialize constants
        Map constants = constantsTable.getConstants();
        for (Iterator iter = constants.values().iterator(); iter.hasNext(); ) {
            Constant constant = (Constant) iter.next();
            this.placeToDep.put(constant, DepSet.UNINIT);
        }
        
        // initialize local function variables & parameters to HARMLESS;
        // locals of main function == globals of the program:
        // their initialization depends on the register_globals setting:
        // true: UNINIT
        // false: like other locals
        for (Iterator iter = functions.iterator(); iter.hasNext(); ) {
            TacFunction function = (TacFunction) iter.next();
            
            if (function.isMain()) {
                
                // small hack: untaint $PHP_SELF
                // TODO: this is an artifact from an earlier version of Pixy;
                // during dependence analysis, we cannot "untaint" anything,
                // so this code is quite useless; check this whole function for
                // such useless "untainting" operations, but make sure that
                // you don't accidentally leave some variables unassociated
                // UPDATE: don't do this, as PHP_SELF can also be controlled by an 
                // attacker! example: www.server.com/file.php/"xss
                /*
                Variable phpSelf = function.getSymbolTable().getVariable("$PHP_SELF");
                if (phpSelf != null) {
                    this.placeToDep.put(phpSelf, DepSet.UNINIT);
                    this.arrayLabels.put(phpSelf, DepSet.UNINIT);
                }
                */
                
                if (MyOptions.optionG) {
                    // if register_globals is active, we don't change the conservative 
                    // base mapping (tainted/dirty) for the locals of the main function;
                    // NOTE: this is also quite useless, as most of the rest of this function;
                    // should be cleaned up some day...
                    continue;
                }
            }
            SymbolTable symtab = function.getSymbolTable();
            Map variables = symtab.getVariables();
            for (Iterator varIter = variables.values().iterator(); varIter.hasNext(); ) {
                Variable variable = (Variable) varIter.next();
                
                // array elements are handled along with their root 
                if (variable.isArrayElement()) {
                    continue;
                }
                // init the whole tree below this variable to untainted
                // (note that using setTree wouldn't work here)
                this.initTree(variable, DepSet.UNINIT);
                this.arrayLabels.put(variable, DepSet.UNINIT);
            }
        }
        
        // untaint the whole $_SESSION array
        Variable sess = superSymbolTable.getVariable("$_SESSION");
        this.initTree(sess, DepSet.UNINIT);
        this.arrayLabels.put(sess, DepSet.UNINIT);

        // untaint harmless superglobals;
        // fill this list with harmless superglobals that must be set to
        // harmless;
        List<Variable> harmlessSuperGlobals = new LinkedList<Variable>();

        // NOTE: WHEN ADDING A HARMLESS SUPERGLOBAL HERE, YOU SHOULD ALSO ADD IT TO
        // TacConverter.addSuperGlobalElements()
        
        // case-sensitive, and that's OK
        //addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "PHP_SELF"); // NOT HARMLESS!
        addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "SERVER_NAME");
        addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "HTTP_HOST");
        //addHarmlessServerVar(harmlessSuperGlobals, superSymbolTable, "HTTP_REFERER");
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

        // collection of harmless variables finished, now make them harmless
        for (Iterator iter = harmlessSuperGlobals.iterator(); iter.hasNext(); ) {
            Variable harmlessSuperGlobal = (Variable) iter.next();
            // if it is null, it hasn't been used in the php script
            if (harmlessSuperGlobal != null) {
                this.placeToDep.put(harmlessSuperGlobal, DepSet.UNINIT);
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
            List<TacPlace> places,
            ConstantsTable constantsTable,
            List functions,
            SymbolTable superSymbolTable,
            Variable memberPlace) {

        DepLatticeElement.DEFAULT = 
            new DepLatticeElement(
                    places, constantsTable, functions, superSymbolTable,
                    memberPlace);
    }
    
//  *********************************************************************************    
//  GET *****************************************************************************
//  *********************************************************************************

//  getPlaceToDep *****************************************************************
    
    public Map<TacPlace,DepSet> getPlaceToDep() {
        return this.placeToDep;
    }
    
//  *********************************************************************************
    
    public Map<Variable,DepSet> getArrayLabels() {
        return this.arrayLabels;
    }

//  ********************************************************************************
    
    public DepSet getDep(TacPlace place) {
        return this.getDepFrom(place, this.placeToDep);
    }

//  ********************************************************************************

    // returns the non-default dep for this place if that mapping exists,
    // or the default dep otherwise
    private DepSet getDepFrom(
            TacPlace place, Map<? extends TacPlace,DepSet> readFrom) {

        if (place instanceof Literal) {
            throw new RuntimeException("SNH any longer");
        }
        
        // if we encounter a non-literal array element:
        // dep depends solely on the array label of the enclosing array
        if (place instanceof Variable) {
            Variable var = (Variable) place;
            if (var.isArrayElement() && var.hasNonLiteralIndices()) {
                return getArrayLabel(var.getTopEnclosingArray());
            }
        }

        // return the non-default mapping (if there is one)
        DepSet nonDefaultDep = readFrom.get(place);
        if (nonDefaultDep != null) {
            return nonDefaultDep;
        }
        
        // return the default mapping if there is no non-default one
        DepSet defaultDep = getDefaultDep(place);
        if (defaultDep == null) {
            throw new RuntimeException("SNH: " + place);
        } else {
            return defaultDep;
        }
    }

//  ********************************************************************************
    
    private static DepSet getDefaultDep(TacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return (DepSet) DepLatticeElement.DEFAULT.getPlaceToDep().get(place);
    }
    
//  ********************************************************************************
    
    // returns the non-default dep for the given place;
    // null if there is no non-default dep for it
    private DepSet getNonDefaultDep(TacPlace place) {
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        return this.placeToDep.get(place);
    }

//  ********************************************************************************
    
    // returns the non-default array label for the given variable if that mapping
    // exists, or the default label otherwise;
    public DepSet getArrayLabel(TacPlace place) {
        
        if ((place instanceof Literal) || (place instanceof Constant)) {
            throw new RuntimeException("SNH any longer");
            //return DepSet.HARMLESS;
        }

        Variable var = (Variable) place;
        
        // redirect request to tree root if necessary
        if (var.isArrayElement()) {
            var = var.getTopEnclosingArray();
        }
        
        // returns the default mapping if there is one
        DepSet nonDefaultArrayLabel = getNonDefaultArrayLabel(var);
        if (nonDefaultArrayLabel != null) {
            return nonDefaultArrayLabel;
        }
        
        // return the default mapping if there is no non-default one
        return getDefaultArrayLabel(var);
    }

//  ********************************************************************************
    
    private DepSet getDefaultArrayLabel(Variable var) {
        return DEFAULT.arrayLabels.get(var);
    }
    
//  ********************************************************************************
    
    private DepSet getNonDefaultArrayLabel(Variable var) {
        return this.arrayLabels.get(var);
    }
    
//  ********************************************************************************    
//  SET ****************************************************************************
//  ********************************************************************************
    
//  ***********************************************************************
    
    private void setDep(TacPlace place, DepSet depSet) {
        
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        if (place instanceof Variable && place.getVariable().isMember()) {
            // we don't want to modify the special member variable
            return;
        }

        if (getDefaultDep(place).equals(depSet)) {
            // if the target dep is the default, we simply remove the mapping
            this.placeToDep.remove(place);
        } else {
            this.placeToDep.put(place, depSet);
        }
    }


//  ***********************************************************************
    
    private void lubDep(TacPlace place, DepSet depSet) {
        
        if (place instanceof Literal) {
            throw new RuntimeException("SNH");
        }
        if (place instanceof Variable && place.getVariable().isMember()) {
            // we don't want to modify the special member variable
            return;
        }
        
        DepSet oldDep = this.getDep(place);
        DepSet resultDep = DepSet.lub(oldDep, depSet);
        if (getDefaultDep(place).equals(resultDep)) {
            // if the target dep is the default, we simply remove the mapping
            this.placeToDep.remove(place);
        } else {
            this.placeToDep.put(place, resultDep);
        }
    }

//  ********************************************************************************
    
    // expects a non-array-element!
    private void setArrayLabel(Variable var, DepSet depSet) {
        
        if (var.isArrayElement()) {
            throw new RuntimeException("SNH: " + var);
        }
        if (var.isMember()) {
            // we don't want to modify the special member variable
            return;
        }
        
        // if the target dep is the default, we simply remove the mapping
        if (getDefaultArrayLabel(var).equals(depSet)) {
            this.arrayLabels.remove(var);
        } else {
            this.arrayLabels.put(var, depSet);
        }
    }

//  ********************************************************************************
    
    private void lubArrayLabel(Variable var, DepSet depSet) {
        
        if (var.isMember()) {
            // we don't want to modify the special member variable
            return;
        }
        
        DepSet oldDep = this.getArrayLabel(var);
        DepSet resultDep = DepSet.lub(depSet, oldDep);
        if (resultDep.equals(getDefaultArrayLabel(var))) {
            // if the resulting dep is equal to this variable's default
            // dep, we simply have to remove the default mapping
            this.arrayLabels.remove(var);
        } else {
            // replace the non-default mapping
            this.arrayLabels.put(var, resultDep);
        }
    }

// setWholeTree ********************************************************************
    
    // sets the dep for all literal array elements in the
    // tree sepcified by the given root, INCLUDING THE ROOT
    private void setWholeTree(Variable root, DepSet depSet) {
        
        this.setDep(root, depSet);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.setWholeTree(element, depSet);
        }
    }

//  lubWholeTree ************************************************************************
    
    // analogous to setWholeTree
    private void lubWholeTree(Variable root, DepSet depSet) {
        
        this.lubDep(root, depSet);
        if (!root.isArray()) {
            return;
        }
        for (Variable element : root.getLiteralElements()) {
            this.lubWholeTree(element, depSet);
        }
    }

//  ********************************************************************************    
//  OTHER **************************************************************************
//  ********************************************************************************

//  lub ****************************************************************************
    
    // lubs the given lattice element over <<this>> lattice element 
    public void lub(LatticeElement foreignX) {
        
        DepLatticeElement foreign = (DepLatticeElement) foreignX;
        
        // DEPS ***
        
        // lub over my non-default mappings;
        // this one is necessary because we must not modify a map while
        // iterating over it
        Map<TacPlace,DepSet> newPlaceToDep = 
            new HashMap<TacPlace,DepSet>(this.placeToDep);
        for (Map.Entry<TacPlace,DepSet> myEntry : this.placeToDep.entrySet()) {
            TacPlace myPlace = myEntry.getKey();
            DepSet myDep = myEntry.getValue();
            DepSet foreignDep = foreign.getDep(myPlace);
            newPlaceToDep.put(myPlace, DepSet.lub(myDep, foreignDep));
        }
        this.placeToDep = newPlaceToDep;
        
        // lub the remaining non-default mappings of "foreign" over my
        // default mappings
        Map<TacPlace,DepSet> foreignPlaceToDep = foreign.getPlaceToDep();
        for (Map.Entry<TacPlace,DepSet> foreignEntry : foreignPlaceToDep.entrySet()) {
            TacPlace foreignPlace = foreignEntry.getKey();
            DepSet foreignDep = foreignEntry.getValue();
            
            DepSet myDep = this.getNonDefaultDep(foreignPlace);
            // make sure that we handle my default mappings now
            if (myDep == null) {
                myDep = getDefaultDep(foreignPlace);
                this.placeToDep.put(foreignPlace, DepSet.lub(foreignDep, myDep));
            }
        }

        // cleaning pass: remove defaults
        for (Iterator<Map.Entry<TacPlace, DepSet>> iter = 
            this.placeToDep.entrySet().iterator(); iter.hasNext(); ) {
            
            Map.Entry<TacPlace, DepSet> entry = iter.next();
            TacPlace place = entry.getKey();
            DepSet dep = entry.getValue();
            if (getDefaultDep(place).equals(dep)) {
                iter.remove();
            }
        }

        // ARRAY LABELS ***
        
        // lub over my non-default mappings
        Map<Variable,DepSet> newArrayLabels = new HashMap<Variable,DepSet>(this.arrayLabels);
        for (Map.Entry<Variable,DepSet> myEntry : this.arrayLabels.entrySet()) {
            Variable myVar = myEntry.getKey();
            DepSet myArrayLabel = myEntry.getValue();
            DepSet foreignArrayLabel = foreign.getArrayLabel(myVar);
            newArrayLabels.put(myVar, DepSet.lub(myArrayLabel, foreignArrayLabel));
        }
        this.arrayLabels = newArrayLabels;
        
        // lub the remaining non-default mappings of "foreign" over my
        // default mappings
        Map<Variable,DepSet> foreignArrayLabels = foreign.getArrayLabels();
        for (Map.Entry<Variable,DepSet> foreignEntry : foreignArrayLabels.entrySet()) {
            Variable foreignVar = foreignEntry.getKey();
            DepSet foreignArrayLabel = foreignEntry.getValue();

            // try non-default mapping:
            DepSet myArrayLabel = getNonDefaultArrayLabel(foreignVar);
            if (myArrayLabel == null) {
                // fetch default mapping:
                myArrayLabel = getDefaultArrayLabel(foreignVar);
                this.arrayLabels.put(foreignVar, DepSet.lub(myArrayLabel, foreignArrayLabel));
            }
        }

        // cleaning pass: remove defaults
        for (Iterator<Map.Entry<Variable, DepSet>> iter = 
            this.arrayLabels.entrySet().iterator(); iter.hasNext(); ) {
            
            Map.Entry<Variable,DepSet> entry = iter.next();
            Variable var = entry.getKey();
            DepSet arrayLabel = entry.getValue();
            if (getDefaultArrayLabel(var).equals(arrayLabel)) {
                iter.remove();
            }
        }
    }
    
//  lub (static) *******************************************************************
    
    // returns the lub of the given deps (the first dep might be reused)
    public static DepSet lub(DepSet dep1, DepSet dep2) {
        return DepSet.lub(dep1, dep2);
    }

//  initTree ***********************************************************************
    
    // inits the deps for all literal array elements in the
    // tree specified by the given root;
    // difference to setTree: doesn't call setDep (which tries
    // to reduce defaults, which don't exist during initialization),
    // but modifies placeToDep directly
    void initTree(Variable root,  DepSet dep) {
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
    public void assign(Variable left, 
            Set mustAliases, Set mayAliases, CfgNode cfgNode) {
        
        // dep to be assigned to the left side
        DepSet dep = DepSet.create(Dep.create(cfgNode));

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
        {
            // strong update for must-aliases (including left itself)
            for (Iterator iter = mustAliases.iterator(); iter.hasNext();) {
                Variable mustAlias = (Variable) iter.next();
                
                this.setDep(mustAlias, dep);
                this.setArrayLabel(mustAlias, dep);
            }
            
            // weak update for may-aliases
            for (Iterator iter = mayAliases.iterator(); iter.hasNext();) {
                Variable mayAlias = (Variable) iter.next();
                this.lubDep(mayAlias, dep);
                this.lubArrayLabel(mayAlias, dep);
            }

            break;
        }

        // array, but not an array element 
        case 2:
        {
            // set target.caFlag
            this.setArrayLabel(left, dep);
            
            // no strong overlap here, but: set the whole subtree to resultDep
            this.setWholeTree(left, dep);
            break;
        }
            
        // array element (and maybe an array) without non-literal indices
        case 3:
        {
            // lub target.root.caFlag
            this.lubArrayLabel(left.getTopEnclosingArray(), dep);
            
            // no strong overlap here, but: set the whole subtree to resultDep
            this.setWholeTree(left, dep);
            break;
        }
            
        // array element (and maybe an array) with non-literal indices
        case 4:
        {
            // lub target.root.caFlag
            this.lubArrayLabel(left.getTopEnclosingArray(), dep);

            // "weak overlap for all MI variables of left"
            // here: lub the whole subtrees of all MI variables of left
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

//  assignArray ********************************************************************
    
    public void assignArray(Variable left, CfgNode cfgNode) {
        // set the whole tree of left
        this.setWholeTree(left, DepSet.create(Dep.create(cfgNode)));
        // if left is not an array element, set its arrayLabel
        if (!left.isArrayElement()) {
            this.setArrayLabel(left, DepSet.create(Dep.create(cfgNode)));
        }
    }
    
//  defineConstant *****************************************************************
    
    // sets the dep of the given constant
    public void defineConstant(Constant c, CfgNode cfgNode) {
        this.setDep(c, DepSet.create(Dep.create(cfgNode)));
    }
    
//  defineConstantWeak *************************************************************
    
    // lubs the taint of the given constant
    public void defineConstantWeak(Constant c, CfgNode cfgNode) {
        this.lubDep(c, DepSet.create(Dep.create(cfgNode)));
    }

    
    
    
    
    

    
    
    
    
    
// getMiList ***********************************************************************
    
    // returns a list of array elements that are maybe identical
    // to the given array element; only array elements without
    // non-literal indices are returned
    List getMiList(Variable var) {
        
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
    
//  resetVariables *****************************************************************
    
    // resets all variables that belong to the given symbol table
    // (by removing their non-default mapping)
    public void resetVariables(SymbolTable symTab) {
        
        // reset deps
        for (Iterator iter = this.placeToDep.entrySet().iterator(); iter.hasNext();) {
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
        
        // reset array labels
        for (Iterator iter = this.arrayLabels.entrySet().iterator(); iter.hasNext();) {
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

//  resetTemporaries ***************************************************************
    
    // resets all temporaries that belong to the given symbol table
    // (by removing their non-default mapping)
    public void resetTemporaries(SymbolTable symTab) {
        
        // reset deps
        for (Iterator iter = this.placeToDep.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace place = (TacPlace) entry.getKey();
            
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
        for (Iterator iter = this.arrayLabels.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace place = (TacPlace) entry.getKey();
            
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

    }

//  setFormal **********************************************************************
    
    // sets the dep and array label of the given formal
    public void setFormal(TacFormalParam formalParam, CfgNode cfgNode) {
        Variable formalVar = formalParam.getVariable();
        Set<Variable> mustAliases = new HashSet<Variable>();
        mustAliases.add(formalVar);
        this.assign(formalVar, mustAliases, Collections.emptySet(), cfgNode);
    }

//  setShadow **********************************************************************
    
    // sets a shadow's dep/label to the dep/label of its original
    public void setShadow(Variable shadow, Variable original) {
        // TODO: might be causing problems in some cases?
        this.setDep(shadow, this.getDep(original));
        this.setArrayLabel(shadow, this.getArrayLabel(original));
    }

//  copyGlobalLike *****************************************************************
    
    // copies the non-default dep/label mappings for "global-like" places 
    // from interIn (i.e., global variables, superglobal variables, and
    // constants)
    public void copyGlobalLike(DepLatticeElement interIn) {
        
        // dep mappings
        Map origPlaceToDep = interIn.getPlaceToDep();
        for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origDep = (DepSet) entry.getValue();

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
        Map origArrayLabels = interIn.getArrayLabels();
        for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origArrayLabel = (DepSet) entry.getValue();

            // decide whether to copy this place
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

//  copyGlobalLike *****************************************************************
    
    // analogous to the one-parameter copyGlobalLike, but also takes into account
    // MOD-info; currently, constants are not considered as global-like for this
    public void copyGlobalLike(DepLatticeElement interIn, DepLatticeElement intraIn,
            Set<TacPlace> calleeMod) {
        
        // dep mappings
        Map origPlaceToDep = interIn.getPlaceToDep();
        for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace interPlace = (TacPlace) entry.getKey();
            DepSet interDep = (DepSet) entry.getValue();

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
        Map interArrayLabels = interIn.getArrayLabels();
        for (Iterator iter = interArrayLabels.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace interPlace = (TacPlace) entry.getKey();
            DepSet interArrayLabel = (DepSet) entry.getValue();

            // decide whether to copy this place
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

//  copyMainTemporaries ****************************************************************
    
    // copies the dep/label mappings for local temporaries of the main function
    public void copyMainTemporaries(DepLatticeElement origElement) {
        
        // dep mappings
        Map origPlaceToDep = origElement.getPlaceToDep();
        for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origDep = (DepSet) entry.getValue();
            
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
            
            //System.out.println("setting dep for " + origVar);
            this.setDep(origVar, origDep);
        }
        
        // array label mappings
        Map origArrayLabels = origElement.getArrayLabels();
        for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origArrayLabel = (DepSet) entry.getValue();

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
            
            this.setArrayLabel(origVar, origArrayLabel);
        }
    }

//  copyMainVariables **************************************************************
    
    // copies the dep/label mappings for all variables of the main function
    public void copyMainVariables(DepLatticeElement origElement) {
        
        // dep mappings
        Map origPlaceToDep = origElement.getPlaceToDep();
        for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origDep = (DepSet) entry.getValue();
            
            // nothing to do for non-variables and non-main's
            if (!(origPlace instanceof Variable)) {
                continue;
            }
            Variable origVar = (Variable) origPlace;
            SymbolTable symTab = origVar.getSymbolTable();
            if (!symTab.isMain()) {
                continue;
            }
            
            //System.out.println("setting dep for " + origVar);
            this.setDep(origVar, origDep);
        }
        
        // array label mappings
        Map origArrayLabels = origElement.getArrayLabels();
        for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origArrayLabel = (DepSet) entry.getValue();

            // nothing to do for non-variables and non-main's
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

//  copyLocals ********************************************************************
    
    // copies the non-default dep/label mappings for local variables from origElement
    public void copyLocals(DepLatticeElement origElement) {
        
        // dep mappings
        Map origPlaceToDep = origElement.getPlaceToDep();
        for (Iterator iter = origPlaceToDep.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origDep = (DepSet) entry.getValue();

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
        Map origArrayLabels = origElement.getArrayLabels();
        for (Iterator iter = origArrayLabels.entrySet().iterator(); iter.hasNext();) {
            
            Map.Entry entry = (Map.Entry) iter.next();
            TacPlace origPlace = (TacPlace) entry.getKey();
            DepSet origArrayLabel = (DepSet) entry.getValue();

            // nothing to do for non-variables and non-locals
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

//  setLocal ***********************************************************************
    
    // sets the dep/label of the given local variable to the given dep/label
    public void setLocal(Variable local, DepSet dep, DepSet arrayLabel) {
        this.setDep(local, dep);
        this.setArrayLabel(local, arrayLabel);
    }

//  handleReturnValue **************************************************************

    // sets the temporary responsible for catching the return value 
    // to the retNode and does NOT reset the return variable
    public void handleReturnValue(CfgNodeCallRet retNode/*, DepLatticeElement calleeIn*/) {

        Variable tempVar = retNode.getTempVar();
        Dep dep = Dep.create(retNode);
        DepSet depSet = DepSet.create(dep);
        this.setWholeTree(tempVar, depSet);
        this.setArrayLabel(tempVar, depSet);
        
        // old technique:
        // sets the temporary responsible for catching the return value 
        // to the given dep/label of the return variable and resets the return variable
        /*
        Variable tempVar = retNode.getTempVar();
        DepSet dep = calleeIn.getDep(retNode.getRetVar());
        DepSet arrayLabel = calleeIn.getArrayLabel(retNode.getRetVar());
        Variable retVar = retNode.getRetVar();

        // LATER: this quick hack was necessary for those cases where a function
        // does not return anything; without these checks, the temporary
        // would be assigned "uninit", which is bad (leads to false positives if
        // the return value of this function is used)
        
        CfgNode tail = retNode.getCallPrepNode().getCallee().getCfg().getTail();
        if (!(tail instanceof CfgNodeExit)) {
            System.out.println("SNH");
            System.out.println(tail.getClass());
            System.out.println(retNode.getFileName());
            System.out.println(retNode.getOrigLineno());
            throw new RuntimeException();
        }

        Set<Dep> depSet = new HashSet<Dep>();
        for (Dep contained : dep.getDepSet()) {
            if (contained == Dep.UNINIT) {
                //taintSet.add(Dep.create(new CfgNodeEmpty(retNode.getParseNode())));
                depSet.add(Dep.create(tail));
            } else {
                depSet.add(contained);
            }
        }
        dep = DepSet.create(depSet);

        depSet = new HashSet<Dep>();
        for (Dep contained : arrayLabel.getDepSet()) {
            if (contained == Dep.UNINIT) {
                //taintSet.add(Dep.create(new CfgNodeEmpty(retNode.getParseNode())));
                depSet.add(Dep.create(tail));
            } else {
                depSet.add(contained);
            }
        }
        arrayLabel = DepSet.create(depSet);

        // ****** end of quick hack
        
        this.setWholeTree(tempVar, dep);
        this.setArrayLabel(tempVar, arrayLabel);
        this.placeToDep.remove(retVar);
        this.arrayLabels.remove(retVar);
        */
    }

//  handleReturnValueUnknown *******************************************************

    public void handleReturnValueUnknown(Variable tempVar, DepSet dep, 
            DepSet arrayLabel, Variable retVar) {

        //System.out.println("callretunknown: " + tempVar + " -> " + dep);
        
        this.setWholeTree(tempVar, dep);
        this.setArrayLabel(tempVar, arrayLabel);
        this.placeToDep.remove(retVar);
        this.arrayLabels.remove(retVar);
    }

//  handleReturnValueBuiltin *******************************************************
    
    public void handleReturnValueBuiltin(Variable tempVar, DepSet dep, 
            DepSet arrayLabel) {
        
        this.setWholeTree(tempVar, dep);
        this.setArrayLabel(tempVar, arrayLabel);
    }
    
//  setRetVar **********************************************************************
    
    // sets the dep/label of the given return variable to the given dep/label
    public void setRetVar(Variable retVar, DepSet dep, DepSet arrayLabel) {
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
        if (!(compX instanceof DepLatticeElement)) {
            return false;
        }
        DepLatticeElement comp = (DepLatticeElement) compX;

        // the dep and CA maps have to be equal
        if (!this.placeToDep.equals(comp.getPlaceToDep())) {
            return false;
        }
        if (!this.arrayLabels.equals(comp.getArrayLabels())) {
            return false;
        }
        
        return true;
    }

//  ********************************************************************************
    
    public int structureHashCode() {
        int hashCode = 17;
        hashCode = 37*hashCode + this.placeToDep.hashCode();
        hashCode = 37*hashCode + this.arrayLabels.hashCode();
        return hashCode;
    }
    
//  ********************************************************************************
    
    public void dump() {
        System.out.println(this.placeToDep);
    }
    


}
