package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacAttributes {

    private int arrayIndex = -1;

    private TacPlace place;
    private Cfg cfg;

    private CfgNode defaultNode;

    private List<TacActualParam> actualParamList;
    private List<TacFormalParam> formalParamList;

    private EncapsList encapsList;

    private boolean isKnownCall;

    TacAttributes() {
    }

// GET *****************************************************************************

    TacPlace getPlace() {
        return this.place;
    }

    Cfg getCfg() {
        return this.cfg;
    }

    int getArrayIndex() {
        return this.arrayIndex;
    }

    CfgNode getDefaultNode() {
        return this.defaultNode;
    }

    List<TacActualParam> getActualParamList() {
        return this.actualParamList;
    }

    List<TacFormalParam> getFormalParamList() {
        return this.formalParamList;
    }

    public EncapsList getEncapsList() {
        return encapsList;
    }

    public boolean isKnownCall() {
        return this.isKnownCall;
    }

// SET *****************************************************************************

    void setPlace(TacPlace place) {
        this.place = place;
    }

    void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    void setCfg(Cfg cfg) {
        this.cfg = cfg;
    }

    void setDefaultNode(CfgNode defaultNode) {
        this.defaultNode = defaultNode;
    }

    void setActualParamList(List<TacActualParam> actualParamList) {
        this.actualParamList = actualParamList;
    }

    void setFormalParamList(List<TacFormalParam> formalParamList) {
        this.formalParamList = formalParamList;
    }

    void addActualParam(TacActualParam param) {
        this.actualParamList.add(param);
    }

    void addFormalParam(TacFormalParam param) {
        this.formalParamList.add(param);
    }

    public void setEncapsList(EncapsList encapsList) {
        this.encapsList = encapsList;
    }

    public void setIsKnownCall(boolean isKnownCall) {
        this.isKnownCall = isKnownCall;
    }
}