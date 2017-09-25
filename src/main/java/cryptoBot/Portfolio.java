package cryptoBot;

import java.sql.ResultSet;
import java.util.*;

public class Portfolio {
	private List<String> coins;
	
	private double totalValuePortfolio;
	
	
	public List<String> getCoins() {
		return this.coins;
	}


	public void setCoins() {
		this.receiveCoinsInPortfolio();
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
		}
		
	}
	
	
	
	
	
}
