package at.ac.tuwien.infosys.www.pixy.analysis.alias.tf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MayAliasPair;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MustAliasGroup;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;

public class AliasTfCallRet
    extends TransferFunction {

    private InterAnalysisNode analysisNodeAtCallPrep;
    private TacFunction callee;
    private List cbrParams;
    private AliasAnalysis aliasAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public AliasTfCallRet(
        InterAnalysisNode analysisNodeAtCallPrep,
        TacFunction callee, AliasAnalysis aliasAnalysis,
        CfgNodeCallPrep cfgNode) {

        this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
        this.callee = callee;
        this.aliasAnalysis = aliasAnalysis;
        this.cbrParams = cfgNode.getCbrParams();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX, Context context) {

        // lattice element entering the call prep node under the current
        // context (= base for local-info)
        AliasLatticeElement origInfo =
            (AliasLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);
        AliasLatticeElement localInfo = new AliasLatticeElement(origInfo);
        localInfo.removeGlobals();

        // lattice element coming in from the callee (= base for interprocedural info);
        // still contains the callee's locals in the current implementation, so we
        // have to remove them first
        AliasLatticeElement calleeIn = (AliasLatticeElement) inX;
        AliasLatticeElement interInfo = new AliasLatticeElement(calleeIn);
        interInfo.removeLocals();

        // start with empty alias information
        AliasLatticeElement outInfo = new AliasLatticeElement();

        // add aliases between locals from local-info
        outInfo.add(localInfo);

        // add aliases between globals from inter-info
        outInfo.add(interInfo);

        // COMPUTE ALIASES BETWEEN LOCALS AND GLOBALS
        // USING G-SHADOWS AND F-SHADOWS

        // contains groups from orig-info that have been tagged as visited
        Set<MustAliasGroup> visitedGroups = new HashSet<MustAliasGroup>();

        // G-SHADOWS, MUST

        // for each must-alias-group in the orig-info
        for (Iterator iter = origInfo.getMustAliases().getGroups().iterator(); iter.hasNext(); ) {
            MustAliasGroup group = (MustAliasGroup) iter.next();

            // pick an arbitrary global from this group
            Variable someGlobal = group.getArbitraryGlobal();

            // get all locals from this group
            Set groupLocals = group.getLocals();

            // retrieve the global's g-shadow
            Variable gShadow = this.callee.getSymbolTable().getGShadow(someGlobal);

            // if it contains at least one global and at least one local variable
            if (someGlobal != null && !groupLocals.isEmpty()) {

                // pick an arbitrary local
                Variable someLocal = (Variable) groupLocals.iterator().next();

                // mark this group as visited
                visitedGroups.add(group);

                // get (non-trivial) must-alias-group for this g-shadow (=> can also be null!)
                MustAliasGroup gShadowGroup = calleeIn.getMustAliasGroup(gShadow);

                // if such a group exists...
                if (gShadowGroup != null) {

                    // pick an arbitrary global from this group
                    Variable gShadowGlobalMustAlias = gShadowGroup.getArbitraryGlobal();

                    // if there is such a global must-alias of the g-shadow
                    if (gShadowGlobalMustAlias != null) {

                        // in the output-info, merge the local's group with the group
                        // that contains this global must-alias, considering implicit
                        // one-element groups as well
                        outInfo.merge(someLocal, gShadowGlobalMustAlias);
                    }
                }
            }

            // for each global may-alias of the g-shadow...
            // (note: it would be cleaner to move this loop into the above if-branch, since
            // this loop has no final effect if the above branch is not entered;
            // that's because of either:
            // - groupLocals is empty
            // - there is no global variable in this must-alias group, which leads to
            //   the iteration over an empty set here
            Set gShadowGlobalMayAliases = calleeIn.getGlobalMayAliases(gShadow);
            for (Iterator globalIter = gShadowGlobalMayAliases.iterator(); globalIter.hasNext(); ) {
                Variable globalMayAlias = (Variable) globalIter.next();
                outInfo.addMayAliasPairs(groupLocals, globalMayAlias);
            }

        }

        // G-SHADOWS, MAY

        // for each may-alias-pair in the orig-info
        for (Iterator iter = origInfo.getMayAliases().getPairs().iterator(); iter.hasNext(); ) {

            MayAliasPair pair = (MayAliasPair) iter.next();

            Variable[] localGlobal = pair.getLocalGlobal();

            // we are only interested in pairs that contain one local
            // and one global variable
            if (localGlobal == null) {
                continue;
            }

            Variable gShadow = this.callee.getSymbolTable().getGShadow(localGlobal[1]);

            // for all global aliases (must and may) of the g-shadow...
            Set globalAliases = calleeIn.getGlobalAliases(gShadow);
            for (Iterator globalIter = globalAliases.iterator(); globalIter.hasNext(); ) {
                Variable globalAlias = (Variable) globalIter.next();
                MayAliasPair addMePair = new MayAliasPair(globalAlias, localGlobal[0]);
                outInfo.add(addMePair);
            }
        }

        // F-SHADOWS

        // for all cbr-params...
        for (Iterator iter = this.cbrParams.iterator(); iter.hasNext(); ) {

            List paramPair = (List) iter.next();
            Variable actual = (Variable) paramPair.get(0);

            // we are only interested in *local* actual cbr-params
            if (!actual.isLocal()) {
                continue;
            }

            Variable formal = (Variable) paramPair.get(1);
            Variable fShadow = this.callee.getSymbolTable().getFShadow(formal);

            // the f-shadow's global must-aliases
            Set fShadowGlobalMustAliases = calleeIn.getGlobalMustAliases(fShadow);

            // the f-shadow's global may-aliases
            Set fShadowGlobalMayAliases = calleeIn.getGlobalMayAliases(fShadow);

            // the actual param's (non-trivial) must-alias group (=> can also be null!)
            MustAliasGroup actualGroup = origInfo.getMustAliasGroup(actual);

            // if this group is not marked as visited...
            if (!visitedGroups.contains(actualGroup)) {

                // differentiate between implicit (null) and explicit group;
                // LATER: maybe you should do this before coming to the if-branch
                Set<Variable> actualGroupLocals = new HashSet<Variable>();
                if (actualGroup == null) {
                    actualGroupLocals.add(actual);
                } else {
                    // mark the group as visited
                    visitedGroups.add(actualGroup);
                    actualGroupLocals = actualGroup.getLocals();
                }

                // if the f-shadow has at least one global must-alias...
                if (!fShadowGlobalMustAliases.isEmpty()) {

                    // pick one
                    Variable fShadowGlobalMustAlias = (Variable) fShadowGlobalMustAliases.iterator().next();

                    // in the output-info, merge the actual's group with the group
                    // that contains this global must-alias, considering implicit
                    // one-element groups as well
                    outInfo.merge(actual, fShadowGlobalMustAlias);

                }

                // for each global may-alias of the f-shadow...
                for (Iterator iterator = fShadowGlobalMayAliases.iterator(); iterator.hasNext(); ) {
                    Variable fShadowGlobalMayAlias = (Variable) iterator.next();
                    outInfo.addMayAliasPairs(actualGroupLocals, fShadowGlobalMayAlias);
                }
            }

            // for every local may-alias of the actual param...
            for (Iterator localIter = origInfo.getLocalMayAliases(actual).iterator(); localIter.hasNext(); ) {
                Variable actualLocalMayAlias = (Variable) localIter.next();

                // for every global must-alias of the f-shadow...
                for (Iterator innerIter = fShadowGlobalMustAliases.iterator(); innerIter.hasNext(); ) {
                    Variable fShadowGlobalMustAlias = (Variable) innerIter.next();
                    outInfo.add(new MayAliasPair(fShadowGlobalMustAlias, actualLocalMayAlias));
                }

                // for every global may-alias of the f-shadow...
                for (Iterator innerIter = fShadowGlobalMayAliases.iterator(); innerIter.hasNext(); ) {
                    Variable fShadowGlobalMayAlias = (Variable) innerIter.next();
                    outInfo.add(new MayAliasPair(fShadowGlobalMayAlias, actualLocalMayAlias));
                }
            }
        }

        // FINAL

        // eliminate alias pairs that "conflict" with must-alias information
        outInfo.removeConflictingPairs();

        // recycle
        outInfo = (AliasLatticeElement) this.aliasAnalysis.recycle(outInfo);

        return outInfo;
    }

    // just a dummy method in order to make me conform to the interface;
    // the Analysis uses the other transfer method instead
    public LatticeElement transfer(LatticeElement inX) {
        throw new RuntimeException("SNH");
    }
}