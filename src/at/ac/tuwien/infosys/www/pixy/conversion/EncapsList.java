package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Empty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * With this class, we generate suitable (and small) cfg's for "encaps_lists" (i.e, strings that are delimited by double
 * quotes and that contain variables).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class EncapsList {
    private List<Object> encapsList;

    EncapsList() {
        this.encapsList = new LinkedList<>();
    }

    void dump() {
        System.out.println("here comes an encaps list:");
        for (Object o : this.encapsList) {
            System.out.println(o);
        }
    }

    void add(TacPlace place, ControlFlowGraph controlFlowGraph) {
        this.encapsList.add(place);
        this.encapsList.add(controlFlowGraph);
    }

    void add(Literal string) {
        this.encapsList.add(string);
    }

    // converts this encapslist into a place and cfg
    // - place
    // - cfg
    TacAttributes makeAtts(Variable temp, ParseNode node) {
        TacAttributes myAtts = new TacAttributes();

        AbstractCfgNode head = new Empty();
        AbstractCfgNode contd = head;

        // is the temporary variable still empty?
        boolean tempEmpty = true;

        Iterator<Object> iter = this.encapsList.iterator();
        Literal lastLiteral = null;
        while (iter.hasNext()) {
            Object obj = iter.next();

            if (obj instanceof Literal) {
                Literal lit = (Literal) obj;

                if (lastLiteral != null) {
                    // merge them!
                    lastLiteral = new Literal(lastLiteral.toString() + lit.toString());
                } else {
                    // start new
                    lastLiteral = lit;
                }
            } else if (obj instanceof TacPlace) {
                if (lastLiteral != null) {
                    // catch the last literal in a temporary

                    AbstractCfgNode cfgNode;
                    if (tempEmpty) {
                        // if the temporary is still empty, we can simply
                        // assign the literal to it
                        cfgNode = new AssignSimple(
                            temp, lastLiteral, node);
                        tempEmpty = false;
                    } else {
                        // if the temporary is non-empty, we have to concat
                        cfgNode = new AssignBinary(
                            temp, temp, lastLiteral, TacOperators.CONCAT, node);
                    }
                    TacConverter.connect(contd, cfgNode);
                    contd = cfgNode;
                    lastLiteral = null;
                }

                // fetch the cfg of this non-literal
                ControlFlowGraph nextControlFlowGraph = (ControlFlowGraph) iter.next();

                AbstractCfgNode cfgNode;
                if (tempEmpty) {
                    cfgNode = new AssignSimple(
                        temp, (TacPlace) obj, node);
                    tempEmpty = false;
                } else {
                    cfgNode = new AssignBinary(
                        temp, temp, (TacPlace) obj, TacOperators.CONCAT, node);
                }
                TacConverter.connect(contd, nextControlFlowGraph);
                TacConverter.connect(nextControlFlowGraph, cfgNode);
                contd = cfgNode;
            } else {
                throw new RuntimeException("SNH");
            }
        }

        if (lastLiteral != null) {
            // some literal is hanging around at the end...
            AbstractCfgNode cfgNode;
            if (tempEmpty) {
                cfgNode = new AssignSimple(
                    temp, lastLiteral, node);
                tempEmpty = false;
            } else {
                cfgNode = new AssignBinary(
                    temp, temp, lastLiteral, TacOperators.CONCAT, node);
            }
            TacConverter.connect(contd, cfgNode);
            contd = cfgNode;
            lastLiteral = null;
        }

        myAtts.setControlFlowGraph(new ControlFlowGraph(head, contd));
        myAtts.setPlace(temp);
        return myAtts;
    }
}