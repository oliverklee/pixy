package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;

import java.util.HashSet;
import java.util.Set;

/**
 * Just a set of Deps.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DependencySet implements Recyclable {
    public static GenericRepository<DependencySet> repos = new GenericRepository<>();

    // no special treatment necessary for the following:
    static public final DependencySet UNINIT = new DependencySet(DependencyLabel.UNINIT);

    static {
        repos.recycle(UNINIT);
    }

    // the contained dependency labels
    private Set<DependencyLabel> dependencyLabelSet;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ********************************************************************************

    private DependencySet() {
        this.dependencyLabelSet = new HashSet<>();
    }

//  ********************************************************************************

    private DependencySet(DependencyLabel dependencyLabel) {
        this.dependencyLabelSet = new HashSet<>();
        this.dependencyLabelSet.add(dependencyLabel);
    }

//  ********************************************************************************

    private DependencySet(Set<DependencyLabel> dependencyLabelSet) {
        this.dependencyLabelSet = dependencyLabelSet;
    }

//  ********************************************************************************

    public static DependencySet create(Set<DependencyLabel> dependencyLabelSet) {
        DependencySet x = new DependencySet(dependencyLabelSet);
        return repos.recycle(x);
    }

//  ********************************************************************************

    public static DependencySet create(DependencyLabel dependencyLabel) {
        Set<DependencyLabel> taintSet = new HashSet<>();
        taintSet.add(dependencyLabel);
        return create(taintSet);
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  ********************************************************************************

    // compute the least upper bound (here: union) of the two taint sets
    public static DependencySet lub(DependencySet a, DependencySet b) {
        // union!
        Set<DependencyLabel> resultSet = new HashSet<>();
        resultSet.addAll(a.dependencyLabelSet);
        resultSet.addAll(b.dependencyLabelSet);
        return DependencySet.create(resultSet);
    }

//  ********************************************************************************

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (DependencyLabel element : this.dependencyLabelSet) {
            buf.append(element.toString());
        }
        return buf.toString();
    }

//  ********************************************************************************

    // returns a copy of the contained taint set
    // (a copy: such that a caller can't modify my state)
    // (shallow copy is sufficient, since the elements of the set are immutable, too)
    public Set<DependencyLabel> getDependencyLabelSet() {
        return new HashSet<>(this.dependencyLabelSet);
    }

//  ********************************************************************************

    public boolean structureEquals(Object compX) {

        if (compX == this) {
            return true;
        }
        if (!(compX instanceof DependencySet)) {
            return false;
        }
        DependencySet comp = (DependencySet) compX;

        // the enclosed sets have to be equal
        return this.dependencyLabelSet.equals(comp.dependencyLabelSet);
    }

//  ********************************************************************************

    public int structureHashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.dependencyLabelSet.hashCode();
        return hashCode;
    }
}