package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public interface InterWorkList {
    void add(CfgNode cfgNode, Context context);

    InterWorkListElement removeNext();

    boolean hasNext();
}