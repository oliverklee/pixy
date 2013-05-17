package at.ac.tuwien.infosys.www.pixy.analysis.dep.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Iterator;
import java.util.Map;

/**
 * Transfer function for function entries.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
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

        Map<Variable, Variable> globals2GShadows = this.function.getSymbolTable().getGlobals2GShadows();

        for (Map.Entry<Variable, Variable> entry : globals2GShadows.entrySet()) {
            Variable global = entry.getKey();
            Variable gShadow = entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // initialize shadow to the taint/label of its original
            out.setShadow(gShadow, global);
        }

        // initialize f-shadows
        Map<Variable, Variable> formals2FShadows = this.function.getSymbolTable().getFormals2FShadows();

        for (Map.Entry<Variable, Variable> entry : formals2FShadows.entrySet()) {
            Variable formal = entry.getKey();
            Variable fShadow = entry.getValue();

            // initialize
            out.setShadow(fShadow, formal);
        }

        return out;
    }
}