package cryptoBot;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
	
	public void appendToStatusMessage(String message) {
		String statusMessageBuffer = message + this.statusMessage;
		
		this.statusMessage = statusMessageBuffer;
	}
	
	public void handleCoinRequest() {
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
			// all coins will result in a loop through the coins in the database
			for(String coinName : portfolio.getCoins()) {
				this.runCoinRequest(coinName);
				this.generateStatusMessageForCoin(coinName);
			}
		}else {
			if(portfolio.getCoins().contains(this.requestedCoin)) {
				this.runCoinRequest(this.requestedCoin);
				this.generateStatusMessageForCoin(this.requestedCoin);
			}else {
				this.statusMessage += "Deze coin bestaat niet!";
			}
		}
		
		this.addTotalValueToMessage();
		this.addLastRequestedByToMessage();
	}
	
	public void handleWalletRequest() {
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
		
	}
	
	private void addWalletToStatusMessage(String walletAddress, String coinName) {
		this.statusMessage += String.format("Wallet address: %s\n", walletAddress);
		this.statusMessage += String.format("Coin: %s\n", coinName);
		this.statusMessage += String.format("Current ammount: %f\n", this.walletBalanceCoin);
		this.statusMessage += String.format("Current value: %.2f euro\n\n", this.walletCurrentValue);
		
	}
	
	private void runWalletRequest(String walletAddress, String coinName) {
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
		
	}
	
	private void runCoinRequest(String coinName) {
		// get the previous results
		Coin previous = new Coin();
		previous.setCoinName(coinName);
		previous.setWalletAddresses();
		previous.setRequestID(this.requestID);
		boolean sinceBegin;
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
		
		
	}
	
	private void registerRequest() {
		String query = "INSERT INTO requests (uuid, name, since) VALUES (?, ?, ?)";
		Object[] parameters = new Object[] {this.uuid, this.requestedBy, this.calculateSince};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			db.close();
		}
	}
	
	private void getRequestID() {
		String query = "SELECT id FROM requests WHERE uuid = ?";
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
			e.printStackTrace();
		}finally {
			db.close();
		}
		
	}
	
	
	private void generateStatusMessageForCoin(String coinName) {
		double differenceCoin = this.currentBalanceCoin - this.previousBalanceCoin;
		double differenceCoinPercentage = (100 * differenceCoin) / this.currentBalanceCoin;
		
		double differenceValue = this.currentValue - this.previousValue;
		double differenceValuePercentage = (100 * differenceValue) / this.currentValue;
		
		
		this.statusMessage += String.format("Coin: %s\n", coinName);
		this.statusMessage += String.format("Current balance: %f (%+.5f, %+.2f%%)\n", this.currentBalanceCoin, differenceCoin, differenceCoinPercentage);
		this.statusMessage += String.format("Current value: %.2f (%+.2f euro, %+.2f%%)\n\n", this.currentValue, differenceValue, differenceValuePercentage);
		
	}
	
	private void addTotalValueToMessage() {
		double differenceValue = this.totalCurrentValuePortfolio - this.totalPrevioustValuePortfolio;
		double differenceValuePercentage = (100 * differenceValue) / this.totalCurrentValuePortfolio;
		
		
		this.statusMessage += String.format("Totale waarde van portfolio: %.2f (%+.2f euro, %+.2f%%)\n\n", this.totalCurrentValuePortfolio, differenceValue, differenceValuePercentage);
		
	}
	
	private void addLastRequestedByToMessage() {
		this.statusMessage += String.format("Laatste aanvraag door %s. Vanaf nu kun je %s de schuld geven als het mis gaat...", this.lastRequestedBy, this.requestedBy);
	}
	
	public void getLastRequestedBy() {
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
			e.printStackTrace();
		} finally {
			db.close();
		}
		
	}
	
	
	
}
