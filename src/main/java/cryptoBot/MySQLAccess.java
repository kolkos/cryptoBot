package cryptoBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private List<HashMap<String, String>> results;
	
	private static final Logger LOG = LogManager.getLogger(MySQLAccess.class);
	
	/**
	 * Connect to the database
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void connectDatabase() throws SQLException, ClassNotFoundException {
		LOG.trace("Entering connectDatabase()");
		
		Class.forName("com.mysql.jdbc.Driver");
		
		String user = "cryptoBot";
		String pass = "cryptoBot";
		String db = "cryptoBot";
		
		String url = "jdbc:mysql://localhost/" + db + "?user=" + user + "&password=" + pass;
		
		LOG.trace("mysql url: {}", url);
		
		connect = DriverManager.getConnection(url);
		LOG.trace("Finished connectDatabase()");
	}
	
	/**
	 * Method to run a select query. This method runs prepared statements. The results are set to the resultSet variable
	 * @param query the SQL query to run
	 * @param parameters options for the prepared statement
	 * @throws Exception
	 */
	public void executeSelectQuery(String query, Object[] parameters) throws Exception {
		LOG.trace("Entering executeSelectQuery()");
		
		// String query, String[] parameters
		this.connectDatabase();
		
		try {
			LOG.trace("Executing prepared statement: {}", query);
			LOG.trace("Prepared parameters: {}", parameters);
			
			preparedStatement = connect.prepareStatement(query);
			
			int i = 1;
			for(Object x : parameters) {
				//System.out.println(x.toString());
				//preparedStatement.setString(i, x.toString());
				LOG.trace("Setting parameter {} => {}", i, x);
				preparedStatement.setObject(i, x);
				i++;
			}
			
			resultSet = preparedStatement.executeQuery();
			
			LOG.info("Query ({}) Executed OK", query);
		} catch (Exception e) {
			throw e;
        }
		LOG.trace("Finished executeSelectQuery()");
	}
	
	/**
	 * Method to execute a update/insert/delete statement to the database
	 * @param query prepared query to run
	 * @param parameters values for the prepared query
	 * @throws Exception
	 */
	public void executeUpdateQuery(String query, Object[] parameters) throws Exception {
		LOG.trace("Entering executeUpdateQuery()");
		
		//connect
		this.connectDatabase();
		
		try {
			LOG.trace("Executing prepared statement: {}", query);
			LOG.trace("Prepared parameters: {}", parameters);
			
			preparedStatement = connect.prepareStatement(query);
			
			int i = 1;
			for(Object x : parameters) {
				//System.out.println(x.toString());
				LOG.trace("Setting parameter {} => {}", i, x);
				preparedStatement.setString(i, x.toString());
				i++;
			}
			
			// just execute
			preparedStatement.executeUpdate();
			LOG.info("Query ({}) Executed OK", query);
		} catch (Exception e) {
        		throw e;
        }
		LOG.trace("Finished executeUpdateQuery()");
	}
	
	/**
	 * Method to return the current resultset
	 * @return resultset with (or without) values of the selecct query
	 */
	public ResultSet getResultSet() {
		return this.resultSet;
	}

	/**
	 * Close the current call
	 */
	public void close() {
		LOG.trace("Entering close()");
		try {
			if (resultSet != null) {
				LOG.trace("Closing resultSet");
				resultSet.close();
			}

			if (statement != null) {
				LOG.trace("Closing statement");
				statement.close();
			}

			if (connect != null) {
				LOG.trace("Closing connection");
				connect.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.fatal("Error closing: {}", e);
		}
		LOG.trace("Entering close()");
	}
	
	
	
}
