package cryptoBot;

import static org.junit.Assert.*;

import java.sql.ResultSet;

import org.junit.Test;

public class TestMySQL {

	@Test
	public void testQuery() {
		MySQLAccess db = new MySQLAccess();
		
		
		String q = "SELECT wallets.address AS walletAddress, coins.name AS coinName FROM wallets, coins WHERE wallets.coin_id = coins.id AND coins.name = ?";
		
		Object[] parameters = new Object[] {"ltc"};
		
		try {
			db.executeSelectQuery(q, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			
			assertEquals(true, resultSet.next());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Database error!");
						
		}
		
		
		
	}

}
