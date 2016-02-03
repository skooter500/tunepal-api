package org.tunepal.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("")
public class Discography {
	
	@GET
	@Path("/Discography")
	@Produces(MediaType.APPLICATION_JSON)
	public Album[] getAlbums(@QueryParam("q") String q)
	{
		ArrayList<Album> albums = new ArrayList<Album>();
		Connection conn = null;
		ResultSet r = null;
		PreparedStatement s = null;
		
		try
		{
			conn = DBHelper.getConnection();
			if (conn == null)
			{
				return new Album[]
						{ new Album(-1,
								"Could not get a database connection. Is the database online?") };
			}
			
			String search = Utils.prepareForLike(q);
			String sql = "select album.id, album.title, album.artist from album, albumtracktune where (upper(  replace(replace(albumtracktune.title, \"'\", \"\"), \",\", \"\") ) like ? AND album.id=albumtracktune.album_id) ";
			s = conn.prepareStatement(sql);
			s.setString(1, search);
			r = s.executeQuery();
			
			while(r.next())
			{	
				Album album = new Album(r);
				albums.add(album);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Album[]
					{ new Album(-1,
							"Exception: " + e.getMessage()) };
			
		}
		DBHelper.safeClose(conn, s, r);
		Album[] aAlbums = new Album[albums.size()];
		albums.toArray(aAlbums);
		return aAlbums;
	}
}
