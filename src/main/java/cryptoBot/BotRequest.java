package cryptoBot;

import static java.lang.Math.toIntExact;

import java.lang.reflect.Method;
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
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public long getChatID() {
		return chatID;
	}
	public void setChatID(long chatID) {
		this.chatID = chatID;
	}
	public long getMessageID() {
		return messageID;
	}
	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void registerChatMessage(long chatID, String firstName, String command) {
		this.setChatID(chatID);
		this.setFirstName(firstName);
		this.setCommand(command);
	}
	
	public void registerCallbackQuery(long chatID, long messageID, String firstName, String command) {
		this.setChatID(chatID);
		this.setMessageID(messageID);
		this.setFirstName(firstName);
		this.setCommand(command);
	}
	
	public SendMessage getBotOptions() {
		String messageText = String.format("Hoi %s, je kunt kiezen uit de volgende opties:", this.firstName);
		
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(chatID).setText(messageText);
		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Help")
				.setCallbackData("method=getHelpText"));
		
		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Waarde portfolio")
				.setCallbackData("method=getPortfolioCoins"));
		rowsInline.add(rowInline);
		
		rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Wallets opvragen")
				.setCallbackData("method=getWallets"));
		rowsInline.add(rowInline);
		
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);		
		
		return message;
	}
	
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
	
	private boolean checkCallDataComplete(List<String> requiredKeys, HashMap<String, String> callDataDetails) {
		boolean checkOK = true;
		// loop through the required keys
		for(String requiredKey: requiredKeys) {
			if(! callDataDetails.containsKey(requiredKey)) {
				checkOK = false;
			}
		}
		
		return checkOK;
	}
	
	public EditMessageText runCallbackQueryCommand() {
		HashMap<String, String> callDataDetails = this.parseCallData();
		EditMessageText message = null;
		
		if(! callDataDetails.containsKey("method")) {
			message = this.generateErrorMessage("Commando niet gevonden");
			return message;
		}

		List<String> requiredKeys = new ArrayList<>();
		
		// now get the method
		String method = callDataDetails.get("method");
		
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
				message = this.generateErrorMessage("Nog niet af!");
				
		}
		
		
		return message;
		
		
	}
	
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
	
	private String generateHelpText() {
		String messageText = "Op dit moment kun je de volgende commando's uitvoeren:\n";
		messageText += "- *bot* => open het menu\n";
		//messageText += "- /registerCoin <afkorting> <coin naam> => Registreren van een nieuwe coin\n";
		//messageText += "- /registerWallet <afkorting coin> <wallet adres> => Registreren van nieuwe wallet";
		messageText += "- /help => verkrijg deze helptekst";
		//messageText += "- /registerDeposit <wallet adres> <aantal coins> <> => Registreren van nieuwe wallet";
		
		
		return messageText;
	}
	
	private SendMessage getHelpTextSend() {
		String messageText = this.generateHelpText();
		
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(chatID).setText(messageText);
		
		return message;
	}
	
	private EditMessageText getHelpTextEdit() {
		String messageText = this.generateHelpText();
		
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		return message;
		
	}
	
	private EditMessageText generateErrorMessage(String messageText) {
		EditMessageText message = new EditMessageText()
                .setChatId(this.chatID)
                .setMessageId(toIntExact(this.messageID))
                .setText(messageText);
		
		return message;
		
	}
	
	
	
}
