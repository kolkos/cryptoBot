package cryptoBot;


import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import org.json.JSONObject;

import com.google.gson.*;


public class Coin {
	private String coin;
	private int currentBalanceSatoshi;
	private float currentBalanceCoins;
	private float currentValue;
	private HashMap<String, String> wallets;
	public static String currency = "eur";
	
	//General general;
	
	/**
	 * Initiate the class
	 * @param coin: the name of the coin
	 */
	public Coin(){
		//this.setCoin(coin);
		this.getWalletAddresses();
		
		//general = new General();
		//general.loadProperties();
	}
	
	/**
	 * Method to set the coin type
	 * @param coin
	 */
	public void setCoin(String coin) {
		this.coin = coin;
	}
	
	public String getCoin() {
		return this.coin;
	}
	
	/**
	 * Method to configure the wallets
	 */
	private void getWalletAddresses() {
		this.wallets = new HashMap<String, String>();
		this.wallets.put("btc", "12st4BrVDSG4vJgkeXnPxvrSfnbHbdwGKT");
		this.wallets.put("ltc", "LMWPhNFedT8e6X7iRQdh456hvZwYnxyStV");
		return;
	}
	
	public List<String> getWalletCoins() {
		List<String> coins = new ArrayList<String>(this.wallets.keySet());
		return coins;
	}
	
	/**
	 * Method to generate the API request URL for checking the balance	
	 * @return: URL for requesting the wallet balance
	 */
	private String getBalanceCheckURL() {
		// first get the wallet addresses
		this.getWalletAddresses();
		
		// now generate the URL
		String url = "https://api.blockcypher.com/v1/" + this.coin + "/main/addrs/" + this.wallets.get(this.coin) + "/balance";
		
		System.out.println(url);
		
		return url;
	}
	
	/**
	 * Method to generate the URL for the current value API request
	 * @return URL for requesting the current coin value
	 */
	public String getCurrentValueURL() {
		String url = "https://www.bitstamp.net/api/v2/ticker_hour/" + this.coin + this.currency + "/";
				
		return url;
	}
	
	/**
	 * Method to print the json response in a pretty way
	 * @param jsonString: the JSON response string
	 */
	public void prettyPrintJSON(String jsonString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    JsonParser jp = new JsonParser();
	    JsonElement je = jp.parse(jsonString);
	    String prettyJsonString = gson.toJson(je);
	    System.out.println(prettyJsonString);
	}
	
	/**
	 * Method to get the JSON response from a URL
	 * @param urlString: the url as a string
	 * @return: json object
	 * @throws Exception
	 */
	public JSONObject doAPIRequest(String urlString) throws Exception {
		URL url = new URL(urlString);
		
		// read from the URL
	    Scanner scan = new Scanner(url.openStream());
	    String str = new String();
	    while (scan.hasNext())
	        str += scan.nextLine();
	    scan.close();
	    
	    // build a JSON object
	    JSONObject jsonObject = new JSONObject(str);
	    
	    // pretty print for debugging
	    this.prettyPrintJSON(str);
	    
	    return jsonObject;
	    
	}
	
	/**
	 * Method to get the current wallet balance. It sets the global variable
	 */
	public void getCurrentWalletBalance() {
		try {
			JSONObject json = this.doAPIRequest(this.getBalanceCheckURL());
			this.currentBalanceSatoshi = (int) json.get("final_balance");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(this.currentBalanceSatoshi);
			
	}
	
	public int getBalanceInSatoshi() {
		return this.currentBalanceSatoshi;
	}
	
	public void fakeAPIRequest() {
		System.out.println("Coin:" + this.coin);
		System.out.println(this.getBalanceCheckURL());
		System.out.println(this.getCurrentValueURL());
	}
}
