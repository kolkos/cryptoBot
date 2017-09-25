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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;

import java.util.prefs.*;
import java.util.regex.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Math.toIntExact;

public class CryptoBot extends TelegramLongPollingBot {

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			// first check if the bot contains the word 'bot'
			String incomingMessageText = update.getMessage().getText();
			long chatID = update.getMessage().getChatId();

			// make the message lowercase
			incomingMessageText.toLowerCase();

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
			String firstName = update.getCallbackQuery().getMessage().getFrom().getFirstName();
			
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
	
	

	



	@Override
	public String getBotUsername() {
		// TODO
		return "geenGezeikIedereenRijkBot";
	}

	@Override
	public String getBotToken() {
		// TODO

		return "429491716:AAHJIRsPvRkRzpYRIdznxZEXgIJtYZm77M0";
	}
}
