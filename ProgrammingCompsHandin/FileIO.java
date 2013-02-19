import java.io.*;

/**
 * A class for handling file input and output, and parsing the information from the input file. The input file is read during 
 * the construction of this object. The information from the input file is stored here, and then after the main class finds the flow
 * and maximizes the flow for each edge, it passes the values of the flow to this object, which then writes the output file.
 * 
 *
 */
public class FileIO 
{
	private static final boolean verbose = false; // for debugging
	
	// Files
	private File in;
	private File out;
	
	// number of nodes and arcs
	private int n_nodes;
	private int n_arcs;
	
	
	// information read in from the files
	private int [] startIndex;
	private int [] target;
	private int [] capacity;
	private double [] weights;
	private double [] demand;
	private double [] flow;	
	
	
	/**
	 * Constructor for FileIO class
	 * 
	 * @param inputLoc The full path name of the input file.
	 * @param outputLoc The full path name of the output file.
	 */
	public FileIO(String inputLoc, String outputLoc)
	{
		in = new File(inputLoc);
		out = new File(outputLoc);
		
		readFile();
		
		flow = null;
	}
	
	/**
	 * Reads the input file, and stores all the information about the graph.
	 */
	private void readFile()
	{
		// for reading in from the file
		BufferedReader br;

		try 
		{
			br = new BufferedReader(new FileReader(in));
			
			// read the first line: number of nodes on the graph
			n_nodes = Integer.parseInt(br.readLine());
			
			if(verbose)
				System.out.println("Read number of nodes.");
			
			// read the second line: start indexes of the arcs coming from each node
			startIndex = new int[n_nodes + 1];
			for(int c = 0; c < startIndex.length; c++)
			{
				startIndex[c] = Integer.parseInt(readWord(br));
			}
			n_arcs = startIndex[startIndex.length -1];
			for(int c = 0; c < startIndex.length; c++)
			{
				if(startIndex[c] < 0 || startIndex[c] > n_arcs)
				{
					System.err.println("Input error: Problem with start index.");
					System.exit(1);
				}
			}
			
			if(verbose)
				System.out.println("Read start indexes.");

			// read the third line: target nodes of each of the arcs
			target = new int[n_arcs];
			for(int c = 0; c < target.length; c++)
			{
				target[c] = Integer.parseInt(readWord(br));
				if(target[c] >= n_nodes || target[c] < 0)
				{
					System.err.println("Input error: Problem with target nodes of arcs.");
					System.exit(1);
				}
			}
			
			
			if(verbose)
				System.out.println("Read target nodes of the arcs.");
			
			
			// read the fourth line: capacity of each arc
			capacity = new int[n_arcs];
			for(int c = 0; c < capacity.length; c++)
			{
				capacity[c] = Integer.parseInt(readWord(br));
				if(capacity[c] <= 0)
				{
					System.err.println("Input error: Capacities must be natural numbers.");
					System.exit(1);
				}
			}
			
			if(verbose)
				System.out.println("Read capacities of the arcs.");
			
			
			// read the fifth line: the weights of each arc
			weights = new double[n_arcs];
			for(int c = 0; c < weights.length; c++)
			{
				weights[c] = Double.parseDouble(readWord(br));
				if(weights[c] < 0)
				{
					System.err.println("Input error: Cannot have negative weights.");
					System.exit(1);
				}
			}
			
			if(verbose)
				System.out.println("Read weights of the arcs.");
			
			
			// read the sixth line: the demand for each node
			demand = new double[n_nodes];
			int supplyNode = 0;
			for(int c = 0; c < demand.length; c++)
			{
				demand[c] = Double.parseDouble(readWord(br));
				if( ((demand[c] < 0)&&(demand[c]!=-1))  || demand[c] > 1)
				{
					System.err.println("Input error: Demands must be non-negative (except for supply node) and the demand of the nodes must sum to one.");
					System.exit(1);
				}
				else if(demand[c] == -1)
				{
					supplyNode ++;
				}
			}
			if(supplyNode != 1)
			{
				System.err.println("Input error: There must be exactly one supply node.");
				System.exit(1);
			}
			
			if(verbose)
				System.out.println("Read deamnds of the nodes.");
			
			
			
			br.close();

		} 
		catch (Exception e) 
		{
			System.err.println("Error reading file:  " + in.getName());
			System.err.println(e.toString());
			System.exit(1);
		}
	}
	
	
	
	/**
	 * Returns the next word from the text file. In this case, a word is a sequence of characters that does not
	 * contain any spaces or newline characters, so that each number from the input file is read in as a word.
	 * 
	 * @param br the BufferedReader that is reading the file
	 * @return the next word from the text file
	 * @throws IOException
	 */
	private String readWord(BufferedReader br) throws IOException
	{
		StringBuffer s = new StringBuffer();
		char c = (char)br.read();
		
		while(c != ' ' && c != '\n')
		{
			s.append(c);
			c = (char)br.read();
		}
		
		if(s.length() == 0 || s.toString().trim().equals(""))
			return readWord(br);
		else
			return s.toString();
	}
	
	
	
	/**
	 * Writes the program's output file.
	 * Precondition: The flow must be set first.
	 */
	private void writeTextFile() 
	{
		if(flow == null)
		{
			System.err.println("Error: Cannot write the output file until the program sets the flow.");
			return;
		}
		
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new FileWriter(out));
			
			// write the first line: number of nodes of the graph
			bw.write(String.valueOf(n_nodes));
			bw.write("\n");

			// write the second line: start indexes of the nodes (followed by the number of arcs)
			for(int c = 0; c < startIndex.length; c++)
			{
				bw.write(String.valueOf(startIndex[c]) + " ");
			}
			bw.write("\n");
			
			// write the third line: the target nodes of the arcs
			for(int c = 0; c < target.length; c++)
			{
				bw.write(String.valueOf(target[c]) + " ");
			}
			bw.write("\n");
			
			// write the fourth line: rational numbers giving the flow on each arc
			for(int c = 0; c < flow.length; c++)
			{
				bw.write(String.format("%f",flow[c]) + " "); // output double as fixed precision
			}
			bw.write("\n");
			bw.close();
		} 
		catch (Exception e) 
		{
			System.err.println("FileUtil writeTextFile error, file:  " + out.getName());
			System.err.println(e.toString());
		}
	}
	
	
	
	
	
	


	
	
	


	/**
	 * 
	 * @return the number of Nodes in the graph
	 */
	public int getNumNodes()
	{
		return n_nodes;
	}
	
	/**
	 * Returns the start index array, which is an array which gives the start index of all arcs coming out of each Node.
	 * The n+1 element of the array contains the total number of arcs.
	 * 
	 * @return the start index array
	 */
	public int [] getStartIndex()
	{
		return startIndex;
	}
	
	/**
	 * 
	 * @return an array which contains the target Node for each arc
	 */
	public int [] getTarget()
	{
		return target;
	}
	
	/**
	 * 
	 * @return an array with contains the capacity of each arc
	 */
	public int [] getCapacity()
	{
		return capacity;
	}
	
	/**
	 * 
	 * @return an array with contains the weight of each arc
	 */
	public double [] getWeights()
	{
		return weights;
	}
	
	/**
	 * 
	 * @return an array which contains the demand of each Node
	 */
	public double [] getDemand()
	{
		return demand;
	}
	
	/**
	 * @return the number of arcs in the graph
	 */
	public int getNumArcs()
	{
		return n_arcs;
	}
	
	
	
	

	
	
	
	/**
	 * Builds Node objects for the graph described in the input file, and outputs them as an array.
	 * 
	 * @return an array of Nodes
	 */
	public Node[] buildNodes()
	{
		Node[] nodes = new Node[startIndex.length -1];
		for(int c = 0; c < nodes.length; c++)
		{
			int arcsOut = startIndex[c+1] - startIndex[c];
			
			nodes[c] = new Node(demand[c], startIndex[c], arcsOut, c);
		}
		return nodes;
	}
	
	/**
	 * Set the maximized flow on the arcs. Once the flow is set, the final results will be written to the output file.
	 * 
	 * @param flow the maximized flow on all the arcs
	 */
	public void setFlow(double [] flow)
	{
		this.flow = flow;
		
		writeTextFile();
	}
	
	
}
