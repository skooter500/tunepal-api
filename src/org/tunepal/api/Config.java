package org.tunepal.api;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Config implements ServletContextListener
{
	static ServletContext context;

	@Override
	public void contextDestroyed(ServletContextEvent arg0)
	{
		log("Tunepal API Servlet context destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0)
	{
		context = arg0.getServletContext();
		log("Tunepal API Servlet context created");
	}
	
	public static String getProperty(String key)
	{		
		return context == null ? null : context.getInitParameter(key);
	}
	
	public static void log(String message)
	{
		if (context != null)
		{
			context.log(message);
		}
		else
		{
			System.out.println("No context in Tunepal API servlet: " + message);
		}
	}
}
