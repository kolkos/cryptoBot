package cryptoBot;

import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private static final Logger LOG = LogManager.getLogger(MySQLAccess.class);
	
	private double getAvgDailyPortfolioValueForDate(Date date) {
		LOG.trace("Entering getAvgDailyPortfolioValueForDate(), date={}", date);
		
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
		
		LOG.trace("Finished getAvgDailyPortfolioValueForDate()");
		return portfolioValue;
	}
	
	private double getDepositedValueForDate(Date date) {
		LOG.trace("Entering getDepositedValueForDate(), date={}", date);
		
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
		
		LOG.trace("Finished getDepositedValueForDate()");
		return depositValue;
	}
	
	private double getPriorDeposits(Date startDate) {
		LOG.trace("Entering getPriorDeposits(), startDate={}", startDate);
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
	
	public String createChartByDay(int numberOfDays) throws IOException {
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
	    	
	    // generate filename
	    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileNameChart = "/tmp/" + timeStamp + "_timechart_" + numberOfDays + "_days";
	    
	    	BitmapEncoder.saveBitmap(chart, fileNameChart, BitmapFormat.PNG);
		
	    	return fileNameChart + ".png";
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
	
	public String generateChartLastXHours(int numberOfHours) throws Exception, IOException {
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
				
				
				
				
			}
			
			
			// add to series
			chart.addSeries(seriesName, xData, yData);
			
		}
		
		// generate filename
	    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileNameChart = "/tmp/" + timeStamp + "_timechart_" + numberOfHours + "_hours";
		
		// now create the chart
		BitmapEncoder.saveBitmap(chart, fileNameChart, BitmapFormat.PNG);
		
			
		return fileNameChart + ".png";
	}
}