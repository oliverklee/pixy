package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Map;

/**
 * Transfer function for function entries.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class FunctionEntry extends AbstractTransferFunction {
    private TacFunction function;
    private AliasAnalysis aliasAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public FunctionEntry(TacFunction function, AliasAnalysis aliasRepos) {
        this.function = function;
        this.aliasAnalysis = aliasRepos;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        AliasLatticeElement in = (AliasLatticeElement) inX;
        AliasLatticeElement out = new AliasLatticeElement(in);

        // initialize g-shadows
        for (Map.Entry<Variable, Variable> entry : this.function.getSymbolTable().getGlobals2GShadows().entrySet()) {
            Variable global = entry.getKey();
            Variable gShadow = entry.getValue();

            // note: the TacConverter already took care that arrays and array elements
            // don't get shadow variables, so you don't have to check this here again

            // perform redirection
            out.redirect(gShadow, global);
        }

        // initialize f-shadows
        for (Map.Entry<Variable, Variable> entry : this.function.getSymbolTable().getFormals2FShadows().entrySet()) {
            Variable formal = entry.getKey();
            Variable fShadow = entry.getValue();

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