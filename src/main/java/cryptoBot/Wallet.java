package cryptoBot;

import java.net.URL;
import java.sql.ResultSet;
import java.util.Scanner;

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
	    
	    return jsonObject;
	}
	
	/**
	 * This method does a API call to get the current balance of this object
	 */
	public void getWalletValue() {
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
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method converts the balance in Satoshi to the value of the coin
	 */
	private void convertSatoshiToCoin() {
		this.balanceCoin = this.balanceSatoshi * 0.00000001;
	}
	
	/**
	 * Calculate the value of the coins in this wallet to the chosen currency
	 */
	private void calculateCurrentValue() {
		// build the url for the api request
		String url = "https://www.bitstamp.net/api/v2/ticker_hour/" + this.coinName + this.currency + "/";
		
		try {
			// try to get the current coin value from the api
			JSONObject json = this.doAPIRequest(url);
			
			// get the current value of the coin from the json response
			double currentCoinValue = Double.parseDouble((String) json.get("last"));
			
			// calculate the current value of the coin
			this.currentValue = this.balanceCoin * currentCoinValue;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Semi-general method to get the coin values from the database
	 * @param query the SQL query to get the results
	 */
	private void getWalletDetailsFromDB(String query) {
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
			
			db.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the last known value of the coins 
	 */
	public void getLastKnownValues() {
		/*
		 * SELECT * FROM
			(SELECT
			results.balance_satoshi as balanceSatoshi,
			results.balance_coin as balanceCoin,
			results.current_value as currentValue,
			results.currency as currency,
			coins.name as coinName,
			results.timestamp as timestamp
			FROM results, coins, wallets
			WHERE results.wallet_id = wallets.id
			AND coins.id = wallets.coin_id
			AND wallets.address = 'LMWPhNFedT8e6X7iRQdh456hvZwYnxyStV'
			ORDER BY results.timestamp DESC
			LIMIT 2)x
			ORDER BY timestamp LIMIT 1
		 */
		
		
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
		
		this.getWalletDetailsFromDB(query);
		
	}
	
	/**
	 * Get the last known value of the coins 
	 */
	public void getFirstKnownValues() {
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
		
		this.getWalletDetailsFromDB(query);
		
	}
	
	private int getWalletID() {
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
			
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return walletID;
	}
	
	public void registerCurrentResult() {
		// check if the result needs to be registered
		// this isn't necessary when getting the historic results

		int walletID = this.getWalletID();
		if(walletID != 0) {
			String query = "INSERT INTO results "+
		                   "(request_id, wallet_id, balance_satoshi, balance_coin, current_value, currency) " +
		                   "VALUES (?, ?, ?, ?, ?, ?)";
			Object[] parameters = new Object[] {this.requestID, walletID, this.balanceSatoshi, this.balanceCoin, this.currentValue, this.currency};
			
			MySQLAccess db = new MySQLAccess();
			try {
				db.executeUpdateQuery(query, parameters);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
}
