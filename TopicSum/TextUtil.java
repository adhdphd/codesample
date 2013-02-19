import java.util.*;
import java.io.*;

/**
 * 
 * basically this is where I put all the methods for reading in sentences; 
 * keeping track of which numbers get assigned to which words
 * 
 * this works a little bit better than PTB-style tokens though perhaps not 
 * well enough to justify the effort if I were to do this over again
 * 
 * 
 *  
 * @author rebecca
 *
 */
public class TextUtil 
{
	private static TextUtil ref;
	
	private Map<String, Integer> stringToInt;
	private Map<Integer, String> intToString;
	
	private int ntypes;
	
	
	
	private TextUtil()
	{
		stringToInt = new HashMap<String, Integer>();
		intToString = new HashMap<Integer, String>();
		
		ntypes = 0;
		
		//isstop = new boolean[0];
		
		
	}
	
	public static TextUtil getInstance()
	{
		if(ref == null)
			ref = new TextUtil();
		
		return ref;
	}

	
	/**
	 * 
	 * @param w an int that represents a word in this corpus
	 * @return the word that is mapped to this int
	 */
	public String getString(int w)
	{
		if(intToString.containsKey(w))
			return intToString.get(w);
		else
			return null;
	}
	
	
	/**
	 * 
	 * @param sentence
	 * @return an array of the lowercased words without punctuation from this sentence
	 */
	public static String[] getWords(String sentence)
	{
		String [] sent = sentence.split(" ");
		
		ArrayList<String> wordsList = new ArrayList<String>();
		
		for(String word : sent)
		{
			word = removePunctuation(word).toLowerCase();
			
			if(!word.equals(""))
				wordsList.add(word);
		}
		
		String[] wordsArray = new String[wordsList.size()];
		return wordsList.toArray(wordsArray);
	}
	
	
	/**
	 * 
	 * @param sentence the sentence to be read in
	 * @return an array of the integers that represent the words in this sentence
	 */
	public static int[] readSent(String sentence)
	{
		String [] words = getWords(sentence);
		
		int[] sent = new int[words.length];
		
		for(int i = 0; i < words.length; i++)
		{
			sent[i] = TextUtil.getInstance().getInt(words[i]);
		}
		
		return sent;
	}
	
	
	
	/**
	 * 
	 * @param w the original string from a sentence
	 * @return the sentence with all the punctuation removed
	 */
	public static String removePunctuation(String w)
	{
		for(int i = 0; i < w.length(); i++)
		{
			if(!Character.isLetterOrDigit(w.charAt(i)))
			{
				w = w.replace(w.charAt(i), ' ');
			}
		}
		
		w = w.trim();
		
		//remove some HTML stuff
		//fixes most of the problems if the data is messy but clean the data better for best results
		if(w.toLowerCase().equals("ql") || w.toLowerCase().equals("lr") || w.toLowerCase().equals("ur"))
		{
			w = "";
		}
		
		return w;
	}
	
	
	/**
	 * 
	 * @param w a token from the corpus
	 * @return the integer that is mapped to this type
	 */
	public int getInt(String w)
	{
		if(stringToInt.containsKey(w)) 
			return stringToInt.get(w);
		else // if we have not seen this string before
		{
			stringToInt.put(w, ntypes);
			intToString.put(ntypes, w);
			
			ntypes ++;
			
			return stringToInt.get(w);
		}
	}
	
	/**
	 * 
	 * @return the number of word types
	 */
	public int ntypes()
	{
		return ntypes;
	}
	
	
}
