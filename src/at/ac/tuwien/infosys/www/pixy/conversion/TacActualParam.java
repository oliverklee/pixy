package at.ac.tuwien.infosys.www.pixy.conversion;

public class TacActualParam {

    private TacPlace place;
    private boolean isReference;

// ********************************************************************************* 
// CONSTRUCTORS ********************************************************************    
// ********************************************************************************* 

    TacActualParam(TacPlace place, boolean isReference) {
        this.place = place;
        this.isReference = isReference;
    }

// ********************************************************************************* 
// GET *****************************************************************************
// *********************************************************************************

    public TacPlace getPlace() {
        return this.place;
    }

    public boolean isReference() {
        return this.isReference;
    }
    
//  ******************************************************************************** 
//  SET ****************************************************************************
//  ********************************************************************************

     public void setPlace(TacPlace place) {
         this.place = place;
     }

}


