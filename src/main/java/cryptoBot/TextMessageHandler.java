package cryptoBot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

public class TextMessageHandler extends CommandHandler {
	
	// attributes specific for text message:
	private static final Logger LOG = LogManager.getLogger(TextMessageHandler.class);
	
	
	
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
	
	public void runTextMessageCommand() {
		LOG.trace("Entered runTextMessageCommand()");
		
		// first check if the message contains the word 'bot'
		Pattern patternBot = Pattern.compile("^(.*?)(\\bbot\\b)(.*)$");
		Matcher matcher = patternBot.matcher(this.getIncomingMessage());
		
		// only do something when the word 'bot' is found
		if (matcher.find()) {
			LOG.trace("Incoming chat message matches {}.", patternBot);
			// register this request
			this.registerRequestInDatabase();
			
			// now send the bot options
			this.sendBotOptions();
			
			// handling the bot command is complete
			LOG.trace("finished runTextMessageCommand()");
			return;
		}
		
		// now check if the command starts with a /
		// if so, check which method has to run
		Pattern patternCommand = Pattern.compile("(?<command>^/\\w+)\\s*(?<rest>.*$)");
		matcher = patternCommand.matcher(this.getIncomingMessage());
		
		if (matcher.find()) {
			LOG.trace("Incoming chat message matches {}.", patternCommand);
			
			// register this request
			this.registerRequestInDatabase();
			
			// now get the command
			String command = matcher.group("command");
			
			LOG.info("Received command: {}", command);
			
			// select the correct method based on the command
			switch(command) {
				case "/deposit":
					// the deposit command is found, call this class
					Deposit deposit = new Deposit();
					deposit.processDepositCommand(this.getChatIDTelegram(), this.getIncomingMessage());
					break;
				case "/help":
					this.sendHelpText();
					break;
				case "/status":
					this.sendStringToChat("Ja ik ben er nog?");
					break;
//				case "/test":
//					this.testReplyKeyboard();
//					break;
//				case "/hide":
//					this.hideKeyboard();
//					break;
				default:
					this.sendStringToChat("Sorry wat?");
			}
		}
		
		LOG.trace("finished runTextMessageCommand()");
	}
	
	private void sendHelpText() {
		String helpText = this.generateHelpText();
		this.sendStringToChat(helpText);
	}
	
//	private void testReplyKeyboard() {
//		SendMessage message = new SendMessage() // Create a message object object
//                .setChatId(this.getChatIDTelegram())
//                .setText("Test keyboard");
//		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//		List<KeyboardRow> keyboard = new ArrayList<>();
//		KeyboardRow row = new KeyboardRow();
//		
//		row.add("/help");
//		row.add("Knopje 2");
//		row.add("Knopje 3");
//		
//		keyboard.add(row);
//		
//		keyboardMarkup.setKeyboard(keyboard);
//		
//		message.setReplyMarkup(keyboardMarkup);
//		
//		this.sendMessageToChat(message);
//	}
//	
//	public void hideKeyboard() {
//		SendMessage msg = new SendMessage()
//                .setChatId(this.getChatIDTelegram())
//                .setText("Keyboard hidden");
//        ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
//        msg.setReplyMarkup(keyboardMarkup);
//        
//        this.sendMessageToChat(msg);
//	}
	
}
