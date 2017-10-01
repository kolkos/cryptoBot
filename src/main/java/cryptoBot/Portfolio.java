package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Portfolio {
	private List<String> coins;
	private List<HashMap<String, String>> wallets;
	
	private double totalValuePortfolio;
	
	private static final Logger LOG = LogManager.getLogger(Portfolio.class);
	
	public List<String> getCoins() {
		return this.coins;
	}

	public List<HashMap<String, String>> getWallets(){
		return this.wallets;
	}

	public void setCoins() {
		this.receiveCoinsInPortfolio();
	}

	public void setWallets() {
		this.receiveWalletsInPortfolio();
	}

	/**
	 * Getting the coins registered in the portfolio. This method will get all used coins with a wallet attached.
	 */
	private void receiveCoinsInPortfolio() {
		LOG.trace("Entering receiveCoinsInPortfolio()");
		
		String query = "SELECT" + 
				" coins.name as coinName" + 
				" FROM coins, wallets" + 
				" WHERE wallets.coin_id = coins.id";
		Object[] parameters = new Object[] {};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			coins = new ArrayList<>();
			while (resultSet.next()) {
				coins.add(resultSet.getString("coinName"));
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
	 * This method is used to receive the wallets in the portfolio. Only wallets with an coin will be return
	 */
	private void receiveWalletsInPortfolio() {
		LOG.trace("Entering receiveWalletsInPortfolio()");
		
		String query = "SELECT wallets.address as walletAddress, coins.name as coinName FROM wallets, coins WHERE wallets.coin_id = coins.id";
		Object[] parameters = new Object[] {};
		
		MySQLAccess db = new MySQLAccess();
		
		this.wallets = new ArrayList<>();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			while (resultSet.next()) {
				HashMap<String, String> values = new HashMap<>();
				
				values.put("coinName", resultSet.getString("coinName"));
				values.put("walletAddress", resultSet.getString("walletAddress"));
				
				this.wallets.add(values);
				
			}
			
			db.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.fatal("Error receiving wallets from database: {}", e);
		}finally {
			db.close();
		}
		LOG.trace("Finished receiveWalletsInPortfolio()");
	}
	
	
	
	
}
