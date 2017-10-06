package cryptoBot;

import static java.lang.Math.toIntExact;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	
	private static final Logger LOG = LogManager.getLogger(BotRequest.class);
	
	
	
	public void runChatMessageCommand() {
		// extract the command from the chat message
		String regex = "(?<command>^/\\w+)\\s.*$";
		Pattern depositPattern = Pattern.compile(regex);
		
		Matcher matcher = depositPattern.matcher(this.command);
		boolean success = matcher.find();
		
		String botCommand = success ? matcher.group("command") : null;
		
		LOG.info("found command {} in chat message.", botCommand);
		
		// call the correct method depending on the given method
		switch(botCommand) {
			case "/deposit":
				// deposit command found
				// The Deposit class will handle this request
				Deposit deposit = new Deposit();
				deposit.registerDeposit(chatID, firstName, this.command);
				break;
			default:
				// if the method is not declared (yet) generate a error message
				//message = this.generateErrorMessage("Ik kan nog niets met dit commando!");
				
		}
		
		// now create a 
	}
	
	
}
