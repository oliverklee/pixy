package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallString;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.EncodedCallStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class ConnectorComputation {

	ConnectorWorkList workList;
	Map<TacFunction, EncodedCallStrings> function2ECS;
	Map<Call, ConnectorFunction> call2ConnectorFunction;
	Map<TacFunction, List<Call>> containedCalls;
	private CallGraph callGraph;
	private TacFunction mainFunction;
	int kSize;

	public ConnectorComputation(List<?> functions, TacFunction mainFunction, int kSize) {
		this.kSize = kSize;
		this.mainFunction = mainFunction;

		List<Call> callNodes = new LinkedList<Call>();
		this.function2ECS = new HashMap<TacFunction, EncodedCallStrings>();
		this.containedCalls = new HashMap<TacFunction, List<Call>>();

		for (Iterator<?> iter = functions.iterator(); iter.hasNext();) {
			TacFunction function = (TacFunction) iter.next();

			this.function2ECS.put(function, new EncodedCallStrings());

			List<Call> calls = function.getContainedCalls();
			callNodes.addAll(calls);

			this.containedCalls.put(function, calls);

		}

		CallString emptyCallString = new CallString();
		this.function2ECS.put(mainFunction, new EncodedCallStrings(emptyCallString));

		this.workList = new ConnectorWorkList();
		this.workList.add(new ConnectorWorkListElement(mainFunction, emptyCallString));

		this.call2ConnectorFunction = new HashMap<Call, ConnectorFunction>();
		for (Iterator<Call> iter = callNodes.iterator(); iter.hasNext();) {
			Call callNode = (Call) iter.next();
			this.call2ConnectorFunction.put(callNode, new ConnectorFunction());
		}
	}

	public CallGraph getCallGraph() {
		return this.callGraph;
	}

	private void makeCallGraph() {

		this.callGraph = new CallGraph(this.mainFunction);
		List<Call> processUs = this.containedCalls.get(this.mainFunction);
		Set<TacFunction> visited = new HashSet<TacFunction>();
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
			ConnectorWorkListElement element = this.workList.removeNext();
			TacFunction p = element.getFunction();
			CallString gamma = element.getCallString();
			EncodedCallStrings ecs_p = (EncodedCallStrings) this.function2ECS.get(p);
			int pos = ecs_p.getPosition(gamma);
			if (pos == -1) {
				throw new RuntimeException("SNH");
			}

			for (Call callNode : this.containedCalls.get(p)) {
				TacFunction q = callNode.getCallee();
				if (q == null) {
					continue;
				}
				CallString gamma_2 = gamma.append(callNode, this.kSize);
				EncodedCallStrings ecs_q = (EncodedCallStrings) this.function2ECS.get(q);
				int pos_2 = ecs_q.getPosition(gamma_2);
				if (pos_2 == -1) {
					pos_2 = ecs_q.append(gamma_2);
					this.workList.add(new ConnectorWorkListElement(q, gamma_2));
				}

				ConnectorFunction conFunc = this.getConFunc(callNode);
				conFunc.add(pos, pos_2);
			}
		}
		this.makeCallGraph();
	}

	public CallStringContext getTargetContext(Call callNode, int sourcePosition) {

		ConnectorFunction conFunc = this.getConFunc(callNode);
		if (conFunc == null) {
			throw new RuntimeException("SNH: " + callNode.getFunctionNamePlace());
		}
		return conFunc.apply(sourcePosition);
	}

	public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, int sourcePosition) {

		if (exitedFunction.isMain()) {
			throw new RuntimeException("SNH");
		}

		List<ReverseTarget> reverseTargets = new LinkedList<ReverseTarget>();

		if (this.kSize == 0) {
			Set<CallStringContext> contextSet = new HashSet<CallStringContext>();
			contextSet.add(new CallStringContext(0));
			Set<Call> callNodes = this.callGraph.getCallsTo(exitedFunction);
			for (Call callNode : callNodes) {
				reverseTargets.add(new ReverseTarget(callNode, contextSet));
			}
		} else {
			EncodedCallStrings exitedECS = (EncodedCallStrings) this.function2ECS.get(exitedFunction);
			CallString exitedCallString = exitedECS.getCallString(sourcePosition);
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

	public int getNumContexts(TacFunction f) {
		return this.function2ECS.get(f).size();
	}

	public Map<Call, ConnectorFunction> getCall2ConnectorFunction() {
		return this.call2ConnectorFunction;
	}

	public void stats(boolean verbose) {
		int sumPhiEntries = 0;
		int sumCfgNodes = 0;
		for (Map.Entry<TacFunction, EncodedCallStrings> entry : this.function2ECS.entrySet()) {
			TacFunction function = entry.getKey();
			EncodedCallStrings ecs = entry.getValue();
			long cfgNodes = function.size();
			long phiEntries = (cfgNodes * ecs.size());
			sumPhiEntries += phiEntries;
			sumCfgNodes += cfgNodes;
			if (verbose) {
				System.out.println("function " + function.getName() + ": " + cfgNodes + " cfg nodes, " + ecs.size()
						+ " contexts, => " + phiEntries + " phi entries");
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
			EncodedCallStrings ecs = entry.getValue();
			if (function.isMain()) {
				continue;
			}
			if (ecs.isEmpty()) {
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
