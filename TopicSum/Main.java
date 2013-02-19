import java.io.*;
import java.util.*;

/**
 * Rebecca Mason
 * 
 * 
 * Implementation of "TopicSum" model from the paper "Content Models for Multi-Document 
 * Summarization" by Aria Haghighi & Lucy Vanderwende (NAACL 2009).
 * 
 * 
 * example arguments: /home/rebecca/summarization/duc/duc2006/ 50
 * 
 * First argument: location of corpus (eg DUC-06, DUC-07)
 * should contain ~50 folders for each of the clusters to summarize
 * each with ~25 text files for the documents in that cluster 
 * files should have markup data & such removed, one sentence per line, but not tokenized
 * 
 * Second argument: number of iterations to run sampler
 * TopicSum usually converges after 25-50 iterations
 * the log-likelihood will print out every 10 iterations
 *
 * note: several different functions for KL-divergence
 * 1. back off to constant value (this is what Aria did originally, 
 * can adjust the value to tend towards extracting longer or shorter 
 * sentences)
 * 
 * 2. back off to background distribution (tends towards long sentences, gets better 
 * ROUGE scores but doesn't improve human-evaluations)
 * 
 * 3. tend away from document specific sentences (improves human evaluations & ROUGE 
 * scores significantly, though in our workshop paper we put this on top of HierSum, 
 * not TopicSum)
 * 
 * @author rebecca
 *
 */
public class Main 
{
	
	public static void main(String[] args)
	{
		
		File corpusLoc = new File(args[0]);
		int iterations = Integer.parseInt(args[1]);
		
		File summLoc = null;
		boolean printout = false;
		
		if(args.length == 3)
		{
			summLoc = new File(args[2]);
		
			if(summLoc.mkdir())
			{
				System.out.println("Creating output folder " + summLoc.getName() + "...");
				printout = true;
			}
			else
			{
				System.out.println("Failed to create output folder.");
				summLoc = null;
			}
		}

		Corpus corpus = new Corpus(corpusLoc);
		
		//build the sampler
		System.out.println("Building the model...");
		Sampler sampler = new Sampler(corpus);
		sampler.estimate(iterations, 10);
		
		/*
		// for debugging the sampler
		System.out.println("Top 25 background words:");
		printTop25(sampler.phib());
		
		System.out.println();
		System.out.println("Top 25 cluster 0 words:");
		printTop25(sampler.phic(0));
		
		System.out.println();
		System.out.println("Top 25 cluster 0, doc 0 words:");
		printTop25(sampler.phid(0,0));
		*/
		
		
		//do the sentence selection and build the summaries
		System.out.println("Writing summaries");
		for(int ci = 0; ci < corpus.nclusters(); ci++)
		{
			
			ArrayList<Sentence> summarySents = summarizeCluster(corpus.getCluster(ci), Sampler.getDist(corpus.getCluster(ci).phic(), 0.001), Sampler.getDist(corpus.phib(), 1.0));
			String summary = basicSentOrdering(summarySents);
			
			if(summLoc != null)
				FileUtil.writeTextFile(new File(summLoc, corpus.getCluster(ci).getName()), summary);			

			System.out.println(summary);
		}
		System.out.println();
		System.out.println("done");
		
		
		
		
	}
	
	/**
	 * for debugging the sampler; find top 25 in distribution d
	 * 
	 * @param d
	 */
	public static void printTop25(Distribution d)
	{
		boolean[] beenused = new boolean[TextUtil.getInstance().ntypes()];
		
		for(boolean b : beenused)
			b = false;
		
		// for every type of word, find the largest 25
		for(int c = 0; c < 25; c++)
		{
			int largest = -1;
			double plargest = 0.0;
			for(int i = 0; i < TextUtil.getInstance().ntypes(); i++)
			{
				if(d.pw(i) > plargest && !beenused[i])
				{
					plargest = d.pw(i);
					largest = i;
				}
			}
			
			if(largest == -1)
			{
				System.out.println("Nothing is larger than 0.");
				break;
			}
			
			// mark the word as used
			beenused[largest] = true;
			
			// print it out
			System.out.println(TextUtil.getInstance().getString(largest) + " " + plargest);
			
		}
	}
	


	
	public static double sum(Sentence s, Distribution phic)
	{
		double sum = 0;
		
		for(int wi = 0; wi < s.nwords(); wi++)
		{
			sum += phic.pw(s.getType(wi));
		}
		
		return sum;
	}
	
	public static int totalc(Sentence s)
	{
		int totalc = 0;
		
		for(int ti = 0; ti < s.nwords(); ti++)
		{
			if(s.getTopic(ti) == Topic.CONTENT)
				totalc++;
		}
		
		return totalc;
	}
	
	/**
	 * Get all of the sentences from a cluster
	 * 
	 * @param c the cluster
	 * @return the sentences from the cluster
	 */
	public static ArrayList<Sentence> getSents(Cluster c)
	{
		ArrayList<Sentence> sents = new ArrayList<Sentence>();
		
		// get the sentences in this cluster
		for(int di = 0; di < c.ndocs(); di++)
		{
			for(int si = 0; si < c.getDoc(di).nsents(); si++)
			{
				Sentence sent = c.getDoc(di).getSent(si);
				sents.add(sent);
			}
		}
		
		return sents;
	}
	
	
	
	/**
	 * Standard sentence selection as described in Haghighi and Vanderwende paper
	 * 
	 * @param c cluster to summarize
	 * @param phic distribution of content words
	 * @param phib distribution of background words
	 * @return the summary of the cluster
	 */
	public static ArrayList<Sentence> summarizeCluster(Cluster c, Distribution phic, Distribution phib)
	{
		ArrayList<Sentence> sents = getSents(c); // the sentences from the cluster
		ArrayList<Sentence> summarySents = new ArrayList<Sentence>(); // where we will put the summary sentences
		
		Distribution sdist = new Distribution();
		
		int sumlen = 0;
		while(sumlen < 250 && !sents.isEmpty())
		{
			// find the sentence with the min KL divergence
			Sentence minsent = null;
			double minkl = Double.MAX_VALUE;
			
			for(Sentence s : sents)
			{
				
				// make a distribution for this sentence
				addToDistribution(s, sdist);
				//double kl = kldiv(phic, Sampler.getDist(s.getDoc().phid(), 1.0), sdist, 0.001);
				double kl = kldiv(phic, sdist, 0.001);
				removeFromDistribution(s, sdist);
				
				if(kl < minkl)
				{
					minsent = s;
					minkl = kl;
				}
			}
			
			if(minsent == null)
			{
				System.out.println("Error: did not select a sentence.");
				break;
			}
			
			// add the minsent to the summarysentences and take out of the cluster sentences
			sents.remove(minsent);
			
			addToDistribution(minsent, sdist); // add the minsent to the summary distribution
			// update the summary length
			sumlen += minsent.nwords();
			summarySents.add(minsent);
		}
		
		return summarySents;
	}
	
	/**
	 * 
	 * 
	 * @param summarySents
	 * @return
	 */
	//TODO use a faster search here?
	public static String basicSentOrdering(ArrayList<Sentence> summarySents)
	{
		String summary = "";
		while(!summarySents.isEmpty())
		{
			Sentence topSent = null;
			double minpos = 100000.0;
			for(Sentence sent : summarySents)
			{
				double pos = ((double)sent.nsent()) / ((double) sent.getDoc().nsents());
				if(pos < minpos)
				{
					topSent = sent;
					minpos = pos;
				}
			}
			if(topSent == null)
			{
				System.out.println("Error: did not select a sentence");
				System.exit(2);
			}
			
			//summary = summary + " \r\nDoc: " + topSent.getDoc().toString() + " Sent: " + topSent.nsent() + " " + topSent.getOriginal();
			summary = summary + "\n" + topSent.getOriginal();
			summarySents.remove(topSent);
		}
		
		return summary;
	}
	
	/**
	 * Test 2 try to optimize summary matching the content with sentence not matching its document specific
	 * 
	 * @param c content distribution
	 * @param d document distribution
	 * @param s summary distribution
	 * @param b background distribution
	 * @return the kldivergence between the content distribution and the summary distribution minus the kldivergence between the document distribution and the summary distribution
	 */
	public static double kldiv(Distribution c, Distribution d, Distribution s, Double bd)
	{
		double kl = 0;
		for(int ti = 0; ti < TextUtil.getInstance().ntypes(); ti++)
		{
			if(s.pw(ti) != 0)
				kl = kl + c.pw(ti) * Math.log(c.pw(ti) / s.pw(ti)) - d.pw(ti) * Math.log(d.pw(ti) / s.pw(ti)) ;
			else
				kl = kl + c.pw(ti) * Math.log(c.pw(ti) / bd) - d.pw(ti) * Math.log(d.pw(ti) /bd);
		}
		
		return kl;
	}
	
	/**
	 * the kldivergence backs off to the background distribution if a word is not in the summary
	 * @param c content distribution
	 * @param s summary distribution
	 * @param b background distribution
	 * @return
	 */
	public static double kldiv(Distribution c, Distribution s, Distribution b)
	{
		double kl = 0;
		for(int ti = 0; ti < TextUtil.getInstance().ntypes(); ti++)
		{
			if(s.pw(ti) != 0)
				kl = kl + c.pw(ti) * Math.log(c.pw(ti) / s.pw(ti));
			else
				kl = kl + c.pw(ti) * Math.log(c.pw(ti) / b.pw(ti));
		}
		
		return kl;
	}
	
	/**
	 * Instead of backing off to the background distribution, backoff to some constant value
	 * 
	 * @param c content distribution
	 * @param s summary distribution
	 * @param d constant backoff value
	 * @return the kldivergence between the content distribution and the summary distribution
	 */
	public static double kldiv(Distribution c, Distribution s, double d)
	{
		double kl = 0;
		for(int ti = 0; ti < TextUtil.getInstance().ntypes(); ti++)
		{
			if(s.pw(ti) != 0)
				kl = kl + c.pw(ti) * Math.log(c.pw(ti) / s.pw(ti));
			else
				kl = kl + c.pw(ti) * Math.log(c.pw(ti) / d);
		}
		
		return kl;
	}
	
	public static void addToDistribution(Sentence s, Distribution d)
	{
		for(int ti = 0; ti < s.nwords(); ti++)
			d.add(s.getType(ti), 1.0);
	}
	
	public static void removeFromDistribution(Sentence s, Distribution d)
	{
		for(int ti = 0; ti < s.nwords(); ti++)
			d.add(s.getType(ti), -1.0);
	}
	
	
}
