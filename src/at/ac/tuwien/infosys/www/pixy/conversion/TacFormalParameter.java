package at.ac.tuwien.infosys.www.pixy.conversion;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacFormalParameter {
    private Variable variable;
    private boolean isReference;
    private boolean hasDefault;
    private ControlFlowGraph defaultControlFlowGraph;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    TacFormalParameter(Variable variable) {
        this.variable = variable;
        this.isReference = false;
        this.hasDefault = false;
        this.defaultControlFlowGraph = null;
    }

    TacFormalParameter(Variable variable, boolean isReference) {
        this.variable = variable;
        this.isReference = isReference;
        this.hasDefault = false;
        this.defaultControlFlowGraph = null;
    }

    TacFormalParameter(Variable variable, boolean hasDefault, ControlFlowGraph defaultControlFlowGraph) {
        this.variable = variable;
        this.isReference = false;
        this.hasDefault = hasDefault;
        this.defaultControlFlowGraph = defaultControlFlowGraph;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public Variable getVariable() {
        return this.variable;
    }

    public boolean isReference() {
        return this.isReference;
    }

    public boolean hasDefault() {
        return this.hasDefault;
    }

    public ControlFlowGraph getDefaultControlFlowGraph() {
        return this.defaultControlFlowGraph;
    }
}