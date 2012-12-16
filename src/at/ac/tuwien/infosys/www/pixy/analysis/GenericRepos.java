package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.*;

// a generic repository of recyclable objects;
// EFF: it might be useful to work with weak references here

// example usage: you have some *immutable* class that contains
// its own static repository of objects that have been instantiated
// from it so far; when a new object is requested from this class,
// it first checks its repository to see if there already is such
// an object; in this case, a reference to the already existing
// object is returned; otherwise, a new object is created, added to
// the repository, and returned; 
// advantages: saves memory (no redundant objects) and allows 
// users of this class to perform quick comparisons with "=="

// guide for how to design such a class:
// - implement the Recyclable interface
// - insert a private static GenericRepos<that class>
// - make the constructor private
// - use a static factory method instead
// - make sure that the private constructor is ONLY called by
//   factory methods (and perhaps by static field initializers), 
//   and that these methods perform recycling by means of the repository
// - do not override its default equals and hashCode
//   (would destroy the advantages mentioned above)
public class GenericRepos<E extends Recyclable> {

    // structure hash code (Integer) -> List of Recyclable's
    private Map<Integer,List<E>> repos;
    
    public GenericRepos() {
        this.repos = new HashMap<Integer,List<E>>();
    }

    // if the given element equals one from the repository: the
    // repository element is returned; else: the element is
    // entered into the repository and returned
    public E recycle(E recycleMe) {
        
        if (recycleMe == null) {
            return recycleMe;
        }

        Integer structureHashCode = new Integer(recycleMe.structureHashCode());
        List<E> candidates = this.repos.get(structureHashCode);
        if (candidates == null) {
            // no candidates list: add recycleMe to the repos and return it
            List<E> recycleMeList = new LinkedList<E>();
            recycleMeList.add(recycleMe);
            this.repos.put(structureHashCode, recycleMeList);
            return recycleMe;
        }
        
        // search the candidates list
        for (E candidate : candidates) {
            if (candidate.structureEquals(recycleMe)) {
                // recycling!
                return candidate;
            }
        }
        
        // no candidate matches: add recycleMe to this list and return it
        candidates.add(recycleMe);
        return recycleMe;
    }
}

/* old implementation, needed casts for the return value of recycle()
 * public class GenericRepos {

    // structure hash code (Integer) -> List of Recyclable's
    private Map<Integer,List<Recyclable>> repos;
    
    public GenericRepos() {
        this.repos = new HashMap<Integer,List<Recyclable>>();
    }

    // if the given element equals one from the repository: the
    // repository element is returned; else: the element is
    // entered into the repository and returned
    public Recyclable recycle(Recyclable recycleMe) {
        
        if (recycleMe == null) {
            return recycleMe;
        }

        Integer structureHashCode = new Integer(recycleMe.structureHashCode());
        List<Recyclable> candidates = this.repos.get(structureHashCode);
        if (candidates == null) {
            // no candidates list: add recycleMe to the repos and return it
            List<Recyclable> recycleMeList = new LinkedList<Recyclable>();
            recycleMeList.add(recycleMe);
            this.repos.put(structureHashCode, recycleMeList);
            return recycleMe;
        }
        
        // search the candidates list
        for (Iterator iter = candidates.iterator(); iter.hasNext(); ) {
            Recyclable candidate = (Recyclable) iter.next();
            if (candidate.structureEquals(recycleMe)) {
                // recycling!
                return candidate;
            }
        }
        
        // no candidate matches: add recycleMe to this list and return it
        candidates.add(recycleMe);
        return recycleMe;
    }
}

*/