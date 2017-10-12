package cryptoBot;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestWallet {

	@Test
	public void testWallet() {
		Wallet testWallet = new Wallet();
		String walletAddress = "1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX";
		testWallet.setWalletAddress(walletAddress);
		testWallet.getCoinNameForWallet();
		
		String expectedCoinName = "tst";
		
		assertEquals(expectedCoinName, testWallet.getCoinName());
		

		
	}
	

}
