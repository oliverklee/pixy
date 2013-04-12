package at.ac.tuwien.infosys.www.pixy.analysis.dep.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.*;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeDefine;

public class DepTfDefine
extends TransferFunction {

    private TacPlace setMe;
    //private TacPlace setTo;
    private TacPlace caseInsensitive;

    private ConstantsTable constantsTable;
    private LiteralAnalysis literalAnalysis;
    private CfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public DepTfDefine(ConstantsTable table, LiteralAnalysis literalAnalysis,
            CfgNodeDefine cfgNode) {

        this.setMe = cfgNode.getSetMe();
        this.caseInsensitive = cfgNode.getCaseInsensitive();
        this.constantsTable = table;
        this.literalAnalysis = literalAnalysis;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // retrieve the literal of the constant to be set
        Literal constantLit;
        if (this.setMe instanceof Literal) {
            // e.g.: define('foo', ...);
            constantLit = (Literal) setMe;
        } else {
            // ask literals analysis
            constantLit = literalAnalysis.getLiteral(setMe, cfgNode);
        }

        // if we can't resolve the constant that is to be set, we can't do
        // anything;
        // to be precise, we would have to set all constants to
        // TOP (or better: those that are still undefined), but since this
        // case is rather seldom, we just issue a warning;
        if (constantLit == Literal.TOP) {
            // warning was already issued by literals analysis
            return out;
        }

        // retrieve the taint that the constant shall be set to
        //DepSet valueTaint = DepSet.create(Dep.create(cfgNode));

        // determine the (boolean) literal of the case flag
        if (this.caseInsensitive == Constant.TRUE) {
            // define insensitive constant

            // all constants in setMe's insensitivity group have to be set
            List insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
            if (insensGroup != null) {
                for (Iterator iter = insensGroup.iterator(); iter.hasNext();) {
                    Constant constant = (Constant) iter.next();
                    out.defineConstant(constant, this.cfgNode);
                }
            } else {
                // this case happens when the user defines a constant which is never
                // used (not even in a case-insensitive way);
                System.out.println("Warning: a constant is defined, but never used");
                System.out.println("- name:    " + constantLit.toString());
                System.out.println("- defined: " + this.cfgNode.getLoc());
            }

        } else if (this.caseInsensitive == Constant.FALSE) {
            // define sensitive constant

            Constant constant = this.constantsTable.getConstant(constantLit.toString());
            if (constant == null) {
                // happens if the constant being defined is never used
                System.out.println("Warning: a constant is defined, but never used");
                System.out.println("- name:    " + constantLit.toString());
                System.out.println("- defined: " + this.cfgNode.getLoc());
            } else {
                out.defineConstant(constant, this.cfgNode);
            }

        } else {

            // we don't know the exact value of this flag;
            // hence, we perform a strong update for the immediate constant in
            // question, and a weak update for all constants in its insensitivity group

            Constant constant = this.constantsTable.getConstant(constantLit.toString());
            if (constant == null) {
                // happens if the constant being defined is never used
                System.out.println("Warning: a constant is defined, but never used");
                System.out.println("- name:    " + constantLit.toString());
                System.out.println("- defined: " + this.cfgNode.getLoc());
            } else {
                out.defineConstant(constant, this.cfgNode);
            }

            // all constants in setMe's insensitivity group have to undergo a weak update
            // (except setMe itself)
            List insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
            if (insensGroup != null) {
                for (Iterator iter = insensGroup.iterator(); iter.hasNext();) {
                    Constant weakConstant = (Constant) iter.next();
                    if (!weakConstant.equals(constant)) {
                        out.defineConstantWeak(weakConstant, this.cfgNode);
                    }
                }
            } else {
                // this case happens when the user defines a constant which is never
                // used (not even in a case-insensitive way);
                System.out.println("Warning: a constant is defined, but never used");
                System.out.println("- name:    " + constantLit.toString());
                System.out.println("- defined: " + this.cfgNode.getLoc());
            }
        }

        return out;
    }
}