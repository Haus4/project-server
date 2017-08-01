package db;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import beans.SudokuBean;

@RunWith(JUnit4.class)
public class DBTest {
	
	public SudokuDB db;
	public String file = "C:\\Projects\\Haus4\\projects\\project-server\\project-server\\src\\main\\webapp\\db\\database.db";
	public SudokuBean testBean;	
	
	@Before
	public void precondition() throws SQLException{
		testBean = new SudokuBean();
		db = new SudokuDB(file);
		db.prepareStatement("drop table if exists sudoku").execute();
		db.prepareStatement("drop table if exists highscore").execute();
		db.prepareStatement("create table sudoku(" + "id integer primary key autoincrement not null unique,"
				+ " diff integer not null," + " solved blob," + " field blob," + " open integer not null)")
				.execute();
		db.prepareStatement("create table highscore(" + "id integer not null primary key autoincrement unique,"
				+ " sudokuid integer not null," + " username string," + " points integer not null,"
				+ " timestamp string)").execute();
		insertSudoku(testBean.getDiff().ordinal(), testBean.getSolved(), testBean.getField(), testBean.getEmptyFields());

		PreparedStatement ps = db.prepareStatement("insert into highscore(sudokuid,username,points,timestamp) values(?,?,?,?)");
		ps.setInt(1, 1);
		ps.setString(2, "testUser");
		ps.setInt(3, 500);
		ps.setString(4, new Date(0).toString());
		db.execute(ps);
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
	
	@Test
	public void dbHasValidFormat() throws SQLException {
		
		//checking Sudoku table
		ResultSet rs = db.executeQuery("SELECT * FROM SUDOKU WHERE ID=1");
		assertEquals(rs.findColumn("id"), 1);
		assertEquals(rs.findColumn("diff"), 2);
		assertEquals(rs.findColumn("solved"), 3);
		assertEquals(rs.findColumn("field"), 4);
		assertEquals(rs.findColumn("open"), 5);
		
		//checking Highscore table
		ResultSet rs2 = db.executeQuery("SELECT * FROM HIGHSCORE WHERE ID=1");
		assertEquals(rs.findColumn("id"), 1);
		assertEquals(rs.findColumn("sudokuid"), 2);
		assertEquals(rs.findColumn("username"), 3);
		assertEquals(rs.findColumn("points"), 4);
		assertEquals(rs.findColumn("timestamp"), 5);
	}
	
	@Test
	public void readFromSudoku() throws SQLException {
		ResultSet rs = db.executeQuery("SELECT * FROM SUDOKU WHERE ID=1");
		
		testBean = new SudokuBean();
		byte[] solved = intArrToBlob(testBean.getSolved());
		byte[] field = intArrToBlob(testBean.getField());
		
		assertEquals(rs.getInt("id"), 1);
		assertEquals(rs.getInt("diff"), testBean.getDiff().ordinal());
		assertThat(rs.getBytes("solved"), equalTo(solved));
		assertThat(rs.getBytes("field"), equalTo(field));
		assertEquals(rs.getInt("open"), testBean.getEmptyFields());
	}

	@Test
	public void readFromHighscore() throws SQLException {
		ResultSet rs = db.executeQuery("SELECT * FROM HIGHSCORE WHERE ID=1");
		
		assertEquals(rs.getInt("id"), 1);
		assertEquals(rs.getInt("sudokuId"), 1);
		assertEquals(rs.getString("username"), "testUser");
		assertEquals(rs.getInt("points"), 500);
		assertEquals(rs.getString("timestamp"), new Date(0).toString());
	}
}
