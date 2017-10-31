package cryptoBot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestChart {
	
	@Test
	public void testChart2() throws Exception {
		Chart2 chart = new Chart2();
		String file = chart.createChartByDay(30);
		System.out.println(file);
	}
	
	@Test
	public void dailyGraph() throws Exception {
		Chart2 chart = new Chart2();
		String file = chart.generateChartLastXHours(24);
		System.out.println(file);
	}
 
}
