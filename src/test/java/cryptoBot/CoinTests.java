package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class CoinTests {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testingLTC(){
		String coin = "ltc";
		Coins litecoin = new Coins(coin);
		
		// check if the coin is set correctly
		assertEquals(coin, litecoin.getCoin());
		
		// check if the URL for getting the balance is set correctly
		String url = "https://www.bitstamp.net/api/v2/ticker_hour/ltceur/";
		assertEquals(url, litecoin.getCurrentValueURL());
		
		// update/get the current balance
		litecoin.getCurrentWalletBalance();
		
		assertEquals(53065134, litecoin.getBalanceInSatoshi());
		
	}

}
