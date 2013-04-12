package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

public class TacFunction {

    private String name;
    private Cfg cfg;    // the CFG's tail MUST be the function's exit node
    private boolean isReference;
    private List<TacFormalParam> params;  // contains TacFormalParam objects
    private Variable retVar;

    private SymbolTable symbolTable;

    // a list of CFG nodes calling this function (CfgNodeCall)
    private List<CfgNodeCall> calledFrom;

    // is this the main function?
    private boolean isMain;

    // name of the enclosing class, if this is a method;
    // the empty string otherwise
    private String className;
    // is this the constructor of the above class?
    private boolean isConstructor;

    private ParseNode parseNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // DON'T FORGET TO SET THE PARAMETERS with setParams()!
    TacFunction(String name, Cfg cfg, Variable retVar, boolean isReference,
            ParseNode parseNode, String className) {

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
        this.calledFrom = new LinkedList<CfgNodeCall>();
        this.isMain = false;

        // this is necessary, even though we use TacFunction's assignFunction()
        // for assigning functions to cfg nodes; the reason is that assignFunction()
        // is called in the final stage of program conversion, but we need function
        // information already during conversion (literals analysis for include
        // file resolution: InterAnalysis);
        // a cleaner solution would be to force the assignment of functions to
        // cfgnodes by requiring this information in each cfgnode's constructor
        this.cfg.getTail().setEnclosingFunction(this);
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public String getName() {
        return this.name;
    }

    public Cfg getCfg() {
        return this.cfg;
    }

    public boolean isReference() {
        return this.isReference;
    }

    public List<TacFormalParam> getParams() {
        return this.params;
    }

    public TacFormalParam getParam(int index) {
        return this.params.get(index);
    }

    public Variable getRetVar() {
        return this.retVar;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    // returns the function's local variable with the given name
    Variable getVariable(String varName) {
        return this.symbolTable.getVariable(varName);
    }

    public List<CfgNodeCall> getCalledFrom() {
        return this.calledFrom;
    }

    public List<CfgNodeCall> getContainedCalls() {
        return this.cfg.getContainedCalls();
    }

    public boolean isMain() {
        return this.isMain;
    }

    public boolean isConstructor() {
        return this.isConstructor;
    }

    // returns true if this function's CFG has only 2 nodes (entry and exit
    // node), and false otherwise
    boolean isEmpty() {
        if (this.cfg.size() == 2) {
            return true;
        } else {
            return false;
        }
    }

    // returns a collection containing this function's locals
    public Collection getLocals() {
        return this.symbolTable.getVariablesColl();
    }

    public int size() {
        return this.cfg.size();
    }

    public String getFileName() {
        return this.parseNode.getFileName();
    }

    public String getLoc() {
        return this.parseNode.getLoc();
    }

    // returns the line number of the contained cfg's head
    public int getLine() {
        return this.cfg.getHead().getOrigLineno();
    }

    public String getClassName() {
        return this.className;
    }

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    // expects a List containing TacFormalParam objects
    void setParams(List<TacFormalParam> params) {
        this.params = params;
    }

    void setIsMain(boolean isMain) {
        this.isMain = isMain;
    }

    public void addCalledFrom(CfgNodeCall callNode) {
        this.calledFrom.add(callNode);
    }

//  *********************************************************************************
//  OTHER ***************************************************************************
//  *********************************************************************************

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
}