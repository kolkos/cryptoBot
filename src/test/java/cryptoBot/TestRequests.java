package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRequests {

	@Test
	public void testRequest() {
		Request request = new Request();
		request.setRequestedBy("JUnit");
		request.setRequestedCoins("all");
		//request.setCalculateSince("begin");
		request.handleRequest();
		
		System.out.println(request.getStatusMessage());
		
	}

}
