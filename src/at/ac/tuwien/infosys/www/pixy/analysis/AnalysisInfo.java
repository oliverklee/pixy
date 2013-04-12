package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.HashMap;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

//at the moment, this is just a wrapper class around a hashtable;
//EFF: more efficient implementation, e.g. by using references
//located in cfg nodes that point to a container object (which
//contains analysis information objects for different types of
//analyses)
public abstract class AnalysisInfo {

    // CfgNode -> AnalysisNode
    protected HashMap<CfgNode,AnalysisNode> map;

    protected AnalysisInfo() {
        this.map = new HashMap<CfgNode,AnalysisNode>();
    }

    public void add(CfgNode cfgNode, AnalysisNode analysisNode) {
        this.map.put(cfgNode, analysisNode);
    }



    public int size() {
        return this.map.size();
    }

    public HashMap getMap() {
        return this.map;
    }
}