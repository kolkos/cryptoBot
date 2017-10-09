package cryptoBot;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class TestCoin {

	@Test
	public void testSingleCoin() throws Exception {
		String coinName = "tst";
		
		Coin coin = new Coin();
						
		// now get the address
		String expectedWalletAddress = "1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX";
		List<String> expectedWalletAddresses = new ArrayList<>();
		expectedWalletAddresses.add(expectedWalletAddress);
		
		assertEquals(expectedWalletAddresses, coin.getWalletAddresses(coinName));
		assertEquals(coinName, coin.getCoinName());
		
		
	}
	
	@Test
	public void testCoinWallets() {
		String coinName = "ltc";
		Coin coin = new Coin();
		
		// get the wallets for the coin
		coin.getWalletsForCoin(coinName);
		
		List<Wallet> wallets = coin.getWallets();
		
		// loop through the results
		for(Wallet wallet : wallets) {
			System.out.println(String.format("Coin: %s", wallet.getCoinName()));
			System.out.println(String.format("Address: %s", wallet.getWalletAddress()));
			System.out.println(String.format("Balance: %f", wallet.getBalanceCoin()));
			System.out.println(String.format("Value: %.2f", wallet.getCurrentValue()));
		}
		
		System.out.println(String.format("Total value: %.2f", coin.getTotalCoinValue()));
		
		
	}
	


}


