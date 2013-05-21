package at.ac.tuwien.infosys.www.pixy.conversion;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacFormalParam {
    private Variable variable;
    private boolean isReference;
    private boolean hasDefault;
    private Cfg defaultCfg;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    TacFormalParam(Variable variable) {
        this.variable = variable;
        this.isReference = false;
        this.hasDefault = false;
        this.defaultCfg = null;
    }

    TacFormalParam(Variable variable, boolean isReference) {
        this.variable = variable;
        this.isReference = isReference;
        this.hasDefault = false;
        this.defaultCfg = null;
    }

    TacFormalParam(Variable variable, boolean hasDefault, Cfg defaultCfg) {
        this.variable = variable;
        this.isReference = false;
        this.hasDefault = hasDefault;
        this.defaultCfg = defaultCfg;
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

    public Cfg getDefaultCfg() {
        return this.defaultCfg;
    }
}