package org.tunepal.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("")
public class KeywordSearch
{
	@Context
	private HttpServletRequest request;
	
	final int MAX_QUERY_LENGTH = 100;
    String[] linkwords = new String[]{"THE", "AT", "AND", "OF", "TO"
    	, "BUT", "SO", "FOR", "AN", "I", "IN", "ON", "YOUR", "A", "WITH", "OUT", "AR"};
    
    //String[] punctuation = new String[]{",", "'", "!", "&", "%", "^", "-", "+", "|", "\\", "/", ".", "?"};
    
    @GET
	@Path("/keywordSearch")
	@Produces(MediaType.APPLICATION_JSON)
	public Tune[] keywordSearch(@QueryParam("q") String q,
			@QueryParam("id") String idStr,
			@QueryParam("time_sigs") String timeSigs,
			@QueryParam("sources") String sources,
			@QueryParam("client") String client,
			@QueryParam("latitude") float latitude,
			@QueryParam("longitude") float longitude,
			@QueryParam("resubmitted") boolean resubmitted,
			@QueryParam("local_tstamp") String localTstamp)
	{
    	Connection conn = null;
		PreparedStatement s = null;
		ResultSet r = null;
		
		
		
		if ((q == null) || (q.length() == 0))
		{
			return new Tune[]
			{ new Tune(-1, "Empty query") };
		}
		if (q.length() > MAX_QUERY_LENGTH)
		{
			q = q.substring(0, MAX_QUERY_LENGTH);
		}
		if (q.length() < 3)
		{
			return new Tune[]
			{ new Tune(-1,
					"Query must be at least 3 characters") };
		}
		int id = -1;
		int correct = -1;
		conn = DBHelper.getConnection();
		if (conn == null)
		{
			return new Tune[]
					{ new Tune(-1,
							"Could not get a database connection. Is the database online?") };
		}
		
		ArrayList<Tune> tunes = new ArrayList<Tune>();
    	try
    	{
    		try
			{
				if ((localTstamp.indexOf("p.m.") != -1)
						|| (localTstamp.indexOf("a.m.") != -1))
				{ // 2012-05-11%2002:52:10%20p.m.
					localTstamp = localTstamp.replaceAll("p.m.", "pm");
					localTstamp = localTstamp.replaceAll("a.m.", "am");
					SimpleDateFormat fromFormat = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss a");
					SimpleDateFormat toFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					localTstamp = toFormat
							.format(fromFormat.parse(localTstamp));
					// out.println(localTstamp);
				}
				s = conn.prepareStatement("insert into titlequeries(query, tstamp, ip, corpus, filter, client, latitude, longitude, local_tstamp) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
				s.setString(1, q);
				s.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				s.setString(3, request.getRemoteAddr());
				s.setString(4, sources);
				s.setString(5, timeSigs);
				s.setString(6, client);
				s.setFloat(7, latitude);
				s.setFloat(8, longitude);
				s.setString(9, localTstamp);
				s.executeUpdate();
			}
			catch (Exception e)
			{
				localTstamp = "NULL";
				s = conn.prepareStatement("insert into titlequeries(query, tstamp, ip, corpus, filter, client, latitude, longitude, local_tstamp) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
				s.setString(1, q);
				s.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				s.setString(3, request.getRemoteAddr());
				s.setString(4, sources);
				s.setString(5, timeSigs);
				s.setString(6, client);
				s.setFloat(7, latitude);
				s.setFloat(8, longitude);
				s.setString(9, localTstamp);
				s.executeUpdate();
			}
	    	
	    	//q = q.replaceAll("\\p{P}\\p{S}", "");
	    	q = q.replaceAll("[^a-zA-Z ]", "");
	    	
	    	String[] words = q.split(" ");
	    	String search = "%";
	    	for(String word:words)
	    	{
		        if (!isLinkWord(word))
		        {
		                search += word + "%"; 
		        }
	    	}
	    	
	    	search = search.toUpperCase();
	    	search = search.replace("HUMOURS", "HUMORS");
	    	String sql = "select tuneindex.id as id, source.source as sourcename, source.id as sourceid, notation, title, alt_title, tunepalid, x, tune_type, key_sig, time_sig  from tuneindex, source where tuneindex.source = source.id and (replace (upper(replace(replace(replace(title, '''', ''), ',', ''), '\', '')), 'HUMOURS', 'HUMORS') like ? or replace(upper(replace(replace(replace(alt_title, '''', ''), ',', ''), '\', '')), 'HUMOURS', 'HUMORS') like ?) ";
	    	Config.log(sql);	    	
	    	if ((sources != null) && (sources.length() != 0)
					&& (!"0".equals(sources)))
	    	{
	    		ArrayList<String>values = new ArrayList<String>();
	    		String[] sourcesArr = sources.split(",");
	    		for(String sourceValue:sourcesArr)
	    		{
	    			values.add("source.id = " + sourceValue);
	    		}
	    		sql +=  " and (" + Utils.join(" OR ", values) + ")";	
	    	}
	    	if ((timeSigs != null) && !"all".equals(timeSigs))
			{
				if ("reels".equals(timeSigs))
				{
					sql += " and (time_sig = 'C' or time_sig = 'C|' or time_sig = '4/4' or time_sig = '2/4' or time_sig = '2/2' or time_sig = '4/2') ";
				}
				else if ("jigs".equals(timeSigs))
				{
					sql += " and (time_sig = '6/8' or time_sig = '12/8')";
				}
				else if ("slip_jigs".equals(timeSigs))
				{
					sql += " and time_sig = '9/8' ";
				}
				else if ("waltzes".equals(timeSigs))
				{
					sql += " and time_sig = '3/4' ";
				}
				else if ("unusual_jigs".equals(timeSigs))
				{
					sql += " and time_sig = '3/8' ";
				}
				else if ("unusual_hornpipes".equals(timeSigs))
				{
					sql += " and (time_sig = '3/2' or time_sig = '6/4') ";
				}
			}    	
	    	sql += " order by title, alt_title LIMIT 100";
	    	
	    	s = conn.prepareStatement(sql);
	    	s.setString(1, search);
	    	s.setString(2, search);	    	
	    	r = s.executeQuery();
	    	
	    	while (r.next())
	    	{
	    		Tune tune = new Tune(r);
	    		tune.notation = null;
	    		tunes.add(tune);
	    	}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
			return new Tune[]
					{ new Tune(-1,
							"Exception: " + e.getMessage()) };
			
		}

		DBHelper.safeClose(conn, s, r);
		Tune[] aTunes = new Tune[tunes.size()];
		tunes.toArray(aTunes);
		return aTunes;
	}

	private boolean isLinkWord(String word)
	{
        String uword = word.toUpperCase();
        for(String link:linkwords)
        {
            if (uword.equals(link))
            {
                    return true;
            }
        }
        return false;	        
	}
	
	
}
