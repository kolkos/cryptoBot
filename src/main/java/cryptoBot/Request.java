package cryptoBot;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Request {
	private String requestedBy;
	private String requestedCoin;
	private String calculateSince = "last";
	
	private double previousBalanceCoin;
	private double previousValue;
	
	private double currentBalanceCoin;
	private double currentValue;
	
	private String statusMessage = "";
	
	private String uuid;
	private int requestID = 0;
	
	private double totalCurrentValuePortfolio = 0;
	private double totalPrevioustValuePortfolio = 0;
	
	private String lastRequestedBy;
	
	private int walletBalanceSatoshi;
	private double walletBalanceCoin;
	private double walletCurrentValue;
	
	private static final Logger LOG = LogManager.getLogger(Request.class);
	
	public int getWalletBalanceSatoshi() {
		return this.walletBalanceSatoshi;
	}
	
	public String getCalculateSince() {
		return this.calculateSince;
	}

	public void setCalculateSince(String calculateSince) {
		this.calculateSince = calculateSince;
	}

	public String getRequestedBy() {
		return this.requestedBy;
	}
	
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	
	public String getRequestedCoins() {
		return this.requestedCoin;
	}
	
	public void setRequestedCoins(String requestedCoins) {
		this.requestedCoin = requestedCoins;
	}
	
	public String getStatusMessage() {
		return this.statusMessage;
	}
	
	/**
	 * This method will append a text at the beginning of the status message.
	 * @param message text to add to the beginning of the message
	 */
	public void appendToStatusMessage(String message) {
		String statusMessageBuffer = message + this.statusMessage;
		
		this.statusMessage = statusMessageBuffer;
	}
	
	/**
	 * This method is called to handle the incoming Coin request.
	 * It will first create an unique identifier. This identifier is used to get the ID of the request from the database
	 * If a specific coin is set, it will just get this coin, when 'all' is used all the coins in the database will be handled.
	 */
	public void handleCoinRequest() {
		LOG.trace("Entering handleCoinRequest()");
		
		// get the available coins
		Portfolio portfolio = new Portfolio();
		portfolio.setCoins();
		
		// generate a request ID
		this.uuid = UUID.randomUUID().toString();
		
		// get the last person who jinxed it
		this.getLastRequestedBy();
		
		// register this call
		this.registerRequest();
		this.getRequestID();
		
		// check if all coins are requested
		if(this.requestedCoin.equals("all")) {
			LOG.info("All coins are requested");
			// all coins will result in a loop through the coins in the database
			for(String coinName : portfolio.getCoinList()) {
				this.runCoinRequest(coinName);
				this.generateStatusMessageForCoin(coinName);
			}
		}else {
			LOG.info("Only {} will be handled", this.requestedCoin);
			if(portfolio.getCoinList().contains(this.requestedCoin)) {
				this.runCoinRequest(this.requestedCoin);
				this.generateStatusMessageForCoin(this.requestedCoin);
			}else {
				this.statusMessage += "Deze coin bestaat niet!";
				LOG.warn("The coin {} is not registered", this.requestedCoin);
			}
		}
		
		this.addTotalValueToMessage();
		this.addLastRequestedByToMessage();
		
		LOG.trace("Finished handleCoinRequest()");
	}
	
	/**
	 * This method handles a incoming wallet request. This means getting all the registered wallets from the database and get each of the wallets value
	 */
	public void handleWalletRequest() {
		LOG.trace("Entering handleWalletRequest()");
		
		// get the available wallets
		Portfolio portfolio = new Portfolio();
		portfolio.setWallets();
		
		// generate a request ID
		this.uuid = UUID.randomUUID().toString();
		
		// get the last person who jinxed it
		this.getLastRequestedBy();
		
		// register this call
		this.registerRequest();
		this.getRequestID();
		
		// now loop through the wallets
		for(HashMap<String, String> wallet : portfolio.getWallets()) {
			String coinName = wallet.get("coinName");
			String walletAddress = wallet.get("walletAddress");
			
			// get the value
			this.runWalletRequest(walletAddress, coinName);
			
			// add to the status message
			this.addWalletToStatusMessage(walletAddress, coinName);
		}
		
		this.addLastRequestedByToMessage();
		LOG.trace("Finished handleWalletRequest()");
	}
	
	/**
	 * This method writes (appends) the result of the wallet request to the status message
	 * @param walletAddress the address of the requested wallet
	 * @param coinName the name of the coin connected to the wallet
	 */
	private void addWalletToStatusMessage(String walletAddress, String coinName) {
		LOG.trace("Entering addWalletToStatusMessage(), walletAddress={}, coinName={}", walletAddress, coinName);
		this.statusMessage += String.format("Wallet address: %s\n", walletAddress);
		this.statusMessage += String.format("Coin: %s\n", coinName);
		this.statusMessage += String.format("Current ammount: %f\n", this.walletBalanceCoin);
		this.statusMessage += String.format("Current value: %.2f euro\n\n", this.walletCurrentValue);
		LOG.trace("Finished addWalletToStatusMessage()");
	}
	
	/**
	 * This method handles the wallet request. It will use the wallet class.
	 * @param walletAddress the address of the requested wallet
	 * @param coinName the name of the coin connected to the wallet
	 */
	private void runWalletRequest(String walletAddress, String coinName) {
		LOG.trace("Entering runWalletRequest(), walletAddress={}, coinName={}", walletAddress, coinName);
		
		Wallet wallet = new Wallet();
		wallet.setCoinName(coinName);
		wallet.setWalletAddress(walletAddress);
		wallet.setCurrency("eur");
		wallet.setRequestID(this.requestID);
		
		// now run the request
		wallet.getWalletValue();
		
		// now calculate the values
		this.walletBalanceSatoshi = wallet.getBalanceSatoshi();
		this.walletBalanceCoin = wallet.getBalanceCoin();
		this.walletCurrentValue = wallet.getCurrentValue();
		
		LOG.trace("Finished runWalletRequest()");
	}
	
	/**
	 * Handling the incoming Coin request. This method uses the Coin class to handle the request. This method will first
	 * get the previous result (the first registered result or the last result).
	 * @param coinName the name of the request coin
	 */
	private void runCoinRequest(String coinName) {
		LOG.trace("Entering runCoinRequest(), coinName={}", coinName);
		
		// get the previous results
		Coin previous = new Coin();
		previous.setCoinName(coinName);
		previous.setWalletAddresses();
		previous.setRequestID(this.requestID);
		boolean sinceBegin;
		
		// check if last result is needed (or the first registered result)
		if(this.calculateSince.equals("last")) {
			sinceBegin = false;
		}else {
			sinceBegin = true;
		}
		previous.calculatePreviousTotalValuesForCoin(sinceBegin);
		
		// register the values to variables
		//this.previousBalanceSatoshi = previous.getTotalBalanceSatoshi();
		this.previousBalanceCoin = previous.getTotalBalanceCoin();
		this.previousValue = previous.getTotalCurrentValue();
		
		// now add the total previous value of the portfolio 
		this.totalPrevioustValuePortfolio += this.previousValue;
		
		// now get the new values
		Coin coin = new Coin();
		coin.setCoinName(coinName);
		coin.setRequestID(this.requestID);
		coin.setWalletAddresses();
		coin.calculateCurrentTotalValuesForCoin();
		
		//this.currentBalanceSatoshi = previous.getTotalBalanceSatoshi();
		this.currentBalanceCoin = coin.getTotalBalanceCoin();
		this.currentValue = coin.getTotalCurrentValue();
		
		// add the current value to the portfolio
		this.totalCurrentValuePortfolio += this.currentValue;
		
		LOG.trace("Finished runWalletRequest()");
	}
	
	/**
	 * This method registers the incoming request. The uuid is registerd to be able to find the ID of the request in a later stage
	 */
	private void registerRequest() {
		LOG.trace("Entering registerRequest()");
		
		String query = "INSERT INTO requests (uuid, name, since) VALUES (?, ?, ?)";
		Object[] parameters = new Object[] {this.uuid, this.requestedBy, this.calculateSince};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error registering request: {}", e);
		}finally {
			db.close();
		}
		LOG.trace("Finished registerRequest()");
	}
	
	/**
	 * This method gets the ID of the registerd request.
	 * It uses the uuid to find this request. It will only get the last registered ID. This is done for the rare occasion that the UUID is duplicate
	 */
	private void getRequestID() {
		LOG.trace("Entering getRequestID()");
		
		String query = "SELECT id FROM requests WHERE uuid = ? ORDER BY id DESC LIMIT 1";
		Object[] parameters = new Object[] {this.uuid};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			while (resultSet.next()) {
				requestID = resultSet.getInt("id");
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error getting the request ID: {}", e);
		}finally {
			db.close();
		}
		LOG.trace("Finished getRequestID()");
	}
	
	/**
	 * This method generates a status message for a coin (and appends it to the existing status message).
	 * @param coinName the name of the requested coin
	 */
	private void generateStatusMessageForCoin(String coinName) {
		LOG.trace("Entering generateStatusMessageForCoin(), coinName={}", coinName);
		
		double differenceCoin = this.currentBalanceCoin - this.previousBalanceCoin;
		double differenceCoinPercentage = (100 * differenceCoin) / this.currentBalanceCoin;
		
		double differenceValue = this.currentValue - this.previousValue;
		double differenceValuePercentage = (100 * differenceValue) / this.currentValue;
				
		this.statusMessage += String.format("Coin: %s\n", coinName);
		this.statusMessage += String.format("Current balance: %f (%+.5f, %+.2f%%)\n", this.currentBalanceCoin, differenceCoin, differenceCoinPercentage);
		this.statusMessage += String.format("Current value: %.2f (%+.2f euro, %+.2f%%)\n\n", this.currentValue, differenceValue, differenceValuePercentage);
		
		LOG.trace("Finished generateStatusMessageForCoin()");
	}
	
	/**
	 * This method appends the current value (and the difference to the last request) to the status message. 
	 * Therefore it will calculate the difference (in value and percentage).
	 */
	private void addTotalValueToMessage() {
		LOG.trace("Entering addTotalValueToMessage()");
		double differenceValue = this.totalCurrentValuePortfolio - this.totalPrevioustValuePortfolio;
		double differenceValuePercentage = (100 * differenceValue) / this.totalCurrentValuePortfolio;
		
		this.statusMessage += String.format("Totale waarde van portfolio: %.2f (%+.2f euro, %+.2f%%)\n\n", this.totalCurrentValuePortfolio, differenceValue, differenceValuePercentage);
		LOG.trace("Finished addTotalValueToMessage()");
	}
	
	/**
	 * This method appends the information about the person who last requested the update and the new requester.
	 */
	private void addLastRequestedByToMessage() {
		LOG.trace("Entering addLastRequestedByToMessage()");
		this.statusMessage += String.format("Laatste aanvraag door %s. Vanaf nu kun je %s de schuld geven als het mis gaat...", this.lastRequestedBy, this.requestedBy);
		LOG.trace("Finished addLastRequestedByToMessage()");
	}
	
	/**
	 * This method simply gets the last person (or bot) who placed a request
	 */
	public void getLastRequestedBy() {
		LOG.trace("Entering getLastRequestedBy()");
		
		String query = "SELECT name FROM requests ORDER BY timestamp DESC LIMIT 1";
		Object[] parameters = new Object[] {};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			while (resultSet.next()) {
				this.lastRequestedBy = resultSet.getString("name");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error getting the last requester: {}", e);
		} finally {
			db.close();
		}
		LOG.trace("Finished getLastRequestedBy()");
	}
	
	
	
}
