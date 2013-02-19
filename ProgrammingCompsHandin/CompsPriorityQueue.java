/**
 * A priority queue based on a minheap. Unlike java.util.PriorityQueue, this implementation does not provide contains(node) or
 * remove(node) methods, however there is a changeKey operation that will update the distance of a node, and then move it into the 
 * correct position in the heap in O(log n) time.
 * 
 * The size of the priority queue is bounded to the total number of nodes in the graph.
 *
 */
public class CompsPriorityQueue 
{
	private int [] heap;
	private int heapSize;
	private int [] heapIndex; // provides a way to look up a node using its ID and find it's position in the heap in constant time
	private Node [] nodes;
	
	
	/**
	 * Creates an empty priority queue with capacity as the number of nodes in the input graph.
	 * 
	 * @param n the Nodes in the graph
	 */
	public CompsPriorityQueue(Node [] n)
	{
		nodes = n;
		heap = new int[nodes.length];
		heapIndex = new int[nodes.length];
		heapSize = 0;
	}
	
	/**
	 * Inserts the Node into this priority queue.
	 * 
	 * @param n the Node to add
	 */
	public void offer(Node n)
	{
		n.setQ(true);
		heapIndex[n.getID()] = heapSize; // keep track of where the node is
		heap[heapSize] = n.getID(); // put the node in the next available space in the heap
		heapSize++;
		
		upHeap(n);
	}
	
	/**
	 * Removes and returns the head of the queue, which is the Node with the least distance to the supply node, or if the 
	 * heap is empty, returns null.
	 * 
	 * @return the head Node of the queue, or null if empty
	 */
	public Node poll()
	{
		if(heapSize == 0)
			return null;
		
		Node head = nodes[heap[0]];
		
		// move the last heap item to the head
		heap[0] = heap[heapSize -1];
		heapIndex[nodes[heap[0]].getID()] = 0;
		downHeap(nodes[heap[0]]);
		heapSize --;
		
		head.setQ(false);
		return head;
	}
	
	/**
	 * Performs an upheap operation to put this node in the correct position on the heap after the distance was changed. 
	 * 
	 * Putting the Node in the correct position on the heap is a O(log n) operation.
	 * 
	 * Precondition: the distance that we changed to to is less than the previous distance from this Node to the supply node.
	 * 
	 * @param n the Node which has its distance being changed
	 */
	public void changeKey(Node n)
	{
		upHeap(n);
	}
	
	/**
	 * Size operation of heap, runs in constant time.
	 * 
	 * @return true if the heap is empty, false if it is not
	 */
	public boolean isEmpty()
	{
		if(heapSize == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Upheaps a node into the correct position.
	 * 
	 * @param n the node to be moved
	 */
	private void upHeap(Node n)
	{
		int pre = (heapIndex[n.getID()] - 1)/2;
		while(true)
		{
		
			if(heapIndex[n.getID()] == 0) // are we already at the head of the heap?
				return;
			
			if(!(n.getDistance() < nodes[heap[pre]].getDistance())) // has heap property
				return;
			
			exchange(n, nodes[heap[pre]]);
			pre = (heapIndex[n.getID()] - 1)/2;
		}
	}
	
	/**
	 * Downheaps a node into the correct position
	 * 
	 * @param n the node to be moved
	 */
	private void downHeap(Node n)
	{
		int descendant = heapIndex[n.getID()]*2 + 1;
		
		while(descendant < heapSize)
		{
			if(descendant + 1 < heapSize) // is there a second descendant?
			{
				if(nodes[heap[descendant]].getDistance() > nodes[heap[descendant + 1]].getDistance())
					descendant ++; // pick the descendant with the minimum distance
			}
			
			if(n.getDistance() < nodes[heap[descendant]].getDistance())
				return; // has heap property
			
			exchange(n, nodes[heap[descendant]]);
			descendant = descendant*2 + 1;
		}
	}
	
	/**
	 * Swap the positions of two nodes in the heap. Changes both the heap, and the heapIndex for locating Nodes in the heap.
	 * 
	 * @param a
	 * @param b
	 */
	private void exchange(Node a, Node b)
	{
		int t = heapIndex[a.getID()];
		heapIndex[a.getID()] = heapIndex[b.getID()];
		heap[heapIndex[a.getID()]] = a.getID();
		
		heapIndex[b.getID()] = t;
		heap[heapIndex[b.getID()]] = b.getID();
	}
	
	

}
