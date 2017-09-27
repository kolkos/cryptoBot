package cryptoBot;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class TestCoin {

	@Test
	public void testCoins() {
		Coin ltc = new Coin();
		ltc.setCoinName("ltc");
		ltc.setWalletAddresses();
		
		List<String> expectedAddresses = new ArrayList<>();
		expectedAddresses.add("LMWPhNFedT8e6X7iRQdh456hvZwYnxyStV");
		
		assertEquals(expectedAddresses, ltc.getWalletAddresses());
		
	}
	
	@Test
	public void getTotalBalanceTest() throws Exception {
		Coin ltc = new Coin();
		ltc.setCoinName("ltc");
		ltc.setWalletAddresses();
		
		ltc.calculateCurrentTotalValuesForCoin();
		
		System.out.println("Satoshi: " + ltc.getTotalBalanceSatoshi());
		System.out.println("Coin: " + ltc.getTotalBalanceCoin());
		System.out.println("Value: " + ltc.getTotalCurrentValue());
		
	}
	
	@Test
	public void getOldValues() throws Exception {
		Coin ltc = new Coin();
		ltc.setCoinName("btc");
		ltc.setWalletAddresses();
		
		ltc.calculatePreviousTotalValuesForCoin(false);
		
		System.out.println("Satoshi: " + ltc.getTotalBalanceSatoshi());
		System.out.println("Coin: " + ltc.getTotalBalanceCoin());
		System.out.println("Value: " + ltc.getTotalCurrentValue());
		
	}

}
