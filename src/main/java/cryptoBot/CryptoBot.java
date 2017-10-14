package cryptoBot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
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
	
	private static final Logger LOG = LogManager.getLogger(CryptoBot.class);
	
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
		LOG.trace("entered onUpdateReceived()");
		
		// handle incoming chat messages
		if (update.hasMessage() && update.getMessage().hasText()) {
			LOG.info("received incoming message");
			
			String incomingMessageText = update.getMessage().getText();
			long chatID = update.getMessage().getChatId();
			String firstName = update.getMessage().getFrom().getFirstName();
			
			// call the CommandHandler
			TextMessageHandler textMessageHandler = new TextMessageHandler();
			
			// now register this incoming message
			textMessageHandler.registerChatMessage(chatID, firstName, incomingMessageText);
			
			// first register this chat (if necessary)
			try {
				textMessageHandler.registerChat();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.fatal("error registering the chatID {}", e);
			}
			
			// check if the chat is allowed to send requests
			if(! textMessageHandler.checkIfChatIsAllowedToSendRequests()) {
				// now allowed, exit method
				LOG.info("Chat isn't authorized (yet).");
				return;
			}
			
			// make the message lowercase
			incomingMessageText = incomingMessageText.toLowerCase();
			//System.out.println(incomingMessageText);
			
			LOG.trace("Incoming chat message {}.", incomingMessageText);
			
			// first check if the message contains the word 'bot'
			Pattern patternBot = Pattern.compile("^(.*?)(\\bbot\\b)(.*)$");
			Matcher matcher = patternBot.matcher(incomingMessageText);
			// only do something when the word 'bot' is found
			if (matcher.find()) {
				LOG.trace("Incoming chat message matches {}.", patternBot);
												
				// rename the command
				//String command = "getBotOptions";
				
				// register this request
				textMessageHandler.registerRequestInDatabase();
				
				// now send the bot options
				textMessageHandler.sendBotOptions();
				
				//LOG.trace("message to send: {}.", message);
				
//				try {
//					sendMessage(message); // Sending our message object to user
//				} catch (TelegramApiException e) {
//					//e.printStackTrace();
//					LOG.fatal("error sending chat message {}", e);
//				}

			}
			
			// handle other commands
			// these commands start with /
			patternBot = Pattern.compile("^/.*$");
			matcher = patternBot.matcher(incomingMessageText);
			if (matcher.find()) {
				LOG.trace("Incoming chat message matches {}.", patternBot);
				
				//TODO: now let the BotRequest class deal with it

				
			}
			
		// handle incoming callback queries
		} else if (update.hasCallbackQuery()) {
			LOG.info("received incoming callback query");
			
			// call data is the command which is set to a inline keyboard button
			String callData = update.getCallbackQuery().getData();
			
			// get the message ID (the message of the inline keyboard message)
			// this ID is used to overwrite this method
			long messageID = update.getCallbackQuery().getMessage().getMessageId();
			long chatID = update.getCallbackQuery().getMessage().getChatId();
			
			// get the first name of the requester
			String firstName = update.getCallbackQuery().getFrom().getFirstName();
			
			// call the CommandHandler
			CommandHandler commandHandler = new CommandHandler();
						
			// register this request
			commandHandler.registerCallbackQuery(chatID, messageID, firstName, callData);
			
			// register this request
			commandHandler.registerRequestInDatabase();
			
			// use the runCallbackQueryCommand to handle this request
			commandHandler.runCallbackQueryCommand();

		}
		LOG.trace("finished onUpdateReceived()");
	}
	

	
	
	/**
	 * This method is called from the scheduler. This method automatically requests the current
	 * value of the portfolio. 
	 * If the current time is between FROM_HOUR and TO_HOUR this method will send a update message
	 * to the registered chats. The method checks if active and receive_update are set to 1
	 */
	public void automaticStatusUpdatePortfolio() {
		LOG.trace("entered automaticStatusUpdatePortfolio()");
		
		Request request = new Request();
		request.setRequestedCoins("all");
		request.setRequestedBy("cryptoBot");
		request.handleCoinRequest();
				
		// now check if the message needs to be send
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int hour = cal.get(Calendar.HOUR_OF_DAY);  //Get the hour from the calendar
		
		LOG.info("Run automatic task automaticStatusUpdatePortfolio() @ {}", cal.getTime());
		
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
					LOG.info("Sending a automatic generated message to {}", chatID);
					
					SendMessage message = new SendMessage() // Create a message object object
							.setChatId(chatID).setText(request.getStatusMessage());
					try {
						sendMessage(message); // Sending our message object to user
					} catch (TelegramApiException e) {
						//System.out.println("Bot 3niet toegevoegd aan chat: " + chatID);
						LOG.warn("Error sending message to (probably not in group) {}", e);
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				LOG.fatal("Error getting details from database: {}", e1);
			}finally {
				db.close();
			}
		}
		LOG.trace("finished automaticStatusUpdatePortfolio()");
	}
	
//	/**
//	 * Super simple method to send a chat message to an ID. 
//	 * @param chatID the id of the chat where to send the message to
//	 * @param messageText the message to send
//	 */
//	public void sendStringToChat(long chatID, String messageText) {
//		LOG.trace("entered sendStringToChat(), chatID={}, messageText={}", chatID, messageText);
//		
//		SendMessage message = new SendMessage() // Create a message object object
//				.setChatId(chatID).setText(messageText);
//		try {
//			sendMessage(message); // Sending our message object to user
//		} catch (TelegramApiException e) {
//			// TODO: iets met fout doen
//			//e.printStackTrace();
//			LOG.fatal("Error sending message: {}", e);
//		}
//		LOG.trace("finished sendStringToChat()");
//	}
//	
//	public void sendPreparedMessageToChat(long chatID, SendMessage message) {
//		LOG.trace("entered sendPreparedMessageToChat(), chatID={}, messageText={}", chatID, message);
//
//		try {
//			sendMessage(message); // Sending our message object to user
//		} catch (TelegramApiException e) {
//			// TODO: iets met fout doen
//			//e.printStackTrace();
//			LOG.fatal("Error sending message: {}", e);
//		}
//		LOG.trace("finished sendPreparedMessageToChat()");
//	}
	
	
	@Override
	public String getBotUsername() {
		return this.properties.getProperty("bot_username");
	}

	@Override
	public String getBotToken() {
		return this.properties.getProperty("bot_token");
	}
}
