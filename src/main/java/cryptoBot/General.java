package cryptoBot;

import java.io.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class General {
	private static final Logger LOG = LogManager.getLogger(General.class);
	
	private Properties properties;
	
	public General() {
		this.loadProperties();
		
	}
	
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Load the properties (configuration) from the database
	 */
	private void loadProperties() {
		LOG.trace("Entered loadProperties()");
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		
		properties = new Properties();
		try {
			InputStream is = classloader.getResourceAsStream("config.properties");
			
			//properties.load(new FileInputStream("config.properties"));
			properties.load(is);
			
			Enumeration<?> e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				properties.getProperty(key);
				//System.out.println("Key : " + key + ", Value : " + value);
			}
			is.close();
			
			LOG.info("Config loaded");
		}catch (IOException e) {
			LOG.fatal("Error loading config, {}", e);
		}
		
	}
		
}
