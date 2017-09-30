package cryptoBot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

import java.util.prefs.*;
import java.util.regex.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.Math.toIntExact;

public class CryptoBot extends TelegramLongPollingBot {
	private Properties properties;
	
	private final static int FROM_HOUR = 12;
	private final static int TO_HOUR = 13;
	
	public CryptoBot() {
		General general = new General();
		this.properties = general.getProperties();
	}
	
	/**
	 * This method handles incoming messages and callback queries.
	 * @param update
	 */
	@Override
	public void onUpdateReceived(Update update) {
		// handle incoming chat messages
		if (update.hasMessage() && update.getMessage().hasText()) {
			
			
			String incomingMessageText = update.getMessage().getText();
			long chatID = update.getMessage().getChatId();

			// first register this chat (if necessary)
			try {
				this.registerChat(chatID);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// check if the chat is allowed to send requests
			if(! this.checkIfChatIsAllowedToSendRequests(chatID)) {
				// now allowed, exit method
				return;
			}
			
			
			// make the message lowercase
			incomingMessageText = incomingMessageText.toLowerCase();
			//System.out.println(incomingMessageText);
			
			// first check if the bot contains the word 'bot'
			Pattern patternBot = Pattern.compile(".*bot.*");
			Matcher matcher = patternBot.matcher(incomingMessageText);
			// only do something when the word 'bot' is found
			if (matcher.find()) {
				
				// get the other details
				String firstName = update.getMessage().getFrom().getFirstName();
				
				String command = "getBotOptions";
				
				BotRequest chatRequest = new BotRequest();
				chatRequest.registerChatMessage(chatID, firstName, command);

				SendMessage message = chatRequest.getBotOptions();
				
				
				
				try {
					sendMessage(message); // Sending our message object to user
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}

			}
		// handle incoming callback queries
		} else if (update.hasCallbackQuery()) {
			// call data is the command which is set to a inline keyboard button
			String callData = update.getCallbackQuery().getData();
			
			// get the message ID (the message of the inline keyboard message)
			// this ID is used to overwrite this method
			long messageID = update.getCallbackQuery().getMessage().getMessageId();
			long chatID = update.getCallbackQuery().getMessage().getChatId();
			
			// get the first name of the requester
			String firstName = update.getCallbackQuery().getFrom().getFirstName();
			
			// create a new BotRequest object
			BotRequest callbackRequest = new BotRequest();
			// register this request
			callbackRequest.registerCallbackQuery(chatID, messageID, firstName, callData);
			
			// use the runCallbackQueryCommand method to determine the message to send
			EditMessageText message = callbackRequest.runCallbackQueryCommand();

			try {
				editMessageText(message);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Register the incoming chat message. This method checks if the chatID is already registerd.
	 * If not, this method will register the chatID. 
	 * By default the active and receive_update are set to 0. This means the chat isn't allowed to
	 * send requests to the bot.
	 * @param chatID the chatID from the group/user which send a request to the bot
	 * @throws Exception mysql error
	 */
	public void registerChat(long chatID) throws Exception {
		// check if the chat is already registered
		String query = "SELECT chat_id FROM chats WHERE chat_id = ?";
		Object[] parameters = new Object[] {chatID};
		MySQLAccess db = new MySQLAccess();
		db.executeSelectQuery(query, parameters);
		
		ResultSet resultSet = db.getResultSet();
		
		if(resultSet.next() == false){
			// the chat is not registered, register it
			query = "INSERT INTO chats (chat_id) VALUES (?)";
			parameters = new Object[] {chatID};
			// run the query
			db.executeUpdateQuery(query, parameters);
		}
	}
	
	/**
	 * This method checks if the chat from where the request is send is allowed to send requests to the bot.
	 * To check this, this method checks if the chat_id is in the database and if active is set to 1
	 * @param chatID the id of the chat that needs checking
	 * @return true if the chat is allowed, else false
	 */
	private boolean checkIfChatIsAllowedToSendRequests(long chatID) {
		// now check if the chat is in the database and if it is allowed to place requests for this bot
		String query = "SELECT * FROM chats WHERE chat_id = ? AND active = 1";
		Object[] parameters = new Object[] {chatID};
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			// check if the resultset contains something
			if(! resultSet.next()) {
				// no results found, this means that the bot isn't allowed to send to this group
				String messageText = String.format("Hoi! Wij kennen elkaar nog niet. Vraag even aan Anton (@Kolkos) of hij deze chat wil activeren. Vermeld dit chatID: %d", chatID);
				this.sendMessageToChat(chatID, messageText);
				return false;
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			// error found, for safety reasons return false
			e1.printStackTrace();
			return false;
		}
		// if the code reaches this part, the chat is allowed to send messages
		return true;
	}
	
	/**
	 * This method is called from the scheduler. This method automatically requests the current
	 * value of the portfolio. 
	 * If the current time is between FROM_HOUR and TO_HOUR this method will send a update message
	 * to the registered chats. The method checks if active and receive_update are set to 1
	 */
	public void automaticStatusUpdatePortfolio() {
		Request request = new Request();
		request.setRequestedCoins("all");
		request.setRequestedBy("cryptoBot");
		request.handleCoinRequest();
				
		// now check if the message needs to be send
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int hour = cal.get(Calendar.HOUR_OF_DAY);  //Get the hour from the calendar
		
		System.out.println("Run automatic task automaticStatusUpdatePortfolio() @ " + cal.getTime());
		
		// Check if hour is between 12 and 13
		// if so send the message
		if(hour >= CryptoBot.FROM_HOUR && hour < CryptoBot.TO_HOUR)               
		{
			// get the chats from the db to determine where to send the updates to
			String query = "SELECT chat_id FROM chats WHERE active = ? AND receive_update = ?";
			Object[] parameters = new Object[] {1, 1};
			MySQLAccess db = new MySQLAccess();
			try {
				db.executeSelectQuery(query, parameters);
				// get the results
				ResultSet resultSet = db.getResultSet();
				
				String statusMessageAppendix = String.format("Hallo lieve kijkbuiskindertjes! De update van ongeveer %d uur is hier!\n\n", hour);
				request.appendToStatusMessage(statusMessageAppendix);
				
				while (resultSet.next()) {
					long chatID = resultSet.getLong("chat_id");
					
					SendMessage message = new SendMessage() // Create a message object object
							.setChatId(chatID).setText(request.getStatusMessage());
					try {
						sendMessage(message); // Sending our message object to user
					} catch (TelegramApiException e) {
						System.out.println("Niet toegevoegd aan chat: " + chatID);
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Super simple method to send a chat message to an ID. 
	 * @param chatID the id of the chat where to send the message to
	 * @param messageText the message to send
	 */
	public void sendMessageToChat(long chatID, String messageText) {
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(chatID).setText(messageText);
		try {
			sendMessage(message); // Sending our message object to user
		} catch (TelegramApiException e) {
			// TODO: iets met fout doen
			e.printStackTrace();
		}
	}
	
	
	@Override
	public String getBotUsername() {
		return this.properties.getProperty("bot_username");
	}

	@Override
	public String getBotToken() {
		return this.properties.getProperty("bot_token");
	}
}
