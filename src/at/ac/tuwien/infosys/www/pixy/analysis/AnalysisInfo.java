package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;

import java.util.HashMap;

/**
 * At the moment, this is just a wrapper class around a hash table.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AnalysisInfo {
    // CfgNode -> AnalysisNode
    protected HashMap<CfgNode, AnalysisNode> map;

    protected AnalysisInfo() {
        this.map = new HashMap<>();
    }

    public void add(CfgNode cfgNode, AnalysisNode analysisNode) {
        this.map.put(cfgNode, analysisNode);
    }

    public int size() {
        return this.map.size();
    }

    public HashMap<CfgNode, AnalysisNode> getMap() {
        return this.map;
    }
}