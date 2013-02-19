
/**
 *  An object that represents a node in the input graph. This object has basic information about the node, keeps track of the 
 *  minimum-path distance and whether or not this Node is in the set of explored nodes (for dijkstra's algorithm), and the 
 *  preceding node in the shortest path, the preceding arc, and whether or not the Node is part of any other Node's minimum path
 *  (to be used for calculating lambda and the maxFlows for each arc).
 *
 */
public class Node 
{
	private double demand;
	private int startIndex;
	private int arcsOut;
	private int id;
	
	// for dijkstra's algorithm
	private double distance;
	private boolean inS;
	private boolean inQ;
	
	//to trace back
	private Node predecessor;
	private int prev_edge;
	private boolean leaf;
	
	
	
	
	
	/**
	 * Constructs a new Node
	 * 
	 * @param demand the demand of this node (can be >=0 or -1 if it is the supply node)
	 * @param startIndex the start index for the arcs leading out of this node
	 * @param arcsOut the number of arcs leading out of this node
	 * @param id the index of where this node is in the node array
	 */
	public Node(double demand, int startIndex, int arcsOut, int id)
	{
		this.demand = demand;
		this.startIndex = startIndex;
		this.arcsOut = arcsOut;
		this.id = id;
		
		// for dijkstra's algorithm
		this.distance = Integer.MAX_VALUE;
		inS = false;
		inQ = false;
		
		//to trace back
		predecessor = null;
		prev_edge = -1;
		leaf = true;
	
		
	}
	
	
	
	
	/**
	 * Returns true if this node is a leaf, and false if it is not. A node is a leaf if it is not a predecessor to any 
	 * other node in S. All nodes are initially set as leaves.
	 * 
	 * @return true if this Node is a leaf
	 */
	public boolean isLeaf()
	{
		return leaf;
	}
	
	
	
	
	/**
	 * Returns the number of the edge that is part of the least-distance path to 
	 * this node, -1 if it is unknown.
	 * 
	 * @return the previous edge of this node
	 */
	public int getPrevEdge()
	{
		return prev_edge;
	}
	
	
	
	
	/**
	 * Returns true if this Node is in S, the set of explored nodes for 
	 * Dijkstra's algorithm; false if it is not.
	 * 
	 * @return true if this Node is in the set of the explored nodes
	 */
	public boolean getS()
	{
		return inS;
	}
	
	/**
	 * Returns the index of this Node in the node array. This is useful for the PriorityQueue locating nodes in the heap in constant
	 * time, as well as for debugging purposes. 
	 * 
	 * @return the index of this Node
	 */
	public int getID()
	{
		return id;
	}
	
	
	
	
	/**
	 * 
	 * @return true if the Node is in the priority queue, false if it is not
	 */
	public boolean inQueue()
	{
		return inQ;
	}
	
	
	
	
	/**
	 * Returns the previous node from the least-distance path to this node,
	 * null if it is unknown.
	 * 
	 * @return the node that is previous to this
	 */
	public Node getPredecessor()
	{
		return predecessor;
	}
	
	
	
	
	
	

	/**
	 * Returns this Node's demand. 
	 * 
	 * The demand of a node is either -1 for the supply node, or >=0 for the rest of the nodes, 
	 * and the demand for the rest of the nodes sum to 1. 
	 * 
	 * Nodes can have a demand of zero, those nodes are called throughput Nodes.
	 * 
	 * @return the demand of this Node
	 */
	public double getDemand() {
		return demand;
	}

	
	
	
	
	/**
	 * Returns the start index for where this Node's arcs are in the weight, capacity, and target arrays.
	 * 
	 * @return the start index of this node
	 */
	public int getStartIndex() {
		return startIndex;
	}

	
	
	
	
	/**
	 * 
	 * @return the number of arcs leading out of this Node
	 */
	public int getArcsOut() {
		return arcsOut;
	}
	
	
	
	
	
	/**
	 * Returns the least known distance from this Node to the supply node.
	 * The distance starts as the maximum value of the integer.
	 * 
	 * @return the least known distance from the supply Node to this node
	 */
	public double getDistance()
	{
		return distance;
	}
	
	
	
	
	
	
	/**
	 * Returns the edge that leads to this Node in the shortest (known) path to this node from the supply node.
	 * 
	 * If there are no known paths to this node, then this returns -1.
	 * 
	 * @param prev_edge the previous edge in the shortest path to this node
	 */
	public void setPrevEdge(int prev_edge)
	{
		if(getS())
		{
			System.err.println("Error in Node " + getID());
			System.err.println("The previous edge is not supposed to change once the node is in the set of explored nodes.");
			System.exit(2);
		}
		this.prev_edge = prev_edge;
	}
	
	
	
	/**
	 * 
	 * Set to true if the node is in the Priority Queue, false when it is not.
	 * 
	 * @param inQ
	 */
	public void setQ(boolean inQ)
	{
		this.inQ = inQ;
	}
	
	
	
	
	/**
	 * Returns the Node that is the previous Node in the shortest (known) path to this node from the supply node. 
	 * 
	 * If there are no known paths to this node, then this returns null.
	 * 
	 * @param predecessor the previous node in the 
	 */
	public void setPredecessor(Node predecessor)
	{
		if(getS())
		{
			System.err.println("Error in Node " + getID());
			System.err.println("The predecessor is not supposed to change once the node is in the set of explored nodes.");
			System.exit(2);
		}
		this.predecessor = predecessor;
	}
	
	
	
	
	/**
	 * Returns the distance of the shortest (known) path from the supply node to this Node.
	 * 
	 * @param distance
	 */
	public void setDistance(double distance)
	{
		if(getS())
		{
			System.err.println("Error in Node " + getID());
			System.err.println("The distance is not supposed to change once the node is in the set of explored nodes.");
			System.exit(2);
		}
		this.distance = distance;
	}
	
	
	
	
	
	/**
	 * 
	 * Meet the demand of a node, setting its demand to zero. This is to ensure that we don't add in a single node's demand 
	 * multiple times.
	 * 
	 */
	public void meetDemand()
	{
		demand = 0;
	}
	
	
	
	/**
	 * Sets the value inS to true. This method is called if this Node has been added to the set of nodes that are in the
	 * "explored" part of the graph.
	 */
	public void inS()
	{
		inS = true;
	}
	
	/**
	 * Sets the leaf property of this node to false. This method is called if this Node is the
	 * predecessor of a Node when that Node is added to the set of selected Nodes.
	 */
	public void notLeaf()
	{
		leaf = false;
	}
}
