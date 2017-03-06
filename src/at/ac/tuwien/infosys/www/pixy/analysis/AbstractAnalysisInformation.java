package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.HashMap;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public abstract class AbstractAnalysisInformation {

	protected HashMap<AbstractCfgNode, AbstractAnalysisNode> map;

	protected AbstractAnalysisInformation() {
		this.map = new HashMap<AbstractCfgNode, AbstractAnalysisNode>();
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
