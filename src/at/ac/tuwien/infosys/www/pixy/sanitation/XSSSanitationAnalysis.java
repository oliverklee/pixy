package at.ac.tuwien.infosys.www.pixy.sanitation;

import at.ac.tuwien.infosys.www.pixy.VulnerabilityInformation;
import at.ac.tuwien.infosys.www.pixy.XSSAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;

import java.util.List;
import java.util.Set;

/**
 * XSS detection.
 *
 * Note: This class will be instantiated via reflection in GenericTaintAnalysis.createAnalysis. It is registered in
 * MyOptions.DependencyClientInformation.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class XSSSanitationAnalysis extends AbstractSanitationAnalysis {
//  ********************************************************************************

    public XSSSanitationAnalysis(DependencyAnalysis dependencyAnalysis) {
        super("xss", dependencyAnalysis, FSAAutomaton.getUndesiredXSSTest());
    }

//  ********************************************************************************

    public List<Integer> detectVulns() {
        return detectVulns(new XSSAnalysis(this.dependencyAnalysis));
    }

    public VulnerabilityInformation detectAlternative() {
        throw new RuntimeException("not yet");
    }

//  ********************************************************************************

    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected void checkForSink(AbstractCfgNode cfgNodeX, TacFunction traversedFunction,
                                List<Sink> sinks) {

        if (cfgNodeX instanceof Echo) {

            // echo() or print()
            Echo cfgNode = (Echo) cfgNodeX;

            // create sink object for this node
            Sink sink = new Sink(cfgNode, traversedFunction);
            sink.addSensitivePlace(cfgNode.getPlace());

            // add it to the list of sensitive sinks
            sinks.add(sink);
        } else if (cfgNodeX instanceof CallBuiltinFunction) {

            // builtin function sinks

            CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
            String functionName = cfgNode.getFunctionName();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
        } else if (cfgNodeX instanceof CallPreparation) {

            CallPreparation cfgNode = (CallPreparation) cfgNodeX;
            String functionName = cfgNode.getFunctionNamePlace().toString();

            // user-defined custom sinks

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
        } else {
            // not a sink
        }
    }

//  ********************************************************************************

    // LATER: this method looks very similar in all client analyses;
    // possibility to reduce code redundancy
    private void checkForSinkHelper(String functionName, AbstractCfgNode cfgNode,
                                    List<TacActualParameter> paramList, TacFunction traversedFunction, List<Sink> sinks) {

        if (this.dci.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            Set<Integer> indexList = this.dci.getSinks().get(functionName);
            if (indexList == null) {
                // special treatment is necessary here
                if (functionName.equals("printf")) {
                    // none of the arguments to printf must be tainted
                    for (TacActualParameter param : paramList) {
                        sink.addSensitivePlace(param.getPlace());
                    }
                    sinks.add(sink);
                }
            } else {
                for (Integer index : indexList) {
                    if (paramList.size() > index) {
                        sink.addSensitivePlace(paramList.get(index).getPlace());
                        // add this sink to the list of sensitive sinks
                        sinks.add(sink);
                    }
                }
            }
        } else {
            // not a sink
        }
    }
}