package com.winterhaven_mc.deathcompass.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

class Queries {

	private final static String propFileName = "queries.properties";
	
	private static Properties properties;
	

	/*
	 * Private class constructor to prevent instantiation
	 */
	private Queries() {
		throw new AssertionError();
	}
	
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

	static String getQuery(final String query) throws SQLException {
		return getQueries().getProperty(query);
	}
	
}
