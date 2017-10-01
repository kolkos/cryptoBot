package cryptoBot;

import java.net.URL;
import java.sql.ResultSet;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Wallet {
	private String walletAddress;
	private String coinName;
	private String currency;
	private int balanceSatoshi;
	private double balanceCoin;
	private double currentValue;
	private int requestID;
	
	private static final Logger LOG = LogManager.getLogger(Wallet.class);
	
	public int getRequestID() {
		return this.requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	/**
	 * Set the required currency eur/usd/etc
	 * @param currency: the name of the currency
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	/**
	 * Get the current currency
	 * @return the currency of this object
	 */
	public String getCurrency() {
		return currency;
	}
	
	/**
	 * Set the name of the coin for this object
	 * @param coinName: the name of the coin as a abbreviation (btc/ltc/etc)
	 */
	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}
	
	/**
	 * Get the current coinName of this object
	 * @return the name of the coin
	 */
	public String getCoinName() {
		return this.coinName;
	}
	
	/**
	 * Set the wallet address for this object
	 * @param walletAddress: the address of the wallet
	 */
	public void setWalletAddress(String walletAddress) {
		this.walletAddress = walletAddress;
	}
	
	/**
	 * Get the current address of the wallet
	 * @return the current address of the wallet for this object
	 */
	public String getWalletAddress() {
		return this.walletAddress;
	}
	
	/**
	 * Get the current balance of this object in Satoshi
	 * @return the balance in Satoshi
	 */
	public int getBalanceSatoshi() {
		return this.balanceSatoshi;
	}
	
	/**
	 * Get the current balance of this object in coins
	 * @return the balance in coins
	 */
	public double getBalanceCoin() {
		return this.balanceCoin;
	}
	
	public double getCurrentValue() {
		return this.currentValue;
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
	 * This method does a API call to get the current balance of this object
	 */
	public void getWalletValue() {
		LOG.trace("Entering getWalletValue()");
		
		// build the url for the request
		String url = "https://api.blockcypher.com/v1/" + this.coinName + "/main/addrs/" + this.walletAddress + "/balance";
		try {
			// try to run the json request
			JSONObject json = this.doAPIRequest(url);
			
			// get the current balance (in Satoshi) from the response
			//String balanceSatoshiString = (String) 
			this.balanceSatoshi = (int) json.get("final_balance");
			
			// convert the value in Satoshi to the coin value
			this.convertSatoshiToCoin();
			
			// now calculate the value of the wallet
			this.calculateCurrentValue();
			
			// now register the result
			this.registerCurrentResult();
			
			LOG.info("API request ({}) OK.", url);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("API request ({}) ERROR: {}", url, e);
		}
		LOG.trace("Finished getWalletValue()");
	}
	
	/**
	 * This method converts the balance in Satoshi to the value of the coin
	 */
	private void convertSatoshiToCoin() {
		LOG.trace("Entering convertSatoshiToCoin()");
		this.balanceCoin = this.balanceSatoshi * 0.00000001;
		LOG.trace("Finished convertSatoshiToCoin()");
	}
	
	/**
	 * Calculate the value of the coins in this wallet to the chosen currency. It uses a API request to get the current value of the coin
	 */
	private void calculateCurrentValue() {
		LOG.trace("Entering calculateCurrentValue()");
		// build the url for the api request
		String url = "https://www.bitstamp.net/api/v2/ticker_hour/" + this.coinName + this.currency + "/";
		
		try {
			// try to get the current coin value from the api
			JSONObject json = this.doAPIRequest(url);
			
			// get the current value of the coin from the json response
			double currentCoinValue = Double.parseDouble((String) json.get("last"));
			
			// calculate the current value of the coin
			this.currentValue = this.balanceCoin * currentCoinValue;
			
			LOG.info("API request ({}) OK.", url);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("API request ({}) ERROR: {}", url, e);
		}
		LOG.trace("Finished calculateCurrentValue()");
		
	}
	
	/**
	 * Semi-general method to get the coin values from the database. The query to run is determined by other methods
	 * @param query the SQL query to get the results
	 */
	private void getWalletDetailsFromDB(String query) {
		LOG.trace("Entering getWalletDetailsFromDB(), query={}", query);
		MySQLAccess db = new MySQLAccess();
		
		// first get the last entry of the wallet

		Object[] parameters = new Object[] {this.walletAddress};
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();

			// now register the received values
			while (resultSet.next()) {
				this.coinName = resultSet.getString("coinName");
				this.currency = resultSet.getString("currency");
				this.balanceSatoshi = resultSet.getInt("balanceSatoshi");
				this.balanceCoin = resultSet.getDouble("balanceCoin");
				this.currentValue = resultSet.getDouble("currentValue");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error getting coin values from database", e);
		}finally {
			db.close();
		}
		LOG.trace("Finished getWalletDetailsFromDB()");
	}
	
	/**
	 * Get the last known value of the coins 
	 */
	public void getLastKnownValues() {
		LOG.trace("Entering getLastKnownValues()");
		// first get the last entry of the wallet
		String query = "SELECT" + 
				" results.balance_satoshi as balanceSatoshi," + 
				" results.balance_coin as balanceCoin," + 
				" results.current_value as currentValue," + 
				" results.currency as currency," + 
				" coins.name as coinName" + 
				" FROM results, coins, wallets" + 
				" WHERE results.wallet_id = wallets.id" + 
				" AND coins.id = wallets.coin_id" + 
				" AND wallets.address = ?" +
				" ORDER BY results.timestamp DESC" +
				" LIMIT 1";
		
		// now call the method to handle the query
		this.getWalletDetailsFromDB(query);
		LOG.trace("Finished getLastKnownValues()");
	}
	
	/**
	 * Get the last known value of the coins 
	 */
	public void getFirstKnownValues() {
		LOG.trace("Entering getFirstKnownValues()");
		// first get the last entry of the wallet
		String query = "SELECT" + 
				" results.balance_satoshi as balanceSatoshi," + 
				" results.balance_coin as balanceCoin," + 
				" results.current_value as currentValue," + 
				" results.currency as currency," + 
				" coins.name as coinName" + 
				" FROM results, coins, wallets" + 
				" WHERE results.wallet_id = wallets.id" + 
				" AND coins.id = wallets.coin_id" + 
				" AND wallets.address = ?" +
				" ORDER BY results.timestamp ASC" +
				" LIMIT 1";
		
		// now call the method to handle the query
		this.getWalletDetailsFromDB(query);
		LOG.trace("Finished getFirstKnownValues()");
	}
	
	/**
	 * This method gets the ID of the wallet from the database to be able to use this ID to register the result
	 * @return the ID of the wallet
	 */
	private int getWalletID() {
		LOG.trace("Entering getWalletID()");
		
		int walletID = 0;
		
		String query = "SELECT id FROM wallets WHERE address = ?";
		Object[] parameters = new Object[] {this.walletAddress};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();

			// now register the received values
			while (resultSet.next()) {
				walletID = resultSet.getInt("id");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error getting the wallet ID from the database: {}", e);
		}finally {
			db.close();
		}
		
		LOG.trace("Finished getWalletID()");
		return walletID;
	}
	
	/**
	 * Register the current result
	 */
	public void registerCurrentResult() {
		LOG.trace("Entering registerCurrentResult()");
		
		int walletID = this.getWalletID();
		if(walletID != 0) {
			String query = "INSERT INTO results "+
		                   "(request_id, wallet_id, balance_satoshi, balance_coin, current_value, currency) " +
		                   "VALUES (?, ?, ?, ?, ?, ?)";
			Object[] parameters = new Object[] {this.requestID, walletID, this.balanceSatoshi, this.balanceCoin, this.currentValue, this.currency};
			
			MySQLAccess db = new MySQLAccess();
			try {
				db.executeUpdateQuery(query, parameters);
				LOG.info("Registered result to the database");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				LOG.fatal("Error saving the result to the database: {}", e);
			}finally {
				db.close();
			}
			
		}
		LOG.trace("Finished registerCurrentResult()");
	}
	
}
