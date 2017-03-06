package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;

public class CallReturn extends AbstractTransferFunction {

	private AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep;
	private TacFunction caller;
	private TacFunction callee;
	private CallPreparation prepNode;
	private at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn retNode;
	private AliasAnalysis aliasAnalysis;
	private List<List<Variable>> cbrParams;
	private Collection<Variable> localCallerVars;

	public CallReturn(AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep, TacFunction caller,
			TacFunction callee, at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation prepNode,
			at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn retNode, AliasAnalysis aliasAnalysis,
			AbstractLatticeElement bottom) {

		this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
		this.caller = caller;
		this.callee = callee;

		this.cbrParams = prepNode.getCbrParams();

		this.localCallerVars = caller.getLocals();
		this.aliasAnalysis = aliasAnalysis;
		this.prepNode = prepNode;
		this.retNode = retNode;

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {

		LiteralLatticeElement origInfo = (LiteralLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);

		if (origInfo == null) {
			throw new RuntimeException("SNH");
		}

		LiteralLatticeElement calleeIn = (LiteralLatticeElement) inX;

		LiteralLatticeElement outInfo = new LiteralLatticeElement();

		Set<Variable> visitedVars = new HashSet<Variable>();

		outInfo.copyGlobalLike(calleeIn);

		if (this.caller.isMain()) {
			this.handleReturnValue(calleeIn, outInfo);
			return outInfo;
		}

		outInfo.copyLocals(origInfo);

		for (Iterator<Variable> iter = localCallerVars.iterator(); iter.hasNext();) {
			Variable localCallerVar = (Variable) iter.next();

			Variable globalMustAlias = this.aliasAnalysis.getGlobalMustAlias(localCallerVar, this.prepNode);
			if (globalMustAlias == null) {
				continue;
			}
			Variable globalMustAliasShadow = this.callee.getSymbolTable().getGShadow(globalMustAlias);
			if (globalMustAliasShadow == null) {
				System.out.println("call: " + this.caller.getName() + " -> " + this.callee.getName());
				throw new RuntimeException("no shadow for: " + globalMustAlias);
			}
			outInfo.setLocal(localCallerVar, calleeIn.getLiteral(globalMustAliasShadow));
			visitedVars.add(localCallerVar);

		}
		for (Iterator<List<Variable>> iter = this.cbrParams.iterator(); iter.hasNext();) {

			List<?> paramPair = (List<?>) iter.next();
			Iterator<?> paramPairIter = paramPair.iterator();
			Variable actualVar = (Variable) paramPairIter.next();
			Variable formalVar = (Variable) paramPairIter.next();
			Set<?> localMustAliases = this.aliasAnalysis.getLocalMustAliases(actualVar, this.prepNode);
			for (Iterator<?> lmaIter = localMustAliases.iterator(); lmaIter.hasNext();) {
				Variable localMustAlias = (Variable) lmaIter.next();
				if (visitedVars.contains(localMustAlias)) {
					continue;
				}
				Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);
				outInfo.setLocal(localMustAlias, calleeIn.getLiteral(fShadow));
				visitedVars.add(localMustAlias);
			}
		}

		for (Iterator<Variable> iter = localCallerVars.iterator(); iter.hasNext();) {
			Variable localCallerVar = (Variable) iter.next();

			if (visitedVars.contains(localCallerVar)) {
				continue;
			}

			Set<?> globalMayAliases = this.aliasAnalysis.getGlobalMayAliases(localCallerVar, this.prepNode);

			if (globalMayAliases.isEmpty()) {
				continue;
			}

			Literal computedLit = origInfo.getLiteral(localCallerVar);

			for (Iterator<?> gmaIter = globalMayAliases.iterator(); gmaIter.hasNext();) {
				Variable globalMayAlias = (Variable) gmaIter.next();

				Variable globalMayAliasShadow = this.callee.getSymbolTable().getGShadow(globalMayAlias);

				Literal shadowLit = calleeIn.getLiteral(globalMayAliasShadow);

				computedLit = LiteralLatticeElement.lub(computedLit, shadowLit);
			}
			outInfo.setLocal(localCallerVar, computedLit);
		}

		for (Iterator<List<Variable>> iter = this.cbrParams.iterator(); iter.hasNext();) {

			List<?> paramPair = (List<?>) iter.next();
			Iterator<?> paramPairIter = paramPair.iterator();
			Variable actualVar = (Variable) paramPairIter.next();
			Variable formalVar = (Variable) paramPairIter.next();

			Set<?> localMayAliases = this.aliasAnalysis.getLocalMayAliases(actualVar, this.prepNode);

			for (Iterator<?> lmaIter = localMayAliases.iterator(); lmaIter.hasNext();) {
				Variable localMayAlias = (Variable) lmaIter.next();

				if (visitedVars.contains(localMayAlias)) {
					continue;
				}

				Literal localLit = outInfo.getLiteral(localMayAlias);

				Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);

				Literal shadowLit = calleeIn.getLiteral(fShadow);

				Literal newLit = LiteralLatticeElement.lub(localLit, shadowLit);
				outInfo.setLocal(localMayAlias, newLit);
			}
		}

		this.handleReturnValue(calleeIn, outInfo);

		return outInfo;

	}

	private void handleReturnValue(LiteralLatticeElement calleeIn, LiteralLatticeElement outInfo) {
		Literal retLit = calleeIn.getLiteral(this.retNode.getRetVar());
		outInfo.handleReturnValue(this.retNode.getTempVar(), retLit, this.retNode.getRetVar());
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		throw new RuntimeException("SNH");
	}

}