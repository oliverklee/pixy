package at.ac.tuwien.infosys.www.pixy.analysis.incdom;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IncDomLatticeElement
    extends LatticeElement {

    // an ordered list of CfgNodes (more specifically: of CfgNodeIncludeStart and
    // CfgNodeIncludeEnd) that dominate the current node
    private List<CfgNode> dominators;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates an empty lattice element
    public IncDomLatticeElement() {
        this.dominators = new LinkedList<CfgNode>();
    }

    // clones the given element
    public IncDomLatticeElement(IncDomLatticeElement cloneMe) {
        this.dominators = new LinkedList<CfgNode>(cloneMe.getDominators());
    }

    public LatticeElement cloneMe() {
        // uses the cloning constructor
        return new IncDomLatticeElement(this);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public List<CfgNode> getDominators() {
        return this.dominators;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // lubs the given element over *this* element
    public void lub(LatticeElement element) {
        if (!(element instanceof IncDomLatticeElement)) {
            throw new RuntimeException("SNH");
        }
        this.lub((IncDomLatticeElement) element);
    }

    public void lub(IncDomLatticeElement element) {
        // longest matching prefix
        Iterator foreignIter = element.getDominators().iterator();
        Iterator myIter = this.dominators.iterator();
        List<CfgNode> newList = new LinkedList<CfgNode>();
        boolean goOn = true;
        while (foreignIter.hasNext() && myIter.hasNext() && goOn) {
            CfgNode myNode = (CfgNode) myIter.next();
            CfgNode foreignNode = (CfgNode) foreignIter.next();
            if (myNode == foreignNode) {
                newList.add(myNode);
            } else {
                goOn = false;
            }
        }
        this.dominators = newList;
    }

    // apends the given CfgNode to the list of dominators
    public void add(CfgNode cfgNode) {
        this.dominators.add(cfgNode);
    }

    // thorough (and slower) structural comparison required by the repository
    public boolean structureEquals(Object compX) {
        IncDomLatticeElement comp = (IncDomLatticeElement) compX;
        if (this.dominators.equals(comp.getDominators())) {
            return true;
        } else {
            return false;
        }
    }

    public int structureHashCode() {
        return this.dominators.hashCode();
    }

    public void dump() {
        System.out.println("IncDomLatticeElement.dump(): not yet");
    }
}