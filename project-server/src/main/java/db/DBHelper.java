package db;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/***
 * Helper class used to init Database and insert Sudokus <br/>
 * <br/>
 * not used by SudokuApplication
 * 
 * @author Mirko.Mueller
 *
 */
public class DBHelper {

	private SudokuDB db;
	private SudokuGenerator sg;

	public DBHelper() {
		this.db = new SudokuDB(
				"D:\\database.db");
		// this.db = new SudokuDB("C:\\Users\\mauri\\Desktop\\database.db");
		this.sg = new SudokuGenerator();

		createTables();

		generateSudokusForDifficulty(0, 100);
		generateSudokusForDifficulty(1, 100);
		generateSudokusForDifficulty(2, 100);

		this.db.close();
	}

	public static void main(String[] args) {
		new DBHelper();
	}

	// WARN: ONLY USE IF TABLE DOESNT EXISTS - TABLEs WILL BE DROPPED!!!!!
	private void createTables() {
		try {
			this.db.prepareStatement("drop table if exists sudoku").execute();
			this.db.prepareStatement("drop table if exists highscore").execute();
			this.db.prepareStatement("create table sudoku(" + "id integer primary key autoincrement not null unique,"
					+ " diff integer not null," + " solved blob," + " field blob," + " open integer not null)")
					.execute();
			this.db.prepareStatement("create table highscore(" + "id integer not null primary key autoincrement unique,"
					+ " sudokuid integer not null," + " username string," + " points integer not null,"
					+ " timestamp string)").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int getHolesForDifficulty(int diff) {
		int minHoles = 0;
		int maxHoles = 0;

		switch (diff) {
		case 0: // Easy
			minHoles = 35;
			maxHoles = 49;
			break;

		case 1: // Medium
			minHoles = 50;
			maxHoles = 54;
			break;

		case 2: // Hard
			minHoles = 55;
			maxHoles = 60;
			break;
		}
		return (int) (Math.random() * (maxHoles - minHoles) + minHoles);
	}

	private void generateSudokusForDifficulty(int diff, int count) {
		for (int i = 0; i < count; ++i) {
			int holes = this.getHolesForDifficulty(diff);
			this.sg.nextBoard(holes);

			int realHoles = 0;
			for (int j = 0; j < 9; ++j) {
				for (int k = 0; k < 9; ++k) {
					if (this.sg.getBoard()[j][k] == 0)
						realHoles++;
				}
			}

			insertSudoku(diff, this.sg.getSolvedBoard(), this.sg.getBoard(), realHoles);
		}
	}

	private void insertSudoku(int diff, int[][] solved, int[][] field, int open) {
		byte[] bSolved = intArrToBlob(solved);
		byte[] bField = intArrToBlob(field);
		try {
			PreparedStatement ps = this.db
					.prepareStatement("insert into sudoku(diff,solved,field,open) values(?,?,?,?)");
			ps.setInt(1, diff);
			ps.setBytes(2, bSolved);
			ps.setBytes(3, bField);
			ps.setInt(4, open);
			db.execute(ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private byte[] intArrToBlob(int[][] arr) {
		byte[] b = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for (int i = 0; i < arr.length; i++) {
				for (int j = 0; j < arr[i].length; j++) {
					dos.writeInt(arr[i][j]);
				}
			}

			b = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
}
