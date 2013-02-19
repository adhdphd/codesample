public class Sentence 
{	
	private String sent; // the original sentence
	private int nwords; // the number of words in the sentence
	
	private Document doc; // the document that this sentence is from
	private int nsent; // the sentence this is from the original document
	
	
	private int[] tokens; // the tokens in this sentence
	private int[] topics; // the topics that generated these tokens
	
	private int[] tcount; // the count of each type of topic in this sentence
	
	
	/**
	 * @param s The original text of the sentence
	 */
	public Sentence(String sent, int nsent, Document doc)
	{
		this.sent = sent;
		this.nsent = nsent;
		this.doc = doc;
		
		tokens = TextUtil.readSent(this.sent);
		nwords = tokens.length;
		topics = new int[tokens.length];
		
		tcount = new int[4];
		tcount[Topic.BACKGROUND] = topics.length; // add the count of all the topics
	}
	
	/**
	 * 
	 * @param ti the index of the token we are updating
	 * @param topic the new topic being assigned
	 */
	public void setTopic(int ti, int topic)
	{
		tcount[topics[ti]] --; // subtract one from count of previous topic
		topics[ti] = topic; // update the assigned topic for this token
		tcount[topic] ++; // add one to count of the new topic
	}
	
	/**
	 * 
	 * @param topic
	 * @return
	 */
	public int count(int topic)
	{
		return tcount[topic];
	}
	
	/**
	 * 
	 * @return the number sentence this sentence is from its original document
	 */
	public int nsent()
	{
		return nsent;
	}
	
	/**
	 * 
	 * @return the original document that this sentence is from
	 */
	public Document getDoc()
	{
		return doc;
	}
	
	/**
	 * 
	 * @param ti index of the token
	 * @return word type of the token
	 */
	public int getType(int ti)
	{
		return tokens[ti];
	}
	
	public int getTopic(int ti)
	{
		return topics[ti];
	}
	
	public int nwords()
	{
		return nwords;
	}
	
	public String getOriginal()
	{
		return sent;
	}
	
}
