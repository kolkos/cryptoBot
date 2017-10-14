package cryptoBot;

import static org.junit.Assert.*;

import java.text.*;
import java.util.*;
import java.util.regex.*;


import org.junit.Test;

public class TestDeposit {

	@Test
	public void testParsingDeposit() {
		String sampleTelegramChatMessage = "/deposit 04-10-2017 45,00 Bitstamp Opmerking hier";
		
		DepositJunk testDeposit = new DepositJunk();
		
		// set the message
		testDeposit.setTelegramChatMessage(sampleTelegramChatMessage);
		
		// run the parsing command
		testDeposit.pasrseDeposit();
		
		// test the values
		String expectedCommand = "/deposit";
				
		try {
			Date expectedDate = new SimpleDateFormat( "yyyyMMdd" ).parse( "20171004" );
			assertEquals(expectedDate, testDeposit.getDepositDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Double expectedDepositValue = 45.00;
		String expectedDestination = "Bitstamp";
		String expectedRemarks = "Opmerking hier";
		
		assertEquals(expectedCommand, testDeposit.getCommand());
		assertEquals(expectedDepositValue, testDeposit.getDepositValue(),0.1);
		assertEquals(expectedDestination, testDeposit.getDestination());
		assertEquals(expectedRemarks, testDeposit.getRemarks());
		
		
		
	}
	
	@Test
	public void testRegisteringDeposit() throws Exception {
		// chatID: -236099150
		long chatID = -236099150;
		String firstName = "JUnit";
		String sampleTelegramChatMessage = "/deposit 04-10-2017 45,00 Bitstamp Opmerking hier";
		
		DepositJunk testDeposit = new DepositJunk();
		testDeposit.registerDeposit(chatID, firstName, sampleTelegramChatMessage);
		
	}

}
