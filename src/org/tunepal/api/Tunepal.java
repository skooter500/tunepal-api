package org.tunepal.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("")
public class Tunepal
{
	@Context
	private HttpServletRequest request;
	
	@GET
	@Path("/RandomTune")
	@Produces(MediaType.APPLICATION_JSON)
	public Tune getRandomTune()
	{
		Tune tune = new Tune();
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement s = null;
		try
		{
			c = DBHelper.getConnection();

			String sql = "SELECT tunepalid FROM tuneindex AS r1 JOIN (SELECT CEIL(RAND() * (SELECT MAX(id)"
					+ "FROM tuneindex)) AS id) AS r2 WHERE r1.id >= r2.id ORDER BY r1.id ASC LIMIT 1";
			
			s = c.prepareStatement(sql);
			rs = s.executeQuery();
			if (rs.next())
			{
				return getTune(rs.getString("tunepalid"));
			}
		}
		catch (SQLException e)
		{
			Config.log("" + e);
			e.printStackTrace();
		}
		finally
		{
			DBHelper.safeClose(c, s, rs);
		}
		return tune;
	}
	
	

	@GET
	@Path("/Hello/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt(@PathParam("name") String name)
	{
		return "Hello " + name;
	}
	
	@GET
	@Path("/downloads")
	@Produces(MediaType.APPLICATION_JSON)
	public Tune[] getTuneDownloads(@QueryParam("howMany") int howMany)
	{
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement s = null;
		ArrayList<Tune> tunes = new ArrayList<Tune>();
		if (howMany == 0)
		{
			howMany = 10;
		}
		
		try
		{
			c = DBHelper.getConnection();

			s = c.prepareStatement("select tuneindex.id as id, source.source as sourcename, source.id as sourceid, notation, tuneindex.title, tuneindex.alt_title, tuneindex.tunepalid, x, tune_type, key_sig, downloaded, downloads.tstamp from tuneindex, source, downloads where tuneindex.source = source.id and tuneindex.tunepalid=downloads.tunepalid order by downloads.tstamp desc limit ?");
			s.setInt(1, howMany);
			rs = s.executeQuery();
			while(rs.next())
			{
				Tune tune = new Tune(rs);
				tune.notation = null;
				tunes.add(tune);
			}
		}
		catch (SQLException e)
		{
			Config.log("" + e);
			e.printStackTrace();
		}
		finally
		{
			DBHelper.safeClose(c, s, rs);
		}
		Tune[] aTunes = new Tune[tunes.size()];
		tunes.toArray(aTunes);
		return aTunes;
	}
	
	private Tune getTune(Connection c, String tunepalid) throws SQLException
	{
		Tune tune = new Tune();
		tune.tunepalid = tunepalid;
		ResultSet rs = null;
		PreparedStatement s = null;
		try
		{
			s = c.prepareStatement("select tuneindex.id as id, source.source as sourcename, source.id as sourceid, notation, title, alt_title, tunepalid, x, tune_type, key_sig, downloaded from tuneindex, source where tuneindex.source = source.id and tunepalid=?");
			s.setString(1, tunepalid);
			rs = s.executeQuery();
			if (rs.next())
			{
				tune = new Tune(rs);
				updateDownloadCount(c, tune.tunepalid, tune.title);
			}			
		}
		catch (SQLException e)
		{
			Config.log("" + e);
			e.printStackTrace();
		}
		finally
		{
			DBHelper.safeClose(null, s, rs);
		}
		return tune;
	}
	
	@GET
	@Path("/Tunes/{tunepalid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Tune getTune(@PathParam("tunepalid") String tunepalid)
	{
		Tune tune = new Tune();
		tune.tunepalid = tunepalid;
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement s = null;
		try
		{
			c = DBHelper.getConnection();

			s = c.prepareStatement("select tuneindex.id as id, source.source as sourcename, source.id as sourceid, notation, title, alt_title, tunepalid, x, tune_type, key_sig, downloaded from tuneindex, source where tuneindex.source = source.id and tunepalid=?");
			s.setString(1, tunepalid);
			rs = s.executeQuery();
			if (rs.next())
			{
				tune = new Tune(rs);
				updateDownloadCount(c, tune.tunepalid, tune.title);
			}			
		}
		catch (SQLException e)
		{
			Config.log("" + e);
			e.printStackTrace();
		}
		finally
		{
			DBHelper.safeClose(c, s, rs);
		}
		return tune;
	}
	
	private void updateDownloadCount(Connection c, String tunepalid, String title) throws SQLException
	{
		PreparedStatement s = null;
		c = DBHelper.getConnection();
		String sql = "update tuneindex set downloaded = downloaded + 1 where tunepalid = ?";
		s = c.prepareStatement(sql);
		s.setString(1, tunepalid);
		s.executeUpdate();
		s.close();
		sql = "insert into downloads (tunepalid, title, tstamp) values (?, ?, NOW())";
		s = c.prepareStatement(sql);
		s.setString(1, tunepalid);
		s.setString(2, title);
		s.executeUpdate();
		s.close();
	}
}
