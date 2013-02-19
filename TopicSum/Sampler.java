import java.util.*;

/**
 * The TopicSum sampler from "Content Models for Multi-Document Summarization" 
 * by Aria Haghighi & Lucy Vanderwende (2009) 
 * 
 * 
 *
 */
public class Sampler 
{
	private Random r;
	
	private double alphab;
	private double alphac;
	private double alphad;
	private double talpha;
	
	private double betab;
	private double betac;
	private double betad;
	
	
	private Topic btopic;
	private Topic [] ctopic;
	private Topic [][] dtopic;
	
	private Corpus corpus;
	private int nclusters; 	// the number of clusters
	private int docsperc; // the most docs that are in any cluster
	private int ntypes;

	
	public Sampler(Corpus c)
	{
		corpus = c;
		nclusters = corpus.nclusters();
		ntypes = TextUtil.getInstance().ntypes();
		
		r = new Random();
		
		// set values of hyperparameters
		alphab = 10.0;
		alphac = 1.0;
		alphad = 5.0;
		talpha = alphab + alphac + alphad;
		
		betab = 1.0;
		betac = 0.1;
		betad = 1.0;
		
		//initialize topics
		btopic = new Topic();
		ctopic = new Topic[nclusters];
		docsperc = 0;
		for(int ci = 0; ci < nclusters; ci++)
		{
			ctopic[ci] = new Topic();
			if(corpus.getCluster(ci).ndocs() > docsperc)
				docsperc = corpus.getCluster(ci).ndocs();
		}
		
		dtopic = new Topic[nclusters][docsperc];
		for(int ci = 0; ci < nclusters; ci++)
		{
			for(int di = 0; di < docsperc; di++)
			{
				if(di < corpus.getCluster(ci).ndocs())
					dtopic[ci][di] = new Topic();
				else
					dtopic[ci][di] = null;
			}
		}
		
		randominit();
	}
	
	
	/**
	 * initialize the topics randomly 
	 * 
	 * note: I asked Aria and he specifically said he did not randomize the models randomly
	 * but for topic sum it doesn't really matter
	 * 
	 */
	private void randominit()
	{
		for(int ci = 0; ci < nclusters; ci++) // for every cluster
		{
			for(int di = 0; di < docsperc; di++) // for every document
			{
				Topic b = btopic;
				Topic c = ctopic[ci];
				Topic d = dtopic[ci][di];
				
				if(di < corpus.getCluster(ci).ndocs()) 
				{					
					for(int si = 0; si < corpus.getCluster(ci).getDoc(di).nsents(); si++) // for every sentence
					{
						Sentence s = corpus.getCluster(ci).getDoc(di).getSent(si);
						
						for(int wi = 0; wi < s.nwords(); wi++) // for every word
						{
							double rand = r.nextDouble();
							
							// randomly assign a topic to the token
							if(rand > 0.666666)
							{
								s.setTopic(wi, Topic.BACKGROUND);
								b.incrementTypeCount(s.getType(wi));
							}
							else if(rand > 0.333333)
							{
								s.setTopic(wi, Topic.CONTENT);
								c.incrementTypeCount(s.getType(wi));
							}
							else
							{
								s.setTopic(wi, Topic.DOCUMENT);
								d.incrementTypeCount(s.getType(wi));
							}
						}
					}
				}			
			}
		}
	}
	
	/**
	 * estimate the topics, do not print the log likelihoods
	 * 
	 * @param iterations
	 */
	public void estimate(int iterations)
	{
		estimate(iterations, -1);
	}
	
	/**
	 * 
	 * @param iterations
	 * @param lcount how often to find the log likelihoood
	 */
	public void estimate(int iterations, int lcount)
	{
		System.out.println("Iteration \t Log-likelihoood");
		for(int i = 0; i < iterations; i++)
		{
			iteration(false); // one iteration of gibbs sampler
			if(lcount != -1 && i%lcount == 0)
				System.out.println(i + "\t" + loglikelihood());
		}
		
		iteration(true); // not a real iteration, but on the last 
		System.out.println("Final Iteration:" + "\t" + loglikelihood());
	}
	
	
	private void iteration(boolean lastIteration)
	{
		for(int ci = 0; ci < corpus.nclusters(); ci++)
		{
			for(int di = 0; di < corpus.getCluster(ci).ndocs(); di ++)
			{
				for(int si = 0; si < corpus.getCluster(ci).getDoc(di).nsents(); si++)
				{
					sampleSentence(ci, di, si, lastIteration); // sample words from this sentence
				}
			}
		}
	}


	/**
	 * 
	 * 
	 * @param ci cluster index
	 * @param di document index 
	 * @param si sentence index
	 * @param lastIteration false if sample the topic randomly, true if we just pick the most likely topics
	 */
	private void sampleSentence(int ci, int di, int si, boolean lastIteration)
	{
		Topic b = btopic;
		Topic c = ctopic[ci];
		Topic d = dtopic[ci][di];
		
		Sentence s = corpus.getCluster(ci).getDoc(di).getSent(si);
		
		for(int ti = 0; ti < s.nwords(); ti++)
		{
			double bands = (double)s.count(Topic.BACKGROUND) + alphab;
			double cands = (double)s.count(Topic.CONTENT) + alphac;
			double dands = (double)s.count(Topic.DOCUMENT) + alphad;
			
			// remove this token from the topic counts
			if(s.getTopic(ti) == Topic.BACKGROUND)
			{
				b.decrementTypeCount(s.getType(ti));
				bands = bands - 1.0;
			}
			else if(s.getTopic(ti)== Topic.CONTENT)
			{
				c.decrementTypeCount(s.getType(ti));
				cands = cands - 1.0;
			}
			else if(s.getTopic(ti) == Topic.DOCUMENT)
			{
				d.decrementTypeCount(s.getType(ti));
				dands = dands - 1.0;
			}
			
			// build a distribution over topics for this token
			double wandb = (double)b.getCount(s.getType(ti)) + betab;
			double wandc = (double)c.getCount(s.getType(ti)) + betac;
			double wandd = (double)d.getCount(s.getType(ti)) + betad;
			
			double allands = bands + cands + dands;
			
			double bw, cw, dw;
			bw = ( wandb / ( (double)b.getTotal() + (double)ntypes*betab) )  * 
				( bands / allands );
			cw = ( wandc / ( (double)c.getTotal() + (double)ntypes*betac) ) * 
				( cands / allands );
			dw = ( wandd / ( (double)d.getTotal() + (double)ntypes*betad) ) * 
				( dands / allands);
			
			double topicWeightSum = bw + cw + dw;
			
			int sample;
			if(!lastIteration) // select the topic for this token using weighted random sample
			{
				double rand = r.nextDouble();
				if(rand > (cw + dw)/topicWeightSum)
					sample = Topic.BACKGROUND;
				else if(rand > dw/topicWeightSum)
					sample = Topic.CONTENT;
				else
					sample = Topic.DOCUMENT;
			} 
			else // simply use the most likely topic
			{
				if(cw > dw && cw > bw)
					sample = Topic.CONTENT;
				else if(dw > bw)
					sample = Topic.DOCUMENT;
				else
					sample = Topic.BACKGROUND;
			}
			
			
			s.setTopic(ti, sample);
			
			// put the new topic into the counts
			if(sample == Topic.BACKGROUND)
				b.incrementTypeCount(s.getType(ti));
			else if(sample == Topic.CONTENT)
				c.incrementTypeCount(s.getType(ti));
			else if(sample == Topic.DOCUMENT)
				d.incrementTypeCount(s.getType(ti));
			
			
		}
	}
	

	
	public Distribution phic(int c)
	{
		return getDist(ctopic[c], betac);
	}
	
	public Distribution phid(int c, int d)
	{
		return getDist(dtopic[c][d], betad);
	}
	
	public Distribution phib()
	{
		return getDist(btopic, betab);
	}
	

	public static Distribution getDist(Topic t, double beta)
	{
		Distribution dist = new Distribution();
		
		for(int i = 0; i < TextUtil.getInstance().ntypes(); i++)
		{
			dist.add(i, (double)t.getCount(i) + beta);
		}
		return dist;
	}
	
	/**
	 * 
	 * this tends to stabilize pretty quickly, like 25-50 iterations on the DUC data
	 * 
	 * @return log likelihood of the data at this point 
	 */
	public double loglikelihood()
	{
		double loglikelihood = 0.0;
		
		double[] topicLogGamma = new double[3];
		topicLogGamma[Topic.BACKGROUND] = Gamma.logGamma(alphab);
		topicLogGamma[Topic.CONTENT] = Gamma.logGamma(alphac);
		topicLogGamma[Topic.DOCUMENT] = Gamma.logGamma(alphad);
		
		
		// P(z)
		for(int ci = 0; ci < nclusters; ci++)
		{
			for(int di = 0; di < corpus.getCluster(ci).ndocs(); di++)
			{
				// second half of equation
				for(int si = 0; si < corpus.getCluster(ci).getDoc(di).nsents(); si++)
				{
					Sentence s = corpus.getCluster(ci).getDoc(di).getSent(si);
					
					loglikelihood += Gamma.logGamma(alphab + s.count(Topic.BACKGROUND));
					loglikelihood += Gamma.logGamma(alphac + s.count(Topic.CONTENT));
					loglikelihood += Gamma.logGamma(alphad + s.count(Topic.DOCUMENT));
					
					// subtract the (sum + parameter) term
					loglikelihood -= Gamma.logGamma(alphab + alphac + alphad + (double)s.nwords());
					
				}
				
				// first half of equation: add the parameter sum term
				double nsents = ((double)corpus.getCluster(ci).getDoc(di).nsents());
				loglikelihood += nsents * Gamma.logGamma(alphab + alphac + alphad); 
				//bottom of this equation
				loglikelihood -= nsents * (topicLogGamma[Topic.BACKGROUND] + topicLogGamma[Topic.CONTENT] + topicLogGamma[Topic.DOCUMENT]);
			}
		}
		
		//P(w|z)
		// first half of first equation
		double W = (double)ntypes;
		loglikelihood += Gamma.logGamma(W * betab) + Gamma.logGamma(W * betac) + Gamma.logGamma(W * betad);
		loglikelihood -= W * (Gamma.logGamma(betab) + Gamma.logGamma(betac) + Gamma.logGamma(betad));
		
		// second half
		double csumtotal = 0.0;
		double dsumtotal = 0.0;
		for(int n = 0; n < ntypes; n++)
		{
			loglikelihood += Gamma.logGamma((double)btopic.getCount(n) + betab); //background first half
			
			double csum = 0.0;
			double dsum = 0.0;
			for(int ci = 0; ci < nclusters; ci++)
			{
				csum += (double)ctopic[ci].getCount(n);
				for(int di = 0; di < corpus.getCluster(ci).ndocs(); di++)
					dsum += (double)dtopic[ci][di].getCount(n);
			}
			loglikelihood += Gamma.logGamma(csum + betac); // content first half
			loglikelihood += Gamma.logGamma(dsum + betad); // document first half
			
			csumtotal += csum;
			dsumtotal += dsum;
		}
		
		loglikelihood -= Gamma.logGamma(btopic.getTotal() + ((double)ntypes)*betab); // background second half
		loglikelihood -= Gamma.logGamma(csumtotal + ((double) ntypes)*betac); // content second half
		loglikelihood -= Gamma.logGamma(dsumtotal + ((double) ntypes) *betad); // document second half
		
		return loglikelihood;
	}
	
}
