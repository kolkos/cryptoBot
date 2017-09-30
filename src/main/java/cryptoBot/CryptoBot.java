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

import static java.lang.Math.toIntExact;

public class CryptoBot extends TelegramLongPollingBot {
	private Properties properties;
	
	public CryptoBot() {
		General general = new General();
		this.properties = general.getProperties();
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			// first check if the bot contains the word 'bot'
			String incomingMessageText = update.getMessage().getText();
			long chatID = update.getMessage().getChatId();

			//System.out.println("chatid=" + chatID);
			
			// make the message lowercase
			incomingMessageText = incomingMessageText.toLowerCase();
			//System.out.println(incomingMessageText);

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

		} else if (update.hasCallbackQuery()) {
			String callData = update.getCallbackQuery().getData();
			
			long messageID = update.getCallbackQuery().getMessage().getMessageId();
			long chatID = update.getCallbackQuery().getMessage().getChatId();
			//String firstName = update.getCallbackQuery().getMessage().getFrom().getFirstName();
			String firstName = update.getCallbackQuery().getFrom().getFirstName();
			
			BotRequest callbackRequest = new BotRequest();
			callbackRequest.registerCallbackQuery(chatID, messageID, firstName, callData);
			
//			EditMessageText message = new EditMessageText()
//	                .setChatId(chatID)
//	                .setMessageId(toIntExact(messageID))
//	                .setText(callData);
			
			EditMessageText message = callbackRequest.runCallbackQueryCommand();

			try {
				editMessageText(message);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void automaticStatusUpdatePortfolio() {
		Request request = new Request();
		request.setRequestedCoins("all");
		request.setRequestedBy("cryptoBot");
		request.handleCoinRequest();
				
		// now check if the message needs to be send
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int hour = cal.get(Calendar.HOUR_OF_DAY);  //Get the hour from the calendar
		
		System.out.println("Auto taak");
		System.out.println(cal.getTime());
		
		// Check if hour is between 12 and 13
		// if so send the message
		if(hour >= 12 && hour < 13)               
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
	
	
	@Override
	public String getBotUsername() {
		return this.properties.getProperty("bot_username");
	}

	@Override
	public String getBotToken() {
		return this.properties.getProperty("bot_token");
	}
}
