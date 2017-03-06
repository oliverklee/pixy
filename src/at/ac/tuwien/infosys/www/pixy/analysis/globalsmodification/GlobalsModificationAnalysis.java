package at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.CallGraphNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset;

public class GlobalsModificationAnalysis {

	Map<TacFunction, Set<AbstractTacPlace>> func2Mod;

	public GlobalsModificationAnalysis(List<TacFunction> functions, CallGraph callGraph) {
		this.analyze(functions, callGraph);
	}

	public Set<AbstractTacPlace> getMod(TacFunction function) {
		return this.func2Mod.get(function);
	}

	private void analyze(List<TacFunction> functions, CallGraph callGraph) {

		this.func2Mod = new HashMap<TacFunction, Set<AbstractTacPlace>>();

		for (TacFunction function : functions) {

			Set<AbstractTacPlace> modSet = new HashSet<AbstractTacPlace>();

			for (AbstractCfgNode cfgNodeX : function.getCfg().dfPreOrder()) {
				this.processNode(cfgNodeX, modSet);
			}

			func2Mod.put(function, modSet);
		}

		Map<TacFunction, Integer> postorder = callGraph.getPostOrder();

		SortedMap<Integer, TacFunction> worklist = new TreeMap<Integer, TacFunction>();
		for (Map.Entry<TacFunction, Integer> entry : postorder.entrySet()) {
			worklist.put(entry.getValue(), entry.getKey());
		}

		while (!worklist.isEmpty()) {
			TacFunction f = worklist.remove(worklist.firstKey());
			Collection<CallGraphNode> callers = callGraph.getCallers(f);
			for (CallGraphNode callerNode : callers) {
				TacFunction caller = callerNode.getFunction();
				Set<AbstractTacPlace> modF = func2Mod.get(f);
				Set<AbstractTacPlace> modCaller = func2Mod.get(caller);
				int modCallerSize = modCaller.size();
				modCaller.addAll(modF);
				if (modCallerSize != modCaller.size()) {
					worklist.put(postorder.get(caller), caller);
				}
			}
		}
	}

	private void processNode(AbstractCfgNode cfgNodeX, Set<AbstractTacPlace> modSet) {

		if (cfgNodeX instanceof BasicBlock) {

			BasicBlock basicBlock = (BasicBlock) cfgNodeX;
			for (AbstractCfgNode cfgNode : basicBlock.getContainedNodes()) {
				processNode(cfgNode, modSet);
			}

		} else if (cfgNodeX instanceof AssignSimple) {

			AssignSimple cfgNode = (AssignSimple) cfgNodeX;
			Variable modVar = cfgNode.getLeft();
			if (modVar.isGlobal() || modVar.isSuperGlobal()) {
				this.modify(modVar, modSet);
			}

		} else if (cfgNodeX instanceof AssignUnary) {

			AssignUnary cfgNode = (AssignUnary) cfgNodeX;
			Variable modVar = cfgNode.getLeft();
			if (modVar.isGlobal() || modVar.isSuperGlobal()) {
				this.modify(modVar, modSet);
			}

		} else if (cfgNodeX instanceof AssignBinary) {

			AssignBinary cfgNode = (AssignBinary) cfgNodeX;
			Variable modVar = cfgNode.getLeft();
			if (modVar.isGlobal() || modVar.isSuperGlobal()) {
				this.modify(modVar, modSet);
			}

		} else if (cfgNodeX instanceof AssignArray) {

			AssignArray cfgNode = (AssignArray) cfgNodeX;
			Variable modVar = cfgNode.getLeft();
			if (modVar.isGlobal() || modVar.isSuperGlobal()) {
				this.modify(modVar, modSet);
			}

		} else if (cfgNodeX instanceof AssignReference) {

			AssignReference cfgNode = (AssignReference) cfgNodeX;
			Variable modVar = cfgNode.getLeft();
			if (modVar.isGlobal() || modVar.isSuperGlobal()) {
				this.modify(modVar, modSet);
			}

		} else if (cfgNodeX instanceof Unset) {

			Unset cfgNode = (Unset) cfgNodeX;
			Variable modVar = cfgNode.getOperand();
			if (modVar.isGlobal() || modVar.isSuperGlobal()) {
				this.modify(modVar, modSet);
			}

		} else {

		}

	}

	private void modify(Variable modVar, Set<AbstractTacPlace> modSet) {
		modSet.add(modVar);
		if (modVar.isArray()) {
			modSet.addAll(modVar.getElementsRecursive());
		}
		if (modVar.isArrayElement()) {
			modSet.add(modVar.getTopEnclosingArray());
		}
	}

	public String dump() {
		StringBuilder b = new StringBuilder();
		for (Map.Entry<TacFunction, Set<AbstractTacPlace>> entry : this.func2Mod.entrySet()) {
			b.append("** ");
			b.append(entry.getKey().getName());
			b.append("\n");
			for (AbstractTacPlace mod : entry.getValue()) {
				b.append(mod);
				b.append(" ");
			}
			b.append("\n");
		}
		return b.toString();
	}

}
