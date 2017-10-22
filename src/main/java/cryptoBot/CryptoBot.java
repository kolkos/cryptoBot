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
		LOG.trace("Entering onUpdateReceived()");
		
		// handle incoming chat messages
		if (update.hasMessage() && update.getMessage().hasText()) {
			LOG.info("received incoming message");
			
			String incomingMessageText = update.getMessage().getText();
			long chatID = update.getMessage().getChatId();
			String firstName = update.getMessage().getFrom().getFirstName();
			
			// call the CommandHandler
			TextMessageHandler textMessageHandler = new TextMessageHandler();
			
			// make the message lower case
			incomingMessageText = incomingMessageText.toLowerCase();
			//System.out.println(incomingMessageText);
			
			LOG.trace("incomingMessageText={}.", incomingMessageText);
			
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
			
			// now use the TextMessageHandler
			textMessageHandler.runTextMessageCommand();
			
		// handle incoming callback queries
		} else if (update.hasCallbackQuery()) {
			LOG.info("received incoming callback query");
			
			// call data is the command which is set to a inline keyboard button
			String callData = update.getCallbackQuery().getData();
			LOG.info("callData={}", callData);
			
			// get the message ID (the message of the inline keyboard message)
			// this ID is used to overwrite this method
			long messageID = update.getCallbackQuery().getMessage().getMessageId();
			long chatID = update.getCallbackQuery().getMessage().getChatId();
			
			// get the first name of the requester
			String firstName = update.getCallbackQuery().getFrom().getFirstName();
			
			// call the CommandHandler
			CallbackQueryHandler callbackQueryHandler = new CallbackQueryHandler();
						
			// register this request
			callbackQueryHandler.registerCallbackQuery(chatID, messageID, firstName, callData);
			
			// register this request
			callbackQueryHandler.registerRequestInDatabase();
			
			// use the runCallbackQueryCommand to handle this request
			callbackQueryHandler.runCallbackQueryCommand();

		}
		LOG.trace("Finished onUpdateReceived()");
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
