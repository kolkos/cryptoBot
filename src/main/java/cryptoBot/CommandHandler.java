package cryptoBot;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class CommandHandler extends CryptoBot {
	private String firstName;
	private long chatID;
	private long messageID;
	private String incomingMessage;
	
	private static final Logger LOG = LogManager.getLogger(CommandHandler.class);
	
	/*
	 * ==========================================================================================
	 * Setters and Getters
	 * ==========================================================================================
	 */
	
	/**
	 * Get the first name from the requester
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * Register the first name
	 * @param firstName first name of the user
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * Get the registered chatID
	 * @return the chatID
	 */
	public long getChatID() {
		return chatID;
	}
	
	/**
	 * Register the chat ID
	 * @param chatID the ID of the chat from where the request is send
	 */
	public void setChatID(long chatID) {
		this.chatID = chatID;
	}
	
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
	
	/**
	 * Get the registered command
	 * @return the command which is end to the bot
	 */
	public String getIncomingMessage() {
		return incomingMessage;
	}
	
	/**
	 * Register the incoming command
	 * @param command a chat message or callback query 
	 */
	public void setIncomingMessage(String command) {
		this.incomingMessage = command;
	}
	
	
	/*
	 * ==========================================================================================
	 * General methods
	 * ==========================================================================================
	 */
	
	/**
	 * This method transforms a string to a SendMessage
	 * @param messageText
	 * @return prepared SendMessage
	 */
	private SendMessage generateSimpleSendMessage(String messageText) {
		LOG.trace("entered generateSimpleSendMessage(), messageText={}", messageText);
		
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(this.chatID).setText(messageText);
		
		LOG.trace("finished generateSimpleSendMessage()");
		
		return message;	
	}
	
	/**
	 * Send the prepared message to a chat
	 * @param message prepared SendMessage
	 */
	private void sendMessageToChat(SendMessage message) {
		LOG.trace("entered sendMessageToChat()");
		
		try {
			sendMessage(message); // Sending our message object to user
		} catch (TelegramApiException e) {
			// TODO: iets met fout doen
			//e.printStackTrace();
			LOG.fatal("Error sending message: {}", e);
		}
		
		LOG.trace("finished sendMessageToChat()");
	}
	
	/**
	 * This method generates a simple edit message from a String
	 * @param messageText the text to send (to edit the existing message)
	 * @return prepared EditMessage
	 */
	private EditMessageText generateSimpleEditMessageText(String messageText) {
		LOG.trace("entered generateSimpleEditMessageText(), messageText={}", messageText);
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		LOG.trace("entered generateSimpleEditMessageText()");
		
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
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("error editing chat message {}", e);
		}
		LOG.trace("finished sendEditMessageText()");
	}
	
	/**
	 * Shortcut to register the incoming chat message. This method is used to force registering the required variables
	 * @param chatID the ID of the chat where the message is send from
	 * @param firstName the name of the user which has send the message
	 * @param incomingChatMessage the incoming chat message/command
	 */
	public void registerChatMessage(long chatID, String firstName, String incomingChatMessage) {
		LOG.trace("entered registerChatMessage(): chat_id={}, firstName={}, incomingChatMessage={}", chatID, firstName, incomingChatMessage);
		this.setChatID(chatID);
		this.setFirstName(firstName);
		this.setIncomingMessage(incomingChatMessage);
		LOG.trace("finished registerChatMessage()");
	}
	
	/**
	 * Shortcut to register the incoming callback query. This method is used to force registering the required variables
	 * @param chatID chatID the ID of the callback query where the message is send from
	 * @param messageID the ID of the message containing the inline query
	 * @param firstName the name of the user which has send the callback query (clicked on the button)
	 * @param incomingCallbackQuery the value of the inline keyboard button
	 */
	public void registerCallbackQuery(long chatID, long messageID, String firstName, String incomingCallbackQuery) {
		LOG.trace("entered registerCallbackQuery(): chat_id={}, messageID={}, firstName={}, incomingCallbackQuery={}", chatID, messageID, firstName, incomingCallbackQuery);
		this.setChatID(chatID);
		this.setMessageID(messageID);
		this.setFirstName(firstName);
		this.setIncomingMessage(incomingCallbackQuery);
		LOG.trace("finished registerCallbackQuery()");
	}
	
	/**
	 * This method parses the incoming callback query to a HashMap. This makes it easier to get parameters from the requested methods
	 * @return a HashMap which contains key/value pairs from the incoming callback query
	 */
	private HashMap<String, String> parseCallData(){
		LOG.trace("entered parseCallData()");
		LOG.trace("callData={}", this.incomingMessage);
		HashMap<String, String> parameters = new HashMap<>();
		String[] keyValuePairs = this.incomingMessage.split(",");
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
			case "getCoinValueOptions":
				requiredKeys.add("coinName");
				message = this.getCoinValueOptions(requiredKeys, callDataDetails);
				break;
			case "getCoinValue":
				requiredKeys.add("coinName");
				requiredKeys.add("since");
				message = this.getCoinValue(requiredKeys, callDataDetails);
				break;
			case "getWallets":
				message = this.getWallets();
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
	
	/**
	 * Method to generate the help text
	 * @return the help text
	 */
	private String generateHelpText() {
		LOG.trace("entered generateHelpText()");
		
		String messageText = "Op dit moment kun je de volgende commando's uitvoeren:\n";
		messageText += "- *bot* => open het menu\n";
		//messageText += "- /registerCoin <afkorting> <coin naam> => Registreren van een nieuwe coin\n";
		//messageText += "- /registerWallet <afkorting coin> <wallet adres> => Registreren van nieuwe wallet";
		messageText += "- /help => verkrijg deze helptekst";
		//messageText += "- /registerDeposit <wallet adres> <aantal coins> <> => Registreren van nieuwe wallet";
		
		LOG.trace("finished generateHelpText()");
		
		return messageText;
	}
	
	
	/*
	 * ==========================================================================================
	 * Request specific methods
	 * ==========================================================================================
	 */
	
	/**
	 * This method creates a message containing the registered coins (with at least one wallet attached)
	 * Next to the found coins, it will contain a option to request all the coins
	 * @return status message containing the found coins and the 'all' buttons
	 */
	private EditMessageText getPortfolioCoins() {
		LOG.trace("entered getPortfolioCoins()");
		
		String messageText = "Op het moment heb ik de volgende coins in het porfolio gevonden:";
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		// get the coins in the portfolio
		Portfolio portfolio = new Portfolio();
		portfolio.setCoins();
		List<String> coins = portfolio.getCoins();
		
		LOG.info("found the following coins: {}", coins);
		
		// loop through the coins to generate the menu
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		for(String coin : coins) {
			
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			String commandString = String.format("method=getCoinValueOptions,coinName=%s", coin);
			
			rowInline.add(new InlineKeyboardButton().setText(coin.toUpperCase())
					.setCallbackData(commandString));
			rowsInline.add(rowInline);
			
		}
		
		// now add the 'all' option
		
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		String commandString = String.format("method=getCoinValueOptions,coinName=%s", "all");
		
		rowInline.add(new InlineKeyboardButton().setText("Alles")
				.setCallbackData(commandString));
		rowsInline.add(rowInline);
		
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		
		LOG.trace("finished getPortfolioCoins()");
		
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
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		LOG.trace("finished getHelpTextEdit()");
		
		return message;
	}
	
	/**
	 * This method sends the options for the chosen coin. This basically means the value to compare the current value to.
	 * There are two options:
	 *   - last => compare the current value to the last request
	 *   - begin => compare the current value against the first registered value
	 * @param requiredKeys the keys which are need to be set to correctly handle this request
	 * @param callDataDetails the HashMap containing the parameters for this request
	 * @return a inline keyboard message containing the two options
	 */
	private EditMessageText getCoinValueOptions(List<String> requiredKeys, HashMap<String, String> callDataDetails) {
		LOG.trace("entered getCoinValueOptions()");
		
		// check if the call is complete
		if(! this.checkCallDataComplete(requiredKeys, callDataDetails)) {
			EditMessageText message = this.generateSimpleEditMessageText("Commando niet compleet");
			LOG.warn("Not all required keys are found for getCoinValueOptions()");
			return message;
		}
		
		String coinName = callDataDetails.get("coinName");
		
		String messageText = "Vanaf welk moment wil je de voortgang zien:";
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		
		String commandStringPattern = "method=getCoinValue,coinName=%s,since=%s";
		
		rowInline.add(new InlineKeyboardButton().setText("Vanaf laatste opvraging")
				.setCallbackData(String.format(commandStringPattern, coinName, "last")));
		
		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Vanaf begin")
				.setCallbackData(String.format(commandStringPattern, coinName, "begin")));
		rowsInline.add(rowInline);
		
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		
		LOG.trace("finished getCoinValueOptions()");
		
		return message;
	}
	
	/**
	 * This method get the values by the coins. This method is called after the following methods:
	 *   - getPortfolioCoins => get the coins in the portfolio
	 *   - getCoinValueOptions => get the options for the coin request (since)
	 * This method uses the checkCallDataComplete() method to check if the call is complete. If not, a error message is returned
	 * @param requiredKeys the keys which are need to be set to correctly handle this request
	 * @param callDataDetails the HashMap containing the parameters for this request
	 * @return a status message (depending on the chosen options) with the current value of the portfolio (by coin)
	 */
	private EditMessageText getCoinValue(List<String> requiredKeys, HashMap<String, String> callDataDetails) {
		LOG.trace("entered getCoinValue()");
		
		// check if the call is complete
		if(! this.checkCallDataComplete(requiredKeys, callDataDetails)) {
			EditMessageText message = this.generateSimpleEditMessageText("Commando niet compleet");
			LOG.warn("Not all required keys are found for getCoinValue()");
			return message;
		}
		
		// create a request
		Request request = new Request();
		request.setRequestedBy(this.firstName);
		request.setRequestedCoins(callDataDetails.get("coinName"));
		request.setCalculateSince(callDataDetails.get("since"));
		request.handleCoinRequest();
		
		String statusMessage = request.getStatusMessage();
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(statusMessage);
		
		LOG.trace("finished getCoinValue()");
		
		return message;
	}
	
	/**
	 * Create a status message containing all the registered wallets (with the current values)
	 * @return status message which contains the current values of the registered wallets
	 */
	private EditMessageText getWallets() {
		LOG.trace("entered getWallets()");
		
		Request request = new Request();
		request.setRequestedBy(this.firstName);
		
		request.handleWalletRequest();
		
		String statusMessage = request.getStatusMessage();
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(statusMessage);
		
		LOG.trace("finished getWallets()");
		
		return message;
		
	}
	
}
