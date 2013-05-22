package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.HashMap;

/**
 * At the moment, this is just a wrapper class around a hash table.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractAnalysisInformation {
    // CfgNode -> AbstractAnalysisNode
    protected HashMap<AbstractCfgNode, AbstractAnalysisNode> map;

    protected AbstractAnalysisInformation() {
        this.map = new HashMap<>();
    }

    public void add(AbstractCfgNode cfgNode, AbstractAnalysisNode analysisNode) {
        this.map.put(cfgNode, analysisNode);
    }

    public int size() {
        return this.map.size();
    }

    public HashMap<AbstractCfgNode, AbstractAnalysisNode> getMap() {
        return this.map;
    }
}