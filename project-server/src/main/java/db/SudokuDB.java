package db;

import java.sql.*;

public class SudokuDB {

	private Connection connection;
	private Statement statement;
	private String backupFilePath;

	public SudokuDB(String file) {
		this.backupFilePath = "D:\\database.db"; //file
		try {
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			this.statement = this.connection.createStatement();
			statement.executeUpdate("restore from " + backupFilePath);
			System.out.println("Database opened");
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public PreparedStatement prepareStatement(String statement) {
		try {
			return this.connection.prepareStatement(statement);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void execute(PreparedStatement statement) {
		try {
			statement.execute();
			this.saveBackup();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet executeQuery(String statement) {
		try {
			return this.statement.executeQuery(statement);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void executeUpdate(String statement) {
		try {
			this.statement.executeUpdate(statement);
			this.saveBackup();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveBackup() {
		try {
			statement.executeUpdate("backup to "+ backupFilePath);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.saveBackup();
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
