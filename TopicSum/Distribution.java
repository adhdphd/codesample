
public class Distribution 
{
	private double [] count;
	private double total;
	
	public Distribution(int ntypes)
	{
		count = new double[ntypes];
		total = 0.0;
	}
	
	public Distribution()
	{
		count = new double[TextUtil.getInstance().ntypes()];
		total = 0.0;
	}
	
	private Distribution(double t, double [] c)
	{
		total = t;
		count = c;
	}
	
	/**
	 * Downweight a type in the distribution. If the type is not in the distribution, do nothing.
	 * 
	 * @param type the type to downweight in the distribution
	 */
	public void downweight(int type)
	{
		if(type < count.length)
		{
			double oldweight = pw(type);
			double newweight = oldweight * oldweight;
			total -= (oldweight - newweight);
		
			count[type] = newweight * total;
		}
	}
	
	/**
	 * Completely remove a type from this distribution. If the type is not in this distribution, do nothing.
	 * 
	 * @param type the type to remove from the distribution
	 */
	public void remove(int type)
	{
		if(type < count.length)
		{
			total -= count[type];
			count[type] = 0;
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return the weight of the type in this distribution
	 */
	public double pw(int type)
	{
		if(type < count.length)
		{
			double pw = 0.0;
			if(total != 0)
				pw =  count[type] / total;
			
			return pw;
		}
		else
			return 0.0;
	}
	
	public void add(int type, double toadd)
	{
		if(type < count.length)
		{
			count[type] += toadd;
			total += toadd;
		}
		else
			System.out.println("can't add to this type");
	}
	
	public void setToZero(int type)
	{
		remove(type);
	}
	
	public Distribution getCopy()
	{
		double [] c = new double[count.length];
		
		for(int i = 0; i < c.length; i ++)
		{
			c[i] = count[i];
		}
		
		return new Distribution(total, c);
	}
}
