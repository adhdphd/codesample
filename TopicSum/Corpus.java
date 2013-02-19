import java.io.*;

public class Corpus 
{
	private File corpusLoc;
	private int nclusters;
	
	private Cluster[] clusters;
	
	private Topic phib;
	
	public Corpus(File f)
	{
		corpusLoc = f;
		nclusters = corpusLoc.listFiles().length;
		clusters = new Cluster[nclusters];
		
		getClusters();
		
		//Do not create topic until after we get clusters!
		phib = new Topic();
	}
	
	private void getClusters()
	{
		for(int ci = 0; ci < nclusters; ci++)
		{
			clusters[ci] = new Cluster(corpusLoc.listFiles()[ci]);
		}
	}
	
	public int nclusters()
	{
		return nclusters;
	}
	
	public Cluster getCluster(int c)
	{
		return clusters[c];
	}
	
	public Cluster[] getCluster()
	{
		return clusters;
	}
	
	/**
	 * 
	 * @return The background word distribution for this corpus.
	 */
	public Topic phib()
	{
		return phib;
	}

}
