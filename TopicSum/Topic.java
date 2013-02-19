
public class Topic 
{
	private int [] typeCount;
	private int totalCount;
	
	public static final int BACKGROUND = 0;
	public static final int CONTENT = 1;
	public static final int DOCUMENT = 2;
	public static final int CONTENTSPECIFIC = 3;
	
	public Topic()
	{	
		typeCount = new int[TextUtil.getInstance().ntypes()];
		totalCount = 0;
	}
	
	public void decrementTypeCount(int type)
	{
		totalCount --;
		typeCount[type]--;
	}
	
	public void incrementTypeCount(int type)
	{
		totalCount ++;
		typeCount[type]++;
	}
	
	/**
	 * 
	 * @param type the word type
	 * @return the number of tokens of type assigned to this topic
	 */
	public int getCount(int type)
	{
		if(type >= typeCount.length)
			return 0;
		
		return typeCount[type];
	}
	
	
	/**
	 * 
	 * @param type the word type
	 * @param gamma the pseudocount of the word in this topic
	 * @return the probability of the word in this topic
	 */
	public double pword(int type, double gamma)
	{
		double pw = ((double)getCount(type)) + gamma;
		pw = pw / (((double)getTotal()) + ((double)TextUtil.getInstance().ntypes())*gamma);
		return pw;
	}
	
	public int getTotal()
	{
		
		return totalCount;
	}
}
