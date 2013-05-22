package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class InclusionDominatorLatticeElement extends AbstractLatticeElement {
    // an ordered list of CfgNodes (more specifically: of IncludeStart and
    // IncludeEnd) that dominate the current node
    private List<AbstractCfgNode> dominators;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    // creates an empty lattice element
    public InclusionDominatorLatticeElement() {
        this.dominators = new LinkedList<>();
    }

    // clones the given element
    public InclusionDominatorLatticeElement(InclusionDominatorLatticeElement cloneMe) {
        this.dominators = new LinkedList<>(cloneMe.getDominators());
    }

    public AbstractLatticeElement cloneMe() {
        // uses the cloning constructor
        return new InclusionDominatorLatticeElement(this);
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public List<AbstractCfgNode> getDominators() {
        return this.dominators;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // lubs the given element over *this* element
    public void lub(AbstractLatticeElement element) {
        if (!(element instanceof InclusionDominatorLatticeElement)) {
            throw new RuntimeException("SNH");
        }
        this.lub((InclusionDominatorLatticeElement) element);
    }

    public void lub(InclusionDominatorLatticeElement element) {
        // longest matching prefix
        Iterator<AbstractCfgNode> foreignIter = element.getDominators().iterator();
        Iterator<AbstractCfgNode> myIter = this.dominators.iterator();
        List<AbstractCfgNode> newList = new LinkedList<>();
        boolean goOn = true;
        while (foreignIter.hasNext() && myIter.hasNext() && goOn) {
            AbstractCfgNode myNode = myIter.next();
            AbstractCfgNode foreignNode = foreignIter.next();
            if (myNode == foreignNode) {
                newList.add(myNode);
            } else {
                goOn = false;
            }
        }
        this.dominators = newList;
    }

    // apends the given CfgNode to the list of dominators
    public void add(AbstractCfgNode cfgNode) {
        this.dominators.add(cfgNode);
    }

    // thorough (and slower) structural comparison required by the repository
    public boolean structureEquals(Object compX) {
        InclusionDominatorLatticeElement comp = (InclusionDominatorLatticeElement) compX;
        return this.dominators.equals(comp.getDominators());
    }

    public int structureHashCode() {
        return this.dominators.hashCode();
    }

    public void dump() {
        System.out.println("InclusionDominatorLatticeElement.dump(): not yet");
    }
}