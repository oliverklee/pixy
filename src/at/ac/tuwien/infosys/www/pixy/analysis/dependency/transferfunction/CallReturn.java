package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
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

	private Set<AbstractTacPlace> calleeMod;

	private List<?> cbrParams;

	private Collection<?> localCallerVars;

	public CallReturn(AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep, TacFunction caller,
			TacFunction callee, CallPreparation prepNode,
			at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn retNode, AliasAnalysis aliasAnalysis,
			Set<AbstractTacPlace> calleeMod) {

		this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
		this.caller = caller;
		this.callee = callee;

		this.cbrParams = prepNode.getCbrParams();

		this.localCallerVars = caller.getLocals();

		this.aliasAnalysis = aliasAnalysis;
		this.calleeMod = calleeMod;

		this.prepNode = prepNode;
		this.retNode = retNode;

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {
		DependencyLatticeElement origInfo = (DependencyLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);
		DependencyLatticeElement calleeIn = (DependencyLatticeElement) inX;

		DependencyLatticeElement outInfo = new DependencyLatticeElement();

		Set<Variable> visitedVars = new HashSet<Variable>();

		if (this.calleeMod == null) {
			outInfo.copyGlobalLike(calleeIn);
		} else {
			outInfo.copyGlobalLike(calleeIn, origInfo, this.calleeMod);
		}
		if (this.caller.isMain()) {
			outInfo.copyMainTemporaries(origInfo);
			outInfo.handleReturnValue(this.retNode);
			return outInfo;
		}
		outInfo.copyLocals(origInfo);

		for (Iterator<?> iter = localCallerVars.iterator(); iter.hasNext();) {
			Variable localCallerVar = (Variable) iter.next();

			Variable globalMustAlias = this.aliasAnalysis.getGlobalMustAlias(localCallerVar, this.prepNode);
			if (globalMustAlias == null) {
				continue;
			}

			Variable globalMustAliasShadow = this.callee.getSymbolTable().getGShadow(globalMustAlias);
			if (globalMustAliasShadow == null) {
				System.out.println(globalMustAlias + " is global? " + globalMustAlias.isGlobal());
				throw new RuntimeException("SNH: " + globalMustAlias);
			}

			outInfo.setLocal(localCallerVar, calleeIn.getDep(globalMustAliasShadow),
					calleeIn.getArrayLabel(globalMustAliasShadow));
			visitedVars.add(localCallerVar);

		}

		for (Iterator<?> iter = this.cbrParams.iterator(); iter.hasNext();) {

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

				outInfo.setLocal(localMustAlias, calleeIn.getDep(fShadow), calleeIn.getArrayLabel(fShadow));
				visitedVars.add(localMustAlias);

			}
		}

		for (Iterator<?> iter = localCallerVars.iterator(); iter.hasNext();) {
			Variable localCallerVar = (Variable) iter.next();

			if (visitedVars.contains(localCallerVar)) {
				continue;
			}
			Set<?> globalMayAliases = this.aliasAnalysis.getGlobalMayAliases(localCallerVar, this.prepNode);

			if (globalMayAliases.isEmpty()) {
				continue;
			}

			DependencySet computedTaint = origInfo.getDep(localCallerVar);
			DependencySet computedArrayLabel = origInfo.getArrayLabel(localCallerVar);

			for (Iterator<?> gmaIter = globalMayAliases.iterator(); gmaIter.hasNext();) {
				Variable globalMayAlias = (Variable) gmaIter.next();

				Variable globalMayAliasShadow = this.callee.getSymbolTable().getGShadow(globalMayAlias);

				DependencySet shadowTaint = calleeIn.getDep(globalMayAliasShadow);
				DependencySet shadowArrayLabel = calleeIn.getArrayLabel(globalMayAliasShadow);

				computedTaint = DependencyLatticeElement.lub(computedTaint, shadowTaint);
				computedArrayLabel = DependencyLatticeElement.lub(computedArrayLabel, shadowArrayLabel);
			}
			outInfo.setLocal(localCallerVar, computedTaint, computedArrayLabel);
		}

		for (Iterator<?> iter = this.cbrParams.iterator(); iter.hasNext();) {

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

				DependencySet localTaint = outInfo.getDep(localMayAlias);
				DependencySet localArrayLabel = outInfo.getArrayLabel(localMayAlias);

				Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);

				DependencySet shadowTaint = calleeIn.getDep(fShadow);
				DependencySet shadowArrayLabel = calleeIn.getArrayLabel(fShadow);

				DependencySet newTaint = DependencyLatticeElement.lub(localTaint, shadowTaint);
				DependencySet newArrayLabel = DependencyLatticeElement.lub(localArrayLabel, shadowArrayLabel);
				outInfo.setLocal(localMayAlias, newTaint, newArrayLabel);
			}
		}
		outInfo.handleReturnValue(this.retNode);
		return outInfo;

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		throw new RuntimeException("SNH");
	}

}
