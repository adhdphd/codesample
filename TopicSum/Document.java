import java.io.*;
import java.util.*;

public class Document 
{
	private File docLoc;
	private Sentence[] sents;
	private int nsents;
	
	private Topic phid;
	
	public Document(File f)
	{
		docLoc = f;
		
		getSents();
		
		// do not create new topics until after we get sentences!
		phid = new Topic();
	}
	
	private void getSents()
	{
		FileUtil docFile = new FileUtil(docLoc);
		
		ArrayList<Sentence> sents = new ArrayList<Sentence>();
		while(docFile.ready())
		{
			String in = docFile.readLine().trim();
			if(!in.equals(""))
			{
				sents.add(new Sentence(in, nsents, this));
				nsents ++;
			}
		}
		
		this.sents = new Sentence[nsents];
		sents.toArray(this.sents);
		
		docFile.closeFile();
	}
	
	/**
	 * 
	 * @return The document word distribution.
	 */
	public Topic phid()
	{
		return phid;
	}
	
	/**
	 * 
	 * @return the number of sentences in the document.
	 */
	public int nsents()
	{
		return nsents;
	}
	
	/**
	 * 
	 * @param si the sentence to retrieve
	 * @return the si^th sentence, or null if there is no si^th sentence
	 */
	public Sentence getSent(int si)
	{
		if(si >= nsents)
			return null;
		else
			return sents[si];
	}
	
	/**
	 * @return the abstract filename of the document
	 */
	public String toString()
	{
		return docLoc.getName();
	}
}
