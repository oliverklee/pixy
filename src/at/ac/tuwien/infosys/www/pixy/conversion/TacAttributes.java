package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacAttributes {
    private int arrayIndex = -1;

    private AbstractTacPlace place;
    private ControlFlowGraph controlFlowGraph;

    private AbstractCfgNode defaultNode;

    private List<TacActualParameter> actualParamList;
    private List<TacFormalParameter> formalParamList;

    private EncapsList encapsList;

    private boolean isKnownCall;

    TacAttributes() {
    }

// GET *****************************************************************************

    AbstractTacPlace getPlace() {
        return this.place;
    }

    ControlFlowGraph getControlFlowGraph() {
        return this.controlFlowGraph;
    }

    int getArrayIndex() {
        return this.arrayIndex;
    }

    AbstractCfgNode getDefaultNode() {
        return this.defaultNode;
    }

    List<TacActualParameter> getActualParamList() {
        return this.actualParamList;
    }

    List<TacFormalParameter> getFormalParamList() {
        return this.formalParamList;
    }

    public EncapsList getEncapsList() {
        return encapsList;
    }

    public boolean isKnownCall() {
        return this.isKnownCall;
    }

// SET *****************************************************************************

    void setPlace(AbstractTacPlace place) {
        this.place = place;
    }

    void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    void setControlFlowGraph(ControlFlowGraph controlFlowGraph) {
        this.controlFlowGraph = controlFlowGraph;
    }

    void setDefaultNode(AbstractCfgNode defaultNode) {
        this.defaultNode = defaultNode;
    }

    void setActualParamList(List<TacActualParameter> actualParamList) {
        this.actualParamList = actualParamList;
    }

    void setFormalParamList(List<TacFormalParameter> formalParamList) {
        this.formalParamList = formalParamList;
    }

    void addActualParam(TacActualParameter param) {
        this.actualParamList.add(param);
    }

    void addFormalParam(TacFormalParameter param) {
        this.formalParamList.add(param);
    }

    public void setEncapsList(EncapsList encapsList) {
        this.encapsList = encapsList;
    }

    public void setIsKnownCall(boolean isKnownCall) {
        this.isKnownCall = isKnownCall;
    }
}