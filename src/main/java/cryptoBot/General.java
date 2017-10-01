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
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		
		properties = new Properties();
		try {
			InputStream is = classloader.getResourceAsStream("config.properties");
			
			//properties.load(new FileInputStream("config.properties"));
			properties.load(is);
			
			Enumeration<?> e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = properties.getProperty(key);
				//System.out.println("Key : " + key + ", Value : " + value);
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void testLogje() {
		LOG.debug("This will be printed on debug");
        LOG.info("This will be printed on info");
        LOG.warn("This will be printed on warn");
        LOG.error("This will be printed on error");
        LOG.fatal("This will be printed on fatal");
        LOG.trace("This will be printed on trace");
        //LOG.error("Error Message Logged !!!", new NullPointerException("NullError"));

        LOG.info("Appending string: {}.", "Hello, World");
	}
	
	
}
