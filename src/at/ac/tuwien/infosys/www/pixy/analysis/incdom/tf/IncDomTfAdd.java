package at.ac.tuwien.infosys.www.pixy.analysis.incdom.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.incdom.IncDomAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.incdom.IncDomLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// transfer function for adding include dominators
public class IncDomTfAdd
    extends TransferFunction {

    private CfgNode cfgNode;
    private IncDomAnalysis incDomAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public IncDomTfAdd(CfgNode cfgNode, IncDomAnalysis incDomAnalysis) {
        this.cfgNode = cfgNode;
        this.incDomAnalysis = incDomAnalysis;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        IncDomLatticeElement in = (IncDomLatticeElement) inX;
        IncDomLatticeElement out = new IncDomLatticeElement(in);
        out.add(this.cfgNode);

        // recycle
        out = (IncDomLatticeElement) this.incDomAnalysis.recycle(out);

        return out;
    }
}