package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public interface InterWorkList {
    void add(AbstractCfgNode cfgNode, Context context);

    InterWorkListElement removeNext();

    boolean hasNext();
}