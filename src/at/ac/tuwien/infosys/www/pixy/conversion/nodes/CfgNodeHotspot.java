package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.Collections;
import java.util.List;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;


// *********************************************************************************
// CfgNodeHotspot ******************************************************************
// *********************************************************************************

// hotspots are only used for JUnit tests
public class CfgNodeHotspot
extends CfgNode {
    
    private Integer hotspotId;
    
//  CONSTRUCTORS *******************************************************************    

    public CfgNodeHotspot(Integer hotspotId) {
        super();
        this.hotspotId = hotspotId;
    }
    
//  GET ****************************************************************************
    
    public Integer getHotspotId() {
        return this.hotspotId;
    }

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }
    
//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        // do nothing
    }

}

