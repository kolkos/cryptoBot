package cryptoBot;

import java.io.*;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
	
	public String convertTimestampToString(Timestamp timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String dateString = dateFormat.format(timestamp);
		
		return dateString;
	}
	
	public String getDutchNumberFormat(double value, String prefix, String suffix, boolean addPlus, int fractionDigits) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumFractionDigits(fractionDigits);
		numberFormat.setMaximumFractionDigits(fractionDigits);
		
		
		String positiveSign = "";
		
		// check if the plus needs to be added
		if(addPlus) {
			// check if the value is greater than 0
			if(value > 0) {
				positiveSign = "+";
			}
		}
		
		// now return the formatted number as a string	
		return positiveSign + prefix + numberFormat.format(value) + suffix;
	}
		
}
