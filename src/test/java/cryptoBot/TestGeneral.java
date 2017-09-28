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
	}

}
