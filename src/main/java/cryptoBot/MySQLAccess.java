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

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private List<HashMap<String, String>> results;
	
	
	public void connectDatabase() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		
		String user = "cryptoBot";
		String pass = "cryptoBot";
		String db = "cryptoBot";
		
		String url = "jdbc:mysql://localhost/" + db + "?user=" + user + "&password=" + pass;
		
		connect = DriverManager.getConnection(url);
	}
	
	
	public void executeSelectQuery(String query, Object[] parameters) throws Exception {
		// String query, String[] parameters
		this.connectDatabase();
		
		try {
			preparedStatement = connect.prepareStatement(query);
			
			int i = 1;
			for(Object x : parameters) {
				//System.out.println(x.toString());
				//preparedStatement.setString(i, x.toString());
				preparedStatement.setObject(i, x);
				i++;
			}
			
			resultSet = preparedStatement.executeQuery();
		} catch (Exception e) {
        		throw e;
        }
	}
	
	public void executeUpdateQuery(String query, Object[] parameters) throws Exception {
		//connect
		this.connectDatabase();
		
		try {
			preparedStatement = connect.prepareStatement(query);
			
			int i = 1;
			for(Object x : parameters) {
				System.out.println(x.toString());
				preparedStatement.setString(i, x.toString());
				i++;
			}
			
			// just execute
			preparedStatement.executeUpdate();
		} catch (Exception e) {
        		throw e;
        }
		
	}
	
	public ResultSet getResultSet() {
		return this.resultSet;
	}

	// You need to close the resultSet
    public void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {
        		e.printStackTrace();
        }
    }
	
	
	
}
