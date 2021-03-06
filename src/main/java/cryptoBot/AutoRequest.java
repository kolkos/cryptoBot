package cryptoBot;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class AutoRequest extends CryptoBot {
	
	private static final Logger LOG = LogManager.getLogger(AutoRequest.class);
	
	/**
	 * This method gets a list of the chats which need to receive the automatic update
	 * @return list of active chat ids
	 */
	private List<Long> getChatIDs(){
		LOG.trace("Entering getChatIDs()");
		List<Long> chatIDs = new ArrayList<>();
		
		// query the database to get the chat ids
		String query = "SELECT chat_id FROM chats WHERE active=1 AND receive_update=1";
		Object[] parameters = new Object[] {};
		MySQLAccess db = new MySQLAccess();
		
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			while(resultSet.next()) {
				chatIDs.add(resultSet.getLong("chat_id"));
			}
			
		} catch (Exception e) {
			LOG.fatal("Error receiving chat IDs, {}", e);
		}
		
		
		LOG.trace("Finished getChatIDs()");
		return chatIDs;
	}
	
	
	/**
	 * Method to run the automatic request.
	 */
	public void runAutomaticRequest() {
		LOG.trace("Entering runAutomaticRequest()");
		// get the chatIDS
		List<Long> chatIDs = this.getChatIDs();
		// loop through the chat IDs
		
		// first create the objects
		// generate a new request by using the CommandHandler
		CommandHandler commandHandler = new CommandHandler();
		Portfolio portfolio = new Portfolio();
		
		
		// set the name
		commandHandler.setFirstName("cryptoBot");
		
		// set the command
		commandHandler.setIncomingMessage("method=getTotalPortfolioValu");
		
		// set the telegram chat id
		commandHandler.setChatIDTelegram(0);
		
		// register the request
		commandHandler.registerRequestInDatabase();
		
		// get the request ID
		int requestID = commandHandler.getRequestID();
		
		// now generate the status message
		
		// get all the coins in the portfolio
		portfolio.setRequestID(requestID);
		
		
		String messageText = portfolio.generatePortfolioStatusMessage("lieve kijkbuiskindertjes");
		
		
		// now loop through the chat IDs
		for(Long chatID : chatIDs) {
			
			// because sending a request to a group without the bot throws an error
			// use this custom code instead
			this.sendAutomaticStatusUpdate(messageText, chatID);
			
		}
		LOG.trace("Finished runAutomaticRequest()");
		
	}
	
	/**
	 * This method actually sends the status message to the chat. It only sends a update during the update between 
	 * two specified hours
	 * @param messageText the text to send
	 * @param chatID the id of the chat to send to
	 */
	private void sendAutomaticStatusUpdate(String messageText, long chatID) {
		LOG.trace("Entering sendAutomaticStatusUpdate()");
		
		int from = 1200;
	    int to = 1300;
	    Date date = new Date();
	    Calendar c = Calendar.getInstance();
	    c.setTime(date);
	    int t = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
	    boolean isBetween = to > from && t >= from && t <= to || to < from && (t >= from || t <= to);
		
	    // if not between 1200 and 1300, exit
	    if(! isBetween) {
	    		return;
	    }
	    
		SendMessage message = new SendMessage()
				.setChatId(chatID)
				.setText(messageText);
		message.setParseMode(ParseMode.MARKDOWN);
		
		try {
			sendMessage(message); // Sending our message object to user
			LOG.trace("Message send: {}", message);
		} catch (TelegramApiException e) {
			// TODO: iets met fout doen
			//e.printStackTrace();
			LOG.warn("Error sending message, bot not in chat (I guess)");
		}
		
		LOG.trace("Finished sendAutomaticStatusUpdate()");
	}
}
