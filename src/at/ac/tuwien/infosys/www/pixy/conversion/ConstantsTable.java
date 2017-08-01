package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

public class ConstantsTable {

	private Map<String, Constant> constants;
	private Map<String, List<Constant>> insensitiveGroups;

	ConstantsTable() {
		this.constants = new LinkedHashMap<String, Constant>();
		this.insensitiveGroups = new HashMap<String, List<Constant>>();
	}

	public Constant getConstant(String label) {
		return ((Constant) this.constants.get(label));
	}

	List<?> getInsensitiveGroup(String label) {
		return ((List<?>) this.insensitiveGroups.get(label.toLowerCase()));
	}

	public Map<String, Constant> getConstants() {
		return this.constants;
	}

	public Map<String, List<Constant>> getInsensitiveGroups() {
		return this.insensitiveGroups;
	}

	public List<?> getInsensitiveGroup(Literal name) {
		return (List<?>) this.insensitiveGroups.get(name.toString().toLowerCase());
	}

	public int size() {
		return this.constants.size();
	}

	void add(Constant newConst) {
		this.constants.put(newConst.getLabel(), newConst);
		String lowLabel = newConst.getLabel().toLowerCase();
		List<Constant> oldList = this.insensitiveGroups.get(lowLabel);
		if (oldList == null) {
			List<Constant> newList = new LinkedList<Constant>();
			newList.add(newConst);
			this.insensitiveGroups.put(lowLabel, newList);
		} else {
			oldList.add(newConst);
		}
	}

	@SuppressWarnings("rawtypes")
	void addAll(ConstantsTable sourceTable) {
		Map<?, ?> sourceConstants = sourceTable.getConstants();
		for (Iterator<?> iter = sourceConstants.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String sourceLabel = (String) entry.getKey();
			Constant sourceConst = (Constant) entry.getValue();
			if (!this.constants.containsKey(sourceLabel)) {
				this.add(sourceConst);
			}
		}
	}
}
