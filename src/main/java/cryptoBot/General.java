package cryptoBot;

import java.io.*;
import java.util.*;

public class General {
	Properties properties;
	
	
	
	
	public void loadProperties() {
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
				System.out.println("Key : " + key + ", Value : " + value);
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
