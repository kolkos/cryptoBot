package cryptoBot;

import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Portfolio {
	private List<String> coinList;
	private List<Coin> coins;
	private double totalCurrentValuePortfolio = 0;
	private double totalPreviousValuePortfolio = 0;
	private double totalDepositedValue = 0;
	
	private int requestID = 0;
	
	private static final Logger LOG = LogManager.getLogger(Portfolio.class);
	
	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public double getTotalDepositedValue() {
		return totalDepositedValue;
	}

	public void setTotalDepositedValue(double totalDepositedValue) {
		this.totalDepositedValue = totalDepositedValue;
	}

	public List<Coin> getCoins() {
		return this.coins;
	}

	public List<String> getCoinList() {
		return this.coinList;
	}
	
	public void setCoinList() {
		this.receiveCoinsInPortfolio();
	}
	
	public double getTotalCurrentValuePortfolio() {
		return this.totalCurrentValuePortfolio;
	}

	public double getTotalPreviousValuePortfolio() {
		return totalPreviousValuePortfolio;
	}


	/**
	 * Getting the coins registered in the portfolio. This method will get all used coins with a wallet attached.
	 */
	private void receiveCoinsInPortfolio() {
		LOG.trace("Entering receiveCoinsInPortfolio()");
		
		String query = "SELECT" + 
				" coins.name as coinName" + 
				" FROM coins, wallets" + 
				" WHERE wallets.coin_id = coins.id "
				+ "AND coins.name != 'tst'";
		Object[] parameters = new Object[] {};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			
			coinList = new ArrayList<>();
			while (resultSet.next()) {
				coinList.add(resultSet.getString("coinName"));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("Error receiving coins from database: {}", e);
		}finally {
			db.close();
		}
		LOG.trace("Finished receiveCoinsInPortfolio()");
	}
	
	/**
	 * This method gets a single coin from the portfolio
	 * @param coinName the name of the coin
	 */
	public void getCoinInPortfolio(String coinName) {
		LOG.trace("Entering getCoinInPortfolio(), coinName={}", coinName);
		
		this.coins = new ArrayList<>();
		Coin coin = new Coin();
		coin.setRequestID(this.requestID);
		coin.getWalletsForCoin(coinName);
		
		// add the total coin value to the portfolio value
		this.totalCurrentValuePortfolio += coin.getTotalCurrentCoinValue();
		
		// add the previous known value
		this.totalPreviousValuePortfolio += coin.getTotalLastKnownCoinValue();
		
		// add to the list
		this.coins.add(coin);
		
		LOG.trace("Finished getCoinInPortfolio()");
	}
	
	/**
	 * This method gets all the coins in the portfolio
	 */
	public void getAllCoinsInPortfolio() {
		LOG.trace("Entering getAllCoinsInPortfolio()");
		
		this.coins = new ArrayList<>();
		
		// reuse the receiveCoinsInPortfolio method
		this.receiveCoinsInPortfolio();
		for(String coinName : coinList) {
			Coin coin = new Coin();
			coin.setRequestID(this.requestID);
			coin.getWalletsForCoin(coinName);
			
			// calculate the total value
			this.totalCurrentValuePortfolio += coin.getTotalCurrentCoinValue();
			
			// add the previous known value
			this.totalPreviousValuePortfolio += coin.getTotalLastKnownCoinValue();
			
			// add the total deposited value
			this.totalDepositedValue += coin.getTotalDepositedCoinValue();
			
			// add to the list
			this.coins.add(coin);
		}
		LOG.trace("Finished getAllCoinsInPortfolio()");
	}
	
	
	
	public String generatePortfolioStatusMessage(String firstName) {
		LOG.trace("Entering generatePortfolioStatusMessage()");
		
		// first get all the coins 
		this.getAllCoinsInPortfolio();
		
		// now start creating the message
		String messageText;
		messageText =  String.format("Hoi %s,\n\n", firstName);
		messageText += "Op dit moment ziet de totale waarde van het portfolio er als volgt uit:\n";
		
		General general = new General();
		
		// loop through the coins
		for(Coin coin : this.coins) {
			String coinName = coin.getCoinName();
			double balance = coin.getTotalCoinBalance();
			double value = coin.getTotalCurrentCoinValue();
			
			// convert the balance to a formatted string
			String balanceFormatted = general.getDutchNumberFormat(balance, "", "", false, 8);
			
			// convert the value to a formatted string
			String formattedValue = general.getDutchNumberFormat(value, "€ ", "", false, 2);
			
			// add it to the message
			messageText += String.format("%s: `%s` (`%s`)\n", coinName, balanceFormatted, formattedValue);
		}
		
		// now calculate the difference to the deposits
		double totalValue = this.getTotalCurrentValuePortfolio();
		double depositedValue = this.getTotalDepositedValue();
		double differenceDepositCurrent = totalValue - depositedValue;
		
		// calculate the ROI
		double roi = (100 * differenceDepositCurrent) / depositedValue;
		
		// convert the values to a formatted string
		String totalValueFormatted = general.getDutchNumberFormat(totalValue, "€ ", "", false, 2);
		String depositedValueFormatted = general.getDutchNumberFormat(depositedValue, "€ ", "", false, 2);
		String differenceDepositCurrentFormatted = general.getDutchNumberFormat(differenceDepositCurrent, "€ ", "", true, 2);
		String roiFormatted = general.getDutchNumberFormat(roi, "", "%", false, 1);
		
		// finally append it to the message
		messageText += String.format("Totale waarde: `%s` (`%s`)\n", totalValueFormatted, differenceDepositCurrentFormatted);
		messageText += String.format("Ingelegd: `%s`\n", depositedValueFormatted );		
		messageText += String.format("Rendement: `%s`\n", roiFormatted );
		
		
		
		
		
		LOG.trace("Finished generatePortfolioStatusMessage()");
		return messageText;
	}
	
}
