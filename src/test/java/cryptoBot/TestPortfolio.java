package cryptoBot;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class TestPortfolio {

	@Test
	public void testPortfolio() {
		List<String> expectedCoins = new ArrayList<>();
		expectedCoins.add("btc");
		expectedCoins.add("ltc");
		//expectedCoins.add("tst");
		
		Portfolio portfolio = new Portfolio();
		
		// get all the coin addresses
		portfolio.setCoinList();
		
		assertEquals(expectedCoins, portfolio.getCoinList());
		
	}
	
	@Test
	public void testSingleCoinInPortfolio() {
		String coinName = "btc";
		Portfolio portfolio = new Portfolio();
		portfolio.getCoinInPortfolio(coinName);
		
		System.out.println("Portfolio:");
		System.out.println(String.format("Portfolio value: %.2f", portfolio.getTotalCurrentValuePortfolio()));
		System.out.println(String.format("Last portfolio value: %.2f", portfolio.getTotalPreviousValuePortfolio()));
		
		// now loop through the coins
		for(Coin coin : portfolio.getCoins()) {
			System.out.println(String.format("  Coin: %s", coin.getCoinName()));
			System.out.println(String.format("  Total value: %.2f", coin.getTotalCurrentCoinValue()));
			System.out.println(String.format("  Last value: %.2f", coin.getTotalLastKnownCoinValue()));
			System.out.println("  Wallets:");
			// loop trough the wallets
			for(Wallet wallet : coin.getWallets()) {
				System.out.println(String.format("    Coin: %s", wallet.getCoinName()));
				System.out.println(String.format("    Address: %s", wallet.getWalletAddress()));
				System.out.println(String.format("    Balance: %f", wallet.getBalanceCoin()));
				System.out.println(String.format("    Value: %.2f", wallet.getCurrentValue()));
				System.out.println(String.format("    Last value: %.2f", wallet.getLastKnownValue()));
			}
			
		}
	}
	
	@Test
	public void testAllCoins() {
		Portfolio portfolio = new Portfolio();
		portfolio.getAllCoinsInPortfolio();
		
		System.out.println("Portfolio:");
		System.out.println(String.format("Portfolio value: %.2f", portfolio.getTotalCurrentValuePortfolio()));
		System.out.println(String.format("Last portfolio value: %.2f", portfolio.getTotalPreviousValuePortfolio()));
		
		// now loop through the coins
		for(Coin coin : portfolio.getCoins()) {
			System.out.println(String.format("  Coin: %s", coin.getCoinName()));
			System.out.println(String.format("  Total value: %.2f", coin.getTotalCurrentCoinValue()));
			System.out.println(String.format("  Last value: %.2f", coin.getTotalLastKnownCoinValue()));
			System.out.println("  Wallets:");
			// loop trough the wallets
			for(Wallet wallet : coin.getWallets()) {
				System.out.println(String.format("    Coin: %s", wallet.getCoinName()));
				System.out.println(String.format("    Address: %s", wallet.getWalletAddress()));
				System.out.println(String.format("    Balance: %f", wallet.getBalanceCoin()));
				System.out.println(String.format("    Value: %.2f", wallet.getCurrentValue()));
				System.out.println(String.format("    Last value: %.2f", wallet.getLastKnownValue()));
			}
			
		}
	}
	

}
