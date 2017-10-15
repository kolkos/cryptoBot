package cryptoBot;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class CommandHandler extends CryptoBot {
	private String firstName;
	private long chatIDTelegram;
	private String incomingMessage;
	private String uuid;
	private int chatID;
	private int requestID;
		
	private static final Logger LOG = LogManager.getLogger(CommandHandler.class);
	
	public int getRequestID() {
		this.requestID = this.getRequestIDFromDB();
		return this.requestID;
	}

	

	public CommandHandler() {
		this.setUuid();
	}
	
	/*
	 * ==========================================================================================
	 * Setters and Getters
	 * ==========================================================================================
	 */
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid() {
		this.uuid = UUID.randomUUID().toString();
	}
	
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
	 * Register the incoming chat message. This method checks if the chatID is already registered.
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
	
	private int getRequestIDFromDB() {
		LOG.trace("entered getRequestIDFromDB()");
		
		String query = "SELECT id AS requestID FROM requests WHERE uuid = ? ORDER BY id DESC LIMIT 1";
		Object[] parameters = new Object[] {this.uuid};
		MySQLAccess db = new MySQLAccess();
		int requestID = 0;
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			while(resultSet.next()) {
				requestID = resultSet.getInt("requestID");
			}
			
			
		} catch (Exception e) {
			LOG.fatal("Error getting chat ID from database: {}", e);
		} finally {
			db.close();
		}
		
		LOG.trace("finished getRequestIDFromDB()");
		return requestID;
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
	public void sendMessageToChat(SendMessage message) {
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
	 * Method to generate the help text
	 * @return the help text
	 */
	protected String generateHelpText() {
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
		
		String messageText = "Maak een uit 1 van de volgende opties:";
		
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
		rowInline.add(new InlineKeyboardButton().setText("Bekijk portfolio")
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
	
	
	
	
	
}
