package cryptoBot;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
	
	public void handleRequest() {
		// get the available coins
		Portfolio portfolio = new Portfolio();
		portfolio.setCoins();
		
		// generate a request ID
		this.uuid = UUID.randomUUID().toString();
		
		this.registerRequest();
		this.getRequestID();
		
		// check if all coins are requested
		if(this.requestedCoin.equals("all")) {
			// all coins will result in a loop through the coins in the database
			for(String coinName : portfolio.getCoins()) {
				System.out.println(coinName);
				this.runCoinRequest(coinName);
				this.generateStatusMessageForCoin(coinName);
			}
		}else {
			if(portfolio.getCoins().contains(this.requestedCoin)) {
				this.runCoinRequest(this.requestedCoin);
			}else {
				System.out.println("Coin bestaat niet");
			}
		}
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
			
			db.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void generateStatusMessageForCoin(String coinName) {
		double differenceCoin = this.currentBalanceCoin - this.previousBalanceCoin;
		double differenceCoinPercentage = (100 * differenceCoin) / this.currentBalanceCoin;
		
		double differenceValue = this.currentValue - this.previousValue;
		double differenceValuePercentage = (100 * differenceValue) / this.currentValue;
		
		
		this.statusMessage += String.format("Coin: %s\n", coinName);
		this.statusMessage += String.format("Current balance: %f (%+.5f, %+.2f%%)\n", this.currentBalanceCoin, differenceCoin, differenceCoinPercentage);
		this.statusMessage += String.format("Current value: %f (%+.2f euro, %+.2f%%)\n\n", this.currentValue, differenceValue, differenceValuePercentage);
		
	}
	
	
}
