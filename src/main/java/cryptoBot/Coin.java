package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Coin {
	private String coinName;
	private List<String> walletAddresses;
	private List<Wallet> wallets;
	private double totalCoinBalance = 0;
	private double totalCurrentCoinValue = 0;
	private double totalLastKnownCoinValue = 0;
	private double totalDepositedCoinValue = 0;
	
	private int requestID = 0;
	
	private static final Logger LOG = LogManager.getLogger(Coin.class);
	
	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public double getTotalCoinBalance() {
		return this.totalCoinBalance;
	}

	public void setTotalCoinBalance(double totalCoinBalance) {
		this.totalCoinBalance = totalCoinBalance;
	}

	public double getTotalDepositedCoinValue() {
		return totalDepositedCoinValue;
	}

	public void setTotalDepositedCoinValue(double totalDepositedCoinValue) {
		this.totalDepositedCoinValue = totalDepositedCoinValue;
	}

	public double getTotalLastKnownCoinValue() {
		return totalLastKnownCoinValue;
	}

	public List<Wallet> getWallets() {
		return this.wallets;
	}
	
	public double getTotalCurrentCoinValue() {
		return totalCurrentCoinValue;
	}

	/**
	 * This public method gets the wallet addresses for the selected coin
	 * @param coinName the name of the coin
	 * @return the list containing the wallets for the selected coin
	 */
	public List<String> getWalletAddresses(String coinName) {
		this.setCoinName(coinName);
		this.receiveWalletAddressesForCoin();
		return this.walletAddresses;
	}

	public String getCoinName() {
		return this.coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}
	
	
	/**
	 * Method to get the wallet addresses from the database
	 */
	private void receiveWalletAddressesForCoin() {
		LOG.trace("entered receiveWalletAddresses()");
		
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
				LOG.info("found wallet: {}", resultSet.getString("walletAddress"));
				this.walletAddresses.add(resultSet.getString("walletAddress"));
			}	
		} catch (Exception e) {
			LOG.fatal("Error receiving wallet addresses, {}", e);
		} finally {
			db.close();
		}
		LOG.trace("finished receiveWalletAddresses()");
		
	}
	
	/**
	 * This method gets the wallet for the selected coin
	 * @param coinName the name of the coin
	 */
	public void getWalletsForCoin(String coinName) {
		this.setCoinName(coinName);
		
		LOG.trace("entered getWalletsForCoin()");
		
		this.wallets = new ArrayList<>();
		// get the wallets for this coin
		// reuse the receiveWalletAddresses method
		this.receiveWalletAddressesForCoin();
		
		// loop through the addresses
		for(String walletAddress : this.walletAddresses) {
			Wallet wallet = new Wallet();
			wallet.setRequestID(this.requestID);

			// now receive the values for this wallet
			wallet.getWalletValue(walletAddress);
			
			// get the total coin balance
			this.totalCoinBalance += wallet.getBalanceCoin();
			
			// add the value of this wallet to the total value
			this.totalCurrentCoinValue += wallet.getCurrentValue();
			
			// calculate the last known coin value
			this.totalLastKnownCoinValue += wallet.getLastKnownValue();
			
			// get the total deposited value
			this.totalDepositedCoinValue += wallet.getTotalDepositedValue();
			
			// now add this address to the wallets list
			this.wallets.add(wallet);
			
			LOG.trace("added wallet {} for coin {}", walletAddress, this.coinName);
		}
		LOG.trace("finished getWalletsForCoin()");
		
	}
	

	
}
