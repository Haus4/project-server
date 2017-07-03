package db;

import java.sql.*;

public class SudokuDB {

	private Connection connection;
	private Statement statement;

	public SudokuDB(String file) {
		try {
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + file);
			this.statement = this.connection.createStatement();
			System.out.println("Database opened: " + file);
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
			connection.setAutoCommit(false);
			statement.executeBatch();
			connection.setAutoCommit(true);
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
