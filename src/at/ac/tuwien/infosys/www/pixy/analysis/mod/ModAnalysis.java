package at.ac.tuwien.infosys.www.pixy.analysis.mod;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.CallGraphNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

import java.util.*;

/**
 * Computes for every function the set of global variables that this function (and its callees) may modify.
 *
 * It  does NOT consider aliases: This is particularly important for the use of the "global" keyword.
 *
 * If you want reasonable results, don't use this ModAnalysis together with a real alias analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ModAnalysis {
    // this is what we want to compute:
    // a set of modified global-likes for each function
    // (global variables, superglobals, and constants)
    // NOTE: currently, we do NOT support constants for this
    Map<TacFunction, Set<TacPlace>> func2Mod;

//  ********************************************************************************

    public ModAnalysis(List<TacFunction> functions, CallGraph callGraph) {
        this.analyze(functions, callGraph);
    }

//  ********************************************************************************

    public Set<TacPlace> getMod(TacFunction function) {
        return this.func2Mod.get(function);
    }

//  ********************************************************************************

    private void analyze(List<TacFunction> functions, CallGraph callGraph) {

        this.func2Mod = new HashMap<>();

        // intraprocedural analysis

        // - for each function:
        //   - make a simple pass over the function's cfg nodes
        //     (order irrelevant => flow-insensitive!)
        //   - result: for this function, a set of global variables that
        //     can be modified inside this function;
        //     ignore function calls at this stage
        for (TacFunction function : functions) {
            Set<TacPlace> modSet = new HashSet<>();

            for (CfgNode cfgNodeX : function.getControlFlowGraph().dfPreOrder()) {
                this.processNode(cfgNodeX, modSet);
            }

            func2Mod.put(function, modSet);
        }

        // interprocedural analysis
        // - operates on the call graph
        // - the worklist consists of functions
        // - the worklist is initialized with all functions in postorder
        // - while (worklist not empty):
        //   - remove next worklist element: function f
        //   - for all functions c that call this function f:
        //     - compute the union u = mod(c) + mod(f)
        //     - if u != mod(c) [faster: if u > mod(c)]
        //       - set mod(c) = u
        //       - add c to the worklist

        Map<TacFunction, Integer> postorder = callGraph.getPostOrder();

        // initialize worklist
        SortedMap<Integer, TacFunction> worklist = new TreeMap<>();
        for (Map.Entry<TacFunction, Integer> entry : postorder.entrySet()) {
            worklist.put(entry.getValue(), entry.getKey());
        }

        // do the worklist algorithm...
        while (!worklist.isEmpty()) {
            TacFunction f = worklist.remove(worklist.firstKey());
            Collection<CallGraphNode> callers = callGraph.getCallers(f);
            for (CallGraphNode callerNode : callers) {
                TacFunction caller = callerNode.getFunction();
                Set<TacPlace> modF = func2Mod.get(f);
                Set<TacPlace> modCaller = func2Mod.get(caller);
                int modCallerSize = modCaller.size();
                modCaller.addAll(modF);
                if (modCallerSize != modCaller.size()) {
                    worklist.put(postorder.get(caller), caller);
                }
            }
        }
    }

//  ********************************************************************************

    // if the given cfg node has an effect on mod info, this method
    // adjusts the given modSet accordingly (i.e., it adds variables to it)
    private void processNode(CfgNode cfgNodeX, Set<TacPlace> modSet) {

        if (cfgNodeX instanceof CfgNodeBasicBlock) {

            CfgNodeBasicBlock basicBlock = (CfgNodeBasicBlock) cfgNodeX;
            for (CfgNode cfgNode : basicBlock.getContainedNodes()) {
                processNode(cfgNode, modSet);
            }
        } else if (cfgNodeX instanceof CfgNodeAssignSimple) {

            CfgNodeAssignSimple cfgNode = (CfgNodeAssignSimple) cfgNodeX;
            Variable modVar = cfgNode.getLeft();
            if (modVar.isGlobal() || modVar.isSuperGlobal()) {
                this.modify(modVar, modSet);
            }
        } else if (cfgNodeX instanceof CfgNodeAssignUnary) {

            CfgNodeAssignUnary cfgNode = (CfgNodeAssignUnary) cfgNodeX;
            Variable modVar = cfgNode.getLeft();
            if (modVar.isGlobal() || modVar.isSuperGlobal()) {
                this.modify(modVar, modSet);
            }
        } else if (cfgNodeX instanceof CfgNodeAssignBinary) {

            CfgNodeAssignBinary cfgNode = (CfgNodeAssignBinary) cfgNodeX;
            Variable modVar = cfgNode.getLeft();
            if (modVar.isGlobal() || modVar.isSuperGlobal()) {
                this.modify(modVar, modSet);
            }
        } else if (cfgNodeX instanceof CfgNodeAssignArray) {

            CfgNodeAssignArray cfgNode = (CfgNodeAssignArray) cfgNodeX;
            Variable modVar = cfgNode.getLeft();
            if (modVar.isGlobal() || modVar.isSuperGlobal()) {
                this.modify(modVar, modSet);
            }
        } else if (cfgNodeX instanceof CfgNodeAssignRef) {

            CfgNodeAssignRef cfgNode = (CfgNodeAssignRef) cfgNodeX;
            Variable modVar = cfgNode.getLeft();
            if (modVar.isGlobal() || modVar.isSuperGlobal()) {
                this.modify(modVar, modSet);
            }
        } else if (cfgNodeX instanceof CfgNodeUnset) {

            CfgNodeUnset cfgNode = (CfgNodeUnset) cfgNodeX;
            Variable modVar = cfgNode.getOperand();
            if (modVar.isGlobal() || modVar.isSuperGlobal()) {
                this.modify(modVar, modSet);
            }
        } else {
            // no change to mod-info for the remaining cfg nodes
        }
    }

//  ********************************************************************************

    private void modify(Variable modVar, Set<TacPlace> modSet) {
        modSet.add(modVar);
        if (modVar.isArray()) {
            // the whole array subtree is modified as well
            modSet.addAll(modVar.getElementsRecursive());
        }
        if (modVar.isArrayElement()) {
            // by marking the top array as modified,
            // we indirectly mark its array label as modified
            modSet.add(modVar.getTopEnclosingArray());
        }
    }

//  ********************************************************************************

    public String dump() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<TacFunction, Set<TacPlace>> entry : this.func2Mod.entrySet()) {
            b.append("** ");
            b.append(entry.getKey().getName());
            b.append("\n");
            for (TacPlace mod : entry.getValue()) {
                b.append(mod);
                b.append(" ");
            }
            b.append("\n");
        }
        return b.toString();
    }
}