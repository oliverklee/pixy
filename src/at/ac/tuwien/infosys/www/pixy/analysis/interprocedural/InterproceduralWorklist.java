package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public interface InterproceduralWorklist {
    void add(AbstractCfgNode cfgNode, Context context);

    InterproceduralWorklistElement removeNext();

    boolean hasNext();
}