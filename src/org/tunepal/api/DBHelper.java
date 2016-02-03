/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tunepal.api;

import java.net.URI;
import java.sql.*;
/**
 *
 * @author Bryan Duggan
 */
public class DBHelper {
    static
    {
        try
        {
        	String driver = Config.getProperty("dbdriver");
        	Config.log("Loading JDBC Driver: " + driver);
            Class.forName(driver);
        }
        catch(Exception e)
        {
            Config.log("Could not load driver");
            e.printStackTrace();
        }                
    }
    
    public static Connection getConnection()
    {    
    	String container = Config.context.getServerInfo();
    	Config.log("Servlet container: " + container);
        try
        {
        	// Are we running on Heroku?
        	if (container.toLowerCase().contains("jetty"))
        	{
        		
    	    	URI dbUri = new URI(System.getenv("CLEARDB_DATABASE_URL"));
    	
    	        String username = dbUri.getUserInfo().split(":")[0];
    	        String password = dbUri.getUserInfo().split(":")[1];
    	        String dbUrl = "jdbc:mysql://" + dbUri.getHost() + dbUri.getPath();	
    	        return DriverManager.getConnection(dbUrl, username, password);
        	}
        	else
        	{
	        	String dburl = Config.getProperty("dburl");
	        	String dbuser = Config.getProperty("dbuser");
	        	String dbpassword = Config.getProperty("dbpassword");
	            Connection conn = DriverManager.getConnection(dburl, dbuser, dbpassword);
	            return conn;            
        	}
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Config.log("Could not get a database connection");
        }        
        return null;
    }
    
    public static void safeClose(Connection c, Statement s, ResultSet r) {
        if (r != null) {
            try {
                r.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (s != null) {
            try {
                s.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (c != null) {
            try {
                c.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }             
    }
}
