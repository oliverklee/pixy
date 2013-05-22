package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.List;

/**
 * Functional or call-string analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractAnalysisType {
    protected AbstractInterproceduralAnalysis enclosedAnalysis;

    // returns the context to which interprocedural propagation shall
    // be conducted (used at call nodes)
    public abstract AbstractContext getPropagationContext(Call callNode, AbstractContext context);

    // returns a set of ReverseTarget objects to which interprocedural
    // propagation shall be conducted (used at exit nodes)
    public abstract List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, AbstractContext contextX);

    // sets the enclosed analysis
    public void setAnalysis(AbstractInterproceduralAnalysis enclosedAnalysis) {
        this.enclosedAnalysis = enclosedAnalysis;
    }

    // creates an appropriate AbstractAnalysisNode
    public abstract AbstractInterproceduralAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, AbstractTransferFunction tf);

    // use function summaries?
    public abstract boolean useSummaries();

    public abstract AbstractContext initContext(AbstractInterproceduralAnalysis analysis);
}