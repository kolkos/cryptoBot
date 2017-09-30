package cryptoBot;

import static java.lang.Math.toIntExact;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class BotRequest {
	private String firstName;
	private long chatID;
	private long messageID;
	private String command;
	
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
	public String getCommand() {
		return command;
	}
	
	/**
	 * Register the incoming command
	 * @param command a chat message or callback query 
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * Shortcut to register the incoming chat message. This method is used to force registering the required variables
	 * @param chatID the ID of the chat where the message is send from
	 * @param firstName the name of the user which has send the message
	 * @param command the incoming chat message/command
	 */
	public void registerChatMessage(long chatID, String firstName, String command) {
		this.setChatID(chatID);
		this.setFirstName(firstName);
		this.setCommand(command);
	}
	
	/**
	 * Shortcut to register the incoming callback query. This method is used to force registering the required variables
	 * @param chatID chatID the ID of the callback query where the message is send from
	 * @param messageID the ID of the message containing the inline query
	 * @param firstName the name of the user which has send the callback query (clicked on the button)
	 * @param command the value of the inline keyboard button
	 */
	public void registerCallbackQuery(long chatID, long messageID, String firstName, String command) {
		this.setChatID(chatID);
		this.setMessageID(messageID);
		this.setFirstName(firstName);
		this.setCommand(command);
	}
	
	/**
	 * This method creates the menu for the users. This menu contains possible commands/requests
	 * Some commands will trigger sending other menu's
	 * @return a new message to send to the chat 
	 */
	public SendMessage getBotOptions() {
		String messageText = String.format("IEMAND ZEI BOT!\n\nHoi %s, je kunt kiezen uit de volgende opties:", this.firstName);
		
		// Create a message object object
		SendMessage message = new SendMessage()
				.setChatId(chatID).setText(messageText);
		
		// create the markupInline object
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		
		// create the list that contains the buttons
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		
		// create a list for a single row
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		
		// now add the objects
		rowInline.add(new InlineKeyboardButton().setText("Help")
				.setCallbackData("method=getHelpText"));
		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		
		// create the shortcut to request portfolio value
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Snel waarde opvragen")
				.setCallbackData("method=getCoinValue,coinName=all,since=last"));
		rowsInline.add(rowInline);
		
		// interactive portfolio request
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Waarde portfolio")
				.setCallbackData("method=getPortfolioCoins"));
		rowsInline.add(rowInline);
		
		// get the wallets with their values
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Wallets opvragen")
				.setCallbackData("method=getWallets"));
		rowsInline.add(rowInline);
		
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);		
		
		// return the message
		return message;
	}
	
	/**
	 * This method parses the incoming callback query to a HashMap. This makes it easier to get parameters from the requested methods
	 * @return a HashMap which contains key/value pairs from the incoming callback query
	 */
	private HashMap<String, String> parseCallData(){
		HashMap<String, String> parameters = new HashMap<>();
		String[] keyValuePairs = this.command.split(",");
		for(String keyValuePair : keyValuePairs) {
			String[] keyValueParts = keyValuePair.split("=");
			String key = keyValueParts[0];
			String value = keyValueParts[1];
			parameters.put(key, value);
		}
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
		// by default the check is OK
		boolean checkOK = true;
		
		// loop through the required keys
		for(String requiredKey: requiredKeys) {
			// if a key isn't found, the check is set to false
			if(! callDataDetails.containsKey(requiredKey)) {
				checkOK = false;
			}
		}
		
		return checkOK;
	}
	
	/**
	 * This method handles the incoming callback queries. It will first parse the incoming callback query to a HashMap. 
	 * Based on the HashMap value of 'method' it will run the required method. Each method will return a EditMessage to this
	 * method. This method then returns this EditMessage.
	 * @return
	 */
	public EditMessageText runCallbackQueryCommand() {
		HashMap<String, String> callDataDetails = this.parseCallData();
		
		// message is the object which is returned to the calling method
		EditMessageText message = null;
		
		// check if method is set
		if(! callDataDetails.containsKey("method")) {
			message = this.generateErrorMessage("Method niet gevonden in callback query");
			return message;
		}
		
		// create a list object for the required keys
		List<String> requiredKeys = new ArrayList<>();
		
		// now get the method
		String method = callDataDetails.get("method");
		
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
				message = this.generateErrorMessage("Ik kan nog niets met dit commando!");
				
		}
		
		// now return the generated message
		return message;
		
		
	}
	
	/**
	 * Create a status message containing all the registered wallets (with the current values)
	 * @return status message which contains the current values of the registered wallets
	 */
	private EditMessageText getWallets() {
		Request request = new Request();
		request.setRequestedBy(this.firstName);
		
		request.handleWalletRequest();
		
		String statusMessage = request.getStatusMessage();
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(statusMessage);
		
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
		// check if the call is complete
		if(! this.checkCallDataComplete(requiredKeys, callDataDetails)) {
			EditMessageText message = this.generateErrorMessage("Commando niet compleet");
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
		// check if the call is complete
		if(! this.checkCallDataComplete(requiredKeys, callDataDetails)) {
			EditMessageText message = this.generateErrorMessage("Commando niet compleet");
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
		
		return message;
	}
	
	/**
	 * This method creates a message containing the registered coins (with at least one wallet attached)
	 * Next to the found coins, it will contain a option to request all the coins
	 * @return status message containing the found coins and the 'all' buttons
	 */
	private EditMessageText getPortfolioCoins() {
		String messageText = "Op het moment heb ik de volgende coins in het porfolio gevonden:";
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		// get the coins in the portfolio
		Portfolio portfolio = new Portfolio();
		portfolio.setCoins();
		List<String> coins = portfolio.getCoins();
		
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
		
		return message;
	}
	
	/**
	 * Method to generate the help text
	 * @return the help text
	 */
	private String generateHelpText() {
		String messageText = "Op dit moment kun je de volgende commando's uitvoeren:\n";
		messageText += "- *bot* => open het menu\n";
		//messageText += "- /registerCoin <afkorting> <coin naam> => Registreren van een nieuwe coin\n";
		//messageText += "- /registerWallet <afkorting coin> <wallet adres> => Registreren van nieuwe wallet";
		messageText += "- /help => verkrijg deze helptekst";
		//messageText += "- /registerDeposit <wallet adres> <aantal coins> <> => Registreren van nieuwe wallet";
		
		
		return messageText;
	}
	
	/**
	 * Transforms the help text to a SendMessage. This method is called when the /help command is send
	 * @return message containing the help text
	 */
	private SendMessage getHelpTextSend() {
		String messageText = this.generateHelpText();
		
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(chatID).setText(messageText);
		
		return message;
	}
	
	/**
	 * Transforms the help text into a EditMessage. This method is called when the help button is pressed
	 * @return
	 */
	private EditMessageText getHelpTextEdit() {
		String messageText = this.generateHelpText();
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		return message;
		
	}
	
	/**
	 * Method to create an error message to send
	 * @param messageText text for the error message
	 * @return the error message
	 */
	private EditMessageText generateErrorMessage(String messageText) {
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		return message;
		
	}
	
	
	
}
