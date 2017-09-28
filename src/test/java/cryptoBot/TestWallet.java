package cryptoBot;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestWallet {

	@Test
	public void testWallet() {
		Wallet testWallet = new Wallet();
		//testWallet.setCoinName("btc");
		testWallet.setCoinName("ltc");
		//testWallet.setWalletAddress("12st4BrVDSG4vJgkeXnPxvrSfnbHbdwGKT");
		testWallet.setWalletAddress("LMWPhNFedT8e6X7iRQdh456hvZwYnxyStV");
		testWallet.setCurrency("eur");
		
		testWallet.getWalletValue();
		
		System.out.println("Satoshi: " + testWallet.getBalanceSatoshi());
		System.out.println("Coins: " + testWallet.getBalanceCoin());
		System.out.println("Value: " + testWallet.getCurrentValue());

		
	}
	
	
	

	@Test
	public void getValuesFromDB() throws Exception {
		Wallet testWallet = new Wallet();
		testWallet.setWalletAddress("12st4BrVDSG4vJgkeXnPxvrSfnbHbdwGKT");
		
		testWallet.getLastKnownValues();
		
		System.out.println("Get last known values from db:");
		System.out.println("Satoshi: " + testWallet.getBalanceSatoshi());
		System.out.println("Coins: " + testWallet.getBalanceCoin());
		System.out.println("Value: " + testWallet.getCurrentValue());
		
		int expectedSatoshi = 2288906;
		double expectedCoins = 0.00734343;
		double expectedValue = 80.34;
		
		assertEquals(expectedSatoshi, testWallet.getBalanceSatoshi());
		assertEquals(expectedCoins, testWallet.getBalanceCoin(), 2);
		assertEquals(expectedValue, testWallet.getCurrentValue(), 2);
		
		testWallet.getFirstKnownValues();
		
		System.out.println("Get first known values from db:");
		System.out.println("Satoshi: " + testWallet.getBalanceSatoshi());
		System.out.println("Coins: " + testWallet.getBalanceCoin());
		System.out.println("Value: " + testWallet.getCurrentValue());
		
		
		
	}
}
