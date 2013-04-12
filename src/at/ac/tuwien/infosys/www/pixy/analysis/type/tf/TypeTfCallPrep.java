package at.ac.tuwien.infosys.www.pixy.analysis.type.tf;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Cfg;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class TypeTfCallPrep
    extends TransferFunction {

    private List actualParams;
    private List formalParams;
    private TacFunction caller;
    private TacFunction callee;
    private TypeAnalysis typeAnalysis;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

    public TypeTfCallPrep(List actualParams, List formalParams,
                          TacFunction caller, TacFunction callee,
                          TypeAnalysis typeAnalysis) {

        this.actualParams = actualParams;
        this.formalParams = formalParams;
        this.caller = caller;
        this.callee = callee;
        this.typeAnalysis = typeAnalysis;
    }

//  *********************************************************************************
//  OTHER ***************************************************************************
//  *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        // set formal params...

        // use a ListIterator for formals because we might need to step back (see below)
        ListIterator formalIter = formalParams.listIterator();
        Iterator actualIter = actualParams.iterator();

        // for each formal parameter...
        while (formalIter.hasNext()) {

            TacFormalParam formalParam = (TacFormalParam) formalIter.next();

            if (actualIter.hasNext()) {

                // there is a corresponding actual parameter; advance iterator
                TacActualParam actualParam = (TacActualParam) actualIter.next();

                // set the formal
                out.assign(formalParam.getVariable(), actualParam.getPlace());
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
                            TransferFunction tf = this.typeAnalysis.getTransferFunction(defaultNode);
                            out = (TypeLatticeElement) tf.transfer(out);
                            defaultNode = defaultNode.getSuccessor(0);
                        }
                    } else {
                        // missing actual parameter;
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
            // only do this for non-recursive calls;
            // EFF: it might be better to reset everything except the formal params;
            // TODO: also think about correctness
            if (!(callee == caller)) {
                out.resetVariables(callerSymTab);
            }
        } else {
            // for the main function, we can at least reset the temporary variables
            out.resetTemporaries(callerSymTab);
        }

        return out;
    }
}