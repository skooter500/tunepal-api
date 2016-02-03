package org.tunepal.api;

import java.util.ArrayList;

public class Utils 
{
	public static final String[] linkWords = {"THE", "AT", "AND", "OF", "TO", "BUT", "SO", "FOR", "AN", "I", "IN", "ON", "YOUR", "A", "WITH", "OUT", "AR"};
	
	public static String join(String conjunction, ArrayList<String> values)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i < values.size() ; i ++)
		{
			sb.append(values.get(i));
			if (i < values.size() - 1)
			{
				sb.append(conjunction);
			}				
		}
		return sb.toString();
	}
	
	public static boolean isLinkWord(String word)
	{
		String uword = word.toUpperCase();
		for(String link:linkWords)
		{
			if (uword.equals(link))
			{
				return true;
			}
		}
		return false;
	}
	
	
	public static String prepareForLike(String q)
	{
		// See http://stackoverflow.com/questions/18830813/how-can-i-remove-punctuation-from-input-text-in-java
		String[] words = q.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		
		StringBuffer search = new StringBuffer("%");
		for(String word:words)
		{
			if (!isLinkWord(word))
			{
				search.append(word);
				search.append("%"); 
			}
		}
		return "" + search;
	}
	
	
}
