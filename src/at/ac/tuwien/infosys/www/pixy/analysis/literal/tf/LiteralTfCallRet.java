package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallRet;

public class LiteralTfCallRet
extends TransferFunction {

    private InterAnalysisNode analysisNodeAtCallPrep;
    private TacFunction caller;
    private TacFunction callee;
    private CfgNodeCallPrep prepNode;
    private CfgNodeCallRet retNode;
    private AliasAnalysis aliasAnalysis;
    
    // call-by-reference parameter pairs
    private List cbrParams;
    
    // local variables of the calling function
    private Collection localCallerVars;
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************     

    public LiteralTfCallRet(
            InterAnalysisNode analysisNodeAtCallPrep,
            TacFunction caller, 
            TacFunction callee,
            CfgNodeCallPrep prepNode,
            CfgNodeCallRet retNode,
            AliasAnalysis aliasAnalysis,
            LatticeElement bottom) {
        
        this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
        this.caller = caller;
        this.callee = callee;
        
        // call-by-reference parameter pairs
        this.cbrParams = prepNode.getCbrParams();
        
        // local variables of the calling function
        this.localCallerVars = caller.getLocals();
        
        this.aliasAnalysis = aliasAnalysis;
        
        this.prepNode = prepNode;
        this.retNode = retNode;
        
    }

// *********************************************************************************    
// OTHER ***************************************************************************
// *********************************************************************************  

    public LatticeElement transfer(LatticeElement inX, Context context) {
        
        // lattice element entering the call prep node under the current
        // context
        LiteralLatticeElement origInfo = 
            (LiteralLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);
        
        if (origInfo == null) {
            throw new RuntimeException("SNH");
        }

        // lattice element coming in from the callee (= base for interprocedural info);
        // still contains the callee's locals
        LiteralLatticeElement calleeIn = (LiteralLatticeElement) inX;

        // start only with default mappings
        LiteralLatticeElement outInfo = new LiteralLatticeElement();

        // contains variables that have been tagged as visited
        Set<Variable> visitedVars = new HashSet<Variable>();
        
        // copy mappings of "global-like" places from calleeIn to outInfo
        // ("global-like": globals, superglobals, and constants)
        outInfo.copyGlobalLike(calleeIn);
        
        
        // LOCAL VARIABLES *************
        
        // no need to do this if the caller is main: 
        // its local variables are global variables
        if (this.caller.isMain()) {
            this.handleReturnValue(calleeIn, outInfo);
            return outInfo;
        }
        
        // initialize local variables with the mappings at call-time
        outInfo.copyLocals(origInfo);
        
        // MUST WITH GLOBALS
        
        // for all local variables of the calling function
        for (Iterator iter = localCallerVars.iterator(); iter.hasNext();) {
            Variable localCallerVar = (Variable) iter.next();
            
            // an arbitrary global must-alias of this local
            Variable globalMustAlias = this.aliasAnalysis.getGlobalMustAlias(localCallerVar, this.prepNode);
            if (globalMustAlias == null) {
                continue;
            }
            
            // the shadow of this global must-alias
            Variable globalMustAliasShadow = this.callee.getSymbolTable().getGShadow(globalMustAlias);
            
            // set & mark
            if (globalMustAliasShadow == null) {
                System.out.println("call: " + this.caller.getName() + " -> " + this.callee.getName());
                throw new RuntimeException("no shadow for: " + globalMustAlias);
            }
            outInfo.setLocal(localCallerVar, calleeIn.getLiteral(globalMustAliasShadow));
            visitedVars.add(localCallerVar);
            
        }
        
        
        // MUST WITH FORMALS
        
        // for each call-by-reference parameter pair
        for (Iterator iter = this.cbrParams.iterator(); iter.hasNext();) {
            
            List paramPair = (List) iter.next();
            Iterator paramPairIter = paramPair.iterator();
            Variable actualVar = (Variable) paramPairIter.next();
            Variable formalVar = (Variable) paramPairIter.next();
            
            // local must-aliases of the actual parameter (including trivial ones,
            // so this set contains at least one element)
            Set localMustAliases = this.aliasAnalysis.getLocalMustAliases(actualVar, this.prepNode);
            
            for (Iterator lmaIter = localMustAliases.iterator(); lmaIter.hasNext();) {
                Variable localMustAlias = (Variable) lmaIter.next();
                
                // no need to handle visited variables again
                if (visitedVars.contains(localMustAlias)) {
                    continue;
                }
                
                // the formal parameter's f-shadow
                Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);
                
                // set & mark
                outInfo.setLocal(localMustAlias, calleeIn.getLiteral(fShadow));
                visitedVars.add(localMustAlias);
                
            }
        }
        
        
        // MAY WITH GLOBALS
        
        // for each local variable that was not visited yet
        for (Iterator iter = localCallerVars.iterator(); iter.hasNext();) {
            Variable localCallerVar = (Variable) iter.next();
            
            if (visitedVars.contains(localCallerVar)) {
                continue;
            }
            
            // global may-aliases of this variable (at the time of call)
            Set globalMayAliases = this.aliasAnalysis.getGlobalMayAliases(localCallerVar, this.prepNode);
            
            // if there are no such aliases: nothing to do
            if (globalMayAliases.isEmpty()) {
                continue;
            }

            // initialize this variable's literal with its original literal
            Literal computedLit = origInfo.getLiteral(localCallerVar);
            
            // for all these global may-aliases...
            for (Iterator gmaIter = globalMayAliases.iterator(); gmaIter.hasNext();) {
                Variable globalMayAlias = (Variable) gmaIter.next();
                
                // its g-shadow
                Variable globalMayAliasShadow = this.callee.getSymbolTable().getGShadow(globalMayAlias);
                
                // the shadow's literal (from flowback-info)
                Literal shadowLit = calleeIn.getLiteral(globalMayAliasShadow);
                
                // lub
                computedLit = LiteralLatticeElement.lub(computedLit, shadowLit);
            }
            
            // set the local's literal to the computed literal in outInfo
            outInfo.setLocal(localCallerVar, computedLit);
            
            // DON'T mark it as visited
        }

        
        // MAY WITH FORMALS
        
        // for each call-by-reference parameter pair
        for (Iterator iter = this.cbrParams.iterator(); iter.hasNext();) {
            
            List paramPair = (List) iter.next();
            Iterator paramPairIter = paramPair.iterator();
            Variable actualVar = (Variable) paramPairIter.next();
            Variable formalVar = (Variable) paramPairIter.next();
            
            // local may-aliases of the actual parameter (at call-time)
            Set localMayAliases = this.aliasAnalysis.getLocalMayAliases(actualVar, this.prepNode);
            
            // for each such may-alias that was not visited yet
            for (Iterator lmaIter = localMayAliases.iterator(); lmaIter.hasNext();) {
                Variable localMayAlias = (Variable) lmaIter.next();
             
                if (visitedVars.contains(localMayAlias)) {
                    continue;
                }
                
                // the current literal of the local may-alias in output-info
                Literal localLit = outInfo.getLiteral(localMayAlias);
                
                // the formal parameter's f-shadow
                Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);
                
                // the shadow's literal (from flowback-info)
                Literal shadowLit = calleeIn.getLiteral(fShadow);
                
                // lub & set
                Literal newLit = LiteralLatticeElement.lub(localLit, shadowLit);
                outInfo.setLocal(localMayAlias, newLit);
            }
        }
        
        this.handleReturnValue(calleeIn, outInfo);
        
        return outInfo;
        
    }
    
    private void handleReturnValue(LiteralLatticeElement calleeIn, LiteralLatticeElement outInfo) {
        
        // literal of the return variable at the end of the called function
        Literal retLit = calleeIn.getLiteral(this.retNode.getRetVar());
        
        // assign this literal to the return node's temporary
        // and clear the return variable afterwards
        outInfo.handleReturnValue(this.retNode.getTempVar(), retLit, this.retNode.getRetVar());

    }
    
    // just a dummy method in order to make me conform to the interface;
    // the Analysis uses the other transfer method instead
    public LatticeElement transfer(LatticeElement inX) {
        throw new RuntimeException("SNH");
    }
    

}



