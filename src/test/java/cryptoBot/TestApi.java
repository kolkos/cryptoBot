package cryptoBot;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

public class TestApi {

	@Test
	public void testApi() {
		ApiRequest apiRequest = new ApiRequest();
		String coinName = "btc";
		String currency = "eur";
		String walletAddress = "38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L";
		
		// fist check the coin value request
		try {
			JSONObject json = apiRequest.currentCoinValueApiRequest(coinName, currency);
			
			// check if reply element last is not null
			// last is currently the only value I use
			assertEquals(false, json.isNull("last"));
			
			//json.isNull("last");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		// now check the wallet api request
		
		try {
			JSONObject json = apiRequest.walletInfoApiRequest(coinName, walletAddress);
			
			// now check the used values for this wallet request
			// final_balance is the only value I use
			assertEquals(false, json.isNull("final_balance"));
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
	
	}

}
