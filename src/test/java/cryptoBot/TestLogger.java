package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;


public class TestLogger {


	@Test
	public void test() {
		General general = new General();
		for(int i = 0; i < 2; i++) {
			general.testLogje();
		}
		
	}

}
