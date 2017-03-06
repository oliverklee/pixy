package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public interface InterproceduralWorklist {

	void add(AbstractCfgNode cfgNode, AbstractContext context);

	InterproceduralWorklistElement removeNext();

	boolean hasNext();

}
