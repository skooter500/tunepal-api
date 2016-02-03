package org.tunepal.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.sql.PreparedStatement;

@Path("")
public class Sources {
	@GET
	@Path("/Sources")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Source[] getSources()
	{
		ArrayList<Source> sources = new ArrayList<Source>();
		Connection conn = null;
		ResultSet r = null;
		PreparedStatement s = null;
		try
		{
			conn = DBHelper.getConnection();
			if (conn == null)
			{
				return new Source[]
						{ new Source(-1,
								"Could not get a database connection. Is the database online?") };
			}
			
			
			String sql = "SELECT source.*, count( tuneindex.source ) as count FROM tuneindex, source WHERE tuneindex.source = source.id GROUP BY tuneindex.source order by source.source";
			s = conn.prepareStatement(sql);
			r = s.executeQuery();
			while(r.next())
			{	
				Source source = new Source(r);
				sources.add(source);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Source[]
					{ new Source(-1,
							"Exception: " + e.getMessage()) };
			
		}
		DBHelper.safeClose(conn, s, r);
		Source[] aSources = new Source[sources.size()];
		sources.toArray(aSources);
		return aSources;
	}

}
