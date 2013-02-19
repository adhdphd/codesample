import java.io.*;

public class Cluster 
{
	private Document[] docs;
	private int ndocs;

	private Topic phic_0;
	private Topic[] phic_k;
	
	private File clusterLoc;
	
	public Cluster(File f)
	{
		clusterLoc = f;
		ndocs = 0;
		
		docs = new Document[clusterLoc.listFiles().length];
		
		getDocs();
		
		// we don't want to make new topics until after the docs are read, or else the number of possible word types won't be correct
		phic_0 = new Topic();
		phic_k = new Topic[0]; // default is no subtopics
		
	}
	
	private void getDocs()
	{
		
		for(int i = 0; i < clusterLoc.listFiles().length ; i ++)
		{
			// if the file is not a hidden file
			if(clusterLoc.listFiles()[i].getName().charAt(0) != '.' && clusterLoc.listFiles()[i].getName().charAt(clusterLoc.listFiles()[i].getName().length() -1) != '~' )
			{
				docs[i] = new Document(clusterLoc.listFiles()[i]);
				ndocs++;
			}
		}
	}
	
	public int ndocs()
	{
		return ndocs;
	}
	
	/**
	 * Set the number of content subtopics to use for this cluster.
	 * @param K the number of content subtopics.
	 */
	public void setSubtopics(int K)
	{
		phic_k = new Topic[K];
		
		for(int k = 0; k < K; k++)
		{
			phic_k[k] = new Topic();
		}
	}
	
	/**
	 * 
	 * @return The general content distribution.
	 */
	public Topic phic()
	{
		return phic_0;
	}
	
	/**
	 * Get a subtopic of the content distribution.
	 * 
	 * @param k the subtopic to return
	 * @return The subtopic phic_k, or null if there is no subtopic k.
	 */
	public Topic phic(int k)
	{
		if(phic_k.length == 0 || phic_k.length <= k)
			return null;
		else
			return phic_k[k];
	}
	
	public Document getDoc(int d)
	{
		return docs[d];
	}
	
	public String getName()
	{
		return clusterLoc.getName();
	}
	
}
