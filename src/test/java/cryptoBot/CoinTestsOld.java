package cryptoBot;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.Test;



public class CoinTestsOld {

	
	
	@Test
	public void testingLTC(){
		String coin = "ltc";
		CoinOld litecoin = new CoinOld();
		litecoin.setCoin(coin);
		
		// check if the coin is set correctly
		assertEquals(coin, litecoin.getCoin());
		
		// check if the URL for getting the balance is set correctly
		String url = "https://www.bitstamp.net/api/v2/ticker_hour/ltceur/";
		assertEquals(url, litecoin.getCurrentValueURL());
		
		// update/get the current balance
		litecoin.getCurrentWalletBalance();
		
		assertEquals(53065134, litecoin.getBalanceInSatoshi());
		
		
		
	}
	
	@Test
	public void testName() throws Exception {
		System.out.println(Long.MAX_VALUE);
	}
	
	@Test
	public void aanroependeMethode() throws Exception {
		String theClass = "cryptoBot.CoinTests";
		String theMethod = "aangeroepenFunctie";
		
		theClass = this.getClass().getName();
		this.commandToMethodInvoker(theClass, theMethod, new Class[] {String.class}, new Object[] {new String("Hello")});
		
		
	}
	
	public void commandToMethodInvoker(String theClass, String theMethod, Class[] params, Object[] args) throws Exception{
		Class c = Class.forName(theClass);
		Method m = c.getDeclaredMethod(theMethod, params);
		Object i = c.newInstance();
	    Object r = m.invoke(i, args);
	}
	
	public void aangeroepenFunctie(String s1) {
		System.out.println("aangeroepenFunctie");
		System.out.println(s1 );
	}
	
	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	@Test
	public void testCommandSplitten() throws Exception {
		String command = "method=getValue,coin=btc,int=1";
		String[] keyValuePairs = command.split(",");
		HashMap<String, Object> parameters = new HashMap<>();
		for(String keyValuePair : keyValuePairs) {
			String[] keyValueParts = keyValuePair.split("=");
			String key = keyValueParts[0];
			Object value = keyValueParts[1];
			
			
			
			
			parameters.put(key, value);
		}
		
	}
	
	@Test
	public void max() throws Exception {
		System.out.println("Int: " + Integer.MAX_VALUE);
		// 114183055
		// 53065134
		// 1525119
		// 734343
		// 2147483647
		System.out.println("Float: " + Float.MAX_VALUE);
		System.out.println("Double: " + Double.MAX_VALUE);
	}

}
