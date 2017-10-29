package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestChart {

	@Test
	public void test() {
		Chart chart = new Chart();
		String file = chart.generateTimeChart(30);
		System.out.println(file);
	}
	
	@Test
	public void testChart2() throws Exception {
		Chart2 chart = new Chart2();
		chart.createChartByDay(30);
	}
	
	@Test
	public void dailyGraph() throws Exception {
		Chart2 chart = new Chart2();
		chart.generateChartLastXHours(24);
	}
 
}
