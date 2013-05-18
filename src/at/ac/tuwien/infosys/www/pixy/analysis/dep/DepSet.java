package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;

import java.util.HashSet;
import java.util.Set;

/**
 * Just a set of Deps.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepSet
    implements Recyclable {

    public static GenericRepos<DepSet> repos = new GenericRepos<>();

    // no special treatment necessary for the following:
    static public final DepSet UNINIT = new DepSet(Dep.UNINIT);

    static {
        repos.recycle(UNINIT);
    }

    // the contained dep labels
    private Set<Dep> depSet;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ********************************************************************************

    private DepSet() {
        this.depSet = new HashSet<>();
    }

//  ********************************************************************************

    private DepSet(Dep dep) {
        this.depSet = new HashSet<>();
        this.depSet.add(dep);
    }

//  ********************************************************************************

    private DepSet(Set<Dep> depSet) {
        this.depSet = depSet;
    }

//  ********************************************************************************

    public static DepSet create(Set<Dep> depSet) {
        DepSet x = new DepSet(depSet);
        return repos.recycle(x);
    }

//  ********************************************************************************

    public static DepSet create(Dep dep) {
        Set<Dep> taintSet = new HashSet<>();
        taintSet.add(dep);
        return create(taintSet);
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  ********************************************************************************

    // compute the least upper bound (here: union) of the two taint sets
    public static DepSet lub(DepSet a, DepSet b) {
        // union!
        Set<Dep> resultSet = new HashSet<>();
        resultSet.addAll(a.depSet);
        resultSet.addAll(b.depSet);
        return DepSet.create(resultSet);
    }

//  ********************************************************************************

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Dep element : this.depSet) {
            buf.append(element.toString());
        }
        return buf.toString();
    }

//  ********************************************************************************

    // returns a copy of the contained taint set
    // (a copy: such that a caller can't modify my state)
    // (shallow copy is sufficient, since the elements of the set are immutable, too)
    public Set<Dep> getDepSet() {
        return new HashSet<>(this.depSet);
    }

//  ********************************************************************************

    public boolean structureEquals(Object compX) {

        if (compX == this) {
            return true;
        }
        if (!(compX instanceof DepSet)) {
            return false;
        }
        DepSet comp = (DepSet) compX;

        // the enclosed sets have to be equal
        if (!this.depSet.equals(comp.depSet)) {
            return false;
        }

        return true;
    }

//  ********************************************************************************

    public int structureHashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.depSet.hashCode();
        return hashCode;
    }
}