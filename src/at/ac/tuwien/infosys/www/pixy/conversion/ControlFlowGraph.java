package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public final class ControlFlowGraph {

	private AbstractCfgNode head;
	private final AbstractCfgNode tail;
	private final int tailEdgeType;

	ControlFlowGraph(AbstractCfgNode head, AbstractCfgNode tail, int tailEdgeType) {
		this.head = head;
		this.tail = tail;
		this.tailEdgeType = tailEdgeType;
	}

	ControlFlowGraph(AbstractCfgNode head, AbstractCfgNode tail) {
		this.head = head;
		this.tail = tail;
		this.tailEdgeType = CfgEdge.NORMAL_EDGE;
	}

	public ControlFlowGraph() {
		this.tail = null;
		this.tailEdgeType = 0;
	}

	public AbstractCfgNode getHead() {
		return this.head;
	}

	public AbstractCfgNode getTail() {
		return this.tail;
	}

	int getTailEdgeType() {
		return this.tailEdgeType;
	}

	public static TacFunction getFunction(AbstractCfgNode cfgNode) {
		return cfgNode.getEnclosingFunction();
	}

	public List<Call> getContainedCalls() {
		List<Call> retMe = new LinkedList<Call>();
		Iterator<?> iter = this.dfPreOrder().iterator();
		while (iter.hasNext()) {
			AbstractCfgNode cfgNode = (AbstractCfgNode) iter.next();
			if (cfgNode instanceof Call) {
				retMe.add((Call) cfgNode);
			}
		}
		return retMe;
	}

	public static AbstractCfgNode getHead(AbstractCfgNode cfgNode) {

		boolean goOn = true;
		while (goOn) {
			List<AbstractCfgNode> pre = cfgNode.getPredecessors();
			if (pre.size() == 0) {
				goOn = false;
			} else if (pre.size() == 1) {
				cfgNode = pre.get(0);
			} else {
				System.out.println(cfgNode.getLoc());
				throw new RuntimeException("SNH");
			}
		}
		return cfgNode;
	}

	void setHead(AbstractCfgNode head) {
		this.head = head;
	}

	public void assignReversePostOrder() {
		LinkedList<?> postorder = this.dfPostOrder();
		ListIterator<?> iter = postorder.listIterator(postorder.size());
		int i = 0;
		while (iter.hasPrevious()) {
			AbstractCfgNode cfgNode = (AbstractCfgNode) iter.previous();
			cfgNode.setReversePostOrder(i);
			i++;
		}
	}

	public long size() {
		Set<AbstractCfgNode> visited = new HashSet<AbstractCfgNode>();
		long s = size(this.head, visited);
		return s;
	}

	long size(AbstractCfgNode node, Set<AbstractCfgNode> visited) {

		if (!visited.contains(node)) {
			visited.add(node);
			long size = 1;
			CfgEdge[] outEdges = node.getOutEdges();
			for (long i = 0; i < outEdges.length; i++) {
				if (outEdges[(int) i] != null) {
				}
			}
			return size;
		} else {
			return 0;
		}
	}

	public Iterator<AbstractCfgNode> bfIterator() {

		LinkedList<AbstractCfgNode> list = new LinkedList<AbstractCfgNode>();
		LinkedList<AbstractCfgNode> queue = new LinkedList<AbstractCfgNode>();
		Set<AbstractCfgNode> visited = new HashSet<AbstractCfgNode>();

		queue.add(this.head);
		visited.add(this.head);

		this.bfIteratorHelper(list, queue, visited);

		return list.iterator();
	}

	private void bfIteratorHelper(List<AbstractCfgNode> list, LinkedList<AbstractCfgNode> queue,
			Set<AbstractCfgNode> visited) {

		AbstractCfgNode cfgNode = (AbstractCfgNode) queue.removeFirst();
		list.add(cfgNode);

		for (int i = 0; i < 2; i++) {
			CfgEdge outEdge = cfgNode.getOutEdge(i);
			if (outEdge != null) {
				AbstractCfgNode succ = outEdge.getDest();
				if (!visited.contains(succ)) {
					queue.add(succ);
					visited.add(succ);
				}
			}
		}
		if (queue.size() > 0) {
			bfIteratorHelper(list, queue, visited);
		}
	}

	public LinkedList<AbstractCfgNode> dfPreOrder() {
		LinkedList<AbstractCfgNode> preorder = new LinkedList<AbstractCfgNode>();
		LinkedList<AbstractCfgNode> postorder = new LinkedList<AbstractCfgNode>();
		this.dfIterator(preorder, postorder);
		return preorder;
	}

	public LinkedList<AbstractCfgNode> dfPostOrder() {
		LinkedList<AbstractCfgNode> preorder = new LinkedList<AbstractCfgNode>();
		LinkedList<AbstractCfgNode> postorder = new LinkedList<AbstractCfgNode>();
		this.dfIterator(preorder, postorder);
		return postorder;
	}

	private void dfIterator(LinkedList<AbstractCfgNode> preorder, LinkedList<AbstractCfgNode> postorder) {

		LinkedList<AbstractCfgNode> stack = new LinkedList<AbstractCfgNode>();
		Set<AbstractCfgNode> visited = new HashSet<AbstractCfgNode>();

		AbstractCfgNode current = this.head;
		visited.add(current);
		stack.add(current);
		preorder.add(current);

		while (!stack.isEmpty()) {
			current = stack.getLast();
			AbstractCfgNode next = null;
			for (int i = 0; (i < 2) && (next == null); i++) {
				CfgEdge outEdge = current.getOutEdge(i);
				if (outEdge != null) {
					next = outEdge.getDest();
					if (visited.contains(next)) {
						next = null;
					} else {
					}
				}
			}
			if (next == null) {
				postorder.add(stack.removeLast());
			} else {
				visited.add(next);
				stack.add(next);
				preorder.add(next);
			}
		}
	}
}