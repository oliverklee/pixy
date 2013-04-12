package at.ac.tuwien.infosys.www.pixy.analysis.dep.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// transfer function for function entries
public class DepTfEntry
extends TransferFunction {

    private TacFunction function;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public DepTfEntry(TacFunction function) {
        this.function = function;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // initialize g-shadows

        Map globals2GShadows = this.function.getSymbolTable().getGlobals2GShadows();

        for (Iterator iter = globals2GShadows.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable global = (Variable) entry.getKey();
            Variable gShadow = (Variable) entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // initialize shadow to the taint/label of its original
            out.setShadow(gShadow, global);
        }

        // initialize f-shadows

        Map formals2FShadows = this.function.getSymbolTable().getFormals2FShadows();

        for (Iterator iter = formals2FShadows.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable formal = (Variable) entry.getKey();
            Variable fShadow = (Variable) entry.getValue();

            // initialize
            out.setShadow(fShadow, formal);
        }

        return out;
    }
}