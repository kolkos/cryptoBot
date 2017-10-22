package cryptoBot;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class Chart {
	private XYPlot plot;
	private int datasetIndex = 0;
	private int numberOfDays;
	
	private static final Logger LOG = LogManager.getLogger(ApiRequest.class);
	
	/**
	 * Method to generate a JPEG chart for the deposit/portfolio value
	 * @param numberOfDays the number of days in the past
	 */
	public String generateTimeChart(int numberOfDays) {
		LOG.trace("Entering generateTimeChart(), numberOfDays={}", numberOfDays);
		
		this.numberOfDays = numberOfDays;
		
		final XYDataset datasetValue = getPortfolioValuesLastXDays();
		final XYDataset datasetDeposit = depositsLastXDays();
		
		// create the chart
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
	            "Waarde portfolio afgelopen " + this.numberOfDays + " dagen", 
	            "Datum", 
	            "Waarde", 
	            datasetValue, 
	            true, 
	            false, 
	            false);
		
		chart.setBackgroundPaint(Color.white);
		
		// now create the plot
		this.plot = chart.getXYPlot();
        this.plot.setBackgroundPaint(Color.lightGray);
        this.plot.setDomainGridlinePaint(Color.white);
        this.plot.setRangeGridlinePaint(Color.white);
        
        final ValueAxis axis = this.plot.getDomainAxis();
        axis.setAutoRange(true);
        
        // add the second dataset
        this.datasetIndex++;
        this.plot.setDataset(this.datasetIndex, datasetDeposit);
        this.plot.setRenderer(this.datasetIndex, new StandardXYItemRenderer());
		
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileNameChart = "/tmp/" + timeStamp + "_timechart_" + this.numberOfDays + "_days.jpeg";
        
        
        int width = 750;   /* Width of the image */
	    int height = 400;  /* Height of the image */ 
	    File timeChart = new File( fileNameChart );
	    try {
			ChartUtilities.saveChartAsJPEG( timeChart, chart, width, height );
		} catch (IOException e) {
			LOG.fatal("Error generating jpeg chart, {}", e);
		}
        
	    return fileNameChart;
	}
	
	private XYDataset getPortfolioValuesLastXDays() {
		TimeSeries series = new TimeSeries("Waarde portfolio");
		
		Calendar end = Calendar.getInstance();
		//end.add(Calendar.DATE, 1);
		//end.set(Calendar.HOUR_OF_DAY, 0);
		//end.set(Calendar.MINUTE, 0);
		//end.set(Calendar.SECOND, 0);
		//end.set(Calendar.MILLISECOND, 0);
		
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, -this.numberOfDays);
		//start.set(Calendar.HOUR_OF_DAY, 0);
		//start.set(Calendar.MINUTE, 0);
		//start.set(Calendar.SECOND, 0);
		//start.set(Calendar.MILLISECOND, 0);
		
		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			double portfolioValue = this.getAvgDailyPortfolioValueForDate(date);
			
			series.add(new Millisecond(date), portfolioValue);
		}
		
		return new TimeSeriesCollection(series);
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
	
	public XYDataset depositsLastXDays(){
		Calendar end = Calendar.getInstance();
		//end.add(Calendar.DATE, 1);
		//end.set(Calendar.HOUR_OF_DAY, 0);
		//end.set(Calendar.MINUTE, 0);
		//end.set(Calendar.SECOND, 0);
		//end.set(Calendar.MILLISECOND, 0);
		
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, -this.numberOfDays);
		//start.set(Calendar.HOUR_OF_DAY, 0);
		//start.set(Calendar.MINUTE, 0);
		//start.set(Calendar.SECOND, 0);
		//start.set(Calendar.MILLISECOND, 0);
		
		double totalDeposited = this.getPriorDeposits(start.getTime());
		
		TimeSeries series = new TimeSeries("Ingelegd");
		
		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			double depositValue = this.getDepositedValueForDate(date);
		    totalDeposited += depositValue;
		    series.add(new Millisecond(date), totalDeposited);
		}
		
		return new TimeSeriesCollection(series);
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
	
	
}
