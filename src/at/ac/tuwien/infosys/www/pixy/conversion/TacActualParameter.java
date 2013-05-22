package at.ac.tuwien.infosys.www.pixy.conversion;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacActualParameter {
    private AbstractTacPlace place;
    private boolean isReference;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    TacActualParameter(AbstractTacPlace place, boolean isReference) {
        this.place = place;
        this.isReference = isReference;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public AbstractTacPlace getPlace() {
        return this.place;
    }

    public boolean isReference() {
        return this.isReference;
    }

//  ********************************************************************************
//  SET ****************************************************************************
//  ********************************************************************************

    public void setPlace(AbstractTacPlace place) {
        this.place = place;
    }
}