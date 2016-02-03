package org.tunepal.api;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Source implements Serializable{
	public int id;
	public String fullName;
	public String shortName;
	public String url;
	public String extra;
	public int count;
	
	public Source()
	{
		
	}
	
	public Source(int errorCode, String errorMessage)
	{
		id = errorCode;
		fullName = errorMessage;
	}
	
	public Source(ResultSet rs) throws SQLException
	{
		id = rs.getInt("id");
		fullName = rs.getString("source");
		shortName = rs.getString("shortName");
		url = rs.getString("url");
		extra = rs.getString("extra");
		count = rs.getInt("count");
	}
}
