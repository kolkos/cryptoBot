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
		LOG.trace("Entering loadProperties()");
		
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
		LOG.trace("Finished loadProperties()");
	}
	
	/**
	 * Convert the timestamp to a more readable format
	 * @param timestamp the timestamp to formatted
	 * @return timestamp formatted dd-MM-yyyy HH:mm:ss
	 */
	public String convertTimestampToString(Timestamp timestamp) {
		LOG.trace("Entering convertTimestampToString(), timestamp={}", timestamp);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String dateString = dateFormat.format(timestamp);
		LOG.trace("Finished convertTimestampToString()");
		return dateString;
	}
	
	/**
	 * Format a number to a dutch format
	 * @param value the value to format
	 * @param prefix prefix for the number (for example €)
	 * @param suffix suffix for the number (for example %)
	 * @param addPlus if true, a plus will be added to a positive number
	 * @param fractionDigits the number of digitis to round to
	 * @return formatted number
	 */
	public String getDutchNumberFormat(double value, String prefix, String suffix, boolean addPlus, int fractionDigits) {
		LOG.trace("Entering getDutchNumberFormat(), value={}, prefix={}, suffix={}, addPlus={}, fractionDigits={}", value, prefix, suffix, addPlus, fractionDigits);
		
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
		LOG.trace("Finished getDutchNumberFormat()");
		return positiveSign + prefix + numberFormat.format(value) + suffix;
	}
		
}
