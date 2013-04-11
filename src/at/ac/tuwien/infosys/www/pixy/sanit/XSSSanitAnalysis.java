package at.ac.tuwien.infosys.www.pixy.sanit;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.VulnInfo;
import at.ac.tuwien.infosys.www.pixy.XSSAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallBuiltin;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeEcho;

// XSS detection
public class XSSSanitAnalysis
extends SanitAnalysis {

//  ********************************************************************************

    public XSSSanitAnalysis(DepAnalysis depAnalysis) {
        super("xss", depAnalysis, FSAAutomaton.getUndesiredXSSTest());
    }

//  ********************************************************************************

    public List<Integer> detectVulns() {
        return detectVulns(new XSSAnalysis(this.depAnalysis));
    }

    public VulnInfo detectAlternative() {
        throw new RuntimeException("not yet");
    }

//  ********************************************************************************

    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected void checkForSink(CfgNode cfgNodeX, TacFunction traversedFunction,
            List<Sink> sinks) {

        if (cfgNodeX instanceof CfgNodeEcho) {

            // echo() or print()
            CfgNodeEcho cfgNode = (CfgNodeEcho) cfgNodeX;

            // create sink object for this node
            Sink sink = new Sink(cfgNode, traversedFunction);
            sink.addSensitivePlace(cfgNode.getPlace());

            // add it to the list of sensitive sinks
            sinks.add(sink);

        } else if (cfgNodeX instanceof CfgNodeCallBuiltin) {

            // builtin function sinks

            CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) cfgNodeX;
            String functionName = cfgNode.getFunctionName();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);

        } else if (cfgNodeX instanceof CfgNodeCallPrep) {

            CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
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
    private void checkForSinkHelper(String functionName, CfgNode cfgNode,
            List<TacActualParam> paramList, TacFunction traversedFunction, List<Sink> sinks) {

        if (this.dci.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            Set<Integer> indexList = this.dci.getSinks().get(functionName);
            if (indexList == null) {
                // special treatment is necessary here
                if (functionName.equals("printf"))  {
                    // none of the arguments to printf must be tainted
                    for (Iterator iter = paramList.iterator(); iter.hasNext();) {
                        TacActualParam param = (TacActualParam) iter.next();
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