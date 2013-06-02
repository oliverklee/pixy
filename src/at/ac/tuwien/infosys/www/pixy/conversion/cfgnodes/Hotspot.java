package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * Hotspots are only used for JUnit tests.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
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
        // do nothing
    }
}