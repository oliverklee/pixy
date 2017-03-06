package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.*;

public class GenericRepository<E extends Recyclable> {

	private Map<Integer, List<E>> repos;

	public GenericRepository() {
		this.repos = new HashMap<Integer, List<E>>();
	}

	public E recycle(E recycleMe) {

		if (recycleMe == null) {
			return recycleMe;
		}

		Integer structureHashCode = new Integer(recycleMe.structureHashCode());
		List<E> candidates = this.repos.get(structureHashCode);
		if (candidates == null) {
			List<E> recycleMeList = new LinkedList<E>();
			recycleMeList.add(recycleMe);
			this.repos.put(structureHashCode, recycleMeList);
			return recycleMe;
		}

		for (E candidate : candidates) {
			if (candidate.structureEquals(recycleMe)) {
				return candidate;
			}
		}

		candidates.add(recycleMe);
		return recycleMe;
	}
}