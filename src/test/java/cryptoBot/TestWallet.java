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
	
	public void createSemiFakeWallet() {
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
		
	}
	
	
	
	public void runRandomDepositCommands() {
		long chatID = -236099150;
		String walletAddress = "38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L";
		
		for(int i=0; i<5; i++) {
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
			
			// now generate the command
			String commandString = String.format("/deposit 14-10-2017 %s %.8f %.2f JUnit test deposit", walletAddress, randomDepositCoinAmount, randomDepositValue);
			
			// run this command string
			TextMessageHandler textMessageHandler = new TextMessageHandler();
			// register a fake text message
			textMessageHandler.registerChatMessage(chatID, "JUnit", commandString);
			
			// now run runTextMessageCommand
			textMessageHandler.runTextMessageCommand();
						
		}
		
	}
	
	public void confirmFakeTransactions() {
		String query = "UPDATE deposits SET confirmed = 1 WHERE wallet_id = ?";
		Object[] parameters = new Object[] {this.walletID};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cleanUpTestData() {
		MySQLAccess db = new MySQLAccess();
		
		// delete the deposits
		String query = "DELETE FROM deposits WHERE wallet_id = ?";
		Object[] parameters = new Object[] {this.walletID};
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// delete the requests
		query = "DELETE FROM requests WHERE firstName = ?";
		parameters = new Object[] {"JUnit"};
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// delete the results
		query = "DELETE FROM results WHERE wallet_id = ?";
		parameters = new Object[] {this.walletID};
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// delete the wallet
		query = "DELETE FROM wallets WHERE id = ?";
		parameters = new Object[] {this.walletID};
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWallet() {
		// first generate a semi fake wallet
		this.createSemiFakeWallet();
		
		Wallet testWallet = new Wallet();
		String walletAddress = "38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L";
		
		this.walletID = testWallet.getWalletIDFromDB(walletAddress);
		
		// now run some deposit commands
		this.runRandomDepositCommands();
		
		// now run the get info method
		testWallet.getWalletValue(walletAddress);
		
		// now do some testing with values we can predict
		// get the wallet address
		assertEquals(walletAddress, testWallet.getWalletAddress());
		
		// get the coin name
		assertEquals("btc", testWallet.getCoinName());
				
		// get the total deposited value
		// because transactions aren't confirmed, value is 0
		assertEquals(0, testWallet.getTotalDepositedValue(), 0.01);
		
		// now dump all the other values
		LOG.info("Coin name       => {}", testWallet.getCoinName());
		LOG.info("Wallet address  => {}", testWallet.getWalletAddress());
		LOG.info("Balance satoshi => {}", testWallet.getBalanceSatoshi());
		LOG.info("Balance         => {}", testWallet.getBalanceCoin());
		LOG.info("Last value      => {}", testWallet.getLastKnownValue());
		LOG.info("Total deposited => {}", testWallet.getTotalDepositedValue());
		
		// now confirm the fake transactions
		this.confirmFakeTransactions();
		
		// run the getWalletValue method again
		testWallet.getWalletValue(walletAddress);
		
		assertEquals(this.expectedTotalCoinValue, testWallet.getTotalDepositedValue(), 0.01);
		
		// now dump all the other values
		LOG.info("Coin name       => {}", testWallet.getCoinName());
		LOG.info("Wallet address  => {}", testWallet.getWalletAddress());
		LOG.info("Balance satoshi => {}", testWallet.getBalanceSatoshi());
		LOG.info("Balance         => {}", testWallet.getBalanceCoin());
		LOG.info("Last value      => {}", testWallet.getLastKnownValue());
		LOG.info("Last date       => {}", testWallet.getLastResultDate());
		LOG.info("Total deposited => {}", testWallet.getTotalDepositedValue());
		
		
		this.cleanUpTestData();
	}
	
	
	
	

}
