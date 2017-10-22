package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCharts {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	public void getValuesByTimestamp() {
		String query = "SELECT timestamp, sum(current_value) as curVal FROM results GROUP BY timestamp ORDER BY timestamp ASC;";
	}

}
