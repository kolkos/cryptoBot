package cryptoBot;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class TestGeneral {

	@Test
	public void test() {
		General general = new General();
		Properties prop = general.getProperties();
		System.out.println(prop.getProperty("bot_token"));
		
		assertEquals("Waarschijnlijk heb je de testbot token gebruikt!", "429491716:AAHJIRsPvRkRzpYRIdznxZEXgIJtYZm77M0", prop.getProperty("bot_token"));
		assertEquals("Waarschijnlijk heb je de testbot username gebruikt!", "geenGezeikIedereenRijkBot", prop.getProperty("bot_username"));
		
	}
	
	@Test
	public void testFormattingNumbers() {
		double number1 = 1111.11111111;
		double number2 = 222.222222222;
		double number3 = 33.3333333333;
		double number4 = 4.44444444444;
		double number5 = 999999999.999;
		
		General general = new General();
//		
//		System.out.println(portfolio.getDutchNumberFormat(number1, "€ ", "", true, 2));
//		System.out.println(portfolio.getDutchNumberFormat(number2, "€ ", "", false, 2));
//		System.out.println(portfolio.getDutchNumberFormat(number3, "", "%", false, 1));
//		System.out.println(portfolio.getDutchNumberFormat(number4, "", "", false, 8));
//		System.out.println(portfolio.getDutchNumberFormat(number5, "€ ", "", false, 0));
//		
		assertEquals("+€ 1.111,11",     general.getDutchNumberFormat(number1, "€ ", "",  true,  2));
		assertEquals("€ 222,22",        general.getDutchNumberFormat(number2, "€ ", "",  false, 2));
		assertEquals("33,3%",           general.getDutchNumberFormat(number3, "",   "%", false, 1));
		assertEquals("4,44444444",      general.getDutchNumberFormat(number4, "",   "" , false, 8));
		assertEquals("€ 1.000.000.000", general.getDutchNumberFormat(number5, "€ ", "",  false, 0));

	}
	

}
