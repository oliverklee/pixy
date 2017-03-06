package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MayAliasPair;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MustAliasGroup;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class CallReturn extends AbstractTransferFunction {

	private AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep;
	private TacFunction callee;
	private List<List<Variable>> cbrParams;
	private AliasAnalysis aliasAnalysis;

	public CallReturn(AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep, TacFunction callee,
			AliasAnalysis aliasAnalysis, at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNode) {

		this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
		this.callee = callee;
		this.aliasAnalysis = aliasAnalysis;
		this.cbrParams = cfgNode.getCbrParams();
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {

		AliasLatticeElement origInfo = (AliasLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);
		AliasLatticeElement localInfo = new AliasLatticeElement(origInfo);
		localInfo.removeGlobals();

		AliasLatticeElement calleeIn = (AliasLatticeElement) inX;
		AliasLatticeElement interInfo = new AliasLatticeElement(calleeIn);
		interInfo.removeLocals();

		AliasLatticeElement outInfo = new AliasLatticeElement();

		outInfo.add(localInfo);

		outInfo.add(interInfo);

		Set<MustAliasGroup> visitedGroups = new HashSet<MustAliasGroup>();

		for (Iterator<?> iter = origInfo.getMustAliases().getGroups().iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			Variable someGlobal = group.getArbitraryGlobal();
			Set<?> groupLocals = group.getLocals();
			Variable gShadow = this.callee.getSymbolTable().getGShadow(someGlobal);
			if (someGlobal != null && !groupLocals.isEmpty()) {
				Variable someLocal = (Variable) groupLocals.iterator().next();
				visitedGroups.add(group);
				MustAliasGroup gShadowGroup = calleeIn.getMustAliasGroup(gShadow);
				if (gShadowGroup != null) {
					Variable gShadowGlobalMustAlias = gShadowGroup.getArbitraryGlobal();
					if (gShadowGlobalMustAlias != null) {
						outInfo.merge(someLocal, gShadowGlobalMustAlias);
					}
				}
			}
			Set<?> gShadowGlobalMayAliases = calleeIn.getGlobalMayAliases(gShadow);
			for (Iterator<?> globalIter = gShadowGlobalMayAliases.iterator(); globalIter.hasNext();) {
				Variable globalMayAlias = (Variable) globalIter.next();
				outInfo.addMayAliasPairs(groupLocals, globalMayAlias);
			}
		}
		for (Iterator<MayAliasPair> iter = origInfo.getMayAliases().getPairs().iterator(); iter.hasNext();) {

			MayAliasPair pair = (MayAliasPair) iter.next();

			Variable[] localGlobal = pair.getLocalGlobal();

			if (localGlobal == null) {
				continue;
			}

			Variable gShadow = this.callee.getSymbolTable().getGShadow(localGlobal[1]);

			Set<?> globalAliases = calleeIn.getGlobalAliases(gShadow);
			for (Iterator<?> globalIter = globalAliases.iterator(); globalIter.hasNext();) {
				Variable globalAlias = (Variable) globalIter.next();
				MayAliasPair addMePair = new MayAliasPair(globalAlias, localGlobal[0]);
				outInfo.add(addMePair);
			}
		}

		for (Iterator<?> iter = this.cbrParams.iterator(); iter.hasNext();) {

			List<?> paramPair = (List<?>) iter.next();
			Variable actual = (Variable) paramPair.get(0);

			if (!actual.isLocal()) {
				continue;
			}

			Variable formal = (Variable) paramPair.get(1);
			Variable fShadow = this.callee.getSymbolTable().getFShadow(formal);

			Set<?> fShadowGlobalMustAliases = calleeIn.getGlobalMustAliases(fShadow);

			Set<?> fShadowGlobalMayAliases = calleeIn.getGlobalMayAliases(fShadow);

			MustAliasGroup actualGroup = origInfo.getMustAliasGroup(actual);

			if (!visitedGroups.contains(actualGroup)) {

				Set<Variable> actualGroupLocals = new HashSet<Variable>();
				if (actualGroup == null) {
					actualGroupLocals.add(actual);
				} else {
					visitedGroups.add(actualGroup);
					actualGroupLocals = actualGroup.getLocals();
				}

				if (!fShadowGlobalMustAliases.isEmpty()) {

					Variable fShadowGlobalMustAlias = (Variable) fShadowGlobalMustAliases.iterator().next();
					outInfo.merge(actual, fShadowGlobalMustAlias);

				}

				for (Iterator<?> iterator = fShadowGlobalMayAliases.iterator(); iterator.hasNext();) {
					Variable fShadowGlobalMayAlias = (Variable) iterator.next();
					outInfo.addMayAliasPairs(actualGroupLocals, fShadowGlobalMayAlias);
				}
			}

			for (Iterator<?> localIter = origInfo.getLocalMayAliases(actual).iterator(); localIter.hasNext();) {
				Variable actualLocalMayAlias = (Variable) localIter.next();

				for (Iterator<?> innerIter = fShadowGlobalMustAliases.iterator(); innerIter.hasNext();) {
					Variable fShadowGlobalMustAlias = (Variable) innerIter.next();
					outInfo.add(new MayAliasPair(fShadowGlobalMustAlias, actualLocalMayAlias));
				}

				for (Iterator<?> innerIter = fShadowGlobalMayAliases.iterator(); innerIter.hasNext();) {
					Variable fShadowGlobalMayAlias = (Variable) innerIter.next();
					outInfo.add(new MayAliasPair(fShadowGlobalMayAlias, actualLocalMayAlias));
				}
			}
		}

		outInfo.removeConflictingPairs();

		outInfo = (AliasLatticeElement) this.aliasAnalysis.recycle(outInfo);

		return outInfo;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		throw new RuntimeException("SNH");
	}

}
