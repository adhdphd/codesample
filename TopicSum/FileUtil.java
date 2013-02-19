

import java.io.*;

/**
 * File IO String utility for summarization system
 * 
 * @author rebecca
 *
 */

public class FileUtil 
{
	
	private BufferedReader br;
	private File f;
	
	public FileUtil(File f)
	{
		this.f = f;
		
		try {
			br = new BufferedReader(new FileReader(this.f));
		}
		catch(Exception e)
		{
			System.err.println("Error in FileUtil constructor: ");
			System.err.println(e.toString());
			System.exit(1);
		}
	}
	
	/**
	 * 
	 * @return the next line of the file, or null if we have reached the end of the file
	 */
	public String readLine()
	{
		try
		{
			if(br.ready())
				return br.readLine();
			else
				return null;
		}
		catch(Exception e)
		{
			System.err.println("Error reading from file " + f.toString() + ":");
			System.err.println(e.toString());
			System.exit(1);
		}
		
		return null;
	}
	
	public boolean ready()
	{
		try
		{
			return br.ready();
		}
		catch(Exception e)
		{
			System.err.println("Error determining if file " + f.toString() + " is ready:");
			System.err.println(e.toString());
			System.exit(1);
		}
		
		return false;
	}
	
	/**
	 * closes the file that was being read
	 */
	public void closeFile()
	{
		try
		{
			br.close();
		}
		catch(Exception e)
		{
			System.err.println("Error closing file " + f.toString() + ":");
			System.err.println(e.toString());
			System.exit(1);
		}
	}
	
	public static String readTextFile(File f) 
	{

		BufferedReader br;
		char buffer[];
		int l = 0, c = 128000;
		StringBuffer s;

		try 
		{
			br = new BufferedReader(new FileReader(f));
			buffer = new char[c];
			s = new StringBuffer();

			l = br.read(buffer);
			while (true) 
			{
				s.append(buffer, 0, l);
				if (l < c)
					break;
				else
					l = br.read(buffer);
			}
			br.close();
			return s.toString();

		} 
		catch (Exception e) 
		{
			System.err.println("FileUtil readTextFile error, file:  " + f.getName());
			System.err.println(e.toString());
			return null;
		}
	}

	/**
	 * 
	 * @param f
	 * @param s
	 */
	public static void writeTextFile(File f, String s) 
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(s);
			bw.close();
		} 
		catch (Exception e) 
		{
			System.err.println("FileUtil writeTextFile error, file:  " + f.getName());
			System.err.println(e.toString());
		}
	}

}