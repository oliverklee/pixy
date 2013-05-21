package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacFunction {
    private String name;
    private ControlFlowGraph controlFlowGraph;    // the CFG's tail MUST be the function's exit node
    private boolean isReference;
    private List<TacFormalParam> params;  // contains TacFormalParam objects
    private Variable retVar;

    private SymbolTable symbolTable;

    // a list of CFG nodes calling this function (Call)
    private List<Call> calledFrom;

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
    TacFunction(String name, ControlFlowGraph controlFlowGraph, Variable retVar, boolean isReference,
                ParseNode parseNode, String className) {

        this.name = name;
        this.controlFlowGraph = controlFlowGraph;
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
        this.calledFrom = new LinkedList<>();
        this.isMain = false;

        // this is necessary, even though we use TacFunction's assignFunction()
        // for assigning functions to controlFlowGraph nodes; the reason is that assignFunction()
        // is called in the final stage of program conversion, but we need function
        // information already during conversion (literals analysis for include
        // file resolution: InterAnalysis);
        // a cleaner solution would be to force the assignment of functions to
        // cfgnodes by requiring this information in each cfgnode's constructor
        this.controlFlowGraph.getTail().setEnclosingFunction(this);
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public String getName() {
        return this.name;
    }

    public ControlFlowGraph getControlFlowGraph() {
        return this.controlFlowGraph;
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

    public List<Call> getCalledFrom() {
        return this.calledFrom;
    }

    public List<Call> getContainedCalls() {
        return this.controlFlowGraph.getContainedCalls();
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
        return this.controlFlowGraph.size() == 2;
    }

    // returns a collection containing this function's locals
    public Collection<Variable> getLocals() {
        return this.symbolTable.getVariablesColl();
    }

    public int size() {
        return this.controlFlowGraph.size();
    }

    public String getFileName() {
        return this.parseNode.getFileName();
    }

    public String getLoc() {
        return this.parseNode.getLoc();
    }

    // returns the line number of the contained controlFlowGraph's head
    public int getLine() {
        return this.controlFlowGraph.getHead().getOrigLineno();
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

    public void addCalledFrom(Call callNode) {
        this.calledFrom.add(callNode);
    }

//  *********************************************************************************
//  OTHER ***************************************************************************
//  *********************************************************************************

    public void assignReversePostOrder() {
        this.controlFlowGraph.assignReversePostOrder();
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TacFunction)) {
            return false;
        }
        TacFunction comp = (TacFunction) obj;
        return this.name.equals(comp.name) && this.className.equals(comp.className);
    }
}