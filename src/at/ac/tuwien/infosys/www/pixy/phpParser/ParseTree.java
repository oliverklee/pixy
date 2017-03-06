package at.ac.tuwien.infosys.www.pixy.phpParser;

import java.util.*;
import java.io.Serializable;

public final class ParseTree implements Serializable {

	private static final long serialVersionUID = 1L;
	private final ParseNode root;

	public ParseTree(ParseNode root) {
		this.root = root;
	}

	public ParseNode getRoot() {
		return this.root;
	}

	public Iterator<ParseNode> leafIterator() {
		LinkedList<ParseNode> list = new LinkedList<ParseNode>();
		this.leafIteratorHelper(list, this.root);
		return list.iterator();
	}

	private void leafIteratorHelper(List<ParseNode> list, ParseNode node) {
		if (node == null) {
			return;
		}
		if (node.isToken()) {
			list.add(node);
			return;

		}
		for (Iterator<?> iter = node.getChildren().iterator(); iter.hasNext();) {
			ParseNode child = (ParseNode) iter.next();
			leafIteratorHelper(list, child);
		}
	}
}