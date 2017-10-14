package cryptoBot;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class TestWallet {

	public int walletID;
	public double expectedTotalDepositedCoin;
	public double expectedTotalCoinValue;
	
	private static final Logger LOG = LogManager.getLogger(TestWallet.class);
	
	public void prepareDatabaseWithTestData() {
		this.expectedTotalDepositedCoin = 0;
		this.expectedTotalCoinValue = 0;
		
		// prepare the database
		
		// first register the test wallet
		
		String walletAddress = "38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L";
		int coinID = 1;
		
		MySQLAccess db = new MySQLAccess();
				
		String query = null;
		query = "INSERT INTO wallets (coin_id, address) VALUES (?, ?)";
		Object[] parameters = new Object[] {coinID, walletAddress};
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		// now get the wallet ID for this wallet
		Wallet wallet = new Wallet();
		this.walletID = wallet.getWalletIDFromDB(walletAddress);
		
		// now insert to deposits with random values to the database
		
		for(int i=0; i<2; i++) {
			Random r = new Random();
			
			// first generate a random coin amount
			double randomCoinMin = 0;
			double randomCoinMax = 3;
			double randomDepositCoinAmount = randomCoinMin + (randomCoinMax - randomCoinMin) * r.nextDouble();
			// add it to the total
			this.expectedTotalDepositedCoin += randomDepositCoinAmount;
			
			// now generate a random value for this purchase
			double randomValueMin = 15;
			double randomValueMax = 45;
			double randomDepositValue = randomValueMin + (randomValueMax - randomValueMin) * r.nextDouble();
			// add it to the total
			this.expectedTotalCoinValue += randomDepositValue;
			
			// register this values to the database
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy", new Locale("nl_NL"));
			Date date;
			try {
				date = format.parse("13-10-1017");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				date = new Date();
			}
			
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			
			query = "INSERT INTO deposits (deposit_date, wallet_id, coin_amount, purchase_value, remarks) VALUES (?, ?, ?, ?, ?)";
			parameters = new Object[] {sqlDate, this.walletID, randomDepositCoinAmount, randomDepositValue, "JUnit test!"};
			try {
				db.executeUpdateQuery(query, parameters);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void deleteTestJunk() {
		List<String> queries = new ArrayList<>();
		queries.add("DELETE FROM wallets WHERE id = ?");
		queries.add("DELETE FROM deposits WHERE wallet_id = ?");
		queries.add("DELETE FROM results WHERE wallet_id = ?");
		Object[] parameters = new Object[] {this.walletID};
		
		MySQLAccess db = new MySQLAccess();
		
		// run the queries in a loop
		for(String query : queries) {
			// run the query
			try {
				db.executeUpdateQuery(query, parameters);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testWallet() throws Exception {
		Wallet testWallet = new Wallet();
		String walletAddress = "38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L";
		
		// first prepare the database
		this.prepareDatabaseWithTestData();
		
		// now run the get info method
		testWallet.getWalletValue(walletAddress);
		
		// now do some testing with values we can predict
		// get the wallet address
		assertEquals(walletAddress, testWallet.getWalletAddress());
		
		// get the walletID
		assertEquals(this.walletID, testWallet.getWalletID());
		
		// get the coin name
		assertEquals("btc", testWallet.getCoinName());
				
		// get the total deposited value
		assertEquals(this.expectedTotalCoinValue, testWallet.getTotalDepositedValue(), 0.01);
		
		// now dump all the other values
		LOG.info("Coin name       => {}", testWallet.getCoinName());
		LOG.info("Wallet address  => {}", testWallet.getWalletAddress());
		LOG.info("Balance satoshi => {}", testWallet.getBalanceSatoshi());
		LOG.info("Balance         => {}", testWallet.getBalanceCoin());
		LOG.info("Last value      => {}", testWallet.getLastKnownValue());
		LOG.info("Total deposited => {}", testWallet.getTotalDepositedValue());
		
		// now remove the test data again
		//this.deleteTestJunk();
	}
	
	
	
	

}
