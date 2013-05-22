package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallPreparation extends TransferFunction {
    private List<TacActualParameter> actualParams;
    private List<TacFormalParameter> formalParams;
    private TacFunction caller;
    private TacFunction callee;
    private LiteralAnalysis literalAnalysis;
    private AbstractCfgNode cfgNode;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

    public CallPreparation(
        List<TacActualParameter> actualParams, List<TacFormalParameter> formalParams, TacFunction caller, TacFunction callee,
        LiteralAnalysis literalAnalysis, AbstractCfgNode cfgNode
    ) {
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
        ListIterator<TacFormalParameter> formalIter = formalParams.listIterator();
        Iterator<TacActualParameter> actualIter = actualParams.iterator();

        // for each formal parameter...
        while (formalIter.hasNext()) {
            TacFormalParameter formalParam = formalIter.next();

            if (actualIter.hasNext()) {
                // there is a corresponding actual parameter
                TacActualParameter actualParam = actualIter.next();
                TacPlace actualPlace = actualParam.getPlace();

                // set the literal of the formal to the literal of the actual
                out.setFormal(formalParam, actualPlace);
            } else {
                // there is no corresponding actual parameter, use default values
                // for the remaining formal parameters

                // make one step back (so we can use a while loop)
                formalIter.previous();

                while (formalIter.hasNext()) {
                    formalParam = formalIter.next();

                    if (formalParam.hasDefault()) {
                        ControlFlowGraph defaultControlFlowGraph = formalParam.getDefaultControlFlowGraph();

                        // default CFG's have no branches;
                        // start at the CFG's head and apply all transfer functions
                        AbstractCfgNode defaultNode = defaultControlFlowGraph.getHead();
                        while (defaultNode != null) {
                            TransferFunction tf = this.literalAnalysis.getTransferFunction(defaultNode);
                            out = (LiteralLatticeElement) tf.transfer(out);
                            defaultNode = defaultNode.getSuccessor(0);
                        }
                    } else {
                        // we have already generated a warning for this during conversion;
                        // simply ignore it (=ok, is exactly what PHP does)
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