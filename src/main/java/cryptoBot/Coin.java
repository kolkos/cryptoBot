package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Coin {
	private String coinName;
	private List<String> walletAddresses;
	private List<Wallet> wallets;
	private double totalCurrentCoinValue = 0;
	private double totalLastKnownCoinValue = 0;
	
	private static final Logger LOG = LogManager.getLogger(Coin.class);
	
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

			// now receive the values for this wallet
			wallet.getWalletValue(walletAddress);
			
			// add the value of this wallet to the total value
			this.totalCurrentCoinValue += wallet.getCurrentValue();
			
			// calculate the last known coin value
			this.totalLastKnownCoinValue += wallet.getLastKnownValue();
						
			// now add this address to the wallets list
			this.wallets.add(wallet);
			
			LOG.trace("added wallet {} for coin {}", walletAddress, this.coinName);
		}
		LOG.trace("finished getWalletsForCoin()");
		
	}
	
//	/**
//	 * This method gets all the wallets
//	 */
//	public void getAllWallets() {
//		LOG.trace("entered getAllWallets()");
//		this.wallets = new ArrayList<>();
//		
//		String query = "SELECT wallets.address AS walletAddress, coins.name AS coinName " + 
//				"FROM wallets, coins " + 
//				"WHERE wallets.coin_id = coins.id " +
//				"AND coins.name != 'tst'";
//		Object[] parameters = new Object[] {};
//		MySQLAccess db = new MySQLAccess();
//			
//		
//		try {
//			// run the query
//			db.executeSelectQuery(query, parameters);
//			
//			// get the result set
//			ResultSet resultSet = db.getResultSet();
//			
//			// now loop through the results
//			while (resultSet.next()) {
//				LOG.info("found wallet {} for coin {}", resultSet.getString("walletAddress"), resultSet.getString("coinName"));
//				
//				Wallet wallet = new Wallet();
//				// now receive the values for this wallet
//				wallet.getWalletValue(resultSet.getString("coinName"), resultSet.getString("walletAddress"));
//				
//				// now add this address to the wallets list
//				this.wallets.add(wallet);
//			}
//			
//		} catch (Exception e) {
//			LOG.fatal("Error receiving wallets, {}", e);
//		}finally {
//			db.close();
//		}
//	}
	
	
//	/**
//	 * This method calculates the value from the found wallets
//	 */
//	public void calculateCurrentTotalValuesForCoin() {
//		LOG.trace("entered calculateCurrentTotalValuesForCoin()");
//		
//		// loop through the addresses
//		
//		for(String walletAddress : this.walletAddresses) {
//			LOG.trace("getting current value for wallet {}", walletAddress);
//			
//			//System.out.println(walletAddress);
//			
//			
//			// first set the necessary values
//			Wallet wallet = new Wallet();
//			wallet.setCurrency(this.currency);
//			wallet.setCoinName(this.coinName);
////			wallet.setRequestID(this.requestID);
//			wallet.setWalletAddress(walletAddress);
//			
//			// now get the current values from the api
//			wallet.getWalletValue();
//			
//			// now append the value from the current wallet to the total coin values
//			this.totalBalanceSatoshi += wallet.getBalanceSatoshi();
//			this.totalBalanceCoin += wallet.getBalanceCoin();
//			this.totalCurrentValue += wallet.getCurrentValue();
//			
//		}
//		
//		LOG.trace("finished calculateCurrentTotalValuesForCoin()");
//	}
//	
//	/**
//	 * Get the values from the coins from the database (results table)
//	 * These values are used to compare the current value to
//	 * @param sinceBegin depending on this value it will get the last or the first value
//	 */
//	public void calculatePreviousTotalValuesForCoin() {
//		LOG.trace("entered calculatePreviousTotalValuesForCoin()");
//		
//		for(String walletAddress : this.walletAddresses) {
//			LOG.trace("getting previous value for wallet {}", walletAddress);
//			//System.out.println(walletAddress);
//			
//			// first set the necessary values
//			Wallet wallet = new Wallet();
//			wallet.setCurrency(this.currency);
//			wallet.setCoinName(this.coinName);
//			wallet.setWalletAddress(walletAddress);
//			
//
//			wallet.getLastKnownValues();
//			
//			
//			// now calculate the values
//			this.totalBalanceSatoshi += wallet.getBalanceSatoshi();
//			this.totalBalanceCoin += wallet.getBalanceCoin();
//			this.totalCurrentValue += wallet.getCurrentValue();
//			
//		}
//		LOG.trace("finished calculatePreviousTotalValuesForCoin()");
//	}
	
}
