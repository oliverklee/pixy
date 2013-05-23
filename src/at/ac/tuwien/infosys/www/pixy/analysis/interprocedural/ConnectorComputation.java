package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallString;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.EncodedCallStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.*;

/**
 * Based on Florian Martin's PhD thesis.
 *
 * Output:
 * - for every function, the set EncodedCallStrings
 * - for every call node, a connector function
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ConnectorComputation {
    // worklist
    ConnectorWorkList workList;

    // TacFunction -> EncodedCallStrings;
    // ECS stands for "encoded call strings"; ECS(p), where p is a procedure,
    // is the set of all call strings that reach a procedure (i.e., these
    // are all call-strings that are used in the header of this procedure's
    // phi table);
    // if a function has an empty EncodedCallStrings, it means that it is not called
    // from anywhere (or that we are working with kSize == 0);
    // the main procedure has only the empty call string
    Map<TacFunction, EncodedCallStrings> function2ECS;

    // Call -> ConnectorFunction;
    // a connector function maps the context of the caller to the target
    // context in the callee;
    // a call node must never have an empty connector function
    Map<Call, ConnectorFunction> call2ConnectorFunction;

    // useful by-product of the computation;
    // associates every function with a list of contained calls
    Map<TacFunction, List<Call>> containedCalls;

    private CallGraph callGraph;

    private TacFunction mainFunction;

    // call-string length
    int kSize;

    public ConnectorComputation(List<TacFunction> functions, TacFunction mainFunction, int kSize) {
        this.kSize = kSize;
        this.mainFunction = mainFunction;

        // start with empty EncodedCallStrings for each function;
        // by the way, build list with call nodes
        // and the call graph

        List<Call> callNodes = new LinkedList<>();
        this.function2ECS = new HashMap<>();
        this.containedCalls = new HashMap<>();

        for (TacFunction function : functions) {
            this.function2ECS.put(function, new EncodedCallStrings());

            List<Call> calls = function.getContainedCalls();
            callNodes.addAll(calls);

            this.containedCalls.put(function, calls);
        }

        // initialize EncodedCallStrings for main function with empty call string
        CallString emptyCallString = new CallString();
        this.function2ECS.put(mainFunction, new EncodedCallStrings(emptyCallString));

        // initialize worklist
        this.workList = new ConnectorWorkList();
        this.workList.add(new ConnectorWorkListElement(mainFunction, emptyCallString));

        // initialize connector functions
        this.call2ConnectorFunction = new HashMap<>();
        for (Call callNode : callNodes) {
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
        List<Call> processUs = this.containedCalls.get(this.mainFunction);
        Set<TacFunction> visited = new HashSet<>();
        visited.add(this.mainFunction);
        while (!processUs.isEmpty()) {
            Call callNode = processUs.remove(0);
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
            EncodedCallStrings encodedCallStrings_p = this.function2ECS.get(p);
            int pos = encodedCallStrings_p.getPosition(gamma);
            if (pos == -1) {
                throw new RuntimeException("SNH");
            }

            // for all calls in function p...
            for (Call callNode : this.containedCalls.get(p)) {
                TacFunction q = callNode.getCallee();
                if (q == null) {
                    // callee is still unknown
                    continue;
                }
                CallString gamma_2 = gamma.append(callNode, this.kSize);
                EncodedCallStrings encodedCallStrings_q = this.function2ECS.get(q);
                int pos_2 = encodedCallStrings_q.getPosition(gamma_2);
                if (pos_2 == -1) {

                    // create new "column"
                    pos_2 = encodedCallStrings_q.append(gamma_2);

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

    public CallStringContext getTargetContext(Call callNode, int sourcePosition) {

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

        List<ReverseTarget> reverseTargets = new LinkedList<>();

        // determine call nodes to which we have to return
        if (this.kSize == 0) {

            // in this case, we are performing a context-insensitive analysis
            // and have to return to all calls to this function;
            // note that context call strings are always the empty call string
            // here, so there is only one position for each function;
            // i.e., |EncodedCallStrings| = 1 for each function

            // prepare one-element context set
            Set<AbstractContext> contextSet = new HashSet<>();
            contextSet.add(new CallStringContext(0));

            // for each call to this function...
            Set<Call> callNodes = this.callGraph.getCallsTo(exitedFunction);
            for (Call callNode : callNodes) {
                reverseTargets.add(new ReverseTarget(callNode, contextSet));
            }
        } else {

            // get the call node at the end of the call string (given by
            // exit node and source position)
            EncodedCallStrings exitedEncodedCallStrings = this.function2ECS.get(exitedFunction);
            CallString exitedCallString = exitedEncodedCallStrings.getCallString(sourcePosition);
            Call returnToMe = exitedCallString.getLast();
            ConnectorFunction returnToMeCF = this.getConFunc(returnToMe);
            Set<CallStringContext> returnToMePositions = returnToMeCF.reverseApply(sourcePosition);

            reverseTargets.add(new ReverseTarget(returnToMe, returnToMePositions));
        }

        return reverseTargets;
    }

    private ConnectorFunction getConFunc(Call callNode) {
        return this.call2ConnectorFunction.get(callNode);
    }

    public Map<TacFunction, EncodedCallStrings> getFunction2ECS() {
        return this.function2ECS;
    }

    // returns the number of contexts for the given function
    public int getNumContexts(TacFunction f) {
        return this.function2ECS.get(f).size();
    }

    public void stats(boolean verbose) {
        int sumPhiEntries = 0;
        int sumCfgNodes = 0;
        for (Map.Entry<TacFunction, EncodedCallStrings> entry : this.function2ECS.entrySet()) {
            TacFunction function = entry.getKey();
            EncodedCallStrings encodedCallStrings = entry.getValue();
            int cfgNodes = function.size();
            int phiEntries = (cfgNodes * encodedCallStrings.size());
            sumPhiEntries += phiEntries;
            sumCfgNodes += cfgNodes;
            if (verbose) {
                System.out.println("function " + function.getName() + ": "
                    + cfgNodes + " cfg nodes, " + encodedCallStrings.size() + " contexts, => " +
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
        for (Map.Entry<TacFunction, EncodedCallStrings> entry : function2ECS.entrySet()) {
            TacFunction function = entry.getKey();
            EncodedCallStrings encodedCallStrings = entry.getValue();
            if (function.isMain()) {
                // main function is not of interest
                continue;
            }
            if (encodedCallStrings.isEmpty()) {
                // functions that are not called from anywhere are not of interest
                continue;
            }
            b.append(function.getName());
            b.append(" called by:\n");
            b.append(encodedCallStrings.dump());
            b.append("\n");
        }
        return b.toString();
    }
}