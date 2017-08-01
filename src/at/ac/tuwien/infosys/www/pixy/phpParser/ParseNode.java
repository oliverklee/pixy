package at.ac.tuwien.infosys.www.pixy.phpParser;

import java.util.*;
import java.io.Serializable;

public final class ParseNode implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int id;
	private static int minFreeId = 0;
	private int lineno = -1;

	private final int symbol;
	private final String name;

	private List<ParseNode> children = new ArrayList<ParseNode>();

	private int tokenLine = -1;
	private int tokenColumn = -1;
	private String tokenContent = "";
	private String tokenFile = "";
	private boolean isToken = false;

	private ParseNode parent = null;

	public ParseNode(int symbol, String name) {
		this.id = ParseNode.minFreeId++;
		this.symbol = symbol;
		this.name = name;
	}

	public ParseNode(int symbol, String name, String content, int line, int column, String file) {
		this(symbol, name);
		this.tokenContent = content;
		this.tokenLine = line;
		this.tokenColumn = column;
		this.isToken = true;
		this.tokenFile = file;
		this.lineno = line;
	}

	public ParseNode(int symbol, String name, String fileName, String content, int line) {
		this(symbol, name);
		this.tokenContent = content;
		this.tokenLine = line;
		this.tokenColumn = -1;
		this.isToken = true;
		this.tokenFile = fileName;
		this.lineno = line;
	}

	public ParseNode(int prodNumber, String prodName, String fileName) {
		this(prodNumber, prodName);
		this.tokenFile = fileName;
	}

	public int symbol() {
		return symbol;
	}

	public String name() {
		return name;
	}

	public int line() {
		return tokenLine;
	}

	public int column() {
		return tokenColumn;
	}

	public String file() {
		return tokenFile;
	}

	public String tokenContent() {
		return tokenContent;
	}

	public boolean isToken() {
		return isToken;
	}

	public List<ParseNode> children() {
		return children;
	}

	public String fileStart() {
		Stack<ParseNode> stack = new Stack<ParseNode>();
		stack.push(this);
		while (stack.size() > 0) {
			ParseNode el = stack.pop();
			if (el.isToken) {
				return el.tokenFile;
			} else {
				for (ParseNode child : el.children) {
					stack.push(child);
				}
			}
		}
		return null;
	}

	public int[] lineColumnStart() {
		int[] res = { -1, -1 };

		if (isToken) {
			res[0] = tokenLine;
			res[1] = tokenColumn;
			return res;
		} else {
			for (ParseNode child : children) {
				int[] r = child.lineColumnStart();
				if (r[0] >= 0) {
					if ((res[0] == -1) || (r[0] < res[0]) || (r[0] == res[0] && r[1] < res[1])) {
						res = r;
					}
				}
			}
			return res;
		}
	}

	public int[] lineColumnEnd() {
		int[] res = new int[2];
		if (isToken) {
			res[0] = tokenLine;
			res[1] = tokenColumn + tokenContent.length();
			return res;
		} else {
			for (ParseNode child : children) {
				int[] r = child.lineColumnEnd();
				if ((r[0] > res[0]) || (r[0] == res[0] && r[1] > res[1])) {
					res = r;
				}
			}
			return res;
		}
	}

	public void parentIs(ParseNode node) {
		parent = node;
	}

	public void newChildrenIs(ParseNode node) {
		children.add(node);
	}

	public void print() {
		print("");
	}

	public void print(String indent) {
		System.out.println(indent + ": " + name + "(" + symbol + ") " + (isToken ? "(Token)" : ""));
		if (isToken) {
			System.out.println(indent + " Line: " + tokenLine + " Content: " + tokenContent);
		} else {
			for (ParseNode child : children) {
				child.print(indent + "  ");
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public String getFileName() {
		return this.tokenFile;
	}

	public int getSymbol() {
		return this.symbol;
	}

	public List<ParseNode> getChildren() {
		return this.children;
	}

	public int getNumChildren() {
		return this.children.size();
	}

	public ParseNode getChild(int index) {
		if (this.isToken) {
			throw new UnsupportedOperationException("Call to getChild for token node " + this.name);
		} else {
			if (index >= children.size()) {
				return null;
			} else {
				ParseNode returned = (ParseNode) this.children.get(index);
				return returned;
			}
		}
	}

	public ParseNode getParent() {
		return this.parent;
	}

	public String getLexeme() {
		if (this.isToken) {
			return this.tokenContent;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public int getLineno() {
		if (this.isToken) {
			return this.lineno;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public int getLinenoLeft() {
		if (this.isToken) {
			return this.lineno;
		} else {
			return this.getChild(0).getLinenoLeft();
		}
	}

	public int getId() {
		return this.id;
	}

	public void setParent(ParseNode parent) {
		this.parent = parent;
	}

	public ParseNode addChild(ParseNode child) {
		if (this.isToken) {
			throw new UnsupportedOperationException();
		} else {
			this.children.add(child);
			child.setParent(this);
			return child;
		}
	}
}