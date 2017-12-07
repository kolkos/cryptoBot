package cryptoBot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.exceptions.TelegramApiException;

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
		LOG.trace("Entering registerChatMessage(): chat_id={}, firstName={}, incomingChatMessage={}", chatIDTelegram, firstName, incomingChatMessage);
		this.setChatIDTelegram(chatIDTelegram);
		this.setFirstName(firstName);
		this.setIncomingMessage(incomingChatMessage);
		LOG.trace("finished registerChatMessage()");
	}
	
	/**
	 * General method to handle the incoming commands
	 */
	public void runTextMessageCommand() {
		LOG.trace("Entering runTextMessageCommand()");
		
		// now check if the command starts with a /
		// if so, check which method has to run
		Pattern patternCommand = Pattern.compile("(?<command>^/\\w+)\\s*(?<rest>.*$)");
		Matcher matcher = patternCommand.matcher(this.getIncomingMessage());
		
		if (matcher.find()) {
			LOG.trace("Incoming chat message matches {}.", patternCommand);
			
			// register this request
			this.registerRequestInDatabase();
			
			// now get the command
			String command = matcher.group("command");
			
			LOG.info("Received command: {}", command);
			
			// check if user is JT
			if(this.getFirstName().equals("JT")) {
				this.sendStringToChat("Hoezo heb je nog Facebook?");
			}
			
			
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
				case "/bot":
				case "/menu":
					this.sendBotOptions();
					break;
//				case "/test":
//					this.testReplyKeyboard();
//					break;
//				case "/hide":
//					this.hideKeyboard();
//					break;
				case "/pic":
					this.sendTestPic();
					break;
				default:
					this.sendStringToChat("Sorry wat?");
			}
		}
		
		LOG.trace("finished runTextMessageCommand()");
	}
	
	/**
	 * This method sends the help text
	 */
	private void sendHelpText() {
		LOG.trace("Entering sendHelpText()");
		String helpText = this.generateHelpText();
		this.sendStringToChat(helpText);
		LOG.trace("Finished sendHelpText()");
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
	
	/**
	 * Test method to send a picture
	 */
	private void sendTestPic() {
		LOG.trace("Entering sendTestPic()");
		
		SendPhoto sendPhotoRequest = new SendPhoto();
		String chatID = "-236099150";
		sendPhotoRequest.setChatId(chatID);
		
		String path = "/Users/antonvanderkolk/Downloads/23-3.jpg";
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(path);
			sendPhotoRequest.setNewPhoto(path, inputStream);
			sendPhoto(sendPhotoRequest);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOG.trace("Finished sendTestPic()");
		
	}
	
	
	
}
