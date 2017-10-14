package cryptoBot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
}
