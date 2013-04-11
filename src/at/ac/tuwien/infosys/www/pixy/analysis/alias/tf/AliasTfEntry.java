package at.ac.tuwien.infosys.www.pixy.analysis.alias.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.*;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// transfer function for function entries
public class AliasTfEntry
extends TransferFunction {

    private TacFunction function;
    private AliasAnalysis aliasAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public AliasTfEntry(TacFunction function, AliasAnalysis aliasRepos) {
        this.function = function;
        this.aliasAnalysis = aliasRepos;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        AliasLatticeElement in = (AliasLatticeElement) inX;
        AliasLatticeElement out = new AliasLatticeElement(in);

        // initialize g-shadows

        Map globals2GShadows = this.function.getSymbolTable().getGlobals2GShadows();

        for (Iterator iter = globals2GShadows.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable global = (Variable) entry.getKey();
            Variable gShadow = (Variable) entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // perform redirection
            out.redirect(gShadow, global);
        }

        // initialize f-shadows

        Map formals2FShadows = this.function.getSymbolTable().getFormals2FShadows();

        for (Iterator iter = formals2FShadows.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable formal = (Variable) entry.getKey();
            Variable fShadow = (Variable) entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // perform redirection
            out.redirect(fShadow, formal);
        }

        // recycle
        out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

        return out;

    }
}