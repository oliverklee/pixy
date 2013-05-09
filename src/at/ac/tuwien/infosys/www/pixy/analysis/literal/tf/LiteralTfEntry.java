package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import java.util.Iterator;
import java.util.Map;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// transfer function for function entries
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralTfEntry
    extends TransferFunction {

    private TacFunction function;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public LiteralTfEntry(TacFunction function) {
        this.function = function;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // initialize g-shadows

        Map globals2GShadows = this.function.getSymbolTable().getGlobals2GShadows();

        for (Iterator iter = globals2GShadows.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Variable global = (Variable) entry.getKey();
            Variable gShadow = (Variable) entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // initialize shadow to the literal of its original
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