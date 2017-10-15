package cryptoBot;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Methods to handle the callback queries
 * @author Anton van der Kolk
 *
 */
public class CallbackQueryHandler extends CommandHandler {
	
	// attributes specific for Callback Queries
	private long messageID;
	
	private static final Logger LOG = LogManager.getLogger(CallbackQueryHandler.class);
	
	
	/**
	 * Get the registered messageID (used for callback queries)
	 * @return the ID from the message which contains the inline keyboard
	 */
	public long getMessageID() {
		return messageID;
	}
	
	/**
	 * Register the message ID
	 * @param messageID the ID of the message which contains the inline query
	 */
	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}
	
	/*
	 * ==========================================================================================
	 * General methods
	 * ==========================================================================================
	 */
	
	/**
	 * Shortcut to register the incoming callback query. This method is used to force registering the required variables
	 * @param chatIDTelegram chatID the ID of the callback query where the message is send from
	 * @param messageID the ID of the message containing the inline query
	 * @param firstName the name of the user which has send the callback query (clicked on the button)
	 * @param incomingCallbackQuery the value of the inline keyboard button
	 */
	public void registerCallbackQuery(long chatIDTelegram, long messageID, String firstName, String incomingCallbackQuery) {
		LOG.trace("entered registerCallbackQuery(): chat_id={}, messageID={}, firstName={}, incomingCallbackQuery={}", chatIDTelegram, messageID, firstName, incomingCallbackQuery);
		this.setChatIDTelegram(chatIDTelegram);
		this.setMessageID(messageID);
		this.setFirstName(firstName);
		this.setIncomingMessage(incomingCallbackQuery);
		LOG.trace("finished registerCallbackQuery()");
	}
	
	/**
	 * This method generates a simple edit message from a String
	 * @param messageText the text to send (to edit the existing message)
	 * @return prepared EditMessage
	 */
	private EditMessageText generateSimpleEditMessageText(String messageText) {
		LOG.trace("entered generateSimpleEditMessageText(), messageText={}", messageText);
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.getChatIDTelegram())
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		LOG.trace("finished generateSimpleEditMessageText()");
		
		return message;
	}
	
	/**
	 * Handle the prepared EditMessageText
	 * @param message the prepared EditMessageText
	 */
	private void sendEditMessageText(EditMessageText message) {
		LOG.trace("entered sendEditMessageText()");
		try {
			editMessageText(message);
		} catch (TelegramApiException e) {
			LOG.fatal("error editing chat message {}", e);
		}
		LOG.trace("finished sendEditMessageText()");
	}
	
	/**
	 * This method parses the incoming callback query to a HashMap. This makes it easier to get parameters from the requested methods
	 * @return a HashMap which contains key/value pairs from the incoming callback query
	 */
	private HashMap<String, String> parseCallData(){
		LOG.trace("entered parseCallData()");
		LOG.trace("callData={}", this.getIncomingMessage());
		HashMap<String, String> parameters = new HashMap<>();
		String[] keyValuePairs = this.getIncomingMessage().split(",");
		for(String keyValuePair : keyValuePairs) {
			String[] keyValueParts = keyValuePair.split("=");
			String key = keyValueParts[0];
			String value = keyValueParts[1];
			parameters.put(key, value);
			LOG.trace("added {} => {}", key, value);
		}
		LOG.trace("finished parseCallData()");
		return parameters;
	}
	
	/**
	 * This method checks if all the necessary parameters are set. This method is called from the methods which handle
	 * the incoming commands. 
	 * @param requiredKeys the keys which are expected to be in the HashMap
	 * @param callDataDetails the HashMap (from parseCallData()) containing the parameters
	 * @return
	 */
	private boolean checkCallDataComplete(List<String> requiredKeys, HashMap<String, String> callDataDetails) {
		LOG.trace("entered checkCallDataComplete(): requiredKeys={}, callDataDetails={}", requiredKeys, callDataDetails);
		
		// by default the check is OK
		boolean checkOK = true;
		
		// loop through the required keys
		for(String requiredKey: requiredKeys) {
			// if a key isn't found, the check is set to false
			if(! callDataDetails.containsKey(requiredKey)) {
				checkOK = false;
			}
		}
		LOG.info("call data check ok: {}", checkOK);
		LOG.trace("finished checkCallDataComplete()");
		
		return checkOK;
	}
	
	/**
	 * This method handles the incoming callback queries. It will first parse the incoming callback query to a HashMap. 
	 * Based on the HashMap value of 'method' it will run the required method. Each method will return a EditMessage to this
	 * method. This method then returns this EditMessage.
	 * @return
	 */
	public void runCallbackQueryCommand() {
		LOG.trace("entered runCallbackQueryCommand()");
		HashMap<String, String> callDataDetails = this.parseCallData();
		
		// message is the object which is returned to the calling method
		EditMessageText message = null;
		
		// check if method is set
		if(! callDataDetails.containsKey("method")) {
			message = this.generateSimpleEditMessageText("Method niet gevonden in callback query");
			LOG.warn("method not found in callback data!");
			
			message = this.generateSimpleEditMessageText("Het commando van deze knop is helaas niet compleet. Kun jij niets aan doen, maar ik kan er nu dus niets mee. #sad");
			this.sendEditMessageText(message);
			// exit the method
			return;
		}
		
		// create a list object for the required keys
		List<String> requiredKeys = new ArrayList<>();
		
		// now get the method
		String method = callDataDetails.get("method");
		LOG.info("found method {} in callback query", method);
		
		// call the correct method depending on the given method
		switch(method) {
			case "getPortfolioCoins":
				message = this.getPortfolioCoins();
				break;
			case "getHelpText":
				message = this.getHelpTextEdit();
				break;
			case "getWalletsForCoin":
				requiredKeys.add("coinName");
				message = this.getWalletsForCoin(requiredKeys, callDataDetails);
				break;
			case "getTotalPortfolioValue":
				message = this.getTotalPortfolioValue();
				break;
			case "getWalletValue":
				requiredKeys.add("coin");
				requiredKeys.add("walletID");
				message = this.getWalletValue(requiredKeys, callDataDetails);
				break;
			default:
				// if the method is not declared (yet) generate a error message
				message = this.generateSimpleEditMessageText("Ik kan (nog) niets met dit commando!");
				
		}
		
		LOG.trace("generated EditTextMessage: {}", message);
		
		// now edit the message with the new prepared message
		this.sendEditMessageText(message);
		
		LOG.trace("finished runCallbackQueryCommand()");
		
	}
	
	/*
	 * ==========================================================================================
	 * Command specific methods
	 * ==========================================================================================
	 */
	
	/**
	 * This method creates a message containing the registered coins (with at least one wallet attached)
	 * Next to the found coins, it will contain a option to request all the coins
	 * @return status message containing the found coins and the 'all' buttons
	 */
	private EditMessageText getPortfolioCoins() {
		LOG.trace("entered getPortfolioCoins()");
		
		String messageText = "Het portfolio bestaat uit de volgende coins. Maak een keuze.";
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.getChatIDTelegram())
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		// get the coins in the portfolio
		Portfolio portfolio = new Portfolio();
		portfolio.setCoinList();
		List<String> coins = portfolio.getCoinList();
		
		LOG.info("found the following coins: {}", coins);
		
		// loop through the coins to generate the menu
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		for(String coin : coins) {
			
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			String commandString = String.format("method=getWalletsForCoin,coinName=%s", coin);
			
			rowInline.add(new InlineKeyboardButton().setText(coin.toUpperCase())
					.setCallbackData(commandString));
			rowsInline.add(rowInline);
			
		}
		
		// now add the 'all' option
		
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		String commandString = String.format("method=getWalletsForCoin,coinName=%s", "all");
		
		rowInline.add(new InlineKeyboardButton().setText("Alles")
				.setCallbackData(commandString));
		rowsInline.add(rowInline);
		
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		
		LOG.trace("finished getPortfolioCoins()");
		
		return message;
	}
	
	/**
	 * Method to get all the wallets for all the coins
	 * @return list containing all the wallets
	 */
	private List<String> getAllWalletAddresses(){
		List<String> walletList = new ArrayList<>();
		
		// get all the coins in the portfolio
		Portfolio portfolio = new Portfolio();
		portfolio.setCoinList();
		List<String> coins = portfolio.getCoinList();
		
		// now loop the coins
		for(String coinName : coins) {
			// now get the wallets for this coin, 
			Coin coin = new Coin();
			List<String> walletAddresses = coin.getWalletAddresses(coinName);
			// now loop through the found wallet addresses
			for(String walletAddress : walletAddresses) {
				String walletLine = String.format("%s,%s", walletAddress, coinName);
				// add this line to the list
				walletList.add(walletLine);
			}
		}
		
		return walletList;
	}
	
	/**
	 * This method generates a message containing the wallet addresses as buttons. 
	 * @param requiredKeys keys required to successfully handle this request
	 * @param callDataDetails the callback data
	 * @return
	 */
	private EditMessageText getWalletsForCoin(List<String> requiredKeys, HashMap<String, String> callDataDetails) {
		LOG.trace("entered getWalletsForCoin()");
		
		// check if the call is complete
		if(! this.checkCallDataComplete(requiredKeys, callDataDetails)) {
			// command is not complete, exit the method with an error text
			EditMessageText message = this.generateSimpleEditMessageText("Sorry het commando van deze knop is niet compleet. #sorrynog");
			LOG.warn("Not all required keys are found for getWalletsForCoin(). Exiting");
			return message;
		}
		
		// first create the list
		// this list will be used to form the buttons
		List<String> walletList = new ArrayList<>();
		
		// if coin is all, get all wallets for all coins
		if(callDataDetails.get("coinName").equals("all")) {
			// get all the coins in the portfolio
			walletList = this.getAllWalletAddresses();
			
		}else {
			// a specific coin has been chosen, get the addresses for this coin
			Coin coin = new Coin();
			List<String> walletAddresses = coin.getWalletAddresses(callDataDetails.get("coinName"));
			for(String walletAddress : walletAddresses) {
				String walletLine = String.format("%s,%s", walletAddress, callDataDetails.get("coinName"));
				// add this line to the list
				walletList.add(walletLine);
			}
		}
		
		// now generate the message
		String messageText = "Ik heb de volgende wallets gevonden. Maak een keuze";
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.getChatIDTelegram())
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		
		// loop through the wallets list
		for(String walletAddressString : walletList) {
			// split the walletAddress string
			String walletAddress = walletAddressString.split(",")[0];
			String coinName = walletAddressString.split(",")[1];
			
			Wallet wallet = new Wallet();
			int walletID = wallet.getWalletIDFromDB(walletAddress);
			
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			
			String buttonText = String.format("%s (%s)", this.shortenString(walletAddress, 15), coinName.toUpperCase());
			String commandString = String.format("method=getWalletValue,coin=%s,walletID=%d", callDataDetails.get("coinName"), walletID);
			
			LOG.info(buttonText);
			LOG.info(commandString);
			
			rowInline.add(new InlineKeyboardButton().setText(buttonText)
					.setCallbackData(commandString));
			rowsInline.add(rowInline);
		}
		
		// add the all option
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		String commandString = String.format("method=getWalletValue,coin=%s,walletID=%d", callDataDetails.get("coinName"), 0);
		
		rowInline.add(new InlineKeyboardButton().setText("Alles")
				.setCallbackData(commandString));
		rowsInline.add(rowInline);
		
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		
		LOG.trace("finished getWalletsForCoin()");
		
		return message;
		
	}
	

	/**
	 * Transforms the help text into a EditMessage. This method is called when the help button is pressed
	 * @return
	 */
	private EditMessageText getHelpTextEdit() {
		LOG.trace("entered getHelpTextEdit()");
		
		String messageText = this.generateHelpText();
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.getChatIDTelegram())
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		LOG.trace("finished getHelpTextEdit()");
		
		return message;
	}
	
	/**
	 * Create a update message containing the total value of the portfolio
	 * @return the edit message containing the 
	 */
	private EditMessageText getTotalPortfolioValue() {
		LOG.trace("entered getTotalPortfolioValue()");
		
		// get all the coins in the portfolio
		Portfolio portfolio = new Portfolio();
		portfolio.setRequestID(this.getRequestID());
		portfolio.getAllCoinsInPortfolio();
		
		List<Coin> coins = portfolio.getCoins();
		
		String messageText;
		messageText = String.format("Hoi %s,\n", this.getFirstName());
		messageText += "Op dit moment ziet de totale waarde van het portfolio er als volgt uit:\n";
		
		// loop through the coins
		for(Coin coin : coins) {
			// get the coin name
			String coinName = coin.getCoinName();
			double balance = coin.getTotalCoinBalance();
			double value = coin.getTotalCurrentCoinValue();
			// add this to the message text
			messageText += String.format("%s: `%.8f` (`€%.2f`)\n", coinName, balance, value);
		}
		
		// now calculate the difference to the deposits
		double totalValue = portfolio.getTotalCurrentValuePortfolio();
		double depositedValue = portfolio.getTotalDepositedValue();
		double differenceDepositCurrent = totalValue - depositedValue;
		
		messageText += String.format("Totale waarde: `€%.2f`\n", totalValue);
		messageText += String.format("Ingelegd: `€%.2f` (`€%+.2f`)", depositedValue, differenceDepositCurrent);
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.getChatIDTelegram())
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		
		message.setParseMode(ParseMode.MARKDOWN);
		
		return message;
	}
	
	/**
	 * Method to shorten a string. 
	 * @param input the input string
	 * @param maxLength the maximum length of the string
	 * @return shortened string
	 */
	private String shortenString(String input, int maxLength) {
		String newString = input.substring(0, Math.min(input.length(), maxLength - 3)) + "...";
		
		return newString;
	}
	
	
	private EditMessageText getWalletValue(List<String> requiredKeys, HashMap<String, String> callDataDetails) {
		LOG.trace("entered getWalletValue()");
		
		// check if the call is complete
		if(! this.checkCallDataComplete(requiredKeys, callDataDetails)) {
			// command is not complete, exit the method with an error text
			EditMessageText message = this.generateSimpleEditMessageText("Sorry het commando van deze knop is niet compleet. #sorrynog");
			LOG.warn("Not all required keys are found for getWalletsForCoin(). Exiting");
			return message;
		}
		
		String messageText = "Hierbij de gevonden wallets:\n\n";
		Portfolio portfolio = new Portfolio();
		
		
		// first check if the walletID is set to 0, this means all the wallets need to be listed
		int walletID = Integer.parseInt(callDataDetails.get("walletID"));
		if(walletID == 0) {
			List<Coin> coins;
			
			// wallet ID is 0, now check if the coin == all, if so, all coins need to be listed
			if(callDataDetails.get("coin").equals("all")) {
				// get all the wallets for all the coins
				portfolio.getAllCoinsInPortfolio();
				coins = portfolio.getCoins();
			}else {
				// get all the wallets for the specified coin
				portfolio.getCoinInPortfolio(callDataDetails.get("coin"));
				coins = portfolio.getCoins();
			}
			// now loop through the coins to form a message
			for(Coin coin : coins) {
				// get the wallets
				List<Wallet> wallets = coin.getWallets();
				// loop through the wallets
				for(Wallet wallet : wallets) {
					messageText += String.format("Walletadres: `%s`\n", wallet.getWalletAddress());
					messageText += String.format("Coin: %s\n", wallet.getCoinName().toUpperCase());
					messageText += String.format("Aantal: `%.8f`\n", wallet.getBalanceCoin());
					messageText += String.format("Waarde: `€%.2f`\n", wallet.getCurrentValue());
					double differenceDeposit = wallet.getCurrentValue() - wallet.getTotalDepositedValue();
					messageText += String.format("Inleg: `€%.2f` (`€%+.2f`)\n", wallet.getTotalDepositedValue(), differenceDeposit);
					double differenceLastRequest = wallet.getCurrentValue() - wallet.getLastKnownValue();
					messageText += String.format("Stijging sinds %s: `€%+.2f`\n\n", wallet.getLastResultDate(), differenceLastRequest);
				}
			}
		}
		
		
		// create the message
		EditMessageText message = new EditMessageText()
                .setChatId(this.getChatIDTelegram())
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		message.setParseMode(ParseMode.MARKDOWN);
		
		LOG.trace("finished getWalletValue()");
		return message;
	}
	
}
