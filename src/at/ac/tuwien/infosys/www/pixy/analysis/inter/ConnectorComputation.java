package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

// based on Florian Martin's PhD thesis;
// output:
// - for every function, the set ECS
// - for every call node, a connector function
public class ConnectorComputation {

    // worklist
    ConnectorWorkList workList;

    // TacFunction -> ECS;
    // ECS stands for "encoded call strings"; ECS(p), where p is a procedure,
    // is the set of all call strings that reach a procedure (i.e., these
    // are all call-strings that are used in the header of this procedure's
    // phi table);
    // if a function has an empty ECS, it means that it is not called
    // from anywhere (or that we are working with kSize == 0);
    // the main procedure has only the empty call string
    Map<TacFunction,ECS> function2ECS;

    // CfgNodeCall -> ConnectorFunction;
    // a connector function maps the context of the caller to the target
    // context in the callee;
    // a call node must never have an empty connector function
    Map<CfgNodeCall,ConnectorFunction> call2ConnectorFunction;

    // useful by-product of the computation;
    // associates every function with a list of contained calls
    Map<TacFunction,List<CfgNodeCall>> containedCalls;

    private CallGraph callGraph;

    private TacFunction mainFunction;

    // call-string length
    int kSize;

    public ConnectorComputation(List functions, TacFunction mainFunction, int kSize) {

        /*
        // LATER: kSize == 0 is not correctly supported yet
        if (kSize == 0) {
            throw new RuntimeException("kSize == 0 is not supported yet");
        }*/

        this.kSize = kSize;
        this.mainFunction = mainFunction;

        // start with empty ECS for each function;
        // by the way, build list with call nodes
        // and the call graph

        List<CfgNodeCall> callNodes = new LinkedList<CfgNodeCall>();
        this.function2ECS = new HashMap<TacFunction,ECS>();
        this.containedCalls = new HashMap<TacFunction,List<CfgNodeCall>>();

        for (Iterator iter = functions.iterator(); iter.hasNext();) {
            TacFunction function = (TacFunction) iter.next();

            this.function2ECS.put(function, new ECS());

            List<CfgNodeCall> calls = function.getContainedCalls();
            callNodes.addAll(calls);

            this.containedCalls.put(function, calls);

        }

        // initialize ECS for main function with empty call string
        CallString emptyCallString = new CallString();
        this.function2ECS.put(mainFunction, new ECS(emptyCallString));

        // initialize worklist
        this.workList = new ConnectorWorkList();
        this.workList.add(new ConnectorWorkListElement(mainFunction, emptyCallString));

        // initialize connector functions
        this.call2ConnectorFunction = new HashMap<CfgNodeCall,ConnectorFunction>();
        for (Iterator iter = callNodes.iterator(); iter.hasNext();) {
            CfgNodeCall callNode = (CfgNodeCall) iter.next();
            this.call2ConnectorFunction.put(callNode, new ConnectorFunction());
        }

    }

    public CallGraph getCallGraph() {
        return this.callGraph;
    }

    // computes the call graph
    private void makeCallGraph() {

        this.callGraph = new CallGraph(this.mainFunction);

        // how we construct the call graph:
        // - enter the calls contained in the main function into a queue
        // - mark the main function as "visited"; if a function is marked
        //   as visited, it means that its contained call nodes have already
        //   been added to the queue
        // - while there is something in the queue:
        //   - remove the next call node from the queue
        //   - draw the appropriate "caller -> callee" part into the call graph
        //   - if the callee has not been visited yet:
        //     - add the callee's contained call nodes to the queue
        //     - mark the callee as visited
        List<CfgNodeCall> processUs = this.containedCalls.get(this.mainFunction);
        Set<TacFunction> visited = new HashSet<TacFunction>();
        visited.add(this.mainFunction);
        while (!processUs.isEmpty()) {
            CfgNodeCall callNode = processUs.remove(0);
            TacFunction caller = callNode.getEnclosingFunction();
            TacFunction callee = callNode.getCallee();
            if (callee != null) {
                callGraph.add(caller, callee, callNode);
                if (!visited.contains(callee)) {
                    processUs.addAll(this.containedCalls.get(callee));
                    visited.add(callee);
                }
            }
        }
    }

    public void compute() {

        while (this.workList.hasNext()) {

            // extract element
            ConnectorWorkListElement element = this.workList.removeNext();
            TacFunction p = element.getFunction();
            CallString gamma = element.getCallString();

            // get position of the current call string  in the current procedure
            ECS ecs_p = (ECS) this.function2ECS.get(p);
            int pos = ecs_p.getPosition(gamma);
            if (pos == -1) {
                throw new RuntimeException("SNH");
            }

            // for all calls in function p...
            for (CfgNodeCall callNode : this.containedCalls.get(p)) {
                TacFunction q = callNode.getCallee();
                if (q == null) {
                    // callee is still unknown
                    continue;
                }
                CallString gamma_2 = gamma.append(callNode, this.kSize);
                ECS ecs_q = (ECS) this.function2ECS.get(q);
                int pos_2 = ecs_q.getPosition(gamma_2);
                if (pos_2 == -1) {

                    // create new "column"
                    pos_2 = ecs_q.append(gamma_2);

                    // expand worklist
                    this.workList.add(new ConnectorWorkListElement(q, gamma_2));

                }

                // expand connector function for this call
                ConnectorFunction conFunc = this.getConFunc(callNode);
                conFunc.add(pos, pos_2);
            }
        }

        // generate the call graph
        this.makeCallGraph();
    }

    public CSContext getTargetContext(CfgNodeCall callNode, int sourcePosition) {

        // retrieve connector function for the given call node
        ConnectorFunction conFunc = this.getConFunc(callNode);

        if (conFunc == null) {
            throw new RuntimeException("SNH: " + callNode.getFunctionNamePlace());
        }

        // query connector function and return result
        return conFunc.apply(sourcePosition);
    }

    // returns a list of ReverseTarget objects;
    // don't call this with exitedFunction == main
    public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, int sourcePosition) {

        if (exitedFunction.isMain()) {
            throw new RuntimeException("SNH");
        }

        List<ReverseTarget> reverseTargets = new LinkedList<ReverseTarget>();

        // determine call nodes to which we have to return
        if (this.kSize == 0) {

            // in this case, we are performing a context-insensitive analysis
            // and have to return to all calls to this function;
            // note that context call strings are always the empty call string
            // here, so there is only one position for each function;
            // i.e., |ECS| = 1 for each function

            // prepare one-element context set
            Set<CSContext> contextSet = new HashSet<CSContext>();
            contextSet.add(new CSContext(0));

            // for each call to this function...
            Set<CfgNodeCall> callNodes = this.callGraph.getCallsTo(exitedFunction);
            //List callNodes = this.containedCalls.get(exitedFunction);
            for (CfgNodeCall callNode : callNodes) {
                reverseTargets.add(new ReverseTarget(callNode, contextSet));
            }

        } else {

            // get the call node at the end of the call string (given by
            // exit node and source position)

            ECS exitedECS = (ECS) this.function2ECS.get(exitedFunction);
            CallString exitedCallString = exitedECS.getCallString(sourcePosition);
            CfgNodeCall returnToMe = exitedCallString.getLast();
            ConnectorFunction returnToMeCF = this.getConFunc(returnToMe);
            Set<CSContext> returnToMePositions = returnToMeCF.reverseApply(sourcePosition);

            reverseTargets.add(new ReverseTarget(returnToMe, returnToMePositions));
        }

        return reverseTargets;
    }

    private ConnectorFunction getConFunc(CfgNodeCall callNode) {
        return this.call2ConnectorFunction.get(callNode);
    }

    public Map<TacFunction,ECS> getFunction2ECS() {
        return this.function2ECS;
    }

    // returns the number of contexts for the given function
    public int getNumContexts(TacFunction f) {
        return this.function2ECS.get(f).size();
    }

    public Map<CfgNodeCall,ConnectorFunction> getCall2ConnectorFunction() {
        return this.call2ConnectorFunction;
    }

    public void stats(boolean verbose) {
        int sumPhiEntries = 0;
        int sumCfgNodes = 0;
        for (Map.Entry<TacFunction,ECS> entry : this.function2ECS.entrySet()) {
            TacFunction function = entry.getKey();
            ECS ecs = entry.getValue();
            int cfgNodes = function.size();
            int phiEntries = (cfgNodes * ecs.size());
            sumPhiEntries += phiEntries;
            sumCfgNodes += cfgNodes;
            if (verbose) {
                System.out.println("function " + function.getName() + ": "
                        + cfgNodes + " cfg nodes, " + ecs.size() + " contexts, => " +
                        phiEntries + " phi entries");
            }
        }
        if (MyOptions.optionV) {
            System.out.println("Total phi entries: " + sumPhiEntries);
            System.out.println("Total cfg nodes: " + sumCfgNodes);
        }
    }

    public String dump() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<TacFunction, ECS> entry : function2ECS.entrySet()) {
            TacFunction function = entry.getKey();
            ECS ecs = entry.getValue();
            if (function.isMain()) {
                // main function is not of interest
                continue;
            }
            if (ecs.isEmpty()) {
                // functions that are not called from anywhere are not of interest
                continue;
            }
            b.append(function.getName());
            b.append(" called by:\n");
            b.append(ecs.dump());
            b.append("\n");
        }
        return b.toString();
    }

}