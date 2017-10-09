package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Portfolio {
	private List<String> coinList;
	private List<Coin> coins;
	private double totalCurrentValuePortfolio = 0;
	private double totalPreviousValuePortfolio = 0;
	
	private static final Logger LOG = LogManager.getLogger(Portfolio.class);
	
	public List<Coin> getCoins() {
		return this.coins;
	}

	public List<String> getCoinList() {
		return this.coinList;
	}
	
	public void setCoinList() {
		this.receiveCoinsInPortfolio();
	}
	
	public double getTotalCurrentValuePortfolio() {
		return this.totalCurrentValuePortfolio;
	}

	public double getTotalPreviousValuePortfolio() {
		return totalPreviousValuePortfolio;
	}


	/**
	 * Getting the coins registered in the portfolio. This method will get all used coins with a wallet attached.
	 */
	private void receiveCoinsInPortfolio() {
		LOG.trace("Entering receiveCoinsInPortfolio()");
		
		String query = "SELECT" + 
				" coins.name as coinName" + 
				" FROM coins, wallets" + 
				" WHERE wallets.coin_id = coins.id "
				+ "AND coins.name != 'tst'";
		Object[] parameters = new Object[] {};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			coinList = new ArrayList<>();
			while (resultSet.next()) {
				coinList.add(resultSet.getString("coinName"));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error receiving coins from database: {}", e);
		}finally {
			db.close();
		}
		LOG.trace("Finished receiveCoinsInPortfolio()");
	}
	
	/**
	 * This method gets a single coin from the portfolio
	 * @param coinName the name of the coin
	 */
	public void getCoinInPortfolio(String coinName) {
		LOG.trace("Entering getCoinInPortfolio(), coinName={}", coinName);
		
		this.coins = new ArrayList<>();
		Coin coin = new Coin();
		coin.getWalletsForCoin(coinName);
		
		// add the total coin value to the portfolio value
		this.totalCurrentValuePortfolio += coin.getTotalCurrentCoinValue();
		
		// add the previous known value
		this.totalPreviousValuePortfolio += coin.getTotalLastKnownCoinValue();
		
		// add to the list
		this.coins.add(coin);
		
		LOG.trace("Finished getCoinInPortfolio()");
	}
	
	/**
	 * This method gets all the coins in the portfolio
	 */
	public void getAllCoinsInPortfolio() {
		LOG.trace("Entering getAllCoinsInPortfolio()");
		
		this.coins = new ArrayList<>();
		
		// reuse the receiveCoinsInPortfolio method
		this.receiveCoinsInPortfolio();
		for(String coinName : coinList) {
			Coin coin = new Coin();
			coin.getWalletsForCoin(coinName);
			
			// calculate the total value
			this.totalCurrentValuePortfolio += coin.getTotalCurrentCoinValue();
			
			// add the previous known value
			this.totalPreviousValuePortfolio += coin.getTotalLastKnownCoinValue();
			
			// add to the list
			this.coins.add(coin);
		}
		LOG.trace("Finished getAllCoinsInPortfolio()");
	}
	
	
	
}
