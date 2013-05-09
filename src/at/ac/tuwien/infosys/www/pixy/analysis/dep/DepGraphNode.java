package at.ac.tuwien.infosys.www.pixy.analysis.dep;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class DepGraphNode {

    public abstract String dotName();

    public abstract String comparableName();

    public abstract String dotNameShort();

    public abstract String dotNameVerbose(boolean isModelled);
}