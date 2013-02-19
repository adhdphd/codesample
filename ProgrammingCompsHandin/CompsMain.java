import java.lang.management.*;

/**
 * The main class of the program. The code for dijkstra's algorithm is here, as well as the code for finding lambda and the max flow.
 *
 */
public class CompsMain
{	
	
	public static final boolean verbose = false;
	
	/**
	 * The main method, which contains the code for dijkstra's algorithm.
	 * 
	 * @param args the input and the output files for this program
	 */
	public static void main(String[] args)
	{			
		
		if(verbose)
			System.out.println("Reading input file...");
		
		// read in the file
		FileIO io = new FileIO(args[0], args[1]);
		
		
		
		//build nodes
		Node[] nodes = io.buildNodes();
		
		
		
		// build edges
		int [] targets = io.getTarget();
		double [] weights = io.getWeights();
		
		
		// start timer
		double startingtime = time();
		
		if(verbose)
			System.out.println("Starting timer...");
		
		// find starting node
		Node start = findSupplyNode(nodes); // find starting node
		start.setDistance(0); 
		start.inS(); // add start to the set of explored nodes
		
		
		
		if(verbose)
			System.out.println("Building the priority queue...");
		
		CompsPriorityQueue pq = new CompsPriorityQueue(nodes);
		
		// update the min distance, and set predecessor and previous edge for nodes connected to the start
		Node v = null;
		for(int c = start.getStartIndex(); c < start.getStartIndex() + start.getArcsOut(); c++)
		{
			v = nodes[targets[c]];

			if(!v.getS()) // in case there is an arc that connects back to the start node
			{
				v.setPrevEdge(c);
				v.setPredecessor(start);

				if(!v.inQueue())// add v to priority queue or update its position if it is already there
				{
					v.setDistance(weights[c]);
					pq.offer(v);
				}
				else // change w's distance and update its position in the priority queue
				{
					v.setDistance(weights[c]);
					pq.changeKey(v); // update value of d'(w)
				}
			}
		}
		
		
		
		if(verbose)
			System.out.println("Starting dijkstra's algorithm ... ");
		
		// dijsktra's algorithm
		while(!pq.isEmpty())
		{	
			v = pq.poll(); // choose the unexplored node with the closest distance to the start node
			v.inS(); // add v to the set of explored nodes
			
			// now we know for sure that v's predecessor is not a leaf
			if(v.getPredecessor() != null)
				v.getPredecessor().notLeaf();
			else
			{
				System.out.println("Error: Graph is not connected.");
				System.exit(2);
			}
			
			Node w;
			// checks all of the nodes that can be followed by v's arcs.
			for(int c = v.getStartIndex(); c < v.getStartIndex() + v.getArcsOut(); c++)
			{
				w = nodes[targets[c]];
				
				// if a node is not already explored and now has a shorter path to the supply node through v
				if(!w.getS() && w.getDistance() > v.getDistance() + weights[c])
				{	
					// update w's shortest path so it passes through v and the arc c
					w.setPredecessor(v);
					w.setPrevEdge(c);
					
					// add w to priority queue or update its position if it is already there
					if(!w.inQueue())
					{
						w.setDistance(v.getDistance() + weights[c]);
						pq.offer(w);
					}
					else // change w's distance and update its position in the priority queue
					{
						w.setDistance(v.getDistance() + weights[c]);
						pq.changeKey(w); // update value of d'(w)
					}
				}
			}
		}	
		
		
		if(verbose)
			System.out.println("Calculating max flows... ");
		
		//find lambda and the flows
		double [] maxFlow = maxFlow(io.getCapacity(), nodes);
		
		System.out.println("Elapsed time is : " + (time() - startingtime) + " seconds");
		
		if(verbose)
			System.out.println("Writing output file...");
		
		// finally, set the flow
		// Once the flow is set, the FileIO object will write the output.
		io.setFlow(maxFlow);
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Returns the amount of time (in seconds) that this thread has been running. Since this is not a multi-threaded 
	 * application, we can use this as a CPU timer for this program.
	 * 
	 * @return The current time of this thread, in seconds.
	 */
	public static double time()
	{
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		if(bean.isCurrentThreadCpuTimeSupported())
		{
			double time = (double)(bean.getCurrentThreadCpuTime()/1000000);
			return time/1000;
		}
		else
		{
			System.err.println("Error: The timer is not supported.");
			return -1;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Traces back through the previous nodes and paths in order to find the max flow for each arc.
	 * 
	 * @param capacity an integer array containing the capacities for each of the arcs
	 * @param nodes the array of Nodes
	 * @return double array containing the maxFlow for each of the arcs
	 */
	public static double[] maxFlow(int [] capacity, Node [] nodes)
	{
		double[] totalDemand = new double[capacity.length];
		
		Node p;
		double lambda = Double.MAX_VALUE, compare;
		
		for(Node n : nodes)
		{
			if(!n.getS())
			{
				System.out.println("Error: Graph is not connected.");
				System.exit(2);
			}
			
			if(n.isLeaf()) // we can save some time by only tracing up on nodes we know won't be covered by another node's path
			{	
				p = n;
				double d = 0;
				while(p.getDemand() != -1) // while we are not at the start node
				{
					d += p.getDemand();
					p.meetDemand(); // set the demand to zero so that we don't add this node in again
					
					totalDemand[p.getPrevEdge()] += d;
					
					// see if we've found lambda
					compare = ((double)capacity[p.getPrevEdge()]) / totalDemand[p.getPrevEdge()];
					if(compare < lambda)
						lambda = compare;
					
					p = p.getPredecessor();
				}
			}
		}
		
		if(verbose)
			System.out.println("Lambda is " + lambda);
		
		// maximize the flow by multiplying by lambda
		for(int c = 0; c < totalDemand.length; c++)
			totalDemand[c] = totalDemand[c]*lambda;
		
		return totalDemand;
	}
	
	
	
	
	
	/**
	 * Find the supply node (the start node) of the graph. The supply node is the node that has a demand of -1.
	 * 
	 * @param nodes an array of the nodes of this graph
	 * @return the supply node of this graph
	 */
	public static Node findSupplyNode(Node[] nodes)
	{
		Node start = null;
		for(int c = 0; c < nodes.length; c++)
		{
			if(nodes[c].getDemand() == -1)
			{
				start = nodes[c];
				break;
			}
		}
		if(start == null)
		{
			System.out.println("Error: There is no node with demand -1.");
			System.exit(1);
		}
		
		return start;
	}
	
}
