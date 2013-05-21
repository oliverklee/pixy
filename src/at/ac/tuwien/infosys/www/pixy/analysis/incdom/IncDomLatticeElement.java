package at.ac.tuwien.infosys.www.pixy.analysis.incdom;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IncDomLatticeElement extends LatticeElement {
    // an ordered list of CfgNodes (more specifically: of CfgNodeIncludeStart and
    // CfgNodeIncludeEnd) that dominate the current node
    private List<CfgNode> dominators;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates an empty lattice element
    public IncDomLatticeElement() {
        this.dominators = new LinkedList<>();
    }

    // clones the given element
    public IncDomLatticeElement(IncDomLatticeElement cloneMe) {
        this.dominators = new LinkedList<>(cloneMe.getDominators());
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
        Iterator<CfgNode> foreignIter = element.getDominators().iterator();
        Iterator<CfgNode> myIter = this.dominators.iterator();
        List<CfgNode> newList = new LinkedList<>();
        boolean goOn = true;
        while (foreignIter.hasNext() && myIter.hasNext() && goOn) {
            CfgNode myNode = myIter.next();
            CfgNode foreignNode = foreignIter.next();
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
        return this.dominators.equals(comp.getDominators());
    }

    public int structureHashCode() {
        return this.dominators.hashCode();
    }

    public void dump() {
        System.out.println("IncDomLatticeElement.dump(): not yet");
    }
}