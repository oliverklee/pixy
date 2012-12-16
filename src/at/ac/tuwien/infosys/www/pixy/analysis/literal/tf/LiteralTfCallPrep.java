package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Cfg;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class LiteralTfCallPrep 
extends TransferFunction {

    private List actualParams;
    private List formalParams;
    private TacFunction caller;
    private TacFunction callee;
    private LiteralAnalysis literalAnalysis;
    private CfgNode cfgNode;
    
//  *********************************************************************************    
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************     

    public LiteralTfCallPrep(List actualParams, List formalParams, 
            TacFunction caller, TacFunction callee,
            LiteralAnalysis literalAnalysis,
            CfgNode cfgNode) {
        
        this.actualParams = actualParams;
        this.formalParams = formalParams;
        this.caller = caller;
        this.callee = callee;
        this.literalAnalysis = literalAnalysis;
        this.cfgNode = cfgNode;
    }

//  *********************************************************************************    
//  OTHER ***************************************************************************
//  *********************************************************************************
    
    public LatticeElement transfer(LatticeElement inX) {
        
        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // set formal params...
        
        // use a ListIterator for formals because we might need to step back (see below)
        ListIterator formalIter = formalParams.listIterator();
        Iterator actualIter = actualParams.iterator();

        // for each formal parameter...
        while (formalIter.hasNext()) {
            
            TacFormalParam formalParam = (TacFormalParam) formalIter.next();
            
            if (actualIter.hasNext()) {
                
                // there is a corresponding actual parameter
                TacActualParam actualParam = (TacActualParam) actualIter.next();
                TacPlace actualPlace = actualParam.getPlace();
                
                // set the literal of the formal to the literal of the actual
                out.setFormal(formalParam, actualPlace);
                
            } else {
                
                // there is no corresponding actual parameter, use default values
                // for the remaining formal parameters

                // make one step back (so we can use a while loop)
                formalIter.previous();

                while (formalIter.hasNext()) {
                    
                    formalParam = (TacFormalParam) formalIter.next();

                    if (formalParam.hasDefault()) {

                        Cfg defaultCfg = formalParam.getDefaultCfg();
                        
                        // default CFG's have no branches;
                        // start at the CFG's head and apply all transfer functions
                        CfgNode defaultNode = defaultCfg.getHead();
                        while (defaultNode != null) {
                            TransferFunction tf = this.literalAnalysis.getTransferFunction(defaultNode);
                            out = (LiteralLatticeElement) tf.transfer(out);
                            defaultNode = defaultNode.getSuccessor(0);
                        }
                        
                    } else {
                        // we have already generated a warning for this during conversion;
                        // simply ignore it (=ok, is exactly what PHP does)
                        /*
                        System.out.println("PHP Syntax Error: calling function \"" + 
                                callee.getName() + "\" with missing parameter");
                        System.out.println(cfgNode.getFileName() + ", line " + cfgNode.getOrigLineno());
                        throw new RuntimeException();
                        */
                    }
                }
            }
        }

        // reset all local variables that belong to the symbol table of the
        // caller; shortcut: if the caller is main, we don't have to do
        // this (since there are no real local variables in the main function) 
        SymbolTable callerSymTab = this.caller.getSymbolTable();
        if (!callerSymTab.isMain()) {
            out.resetVariables(callerSymTab);
        }

        return out;
    }

}
