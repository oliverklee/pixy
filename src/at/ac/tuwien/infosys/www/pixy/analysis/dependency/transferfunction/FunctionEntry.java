package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Map;

/**
 * Transfer function for function entries.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class FunctionEntry extends TransferFunction {
    private TacFunction function;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public FunctionEntry(TacFunction function) {
        this.function = function;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // initialize g-shadows
        for (Map.Entry<Variable, Variable> entry : this.function.getSymbolTable().getGlobals2GShadows().entrySet()) {
            Variable global = entry.getKey();
            Variable gShadow = entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // initialize shadow to the taint/label of its original
            out.setShadow(gShadow, global);
        }

        // initialize f-shadows

        for (Map.Entry<Variable, Variable> entry : this.function.getSymbolTable().getFormals2FShadows().entrySet()) {
            Variable formal = entry.getKey();
            Variable fShadow = entry.getValue();

            // initialize
            out.setShadow(fShadow, formal);
        }

        return out;
    }
}