package cryptoBot;

import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jfree.data.general.Dataset;
import org.jfree.data.time.Millisecond;
import org.knowm.xchart.*;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class Chart2 {
	public void dummyChart() {
		// create chart
		XYChart chart = new XYChartBuilder().width(800).height(600).title("Waarde portfolio").xAxisTitle("Datum").yAxisTitle("Waarde").build();
		
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, -7);
		
		Calendar end = Calendar.getInstance();
		
		List<Date> xData = new ArrayList<Date>();
		List<Double> yData = new ArrayList<Double>();
		
		
		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			System.out.println(date);
			xData.add(date);
			yData.add(Math.random() * 3);
			
		}
		
		// Series
	    XYSeries series = chart.addSeries("Fake Data", xData, yData);
	    series.setLineColor(XChartSeriesColors.BLUE);
	    series.setMarkerColor(Color.ORANGE);
	    series.setMarker(SeriesMarkers.CIRCLE);
	    series.setLineStyle(SeriesLines.SOLID);
	    
	    try {
			BitmapEncoder.saveBitmap(chart, "./tmp", BitmapFormat.JPG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double getAvgDailyPortfolioValueForDate(Date date) {
		double portfolioValue = 0;
		
		String query = "SELECT SUM(avgWalletValue) AS avgTotalWalletValue FROM " + 
				"(SELECT " + 
				"wallet_id, " + 
				"AVG(current_value) AS avgWalletValue " + 
				"FROM results " + 
				"WHERE DATE(timestamp) = Date(?) " + 
				"GROUP BY wallet_id) x";
		
		
		
		
		Object[] parameters = new Object[] {date};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			while(resultSet.next()) {
				portfolioValue = resultSet.getDouble("avgTotalWalletValue");
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		return portfolioValue;
	}
	
	private double getDepositedValueForDate(Date date) {
		String query = "SELECT SUM(purchase_value) AS totalPurchased FROM deposits WHERE deposit_date = Date(?)";
		Object[] parameters = new Object[] {date};
		MySQLAccess db = new MySQLAccess();
		
		double depositValue = 0;
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			while(resultSet.next()) {
				depositValue = resultSet.getDouble("totalPurchased");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		
		return depositValue;
	}
	
	private double getPriorDeposits(Date startDate) {
		double totalDeposited = 0;
		
		String query = "SELECT SUM(purchase_value) AS priorDepositsValue FROM deposits WHERE deposit_date < Date(?)";
		Object[] parameters = new Object[] {startDate};
		
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			while(resultSet.next()) {
				totalDeposited = resultSet.getDouble("priorDepositsValue");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		return totalDeposited;
	}
	
	public void createChartByDay(int numberOfDays) {
		// create chart
		XYChart chart = new XYChartBuilder().width(1000).height(600).title("Waarde portfolio").xAxisTitle("Datum").yAxisTitle("Waarde").theme(ChartTheme.GGPlot2).build();
		
		// format chart
		chart.getStyler().setDatePattern("dd-MM-yyyy");
		chart.getStyler().setXAxisLabelRotation(45);
		chart.getStyler().setLegendPosition(LegendPosition.OutsideS);
		
		
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, -numberOfDays);
		
		Calendar end = Calendar.getInstance();
		
		List<Date> xData = new ArrayList<Date>();
		List<Double> yDataPortfolioValue = new ArrayList<Double>();
		List<Double> yDataDepositValue = new ArrayList<Double>();
		
		double totalDeposited = this.getPriorDeposits(start.getTime());
		
		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			System.out.println(date);
			
			double portfolioValue = this.getAvgDailyPortfolioValueForDate(date);
			double depositValue = this.getDepositedValueForDate(date);
		    totalDeposited += depositValue;
			
		    
		    
			xData.add(date);
			yDataPortfolioValue.add(portfolioValue);
			yDataDepositValue.add(totalDeposited);
			
		}
		
		// Series - deposit value
	    XYSeries seriesDeposit = chart.addSeries("Inleg", xData, yDataDepositValue);
	    seriesDeposit.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
	    seriesDeposit.setMarker(SeriesMarkers.NONE);
	    seriesDeposit.setFillColor(new Color(255, 189, 100));
	    seriesDeposit.setLineColor(new Color(255, 146, 0));
	    
		
		// Series - portfolio value
	    XYSeries series = chart.addSeries("Waarde", xData, yDataPortfolioValue);
	    series.setLineColor(XChartSeriesColors.BLUE);
	    series.setMarkerColor(Color.BLUE);
	    series.setMarker(SeriesMarkers.DIAMOND);
	    series.setLineStyle(SeriesLines.SOLID);
	    	    
	    try {
	    		BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double getWalletValueForTimestamp(Date start, Date end, int walletID) {
		double avgValue = 0;
		
		
		String query = "SELECT AVG(current_value) AS avgValue " + 
				"FROM results " + 
				"WHERE wallet_id = ? " + 
				"AND(" + 
				"    timestamp BETWEEN ? AND ? " + 
				")";
		Object[] parameters = new Object[] {walletID, start, end};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			ResultSet resultSet = db.getResultSet();
			
			while(resultSet.next()) {
				avgValue = resultSet.getDouble("avgValue");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return avgValue;
	}
	
	public void generateChartLastXHours(int numberOfHours) throws Exception {
		// create chart
		XYChart chart = new XYChartBuilder().width(1000).height(600).title("Waarde per wallet").xAxisTitle("Tijdstip").yAxisTitle("Waarde").theme(ChartTheme.GGPlot2).build();
		
		// format chart
		chart.getStyler().setDatePattern("HH:mm:ss");
		chart.getStyler().setXAxisLabelRotation(45);
		chart.getStyler().setLegendPosition(LegendPosition.OutsideS);
		
		String query = "SELECT wallets.id AS walletID, wallets.address AS walletAddress, coins.name AS coinName " + 
				"FROM wallets, coins, results " + 
				"WHERE wallets.coin_id = coins.id " + 
				"AND results.wallet_id = wallets.id " + 
				"GROUP BY walletID";
		Object[] parameters = new Object[] {};
		MySQLAccess db = new MySQLAccess();
				
		db.executeSelectQuery(query, parameters);
		ResultSet resultSet = db.getResultSet();
		
		
		// loop through the wallets
		while(resultSet.next()) {
			// create a new y series
			List<Double> yData = new ArrayList<Double>();
			
			String seriesName = String.format("%s (%s)", resultSet.getString("coinName").toUpperCase(), resultSet.getString("walletAddress"));
			int walletID = resultSet.getInt("walletID");
			
			System.out.println(seriesName);
			
			Calendar start = Calendar.getInstance();
			start.add(Calendar.HOUR_OF_DAY, -numberOfHours);
			start.set(Calendar.MINUTE, 0);
			start.set(Calendar.SECOND, 0);
			
			Calendar end = Calendar.getInstance();
			//end.add(Calendar.HOUR, 1);
			end.set(Calendar.MINUTE, 0);
			end.set(Calendar.SECOND, 0);
			
			// check if the date exists, if not add it
			List<Date> xData = new ArrayList<Date>();
			
			double lastKnowValue = 0;
			
			for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.HOUR, 1), date = start.getTime()) {
				
				xData.add(date);
				
				
				Calendar lastMinute = Calendar.getInstance();
				lastMinute.setTime(date);
				lastMinute.set(Calendar.MINUTE, 59);
				lastMinute.set(Calendar.SECOND, 59);
				
				System.out.println("Zoeken naar waarden tussen " + date + " en " + lastMinute.getTime());
				
				// add value to xData
				double avgValue = this.getWalletValueForTimestamp(date, lastMinute.getTime(), walletID);
				
				// check if the value is zero
				if(avgValue == 0) {
					// set the value to the last known value
					avgValue = lastKnowValue;
				}else {
					// set the last known value
					lastKnowValue = avgValue;
				}
				
				yData.add(avgValue);
				
				System.out.println(avgValue);
				
				
				
			}
			
			System.out.println("xData: " + xData.size());
			System.out.println("yData: " + yData.size());
			
			// add to series
			chart.addSeries(seriesName, xData, yData);
			
		}
		// now create the chart
		try {
    			BitmapEncoder.saveBitmap(chart, "./Sample_Chart2", BitmapFormat.PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
}