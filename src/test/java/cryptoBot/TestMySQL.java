package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMySQL {

	@Test
	public void testQuery() {
		MySQLAccess db = new MySQLAccess();
		
		
		String q = "SELECT wallets.address AS walletAddress, coins.name AS coinName FROM wallets, coins WHERE wallets.coin_id = coins.id AND coins.name = ?";
		
		Object[] parameters = new Object[] {"ltc"};
		
		try {
			db.executeSelectQuery(q, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			
		}
		
		q = "SELECT wallets.address AS walletAddress, coins.name AS coinName FROM wallets, coins WHERE wallets.coin_id = coins.id AND coins.name = ? AND coins.id = ?";
		parameters = new Object[] {"ltc", 2};
		
		try {
			db.executeSelectQuery(q, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			
		}
		
		q = "SELECT wallets.address AS walletAddress, coins.name AS coinName FROM wallets, coins WHERE wallets.coin_id = coins.id";
		parameters = new Object[] {};
		
		try {
			db.executeSelectQuery(q, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//q = "INSERT INTO ";
		
	}

}
