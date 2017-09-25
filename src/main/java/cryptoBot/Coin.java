package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

public class Coin {
	private String coinName;
	private int totalBalanceSatoshi = 0;
	private double totalBalanceCoin = 0;
	private double totalCurrentValue = 0;
	private List<String> walletAddresses;
	private String currency = "eur";
	private int requestID;
	
	
	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public List<String> getWalletAddresses() {
		return this.walletAddresses;
	}

	public void setWalletAddresses() {
		this.receiveWalletAddresses();
	}

	public String getCoinName() {
		return this.coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}
	
	public int getTotalBalanceSatoshi() {
		return this.totalBalanceSatoshi;
	}
	
	public double getTotalBalanceCoin() {
		return this.totalBalanceCoin;
	}
	
	public double getTotalCurrentValue() {
		return this.totalCurrentValue;
	}
	
	private void receiveWalletAddresses() {
		String query = "SELECT wallets.address AS walletAddress " + 
				"FROM wallets, coins " + 
				"WHERE wallets.coin_id = coins.id " + 
				"AND coins.name = ?";
		Object[] parameters = new Object[] {this.coinName};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			this.walletAddresses = new ArrayList<>();
			while (resultSet.next()) {
				this.walletAddresses.add(resultSet.getString("walletAddress"));
			}
			
			db.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void calculateCurrentTotalValuesForCoin() {
		// loop through the addresses
		
		for(String walletAddress : this.walletAddresses) {
			//System.out.println(walletAddress);
			
			
			// first set the necessary values
			Wallet wallet = new Wallet();
			wallet.setCurrency(this.currency);
			wallet.setCoinName(this.coinName);
			wallet.setRequestID(this.requestID);
			wallet.setWalletAddress(walletAddress);
			
			// now get the current values from the api
			wallet.getWalletValue();
			
			// now calculate the values
			this.totalBalanceSatoshi += wallet.getBalanceSatoshi();
			this.totalBalanceCoin += wallet.getBalanceCoin();
			this.totalCurrentValue += wallet.getCurrentValue();
			
		}
	}
	
	public void calculatePreviousTotalValuesForCoin(boolean sinceBegin) {
		for(String walletAddress : this.walletAddresses) {
			//System.out.println(walletAddress);
			
			// first set the necessary values
			Wallet wallet = new Wallet();
			wallet.setCurrency(this.currency);
			wallet.setCoinName(this.coinName);
			wallet.setWalletAddress(walletAddress);
			
			// now determine which method needs to be called
			// this depends if the user wants to receive the first entry (the beginning) 
			// or since the last request
			if(sinceBegin) {
				wallet.getFirstKnownValues();
			}else {
				wallet.getLastKnownValues();
			}
			
			// now calculate the values
			this.totalBalanceSatoshi += wallet.getBalanceSatoshi();
			this.totalBalanceCoin += wallet.getBalanceCoin();
			this.totalCurrentValue += wallet.getCurrentValue();
			
		}
	}
	
}
