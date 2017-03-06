package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgExit;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;

public abstract class AbstractInterproceduralAnalysis extends AbstractAnalysis {

	protected AbstractAnalysisType analysisType;

	protected InterproceduralAnalysisInformation interproceduralAnalysisInformation;

	protected TacFunction mainFunction;

	protected AbstractContext mainContext;

	InterproceduralWorklist workList;

	protected void initGeneral(List<TacFunction> functions, TacFunction mainFunction, AbstractAnalysisType analysisType,
			InterproceduralWorklist workList) {

		this.analysisType = analysisType;
		this.analysisType.setAnalysis(this);
		this.functions = functions;

		this.mainFunction = mainFunction;
		ControlFlowGraph mainCfg = this.mainFunction.getCfg();
		AbstractCfgNode mainHead = mainCfg.getHead();

		this.initLattice();

		this.mainContext = this.analysisType.initContext(this);

		this.workList = workList;
		this.workList.add(mainHead, this.mainContext);

		this.interproceduralAnalysisInformation = new InterproceduralAnalysisInformation();
		this.genericAnalysisInfo = interproceduralAnalysisInformation;
		this.initTransferFunctions();

		AbstractInterproceduralAnalysisNode startAnalysisNode = (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation
				.getAnalysisNode(mainHead);
		startAnalysisNode.setPhiValue(this.mainContext, this.startValue);
	}

	void initTransferFunctions() {
		for (TacFunction function : this.functions) {

			List<?> params = function.getParams();

			for (Iterator<?> iter2 = params.iterator(); iter2.hasNext();) {

				TacFormalParameter param = (TacFormalParameter) iter2.next();

				if (param.hasDefault()) {
					ControlFlowGraph defaultCfg = param.getDefaultCfg();
					this.traverseCfg(defaultCfg, function);
				}
			}
		}

		for (TacFunction function : this.functions) {
			this.traverseCfg(function.getCfg(), function);
		}
	}

	public AbstractContext getPropagationContext(Call callNode, AbstractContext context) {
		return this.analysisType.getPropagationContext(callNode, context);
	}

	public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, AbstractContext context) {
		return this.analysisType.getReverseTargets(exitedFunction, context);
	}

	public AbstractTransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
		return this.interproceduralAnalysisInformation.getTransferFunction(cfgNode);
	}

	public InterproceduralAnalysisInformation getInterAnalysisInfo() {
		return this.interproceduralAnalysisInformation;
	}

	public AbstractInterproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
		return (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation.getAnalysisNode(cfgNode);
	}

	protected AbstractAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, AbstractTransferFunction tf) {
		return this.analysisType.makeAnalysisNode(cfgNode, tf);
	}

	protected abstract Boolean evalIf(If ifNode, AbstractLatticeElement inValue);

	protected boolean useSummaries() {
		return this.analysisType.useSummaries();
	}

	@SuppressWarnings("unused")
	private static void debug(String s) {
		int a = 1;
		if (a == 2) {
			System.out.println(s);
		}
	}

	public void analyze() {

		int steps = 0;

		while (this.workList.hasNext()) {

			steps++;
			if (steps % 10000 == 0)
				System.out.println("Steps so far: " + steps);

			InterproceduralWorklistElement element = this.workList.removeNext();

			AbstractCfgNode node = element.getCfgNode();
			AbstractContext context = element.getContext();
			AbstractInterproceduralAnalysisNode analysisNode = (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation
					.getAnalysisNode(node);
			AbstractLatticeElement inValue = analysisNode.getPhiValue(context);
			if (inValue == null) {
				throw new RuntimeException("SNH");
			}
			try {
				if (node instanceof Call) {

					Call callNode = (Call) node;
					TacFunction function = callNode.getCallee();
					CallReturn callRet = (CallReturn) node.getOutEdge(0).getDest();

					if (function == null) {
						propagate(context, inValue, callRet);
						continue;
					}

					ControlFlowGraph functionCfg = function.getCfg();

					AbstractCfgNode exitNode = functionCfg.getTail();
					if (!(exitNode instanceof CfgExit)) {
						throw new RuntimeException("SNH");
					}

					AbstractContext propagationContext = this.getPropagationContext(callNode, context);
					AbstractInterproceduralAnalysisNode exitAnalysisNode = (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation
							.getAnalysisNode(exitNode);
					if (exitAnalysisNode == null) {
						AbstractCfgNode entryNode = functionCfg.getHead();
						propagate(propagationContext, inValue, entryNode);
						continue;
					}

					AbstractLatticeElement exitInValue = exitAnalysisNode.getPhiValue(propagationContext);

					if (this.useSummaries() && exitInValue != null) {
						CfgEdge[] outEdges = callNode.getOutEdges();
						AbstractCfgNode succ = outEdges[0].getDest();
						propagate(context, exitInValue, succ);

					} else {
						if ((this.analysisType instanceof CallStringAnalysis) && exitInValue != null) {
							this.workList.add(exitNode, propagationContext);
						}
						AbstractCfgNode entryNode = functionCfg.getHead();
						propagate(propagationContext, inValue, entryNode);
					}

				} else if (node instanceof CfgExit) {

					CfgExit exitNode = (CfgExit) node;

					TacFunction function = exitNode.getEnclosingFunction();

					if (function == this.mainFunction) {
						continue;
					}

					AbstractLatticeElement outValue = inValue;

					List<?> reverseTargets = this.getReverseTargets(function, context);

					for (Iterator<?> iter = reverseTargets.iterator(); iter.hasNext();) {
						ReverseTarget reverseTarget = (ReverseTarget) iter.next();

						Call callNode = reverseTarget.getCallNode();
						CfgEdge[] outEdges = callNode.getOutEdges();
						CallReturn callRetNode = (CallReturn) outEdges[0].getDest();
						CallPreparation callPrepNode = callRetNode.getCallPrepNode();
						Set<?> contextSet = reverseTarget.getContexts();
						for (Iterator<?> contextIter = contextSet.iterator(); contextIter.hasNext();) {
							AbstractContext targetContext = (AbstractContext) contextIter.next();
							AbstractInterproceduralAnalysisNode callPrepANode = (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation
									.getAnalysisNode(callPrepNode);
							if (callPrepANode.getPhiValue(targetContext) == null) {
							} else {
								propagate(targetContext, outValue, callRetNode);
							}
						}
					}

				} else if (node instanceof If) {

					If ifNode = (If) node;
					AbstractLatticeElement outValue = this.interproceduralAnalysisInformation.getAnalysisNode(node)
							.transfer(inValue);
					CfgEdge[] outEdges = node.getOutEdges();
					Boolean eval = this.evalIf(ifNode, inValue);

					if (eval == null) {
						propagate(context, outValue, outEdges[0].getDest());
						propagate(context, outValue, outEdges[1].getDest());

					} else if (eval == Boolean.TRUE) {
						propagate(context, outValue, outEdges[1].getDest());
					} else {
						propagate(context, outValue, outEdges[0].getDest());
					}

				} else if (node instanceof CallReturn) {

					AbstractInterproceduralAnalysisNode aNode = (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation
							.getAnalysisNode(node);
					AbstractLatticeElement outValue = aNode.transfer(inValue, context);

					CfgEdge[] outEdges = node.getOutEdges();
					for (int i = 0; i < outEdges.length; i++) {
						if (outEdges[i] != null) {

							AbstractCfgNode succ = outEdges[i].getDest();

							propagate(context, outValue, succ);
						}
					}

				} else {

					AbstractLatticeElement outValue;
					outValue = this.interproceduralAnalysisInformation.getAnalysisNode(node).transfer(inValue);

					CfgEdge[] outEdges = node.getOutEdges();
					for (int i = 0; i < outEdges.length; i++) {
						if (outEdges[i] != null) {

							AbstractCfgNode succ = outEdges[i].getDest();
							propagate(context, outValue, succ);
						}
					}
				}

			} catch (RuntimeException ex) {
				System.out.println("File:" + node.getFileName() + ", Line: " + node.getOriginalLineNumber());
				throw ex;
			}
		}

		if (!MyOptions.optionB && MyOptions.optionV) {
			System.out.println("Steps total: " + steps);
		}
	}

	void propagate(AbstractContext context, AbstractLatticeElement value, AbstractCfgNode target) {

		AbstractInterproceduralAnalysisNode analysisNode = (AbstractInterproceduralAnalysisNode) this.interproceduralAnalysisInformation
				.getAnalysisNode(target);

		if (analysisNode == null) {
			System.out.println(Dumper.makeCfgNodeName(target));
			throw new RuntimeException("SNH: " + target.getClass());
		}

		AbstractLatticeElement oldPhiValue = analysisNode.getPhiValue(context);
		if (oldPhiValue == null) {
			oldPhiValue = this.initialValue;
		}
		if (value == oldPhiValue) {
			return;
		}

		AbstractLatticeElement newPhiValue = this.lattice.lub(value, oldPhiValue);
		if (!oldPhiValue.equals(newPhiValue)) {

			analysisNode.setPhiValue(context, newPhiValue);
			this.workList.add(target, context);

		}
	}
}
