package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestChart {

	@Test
	public void test() {
		Chart chart = new Chart();
		chart.generateTimeChart(30);
	}

}
