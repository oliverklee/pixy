package at.ac.tuwien.infosys.www.pixy.sanit;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.SQLAnalysis;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.VulnInfo;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallBuiltin;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;

// SQL Injection detection (with precise sanitization detection)
public class SQLSanitAnalysis
extends SanitAnalysis {

//  ********************************************************************************

    public SQLSanitAnalysis(DepAnalysis depAnalysis) {
        this(depAnalysis, true);
    }

    public SQLSanitAnalysis(DepAnalysis depAnalysis, boolean getIsTainted) {
        super("sql", depAnalysis, FSAAutomaton.getUndesiredSQLTest());
        this.getIsTainted = getIsTainted;
        if (MyOptions.fsa_home == null) {
            Utils.bail("SQL Sanitization analysis requires FSA Utilities.\n" +
                "Please set a valid path in the config file.");
        }
    }

//  ********************************************************************************

    public List<Integer> detectVulns() {
        return detectVulns(new SQLAnalysis(this.depAnalysis));
    }

    public VulnInfo detectAlternative() {
        throw new RuntimeException("not yet");
    }

//  ********************************************************************************

    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected void checkForSink(CfgNode cfgNodeX, TacFunction traversedFunction,
            List<Sink> sinks) {

        if (cfgNodeX instanceof CfgNodeCallBuiltin) {

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

    private void checkForSinkHelper(String functionName, CfgNode cfgNode,
            List<TacActualParam> paramList, TacFunction traversedFunction, List<Sink> sinks) {

        if (this.dci.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            for (Integer param : this.dci.getSinks().get(functionName)) {
                if (paramList.size() > param) {
                    sink.addSensitivePlace(paramList.get(param).getPlace());
                    // add this sink to the list of sensitive sinks
                    sinks.add(sink);
                }
            }
        } else {
            // not a sink
        }
    }
}