package cryptoBot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Deposit {
	private String command;
	private Date depositDate;
	private double depositValue;
	private String destination;
	private String description;
	
	public boolean pocParsingDeposit() {
		// voor een deposit naar bijvoorbeeld bitstamp:
		// Deposits
		// ----------------------------
		//   id (pk)
		//   date *
		//   amount
		//   destination *
		//   remarks (default null)
		
		// voor een deposit naar een wallet:
		// WalletDeposits
		// ----------------------------
		//   id (pk)
		//   date
		//   wallet_id
		//   coin_amount
		//   current_value
		//   remarks (default null)
		
		
		

		String sampleCommand = "/deposit 04-10-2017 45,00 Bitstamp Opmerking hier";
		String regex = "(?<command>^/\\w+)\\s"                      // retrieve the command
				     + "(?<date>\\d{2}\\-\\d{2}\\-\\d{4})\\s"   // retrieve the date
		             + "(?<value>\\d{1,},\\d{1,2}|\\d{1,})\\s"      // retrieve the deposit value
		             + "(?<destination>[A-Za-z0-9]+)\\s{0,}"        // retrieve the destination
		             + "(?<remarks>.*$|$)";                         // retrieve the optional remarks
		Pattern depositPattern = Pattern.compile(regex);
		
		Matcher matcher = depositPattern.matcher(sampleCommand);
		boolean success = matcher.find();
		
		String command = success ? matcher.group("command") : null;
		
		//parse the date
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy", new Locale("nl_NL"));
		Date date;
		try {
			date =      success ? format.parse(matcher.group("date")) : null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// can't parse date
			e.printStackTrace();
			return false;
		}
		
		// parse the current value
		String valueString = success ? matcher.group("value") : null;
		if(valueString.equals(null)) {
			// error, no valid value found
			// exit method
			// TODO: error loggen hier plzkthx
			return false;
		}
		
		valueString = valueString.replace(",", ".");
		double value = Double.parseDouble(valueString);
		
		// get the destination
		String destination = success ? matcher.group("destination") : null;
		
		// finally get the remarks
		String remarks = success ? matcher.group("remarks") : null;
		
		System.out.println("Command     => " + command);
		System.out.println("Date        => " + date);
		System.out.println("Value       => " + value);
		System.out.println("Destination => " + destination);
		System.out.println("Remarks     => " + remarks);
		
		// now check if the required fields aren't null
		// some fields are already checked (date and value)
		if(command.equals(null)) {
			return false;
		}
		if(destination.equals(null)) {
			return false;
		}
		// the remarks are optional
		
		return true;
		
	}
}
