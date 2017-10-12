package cryptoBot;

import static java.lang.Math.toIntExact;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class CommandHandler extends CryptoBot {
	private String firstName;
	private long chatIDTelegram;
	private long messageID;
	private String incomingMessage;
	private String uuid;
	private int chatID;
		
	private static final Logger LOG = LogManager.getLogger(CommandHandler.class);
	
	public CommandHandler() {
		this.uuid = UUID.randomUUID().toString();
	}
	
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
	public long getChatIDTelegram() {
		return chatIDTelegram;
	}
	
	/**
	 * Register the chat ID
	 * @param chatID the ID of the chat from where the request is send
	 */
	public void setChatIDTelegram(long chatID) {
		this.chatIDTelegram = chatID;
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
	 * Register the incoming chat message. This method checks if the chatID is already registerd.
	 * If not, this method will register the chatID. 
	 * By default the active and receive_update are set to 0. This means the chat isn't allowed to
	 * send requests to the bot.
	 * @param chatIDTelegram the chatID from the group/user which send a request to the bot
	 * @throws Exception mysql error
	 */
	public void registerChat() throws Exception {
		LOG.trace("entered registerChat()");
		
		// check if the chat is already registered
		String query = "SELECT chat_id FROM chats WHERE chat_id = ?";
		Object[] parameters = new Object[] {this.chatIDTelegram};
		MySQLAccess db = new MySQLAccess();
		db.executeSelectQuery(query, parameters);
		
		ResultSet resultSet = db.getResultSet();
		
		if(resultSet.next() == false){
			LOG.info("chatID {} is not registered yet", this.chatIDTelegram);
			
			// the chat is not registered, register it
			query = "INSERT INTO chats (chat_id) VALUES (?)";
			parameters = new Object[] {this.chatIDTelegram};
			// run the query
			db.executeUpdateQuery(query, parameters);
		}
		
		db.close();
		LOG.trace("finished registerChat()");
	}
	
	/**
	 * This method checks if the chat from where the request is send is allowed to send requests to the bot.
	 * To check this, this method checks if the chat_id is in the database and if active is set to 1
	 * @param chatIDTelegram the id of the chat that needs checking
	 * @return true if the chat is allowed, else false
	 */
	public boolean checkIfChatIsAllowedToSendRequests() {
		LOG.trace("entered checkIfChatIsAllowedToSendRequests()");
		// now check if the chat is in the database and if it is allowed to place requests for this bot
		String query = "SELECT * FROM chats WHERE chat_id = ? AND active = 1";
		Object[] parameters = new Object[] {this.chatIDTelegram};
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			// check if the resultset contains something
			if(! resultSet.next()) {
				// no results found, this means that the bot isn't allowed to send to this group
				String messageText = String.format("Hoi! Wij kennen elkaar nog niet. Vraag even aan Anton (@Kolkos) of hij deze chat wil activeren. Vermeld dit chatID: %d", this.chatIDTelegram);
				this.sendStringToChat(messageText);
				return false;
			}
		} catch (Exception e) {
			db.close();
			LOG.fatal("Error getting chats from database: {}", e);
			return false;
		}finally {
			db.close();
		}
		// if the code reaches this part, the chat is allowed to send messages
		LOG.trace("finished checkIfChatIsAllowedToSendRequests()");
		return true;
	}
	
	/**
	 * Get the ID of the chat in the database
	 * @return the ID of the chat in the database
	 */
	private int getChatIDFromDB() {
		LOG.trace("entered getChatIDFromDB()");
		int chatID = 0;
		
		String query = "SELECT id FROM chats WHERE chat_id = ?";
		Object[] parameters = new Object[] {this.chatIDTelegram};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			while(resultSet.next()) {
				chatID = resultSet.getInt("id");
			}
			
			
		} catch (Exception e) {
			LOG.fatal("Error getting chat ID from database: {}", e);
		} finally {
			db.close();
		}
		
		LOG.trace("finished getChatIDFromDB()");
		return chatID;
	}
	
	/**
	 * Register the current request into the database
	 */
	public void registerRequestInDatabase() {
		LOG.trace("entered registerRequestInDatabase()");
		
		this.chatID = this.getChatIDFromDB();
		String query = "INSERT INTO requests (uuid, chat_id, firstName, command) VALUES (?, ?, ?, ?)";
		Object[] parameters = new Object[] {this.uuid, this.chatID, this.firstName, this.incomingMessage};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			LOG.fatal("Error registering request: {}", e);
		} finally {
			db.close();
		}
		LOG.trace("finished registerRequestInDatabase()");
	}
	
	/**
	 * This method sends a string to a chat. This method combines the methods:
	 * generateSimpleSendMessage
	 * sendMessageToChat
	 * @param messageText text to send to the chat
	 */
	public void sendStringToChat(String messageText) {
		LOG.trace("entered sendStringToChat(), messageText={}", messageText);
		SendMessage message = this.generateSimpleSendMessage(messageText);
		this.sendMessageToChat(message);
		LOG.trace("finished sendStringToChat()");
	}
	
	
	/**
	 * This method transforms a string to a SendMessage
	 * @param messageText
	 * @return prepared SendMessage
	 */
	private SendMessage generateSimpleSendMessage(String messageText) {
		LOG.trace("entered generateSimpleSendMessage(), messageText={}", messageText);
		
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(this.chatIDTelegram).setText(messageText);
		
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
			LOG.trace("Message send: {}", message);
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
                .setChatId(this.chatIDTelegram)
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
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOG.fatal("error editing chat message {}", e);
		}
		LOG.trace("finished sendEditMessageText()");
	}
	
	/**
	 * Shortcut to register the incoming chat message. This method is used to force registering the required variables
	 * @param chatIDTelegram the ID of the chat where the message is send from
	 * @param firstName the name of the user which has send the message
	 * @param incomingChatMessage the incoming chat message/command
	 */
	public void registerChatMessage(long chatIDTelegram, String firstName, String incomingChatMessage) {
		LOG.trace("entered registerChatMessage(): chat_id={}, firstName={}, incomingChatMessage={}", chatIDTelegram, firstName, incomingChatMessage);
		this.setChatIDTelegram(chatIDTelegram);
		this.setFirstName(firstName);
		this.setIncomingMessage(incomingChatMessage);
		LOG.trace("finished registerChatMessage()");
	}
	
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
			case "getWalletsForCoin":
				requiredKeys.add("coinName");
				message = this.getWalletsForCoin(requiredKeys, callDataDetails);
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
	
	
	public void sendBotOptions() {
		LOG.trace("entered sendBotOptions()");
		
		String messageText = "Maak een keuze uit 1 van de volgende opties:";
		
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(this.chatIDTelegram).setText(messageText);
		
		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		
		// create first line
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Waarde opvragen")
				.setCallbackData("method=getTotalPortfolioValue"));
		rowsInline.add(rowInline);
		
		// create the second line
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Portfolio")
				.setCallbackData("method=getPortfolioCoins"));
		rowsInline.add(rowInline);
		
		// create the third line
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Help")
				.setCallbackData("method=editMessageHelpText"));
		rowsInline.add(rowInline);
		
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		
		// now send this message
		this.sendMessageToChat(message);
		
		LOG.trace("finished sendBotOptions()");
	}
	
	
	/**
	 * This method creates a message containing the registered coins (with at least one wallet attached)
	 * Next to the found coins, it will contain a option to request all the coins
	 * @return status message containing the found coins and the 'all' buttons
	 */
	private EditMessageText getPortfolioCoins() {
		LOG.trace("entered getPortfolioCoins()");
		
		String messageText = "Het portfolio bestaat uit de volgende coins. Maak een keuze.";
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatIDTelegram)
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
			EditMessageText message = this.generateSimpleEditMessageText("Sorry het commando is niet compleet");
			LOG.warn("Not all required keys are found for getCoinValueOptions(). Exiting");
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
                .setChatId(this.chatIDTelegram)
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
			
			String buttonText = String.format("%s (%s)", walletAddress, coinName.toUpperCase());
			String commandString = String.format("method=getWalletValue,walletID=%d", walletID);
			
			LOG.info(buttonText);
			LOG.info(commandString);
			
			rowInline.add(new InlineKeyboardButton().setText(buttonText)
					.setCallbackData(commandString));
			rowsInline.add(rowInline);
		}
		
		// add the all option
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		String commandString = "method=getAllWalletsValues";
		
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
                .setChatId(this.chatIDTelegram)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		LOG.trace("finished getHelpTextEdit()");
		
		return message;
	}
	
	private String generateOverallValueMessage() {
		String valueMessage = "";
		
		/*
		 * TODO: 
		 * 	Per coin:
		 * 		totale balans
		 * 	Totaal:
		 * 		totale waarde portfolio
		 * 		+/- tov vorige keer opgevraagd
		 * 		+/- percentage tov vorige keer opgevraagd
		 * 		Datum vorige keer opgevraagd
		 * 	Winst/verlies
		 * 		
		 * 
		 */
		
		return valueMessage;
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
                .setChatId(this.chatIDTelegram)
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
                .setChatId(this.chatIDTelegram)
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
                .setChatId(this.chatIDTelegram)
                .setMessageId(toIntExact(this.messageID))
                .setText(statusMessage);
		
		LOG.trace("finished getWallets()");
		
		return message;
		
	}
	
}
