package org.tunepal.api;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Album implements Serializable
{
	public int id;
	public String title;
	public String artist;
	
	public Album()
	{
		
	}
	
	public Album(ResultSet r) throws SQLException
	{
		id = r.getInt("id");
		title = r.getString("title");
		artist = r.getString("artist");
	}

	public Album(int errorCode, String message) {
		id = errorCode;
		title = message;
	}
}
