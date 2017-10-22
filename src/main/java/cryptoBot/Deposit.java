package cryptoBot;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class Deposit {
	private static final Logger LOG = LogManager.getLogger(Deposit.class);
	
	private int depositID;
	private Date depositDate;
	private int walletID;
	private double amount;
	private double purchaseValue;
	private String uuid;
	private String remarks;
	private String walletAddress;
	private long chatID;
	
	/**
	 * This method processes the deposit command
	 * @param chatID the ID of the chat
	 * @param depositCommand the complete command
	 */
	public void processDepositCommand(long chatID, String depositCommand) {
		LOG.trace("Entering processDepositCommand(), chatID={}, depositCommand={}", chatID, depositCommand);
		
		this.uuid = UUID.randomUUID().toString();
		
		this.chatID = chatID;
		
		// first parse the command to attributes
		if(! this.parseDepositCommand(depositCommand)) {
			// error while parsing command, exit
			LOG.error("Error found while parsing the command");
			return;
		}
				
		// now register this deposit
		this.registerDeposit();
		
		// get the deposit ID from the database
		this.depositID = this.getDepositID(this.uuid);
		
		// generate the confirmation message
		SendMessage message = this.generateConfirmationMessage();
		
		// now send the confirmation message
		this.sendMessage(message);
		
		
		LOG.trace("Finished processDepositCommand()");
	}
	
	/**
	 * Method to send an error message to the chat
	 * @param messageText the text to send to the chat
	 */
	private void sendErrorMessage(String messageText) {
		LOG.trace("Entering sendErrorMessage()");
		
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.setChatIDTelegram(this.chatID);
		// now send the message
		commandHandler.sendStringToChat(messageText);
		
		LOG.trace("Finished sendErrorMessage()");
	}
	
	/**
	 * Simple method that uses the CommandHandler.sendMessageToChat() method.
	 * @param message the prepared message to send
	 */
	private void sendMessage(SendMessage message) {
		LOG.trace("Entering sendMessage()");
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.sendMessageToChat(message);
		LOG.trace("Finished sendMessage()");
	}
	
	/**
	 * Parse the telegram command to attributes
	 * @param depositCommand
	 * @return true if successful, false if failed
	 */
	private boolean parseDepositCommand(String depositCommand) {
		LOG.trace("Entering parseDepositCommand(), depositCommand={}", depositCommand);
		/* example deposit command:
		 * 		/deposit 14-10-2017 38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L 0,01234567 12,34 voorbeeld deposit
		 */
		String regex = "(?<command>^/\\w+)\\s"                  // retrieve the command
			     + "(?<date>\\d{2}\\-\\d{2}\\-\\d{4})\\s"       // retrieve the date
			     + "(?<walletAddress>[A-Za-z0-9]*)\\s"          // retrieve the wallet
			     + "(?<amount>\\d{1,},\\d{1,8}|\\d{1,})\\s"     // retrieve the amount
	             + "(?<value>\\d{1,},\\d{1,2}|\\d{1,})\\s"      // retrieve the deposit value
	             + "(?<remarks>.*$|$)";                         // retrieve the optional remarks
		Pattern depositPattern = Pattern.compile(regex);
		
		Matcher matcher = depositPattern.matcher(depositCommand);
		boolean success = matcher.find();
		
		String command = success ? matcher.group("command") : null;
		
		//parse the date
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy", new Locale("nl_NL"));
		Date date;
		try {
			date =      success ? format.parse(matcher.group("date")) : null;
		} catch (ParseException e) {
			LOG.error("Unable to parse date: {}", e);
			this.sendErrorMessage("Sorry, ik kan de datum niet verwerken.");
			return false;
		}
		
		// parse the wallet address
		String walletAddress = success ? matcher.group("walletAddress") : null;
		
		// parse the deposited amount
		String amountString = success ? matcher.group("amount") : null;
		if(amountString == null) {
			// error, no valid value found
			// exit method
			LOG.error("Unable to parse deposit amount");
			this.sendErrorMessage("Sorry, ik kan het aantal coins niet verwerken.");
			return false;
		}
		amountString = amountString.replace(",", ".");
		double amount = Double.parseDouble(amountString);
		
		// parse the current value
		String valueString = success ? matcher.group("value") : null;
		if(valueString.equals(null)) {
			// error, no valid value found
			// exit method
			LOG.error("Unable to parse deposit value");
			this.sendErrorMessage("Sorry, ik kan de aanschafwaarde niet verwerken.");
			return false;
		}
		valueString = valueString.replace(",", ".");
		double value = Double.parseDouble(valueString);
		
		// finally get the remarks
		String remarks = success ? matcher.group("remarks") : null;
		
		// now check if the required fields aren't null
		// some fields are already checked (date, value and amount)
		if(command.equals(null)) {
			LOG.error("Unable to get command");
			this.sendErrorMessage("Sorry, ik kan het commando niet verwerken.");
			return false;
		}
		
		// now get the wallet ID
		Wallet wallet = new Wallet();
		int walletID = wallet.getWalletIDFromDB(walletAddress);
		
		// check if walletID isn't 0, else send a message and exit
		if(walletID == 0) {
			LOG.error("Wallet {} not found", walletAddress);
			this.sendErrorMessage(String.format("Sorry, ik kan de wallet '%s' niet vinden.", walletAddress));
			return false;
		}
		
		
		// now set the attributes
		this.depositDate = date;
		this.walletID = walletID;
		this.amount = amount;
		this.purchaseValue = value;
		this.remarks = remarks;
		this.walletAddress = walletAddress;
		
		LOG.trace("Finished parseDepositCommand()");
		
		return true;
	}
	
	/**
	 * This method registers the deposit into the database. By default the deposit isn't confirmed yet. The user
	 * has to click on the confirm message to confirm the deposit
	 */
	private void registerDeposit() {
		LOG.trace("Entering registerDeposit()");
		
		java.sql.Date sqlDate = new java.sql.Date(this.depositDate.getTime());
		
		String query = "INSERT INTO deposits (uuid, deposit_date, wallet_id, coin_amount, purchase_value, remarks) VALUES (?, ?, ?, ?, ?, ?)";
		Object[] parameters = new Object[] {this.uuid, sqlDate, this.walletID, this.amount, this.purchaseValue, this.remarks};
		
		MySQLAccess db = new MySQLAccess();
		try {
			db.executeUpdateQuery(query, parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.fatal("Error registering deposit. {}", e);
		} finally {
			db.close();
		}
		
		LOG.trace("Finished registerDeposit()");
	}
	
	/**
	 * Get the id of the deposit based on the generated uuid
	 * @param uuid the uuid generated for this deposit
	 * @return id of the deposit in the database
	 */
	private int getDepositID(String uuid) {
		LOG.trace("Entering getDepositID(), uuid={}", uuid);
		
		// just to be sure order the deposits descending by id and get only one id
		String query = "SELECT id FROM deposits WHERE uuid = ? ORDER BY id DESC LIMIT 1";
		Object[] parameters = new Object[] {uuid};
		MySQLAccess db = new MySQLAccess();
		
		int depositID = 0;
		
		try {
			db.executeSelectQuery(query, parameters);
			ResultSet resultSet = db.getResultSet();
			while(resultSet.next()) {
				depositID = resultSet.getInt("id");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.fatal("Error getting deposit ID. {}", e);
		} finally {
			db.close();
		}
				
		LOG.trace("Finished getDepositID()");
		return depositID;
	}
	
	private SendMessage generateConfirmationMessage() {
		LOG.trace("Entering generateConfirmationMessage()");
		/* example deposit command:
		 * 		/deposit 14-10-2017 38Ee9XUoHp6usVRDKNTdUvS1EUsca3Sb6L 0,01234567 12,34 remark
		 */
		
		String messageText = "Ik heb het volgende uit jouw commando gehaald:\n";
		messageText += String.format("Datum: `%s`\n", this.depositDate);
		messageText += String.format("Wallet: `%s`\n", this.walletAddress);
		messageText += String.format("Aantal: `%.8f`\n", this.amount);
		messageText += String.format("Aanschafwaarde: `%.2f`\n\n", this.purchaseValue);
		messageText += "Klopt dit?";
		
		// generate the message
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(this.chatID).setText(messageText);
		
		
		String commandStringPattern = "method=confirmDeposit,confirm=%d,depositID=%d";
		
		
		// now add a keyboard
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		
		// create yes button
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Ja")
				.setCallbackData(String.format(commandStringPattern, 1, this.depositID)));
				
		// create no button
		rowInline.add(new InlineKeyboardButton().setText("Nee")
				.setCallbackData(String.format(commandStringPattern, 0, this.depositID)));
		
		rowsInline.add(rowInline);
		
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		message.setParseMode(ParseMode.MARKDOWN);
		
		LOG.trace("Finished generateConfirmationMessage()");
		return message;
	}
	
	/**
	 * This method handles the confirmation of the deposit
	 * @param confirm confirm 1 = yes, 0 = no
	 * @param depositID the database ID of the deposit
	 * @throws Exception SQL error
	 */
	public void confirmDeposit(int confirm, int depositID) throws Exception {
		LOG.trace("Entering confirmDeposit(), confirm={}, depositID={}", confirm, depositID);
		
		// depending on the value of confirm, the deposit must be deleted or updated
		// form query based on thos value
		String query;
		if(confirm == 0) {
			// just to be sure, only delete if confirmed = 0
			query = "DELETE FROM deposits "
					+ "WHERE id = ? "
					+ "AND confirmed = 0";
		} else {
			query = "UPDATE deposits SET confirmed = 1 WHERE id = ?";
		}
		Object[] parameters = new Object[] {depositID};
		
		MySQLAccess db = new MySQLAccess();
				
		db.executeUpdateQuery(query, parameters);
		db.close();
		
		LOG.trace("Finished confirmDeposit()");
	}
	
}
