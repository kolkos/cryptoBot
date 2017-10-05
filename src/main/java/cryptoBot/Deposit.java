package cryptoBot;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class Deposit {
	private String command;
	private Date depositDate;
	private double depositValue;
	private String destination;
	private String remarks;
	private long chatID;
	private String firstName;
	private int depositID;
	
	private String telegramChatMessage;
	
	private static final Logger LOG = LogManager.getLogger(Deposit.class);
	
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getCommand() {
		return command;
	}

	public long getChatID() {
		return chatID;
	}

	public void setChatID(long chatID) {
		this.chatID = chatID;
	}

	public Date getDepositDate() {
		return depositDate;
	}

	public double getDepositValue() {
		return depositValue;
	}

	public String getDestination() {
		return destination;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getTelegramChatMessage() {
		return telegramChatMessage;
	}

	public void setTelegramChatMessage(String telegramChatMessage) {
		this.telegramChatMessage = telegramChatMessage;
	}

	/**
	 * This method parses the telegram chatmessage to values needed for registering the deposit/
	 * The command must be in the following format:
	 * \/deposit dd-MM-yyyy 00,00 
	 * @return returns false if parsing has failed, else true
	 */
	public boolean pasrseDeposit() {
		// TODO: deze meuk weghalen
		
		// voor een deposit naar een wallet:
		// WalletDeposits
		// ----------------------------
		//   id (pk)
		//   date
		//   wallet_id
		//   coin_amount
		//   current_value
		//   remarks (default null)

		LOG.trace("entering parseDeposit()");
		LOG.info("Parsing telegram command: {}", this.telegramChatMessage);
		
		String regex = "(?<command>^/\\w+)\\s"                      // retrieve the command
				     + "(?<date>\\d{2}\\-\\d{2}\\-\\d{4})\\s"       // retrieve the date
		             + "(?<value>\\d{1,},\\d{1,2}|\\d{1,})\\s"      // retrieve the deposit value
		             + "(?<destination>[A-Za-z0-9]+)\\s{0,}"        // retrieve the destination
		             + "(?<remarks>.*$|$)";                         // retrieve the optional remarks
		Pattern depositPattern = Pattern.compile(regex);
		
		Matcher matcher = depositPattern.matcher(this.telegramChatMessage);
		boolean success = matcher.find();
		
		String command = success ? matcher.group("command") : null;
		
		//parse the date
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy", new Locale("nl_NL"));
		Date date;
		try {
			date =      success ? format.parse(matcher.group("date")) : null;
		} catch (ParseException e) {
			LOG.fatal("Unable to parse date: {}", e);
			return false;
		}
		
		// parse the current value
		String valueString = success ? matcher.group("value") : null;
		if(valueString.equals(null)) {
			// error, no valid value found
			// exit method
			LOG.error("Unable to parse deposit value");
			return false;
		}
		
		valueString = valueString.replace(",", ".");
		double value = Double.parseDouble(valueString);
		
		// get the destination
		String destination = success ? matcher.group("destination") : null;
		
		// finally get the remarks
		String remarks = success ? matcher.group("remarks") : null;
		
		// now check if the required fields aren't null
		// some fields are already checked (date and value)
		if(command.equals(null)) {
			LOG.error("Unable to get command");
			return false;
		}
		if(destination.equals(null)) {
			LOG.error("Unable to get deposit destination");
			return false;
		}
		// the remarks are optional
		
		// now set the global variables
		this.command = command;
		this.depositDate = date;
		this.depositValue = value;
		this.destination = destination;
		this.remarks = remarks;
		
		// exit the method and tell everything is well
		
		LOG.trace("finished parseDeposit()");
		LOG.trace("Values: command={}, depositDate={}, depositValue={}, destination={}, remarks={}", this.command, this.depositDate, this.depositValue, this.destination, this.remarks);
		return true;
		
	}
	
	/**
	 * This method registers the deposit (if the checks are ok). By default the deposit isn't confirmed yet.
	 * The user will receive a message from the bot to confirm the deposit. 
	 * @param chatID: the ID from the chat where the command is send from (needed to send a confirmation)
	 * @param firstName: the name of the person which send the deposit date
	 * @param telegramChatMessage: the incoming message
	 */
	public void registerDeposit(long chatID, String firstName, String telegramChatMessage){
		
		// register the required information
		this.setChatID(chatID);
		this.setTelegramChatMessage(telegramChatMessage);
		this.setFirstName(firstName);
		
		
		// now try to parse the incoming chat message
		if(! this.pasrseDeposit()) {
			// parsing unsuccessful
			LOG.error("Parsing the message unsuccessful.");
			CryptoBot bot = new CryptoBot();
			String messageToSend = "Sorry, het is niet gelukt om jouw commando te verwerken."
					             + "Waarschijnlijk is het commando niet in het volgende formaat verstuurd:\n"
					             + "commando   => /deposit\n"
					             + "datum      => dd-MM-yyyy (01-01-2017)\n"
					             + "waarde     => 0,00 (45,0 of 45,00)\n"
					             + "bestemming => <Naam Bestemming> (1 woord, bijvoorbeeld Bitstamp)"
					             + "opmerking  => <Opmerking hier> (Optioneel, meerdere woorden toegestaan)";
			bot.sendStringToChat(this.chatID, messageToSend);
			// now exit the method
			return;
		}
		
		// check ok, continue
		String query = "INSERT INTO deposits (name, date, value, destination, remarks) VALUES (?, ?, ?, ?, ?)";
		
		java.sql.Date sqlDate = new java.sql.Date(this.depositDate.getTime());
		
		Object[] parameters = new Object[] {this.firstName, sqlDate, this.depositValue, this.destination, this.remarks};
		
		// register the deposit
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.fatal("Registering the deposit failed: {}", e);
		}finally {
			db.close();
		}
		
		// now get the id of this deposit
		this.getDepositID();
		
		// only continue if the id isn't 0
		if(this.depositID > 0) {
			// generate the message
			SendMessage message = this.generateConfirmationMessage();
			
			// now send the message by using the CryptoBot class
			CryptoBot bot = new CryptoBot();
			bot.sendPreparedMessageToChat(this.chatID, message);
		}
	}
	
	private void getDepositID() {
		String query = "SELECT id FROM deposits "
				+ "WHERE name = ? "
				+ "AND date = ? "
				+ "AND value = ? "
				+ "AND destination = ? "
				+ "AND confirmed = 0 "
				+ "LIMIT 1";
		java.sql.Date sqlDate = new java.sql.Date(this.depositDate.getTime());
		Object[] parameters = new Object[] {this.firstName, sqlDate, this.depositValue, this.destination};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeSelectQuery(query, parameters);
			
			ResultSet resultSet = db.getResultSet();
			if(! resultSet.next()) {
				LOG.error("No deposit found");
				this.depositID = 0;
				// close the connection
				db.close();
				return;
			}
			while(resultSet.next()) {
				this.depositID = resultSet.getInt("id");
			}
			
			
		} catch (Exception e) {
			LOG.fatal("Error getting deposit ID: {}", e);
			this.depositID = 0;
		}finally {
			db.close();
		}
	}
	
	/**
	 * Build the confirmation message. Handling this confirmation message will be done by the BotRequest class
	 * @return the confirmation message to send
	 */
	public SendMessage generateConfirmationMessage() {
		LOG.trace("entered generateConfirmationMessage()");
		
		String messageText = String.format("Hoi %s, ik heb het volgende uit jouw commando gehaald:\n", this.firstName);
		messageText += String.format("Datum storting: %t\n", this.depositDate);
		messageText += String.format("Waarde: %.2f\n", this.depositValue);
		messageText += String.format("Bestemming: %s\n", this.destination);
		messageText += String.format("Opmerking: %s\n\n", this.remarks);
		messageText += "Klopt dit?";
		
		// Create a message object object
		SendMessage message = new SendMessage()
				.setChatId(this.chatID).setText(messageText);
		
		// create the markupInline object
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		
		// create the list that contains the buttons
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		
		// create a list for a single row
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		
		// now add the objects
		// confirm the deposit
		rowInline.add(new InlineKeyboardButton().setText("Ja")
				.setCallbackData(String.format("method=confirmDeposit,depositID=%d", this.depositID)));
		
		// decline the deposit
		rowInline.add(new InlineKeyboardButton().setText("Nee")
				.setCallbackData(String.format("method=declineDeposit,depositID=%d", this.depositID)));
		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);		
		
		LOG.trace("message={}", message);
		LOG.trace("finished generateConfirmationMessage()");
		
		// return the message
		return message;
		
	}
	
	
	
}
