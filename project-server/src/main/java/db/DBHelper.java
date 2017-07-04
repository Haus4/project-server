package db;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/***
 * Helper class used to init Database and insert Sudokus <br/> <br/>
 * 		not used by SudokuApplication
 * 
 * @author Mirko.Mueller
 *
 */
public class DBHelper {
	
	private int[][] field = {{0,0,2,0,0,0,0,5,6},
							 {0,0,0,0,9,0,0,0,0},
							 {0,5,0,0,0,3,0,0,2},
							 {0,8,0,6,0,0,0,0,9},
							 {0,9,0,0,1,0,6,0,0},
							 {5,0,0,7,0,0,0,8,0},
							 {1,0,0,0,0,0,8,0,7},
							 {0,0,0,0,0,0,0,1,0},
							 {0,6,3,0,0,0,4,0,0}};
	
	private int[][] solved ={{9,1,4,2,8,7,3,5,6},
			 				 {6,3,2,1,9,5,7,4,8},
			 				 {7,5,8,4,6,3,1,9,2},
			 				 {3,8,1,6,4,2,5,7,9},
			 				 {2,9,7,5,1,8,6,3,4},
			 				 {5,4,6,7,3,9,2,8,1},
			 				 {1,2,9,3,5,4,8,6,7},
			 				 {4,7,5,8,2,6,9,1,3},
			 				 {8,6,3,9,7,1,4,2,5}};
	
	private int emptyFields = 36;
	
	private SudokuDB db;
	
	public DBHelper(){
		this.db = new SudokuDB("C:\\Projects\\Haus4\\projects\\project-server\\project-server\\src\\main\\webapp\\db\\database.db");
		//createTables();
		insertSudoku(2, solved, field, emptyFields);
		this.db.close();
	}
	
	public static void main(String[] args){
		new DBHelper();
	}
	
	//WARN: ONLY USE IF TABLE DOESNT EXISTS - TABLEs WILL BE DROPPED!!!!!
	private void createTables(){
		try {
			this.db.prepareStatement("drop table if exists sudoku").execute();
			this.db.prepareStatement("drop table if exists highscore").execute();
			this.db.prepareStatement("create table sudoku("
					+ "id integer primary key autoincrement not null unique,"
					+ " diff integer not null,"
					+ " solved blob,"
					+ " field blob,"
					+ " open integer not null)").execute();
			this.db.prepareStatement("create table highscore("
					+ "id integer not null primary key autoincrement unique,"
					+ " sudokuid integer not null,"
					+ " username string,"
					+ " points integer not null)").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertSudoku(int diff, int[][] solved, int[][] field, int open){
		byte[] bSolved = intArrToBlob(solved);
		byte[] bField = intArrToBlob(field);
		try {
			PreparedStatement ps = this.db.prepareStatement("insert into sudoku(diff,solved,field,open) values(?,?,?,?)");
			ps.setInt(1, diff);
			ps.setBytes(2, bSolved);
			ps.setBytes(3, bField);
			ps.setInt(4, open);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] intArrToBlob(int[][] arr){
		byte[] b = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for(int i=0; i < arr.length; i++) {
				for(int j=0; j < arr[i].length; j++){
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
