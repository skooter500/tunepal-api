package org.tunepal.api;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlRootElement;

public class Tune implements Comparable, Serializable
{
	public String title;
	public String altTitle;
	public int x;
	public int id;
	public String tunepalid;
	public String source;
	public int sourceId;
	public String tuneType;
	public String keySignature;
	public String notation;
	public String searchKey;
	public float confidence;
	public float ed;
	
	public Tune()
	{
		
	}
	
	
	
	public Tune(ResultSet rs) throws SQLException
	{
		title = restoreFadas(rs.getString("title"));
		altTitle = restoreFadas(rs.getString("alt_title"));
		id = rs.getInt("id");
		x = rs.getInt("x");
		tunepalid = rs.getString("tunepalid");
		source = rs.getString("sourcename");
		sourceId = rs.getInt("sourceid");
		tuneType = rs.getString("tune_type");
		keySignature = rs.getString("key_sig");		
		notation = rs.getString("notation");
		//searchKey = rs.getString("search_key");
	}	
	
	public Tune(int errorCode, String errorMessage)
	{
		id = errorCode;
		title = errorMessage;
	}
	
	public int compareTo(Object o1)
    {
        Tune match0 = (Tune) this;
        Tune match1 = (Tune) o1;

        if (match0.ed < match1.ed)
        {
            return -1;
        }
        if (match0.ed == match1.ed)
        {
            return 0;
        }
        return 1;
    }
	
	private String restoreFadas(String word)
	{
		if (word == null)
		{
			return null;
		}
		String[] search  = new String[]{"\\\'a", "\\\'e", "\\\'i", "\\\'o", "\\\'u"};
		String[] replace = new String[]{"\u0225", "\u0233", "\u0237", "\u0243", "\u0250"};
		
		int i = 0;
		for(String s:search)
		{
			word = word.replace(s,  replace[i++]);
		}
		return word;
	}
}
