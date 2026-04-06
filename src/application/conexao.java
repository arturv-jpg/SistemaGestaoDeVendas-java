package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class conexao {
	private static final String URL="jdbc:mysql://localhost:3307/sistema";
	private static final String USER="root";
	private static final String PASS="";
	
	public static Connection getConnection() throws SQLException{
		return DriverManager.getConnection(URL,USER,PASS);
	}
	
}
