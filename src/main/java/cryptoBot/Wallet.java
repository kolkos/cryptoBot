package cryptoBot;

import java.net.URL;
import java.sql.ResultSet;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Wallet {
	private String walletAddress;
	private String coinName;
	private static String currency = "eur";
	
	private double balanceCoin;
	private double currentValue;
	private double lastKnownValue;
	
	private int walletID;
	private int balanceSatoshi;
	private int requestID = 0;
	
	private static final Logger LOG = LogManager.getLogger(Wallet.class);
	
	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public double getLastKnownValue() {
		return lastKnownValue;
	}

	public int getWalletID() {
		return this.walletID;
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
	 * Simple method to get the id of a wallet address from the database
	 * @param walletAddress the address of the wallet
	 * @return id of the wallet in the database
	 */
	public int getWalletIDFromDB(String walletAddress) {
		// first set the walletID to 0
		LOG.trace("Entering getWalletIDFromDB()");
		int walletID = 0;
		
		String query = "SELECT id FROM wallets WHERE address = ?";
		Object[] parameters = new Object[] {walletAddress};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			ResultSet resultSet = db.getResultSet();
			while (resultSet.next()) {
				walletID = resultSet.getInt("id");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		LOG.trace("Finished getWalletIDFromDB()");
		return walletID;
	}
	
	/**
	 * Get the name of the coin for the selected wallet.
	 * This method sets the attribute
	 */
	public void getCoinNameForWallet() {
		LOG.trace("Entering getCoinNameForWallet()");
		String query = "SELECT coins.name AS coinName " + 
				"FROM coins, wallets " + 
				"WHERE wallets.coin_id = coins.id " + 
				"AND wallets.address = ? "
				+ "LIMIT 1";
		Object[] parameters = new Object[] {this.walletAddress};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			ResultSet resultSet = db.getResultSet();
			while (resultSet.next()) {
				this.coinName = resultSet.getString("coinName");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
		LOG.trace("Finished getCoinNameForWallet()");
	}
	
	/**
	 * This method gets the current value for the selected wallet. It uses the api request to get the values
	 * It also registers the attributes
	 * @param coinName the name of the coin
	 * @param walletAddress the address of the wallet 
	 */
	public void getWalletValue(String walletAddress) {
		LOG.trace("Entering getWalletValue()");
		
		// register the attributes
		this.setWalletAddress(walletAddress);
		this.getCoinNameForWallet();

		try {
			// try to run the json request
			
			ApiRequest api = new ApiRequest();
			JSONObject json = api.walletInfoApiRequest(this.coinName, this.walletAddress);
			
			// get the current balance (in Satoshi) from the response
			//String balanceSatoshiString = (String) 
			this.balanceSatoshi = (int) json.get("final_balance");
			
			// convert the value in Satoshi to the coin value
			this.convertSatoshiToCoin();
			
			// now calculate the value of the wallet
			this.calculateCurrentValue();
			
			// get the last known value
			this.lastKnownValue = this.getPreviousKnownWalletValue();

			// get the ID of this wallet
			this.walletID = this.getWalletIDFromDB();
			
			// register this result
			this.registerCurrentResult();
			
			LOG.info("API request OK.");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("API request ERROR: {}", e);
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
		
		try {
			// try to get the current coin value from the api
			ApiRequest api = new ApiRequest();
			JSONObject json = api.currentCoinValueApiRequest(this.coinName, Wallet.currency);
			
			// get the current value of the coin from the json response
			double currentCoinValue = Double.parseDouble((String) json.get("last"));
			
			// calculate the current value of the coin
			this.currentValue = this.balanceCoin * currentCoinValue;
			
			LOG.info("API request OK.");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("API request ERROR: {}", e);
		}
		LOG.trace("Finished calculateCurrentValue()");
		
	}
	
	private double getPreviousKnownWalletValue() {
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
		Object[] parameters = new Object[] {this.walletAddress};
		
		MySQLAccess db = new MySQLAccess();
		
		double lastKnownValue = 0;
		
		try {
			db.executeSelectQuery(query, parameters);
			ResultSet resultSet = db.getResultSet();
			while (resultSet.next()) {
				lastKnownValue = resultSet.getDouble("currentValue");
			}
		} catch (Exception e) {
			LOG.fatal("Error getting coin values from database", e);
		} finally {
			db.close();
		}
		return lastKnownValue;
	}
	
	
	/**
	 * This method gets the ID of the wallet from the database to be able to use this ID to register the result
	 * @return the ID of the wallet
	 */
	private int getWalletIDFromDB() {
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
