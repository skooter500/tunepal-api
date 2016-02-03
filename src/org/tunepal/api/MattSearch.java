package org.tunepal.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("")
public class MattSearch
{
	@Context
	private HttpServletRequest request;
	
	static final int INDEX_MASK = ((1 << 16) - 1);
	
	static final int MAX_KEY_LENGTH = 1000;
	static final int MAX_QUERY_LENGTH = 300;
	
	private static Tune[] tuneCache;
	
	private static synchronized Tune[] getTuneCache()
	{
		if (tuneCache == null)
		{
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement s = null;
			ArrayList<Tune> tempTuneCache = new ArrayList<Tune>();
			try
			{
				conn = DBHelper.getConnection();
				s = conn.prepareStatement("select tuneindex.id as id, tune_type, notation, source.id as sourceid, url, source.source as sourcename, substring(search_key, 1, "
						+ MAX_KEY_LENGTH
						+ ") as search_key, title, alt_title, tunepalid, x, midi_file_name, key_sig from tuneindex, tunekeys, source where tuneindex.source = source.id and tunekeys.tuneid= tuneindex.id order by downloaded desc"
						+ "");
				rs = s.executeQuery();
				while(rs.next())
				{
					Tune tune = new Tune(rs);
					tempTuneCache.add(tune);
				}
			}
			catch (Exception e)
			{
				System.out.println("Could not load Tune Cache!!");
				e.printStackTrace();
			}
			finally
			{
				DBHelper.safeClose(conn, s, rs);

			}
			tuneCache = new Tune[tempTuneCache.size()];
			tempTuneCache.toArray(tuneCache);
		}
		
		return tuneCache;
	}

	@GET
	@Path("/mattSearch1")
	@Produces(MediaType.APPLICATION_JSON)
	public Tune[] mattSearch1(@QueryParam("q") String q,
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
		
		// Reuse this array
		int[][] d = new int[MAX_QUERY_LENGTH + 1][MAX_KEY_LENGTH + 1];
		ArrayList<Tune> tunes = new ArrayList<Tune>();

		try
		{

			if ((q == null) || (q.length() == 0))
			{
				return new Tune[]
				{ new Tune(-1, "Please record and transcribe a query first.") };
			}
			if (q.length() > MAX_QUERY_LENGTH)
			{
				q = q.substring(0, MAX_QUERY_LENGTH);
				// out.println("The maximum length for a query is 300 notes. Try a shorter piece.");
			}
			if (q.length() < 15)
			{
				return new Tune[]
				{ new Tune(-1,
						"Please record and transcribe a minimum of 15 notes.") };
			}
			int id = -1;
			int correct = -1;
			conn = DBHelper.getConnection();

			// Its a new search
			int silence = 0;

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
				s = conn.prepareStatement("insert into tunequeries(correct, query, tstamp, ip, corpus, filter, client, latitude, longitude, resubmitted, local_tstamp) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				s.setInt(1, -1);
				s.setString(2, q);
				s.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				s.setString(4, request.getRemoteAddr());
				s.setString(5, sources);
				s.setString(6, timeSigs);
				s.setString(7, client);
				s.setFloat(8, latitude);
				s.setFloat(9, longitude);
				s.setBoolean(10, resubmitted);
				s.setString(11, localTstamp);
				s.executeUpdate();
			}
			catch (Exception e)
			{
				localTstamp = "NULL";
				s = conn.prepareStatement("insert into tunequeries(correct, query, tstamp, ip, corpus, filter, client, latitude, longitude, resubmitted) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				s.setInt(1, -1);
				s.setString(2, q);
				s.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				s.setString(4, request.getRemoteAddr());
				s.setString(5, sources);
				s.setString(6, timeSigs);
				s.setString(7, client);
				s.setFloat(8, latitude);
				s.setFloat(9, longitude);
				s.setBoolean(10, resubmitted);
				s.executeUpdate();
			}

			s = conn.prepareStatement("select max(id) as maxid from tunequeries");
			r = s.executeQuery();
			r.next();
			id = r.getInt("maxid");
			r.close();

			/*String sql = "select tuneindex.id as id, tune_type, notation, source.id as sourceid, url, source.source as sourcename, substring(search_key, 1, "
					+ MAX_KEY_LENGTH
					+ ") as search_key, title, alt_title, tunepalid, x, midi_file_name, key_sig from tuneindex, tunekeys, source where tuneindex.source = source.id and tunekeys.tuneid= tuneindex.id";
			if ((sources != null) && (sources.length() != 0)
					&& (!"0".equals(sources)))
			{
				StringTokenizer stTok = new StringTokenizer(sources, ",");
				sql += " and (";
				boolean isFirst = true;
				while (stTok.hasMoreTokens())
				{
					if (isFirst)
					{
						isFirst = false;
					}
					else
					{
						sql += " or ";
					}
					sql += " source.id = " + stTok.nextToken();
				}
				sql += ") ";
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
			*/

			/*
			 * if ((type != null) && (!"All".equals(type))) { StringTokenizer
			 * stTok = new StringTokenizer(type, ","); sql += " and ("; boolean
			 * isFirst = true; while (stTok.hasMoreTokens()) { if (isFirst) {
			 * isFirst = false; } else { sql += " or "; } sql +=
			 * " tune_type = \'%" + stTok.nextToken() + "%\'"; } sql += ") "; }
			 * out.println(sql);
			 */
			int howMany = 20;
			
			Tune[] tuneCache = getTuneCache();
			int[] scoreAndIndex = new int[tuneCache.length];
			// Put your code here
			for (int i=0; i<tuneCache.length; i++) {
				int ed = (int)(EditDistance.minEdSubString(q, tuneCache[i].searchKey, d));
				scoreAndIndex[i] = ed << 16 | i;
				if (scoreAndIndex[i] < 0)
				{
					System.out.println("Not supposed to happen");
				}
			}
			Arrays.sort(scoreAndIndex);

			int closestIndex = scoreAndIndex[0] & INDEX_MASK;
			Tune closest = tuneCache[closestIndex];
			// Update the tunequeries table with the closest match
			if (idStr == null)
			{
				s = conn.prepareStatement("update tunequeries set tunepalid = ?, ed = ?, normalEd = ? where id = ?");
				s.setString(1, closest.tunepalid);
				s.setFloat(2, closest.ed);
				s.setFloat(3, closest.ed / (float) q.length());
				s.setInt(4, id);
				s.executeUpdate();

			}
			String lastTunePalId = "DUMMY";
			
			for (int i = 0 ; i < howMany ; i ++)
			{
				int score = scoreAndIndex[i] >> 16;
				int index = scoreAndIndex[i] & INDEX_MASK;
				Tune tune = tuneCache[index];
				float normalEd = ((float) score / (float) q.length());
				tune.confidence = 1.0f - normalEd;
				tune.ed = score; 	
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
	
	@GET
	@Path("/mattSearch")
	@Produces(MediaType.APPLICATION_JSON)
	public Tune[] mattSearch(@QueryParam("q") String q,
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
		final int MAX_KEY_LENGTH = 1000;
		final int MAX_QUERY_LENGTH = 300;

		// Reuse this array
		int[][] d = new int[MAX_QUERY_LENGTH + 1][MAX_KEY_LENGTH + 1];
		
		ArrayList<Tune> tunes = new ArrayList<Tune>();

		try
		{

			if ((q == null) || (q.length() == 0))
			{
				return new Tune[]
				{ new Tune(-1, "Please record and transcribe a query first.") };
			}
			if (q.length() > MAX_QUERY_LENGTH)
			{
				q = q.substring(0, MAX_QUERY_LENGTH);
				// out.println("The maximum length for a query is 300 notes. Try a shorter piece.");
			}
			if (q.length() < 15)
			{
				return new Tune[]
				{ new Tune(-1,
						"Please record and transcribe a minimum of 15 notes.") };
			}
			int id = -1;
			int correct = -1;
			conn = DBHelper.getConnection();

			// Its a new search
			int silence = 0;

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
				s = conn.prepareStatement("insert into tunequeries(correct, query, tstamp, ip, corpus, filter, client, latitude, longitude, resubmitted, local_tstamp) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				s.setInt(1, -1);
				s.setString(2, q);
				s.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				s.setString(4, request.getRemoteAddr());
				s.setString(5, sources);
				s.setString(6, timeSigs);
				s.setString(7, client);
				s.setFloat(8, latitude);
				s.setFloat(9, longitude);
				s.setBoolean(10, resubmitted);
				s.setString(11, localTstamp);
				s.executeUpdate();
			}
			catch (Exception e)
			{
				localTstamp = "NULL";
				s = conn.prepareStatement("insert into tunequeries(correct, query, tstamp, ip, corpus, filter, client, latitude, longitude, resubmitted) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				s.setInt(1, -1);
				s.setString(2, q);
				s.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				s.setString(4, request.getRemoteAddr());
				s.setString(5, sources);
				s.setString(6, timeSigs);
				s.setString(7, client);
				s.setFloat(8, latitude);
				s.setFloat(9, longitude);
				s.setBoolean(10, resubmitted);
				s.executeUpdate();
			}

			s = conn.prepareStatement("select max(id) as maxid from tunequeries");
			r = s.executeQuery();
			r.next();
			id = r.getInt("maxid");
			r.close();

			String sql = "select tuneindex.id as id, tune_type, notation, source.id as sourceid, url, source.source as sourcename, substring(search_key, 1, "
					+ MAX_KEY_LENGTH
					+ ") as search_key, title, alt_title, tunepalid, x, midi_file_name, key_sig from tuneindex, tunekeys, source where tuneindex.source = source.id and tunekeys.tuneid= tuneindex.id";
			if ((sources != null) && (sources.length() != 0)
					&& (!"0".equals(sources)))
			{
				StringTokenizer stTok = new StringTokenizer(sources, ",");
				sql += " and (";
				boolean isFirst = true;
				while (stTok.hasMoreTokens())
				{
					if (isFirst)
					{
						isFirst = false;
					}
					else
					{
						sql += " or ";
					}
					sql += " source.id = " + stTok.nextToken();
				}
				sql += ") ";
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

			/*
			 * if ((type != null) && (!"All".equals(type))) { StringTokenizer
			 * stTok = new StringTokenizer(type, ","); sql += " and ("; boolean
			 * isFirst = true; while (stTok.hasMoreTokens()) { if (isFirst) {
			 * isFirst = false; } else { sql += " or "; } sql +=
			 * " tune_type = \'%" + stTok.nextToken() + "%\'"; } sql += ") "; }
			 * out.println(sql);
			 */
			s = conn.prepareStatement(sql);
			r = s.executeQuery();
			int howMany = 20;
			
			PriorityQueue<Tune> pq = new PriorityQueue();
			while (r.next())
			{
				Tune entry = new Tune(r);
				entry.notation = null; // Dont send this now...
				String key = r.getString("search_key");
				entry.ed = EditDistance.minEdSubString(q, key, d);
				pq.add(entry);
			}
			
			if (pq.isEmpty())
			{
				howMany = 0;
			}
			else
			{
				// Update the tunequeries table with the closest match
				if (idStr == null)
				{
					s = conn.prepareStatement("update tunequeries set tunepalid = ?, ed = ?, normalEd = ? where id = ?");
					s.setString(1, pq.peek().tunepalid);
					s.setFloat(2, pq.peek().ed);
					s.setFloat(3, pq.peek().ed / (float) q.length());
					s.setInt(4, id);
					s.executeUpdate();

				}
				int i = 0;
				String lastTunePalId = "DUMMY";

				while (i < howMany)
				{
					Tune entry = pq.poll();
					if (entry == null)
					{
						break;
					}
					// Have we already printed this one?
					if (entry.tunepalid.equals(lastTunePalId))
					{
						continue;
					}
					float normalEd = ((float) entry.ed / (float) q.length());
					entry.confidence = 1.0f - normalEd;

					tunes.add(entry);
					i++;
				}
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
}
