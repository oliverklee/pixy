package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNodeCall;

import java.util.List;

/**
 * Functional or call-string analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AnalysisType {
    protected InterAnalysis enclosedAnalysis;

    // returns the context to which interprocedural propagation shall
    // be conducted (used at call nodes)
    public abstract Context getPropagationContext(CfgNodeCall callNode, Context context);

    // returns a set of ReverseTarget objects to which interprocedural
    // propagation shall be conducted (used at exit nodes)
    public abstract List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, Context contextX);

    // sets the enclosed analysis
    public void setAnalysis(InterAnalysis enclosedAnalysis) {
        this.enclosedAnalysis = enclosedAnalysis;
    }

    // creates an appropriate AnalysisNode
    public abstract InterAnalysisNode makeAnalysisNode(CfgNode cfgNode, TransferFunction tf);

    // use function summaries?
    public abstract boolean useSummaries();

    public abstract Context initContext(InterAnalysis analysis);
}