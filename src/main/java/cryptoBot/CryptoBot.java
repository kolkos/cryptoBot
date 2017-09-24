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
				String messageText = "Iemand zei bot!";

				// get the other details
				String firstName = update.getMessage().getFrom().getFirstName();
				String lastName = update.getMessage().getFrom().getLastName();
				String username = update.getMessage().getFrom().getUserName();
				long userID = update.getMessage().getFrom().getId();

				SendMessage message = new SendMessage() // Create a message object object
						.setChatId(chatID).setText(messageText);

				InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
				List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
				List<InlineKeyboardButton> rowInline = new ArrayList<>();
				rowInline.add(new InlineKeyboardButton().setText("Huidige waarde portfolio")
						.setCallbackData("getPortfolioValueOptions"));

				// Set the keyboard to the markup
				rowsInline.add(rowInline);

				rowInline = new ArrayList<>();
				rowInline.add(new InlineKeyboardButton().setText("Huidige waarde coins")
						.setCallbackData("getCoinValueOptions"));
				rowsInline.add(rowInline);

				// Add it to the message
				markupInline.setKeyboard(rowsInline);
				message.setReplyMarkup(markupInline);

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
			
			System.out.println(messageID);

			//System.out.println(callData);
			
			String theClass = this.getClass().getName();
			try {
				this.commandToMethodInvoker(theClass, callData, new Class[] {long.class, long.class}, new Object[] {chatID, messageID});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public HashMap<String, String> parseCommand(String commandString ){
		HashMap<String, String> parameters = new HashMap<>();
		String[] keyValuePairs = commandString.split(",");
		for(String keyValuePair : keyValuePairs) {
			String[] keyValueParts = keyValuePair.split("=");
			String key = keyValueParts[0];
			String value = keyValueParts[1];
			parameters.put(key, value);
		}
		return parameters;
	}
	

	public void commandToMethodInvoker(String theClass, String theMethod, Class[] params, Object[] args) throws Exception{
		Class c = Class.forName(theClass);
		Method m = c.getDeclaredMethod(theMethod, params);
		Object i = c.newInstance();
	    Object r = m.invoke(i, args);
	}
	
	public void getPortfolioValueOptions(long chatID, long messageID) {
		System.out.println("getPortfolioValueOptions");
		System.out.format("%d%n", chatID);

		String messageText = "getPortfolioValueOptions";
		//SendMessage message = new SendMessage() // Create a message object object
		//		.setChatId(chatID).setText(messageText);

		EditMessageText message = new EditMessageText()
                .setChatId(chatID)
                .setMessageId(toIntExact(messageID))
                .setText(messageText);
		
		
		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("LTC").setCallbackData("LTC"));
		rowInline.add(new InlineKeyboardButton().setText("BTC").setCallbackData("BTC"));
		rowsInline.add(rowInline);
		
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Alles")	.setCallbackData("All"));
		rowsInline.add(rowInline);
		
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		
		try {
			editMessageText(message); // Sending our message object to user
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		
		
		return;
	}

	public void getCoinValueOptions(long chatID) {
		System.out.println("getCoinValueOptions");
		System.out.format("%d%n", chatID);
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
