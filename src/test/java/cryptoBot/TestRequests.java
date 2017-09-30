package cryptoBot;

import static org.junit.Assert.*;

import java.sql.ResultSet;

import org.junit.Test;

public class TestRequests {

	@Test
	public void testRequest() {
		Request request = new Request();
		request.setRequestedBy("JUnit");
		request.setRequestedCoins("ltc");
		//request.setCalculateSince("begin");
		request.handleCoinRequest();
		
		System.out.println(request.getStatusMessage());
		
	}
	
	@Test
	public void runWalletRequest() throws Exception {
		Request request = new Request();
		request.setRequestedBy("JUnit");
		
		request.handleWalletRequest();
		
		System.out.println(request.getStatusMessage());
	}
	
	

}
