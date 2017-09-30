package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

public class Portfolio {
	private List<String> coins;
	private List<HashMap<String, String>> wallets;
	
	private double totalValuePortfolio;
	
	
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

	private void receiveCoinsInPortfolio() {
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
			
			db.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			db.close();
		}
		
	}
	
	private void receiveWalletsInPortfolio() {
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
		}finally {
			db.close();
		}
	}
	
	
	
	
}
