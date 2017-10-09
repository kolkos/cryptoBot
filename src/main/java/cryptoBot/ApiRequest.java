package cryptoBot;

import java.net.URL;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ApiRequest {
	private static final Logger LOG = LogManager.getLogger(ApiRequest.class);
	
	
	/**
	 * This method gets the current wallet information from the api
	 * @param coinName the name of the requested coin
	 * @param walletAddress the address of the wallet
	 * @return a json object containing the results of the api request
	 * @throws Exception api error
	 */
	public JSONObject walletInfoApiRequest(String coinName, String walletAddress) throws Exception {
		// build the url for the request
		String url = "https://api.blockcypher.com/v1/" + coinName + "/main/addrs/" + walletAddress + "/balance";
		JSONObject json = this.doAPIRequest(url);
		return json;
	}
	
	/**
	 * This method gets the current value of the coin
	 * @param coinName the name of the coin
	 * @param currency currency to calculate the value
	 * @return a json object containing the result of the api request
	 * @throws Exception a api error
	 */
	public JSONObject currentCoinValueApiRequest(String coinName, String currency) throws Exception {
		// build the url for the api request
		String url = "https://www.bitstamp.net/api/v2/ticker_hour/" + coinName + currency + "/";
		JSONObject json = this.doAPIRequest(url);
		return json;
	}
	
	/**
	 * General method to handle the API requests
	 * @param the URL of the API request
	 * @return a JSONObject containing the response of the server
	 * @throws Exception
	 */
	private JSONObject doAPIRequest(String urlString) throws Exception {
		LOG.trace("Entering doAPIRequest(), url={}", urlString);
		
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
	    //this.prettyPrintJSON(str);
	    
	    LOG.trace("Fished doAPIRequest()");
	    
	    return jsonObject;
	}
	
	/**
	 * Method to print the json response in a pretty way
	 * @param jsonString: the JSON response string
	 */
	private void prettyPrintJSON(String jsonString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(jsonString);
		String prettyJsonString = gson.toJson(je);
		System.out.println(prettyJsonString);
	}
	
}
