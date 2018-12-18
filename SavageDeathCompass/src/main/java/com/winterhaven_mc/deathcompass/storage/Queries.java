package com.winterhaven_mc.deathcompass.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Retrieve sql queries from properties resource
 */
class Queries {

	private final static String propFileName = "queries.properties";
	
	private static Properties properties;
	

	/**
	 * Private class constructor to prevent instantiation
	 */
	private Queries() {
		throw new AssertionError();
	}


	/**
	 * Load properties file
	 * @return Properties object
	 * @throws SQLException if properties file could not be loaded
	 */
	private static Properties getQueries() throws SQLException {
		
		// singleton
		if (properties == null) {
			properties = new Properties();
			try {
				
				InputStream inputStream = Queries.class.getResourceAsStream("/" + propFileName);
				
				if (inputStream == null) {
					throw new SQLException("Unable to load property file: " + propFileName);
				}
				properties.load(inputStream);
			}
			catch (IOException e) {
				throw new SQLException("Unable to load property file: " + propFileName);
			}
		}
		
		return properties;
	}


	/**
	 * Retreive an sql query string
	 * @param query the properties key for the query string to be retrieved
	 * @return String the sql query string
	 * @throws SQLException if query string could not be retrieved
	 */
	static String getQuery(final String query) throws SQLException {
		return getQueries().getProperty(query);
	}
	
}
