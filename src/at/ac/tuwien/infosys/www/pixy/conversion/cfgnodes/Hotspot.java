package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.Collections;
import java.util.List;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Hotspot extends AbstractCfgNode {

	private Integer hotspotId;

	public Hotspot(Integer hotspotId) {
		super();
		this.hotspotId = hotspotId;
	}

	public Integer getHotspotId() {
		return this.hotspotId;
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public void replaceVariable(int index, Variable replacement) {
	}
}