package servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import beans.HighscoreBean;
import beans.SudokuBean;
import db.SudokuDB;
import enums.SudokuDifficulty;

/**
 * Servlet implementation class SudokuServlet
 */
@WebServlet("/sudoku")
public class SudokuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Random rand;
	private SudokuBean sudoku;
	private SudokuDB db;
	private List<HighscoreBean> tempHighscores = new ArrayList<HighscoreBean>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SudokuServlet() {
		super();
	}

	public void init() {
		this.rand = new Random();
		this.db = new SudokuDB(this.getServletContext().getRealPath("/db/database.db"));
		this.log("starting up servlet -- connecting to db ...");
		this.sudoku = new SudokuBean();
	}

	@Override
	public void destroy() {
		this.db.close();
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *      
	 *      Possible Request:
	 *      	- Reload Sudoku & return : diff != null
	 *      		forwards to jsp
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String diff = request.getParameter("diff");
		if(diff != null) {
			diff = diff.toUpperCase();
			this.sudoku.updateFormattedArr();
			this.loadSudokuFromDB(SudokuDifficulty.valueOf(diff));
			request.getSession().setAttribute("sudokuBean", sudoku);
			request.getServletContext()
			.getRequestDispatcher("/sudoku.jsp")
			.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *      
	 *      Possible Requests (worked through in this order):
	 *      
	 *      ------- SUDOKU IS RELOADED BY ID (THROUGH DB OR TEMPHS) --------
	 *      
	 *      	- Check Field & return : id != null && value != null
	 *      	(+ username is empty: New Username)
	 *      		returns json with username + check
	 *      	
	 *      	- Get Highscore & return : hsQuery != null
	 *      		returns json with highscore/(s) (username + points)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String sudokuId = request.getParameter("sudokuId");
		String username = request.getParameter("username");
		if(username != null && username.isEmpty()){
			int currentId = Integer.parseInt(sudokuId);
			if(sudoku.getSudokuId() != currentId){
				loadSudokuForId(currentId);
			}
			username = "guest"+UUID.randomUUID().toString();
			request.setAttribute("username", username);
			tempHighscores.add(new HighscoreBean(username, sudoku));
			createHighscoreRow(sudoku.getSudokuId(), username);
		}
		if(sudokuId != null && username != null && !username.isEmpty()){
			SudokuBean newSudoku = getSudokuForId(Integer.parseInt(sudokuId));
			sudoku = newSudoku != null ? newSudoku : sudoku;
		} else {
			response.sendError(500, "No sudokuID given ...");
		}
		String id = request.getParameter("id");
		String value = request.getParameter("value");
		if (id != null && value != null) {
			boolean isCorrect = this.checkSudokuField(id, value, username);
			response.setContentType("application/json");
			response.getWriter().append("{ \"username\" : \""+ username+"\", \"check\" : \"" + String.valueOf(isCorrect)+"\" }");
			response.getWriter().flush();
			return;
		}
		boolean getHighscore = request.getParameter("getHS") != null ? true : false;
		if (getHighscore) {
			String query = request.getParameter("getHS");
			String json = queryHighscoresFromDB(query.toLowerCase(), username);
			response.setContentType("application/json");
			response.getWriter().append(json);
			response.getWriter().flush();
			return;
		}
	}

	private boolean checkSudokuField(String id, String value, String username) {
		String[] split = StringUtils.split(id, ".");
		int row = Integer.parseInt(split[0]);
		int col = Integer.parseInt(split[1]);
		int val = Integer.parseInt(value);
		if (row < 0 || row >= 9){
			row = 0;
		}
		if (col < 0 || col >= 9){
			col = 0;
		}
		if (val < 0 || val > 9){
			val = 0;
		}
		boolean result = this.sudoku.getResultField(row, col) == val;
		int index = getHighscoreIndex(username);
		boolean saveHighscore = tempHighscores.get(index).checkHighscore(result);
		if(saveHighscore) insertHighscore(100, username);
		return result;
	}
	
	private void loadSudokuFromDB(SudokuDifficulty diff) {
		try {
			int count = this.db.executeQuery("SELECT COUNT(ID) FROM SUDOKU WHERE DIFF =" + diff.ordinal()).getInt(1);
			int index = this.rand.nextInt(++count);
			ResultSet rs = this.db.executeQuery("SELECT * FROM SUDOKU WHERE DIFF =" + diff.ordinal());
			while(index > 1){
				rs.next();
				index--;
			}
			int[][] field = byteToIntArr(rs.getBytes("field"));
			int[][] solved = byteToIntArr(rs.getBytes("solved"));
			int id = rs.getInt("id");
			int open = rs.getInt("open");
			sudoku.setSudokuId(id);
			sudoku.setField(field);
			sudoku.setSolved(solved);
			sudoku.setEmptyFields(open);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private int[][] byteToIntArr(byte[] b) {
		int[][] intArr = new int[b.length/36][b.length/36];
		IntBuffer intBuf = ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] arr = new int[intBuf.remaining()];
		intBuf.get(arr);
		for(int i=0; i<intArr.length; i++) {
			for(int j=0; j<intArr[i].length; j++){
				intArr[i][j] = arr[i*intArr[i].length + j];
			}
		}
		return intArr;
	}
	
	private void createHighscoreRow(int sudokuId, String username) {
		try {
			PreparedStatement ps = this.db.prepareStatement("insert into highscore(sudokuid,username,points) values(?,?,?)");
			ps.setInt(1, sudokuId);
			ps.setString(2, username);
			ps.setInt(3, 0);
			ps.execute();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	private void insertHighscore(int points, String username) {
		try {
			PreparedStatement ps = this.db.prepareStatement("update highscore set points = ? where username = ?");
			ps.setInt(1, points);
			ps.setString(2, username);
			ps.execute();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	private int getHighscoreIndex(String username) {
		int index = 0;
		for(HighscoreBean hsb : tempHighscores){
			if(hsb.getUsername() == username) index = tempHighscores.indexOf(hsb);
		}
		return index;
	}
	
	private String queryHighscoresFromDB(String query, String username) {
		String json = "";
		try {
			if (query.equalsIgnoreCase("player")) {
				PreparedStatement ps = this.db.prepareStatement("SELECT * FROM HIGHSCORE WHERE USERNAME = ?");
				ps.setString(1, username);
				ResultSet rs = ps.executeQuery();
				json = "{ \"username\" : \""+ username + "\", \"points\" : " + rs.getInt("points") + " }";
			} else if (query.equalsIgnoreCase("sudoku")) {
				PreparedStatement ps = this.db.prepareStatement("SELECT * FROM HIGHSCORE WHERE SUDOKUID = ? ORDER BY points DESC");
				ps.setInt(1, sudoku.getSudokuId());
				ResultSet rs = ps.executeQuery();
				json = "{\n\t\"scores\" : [\n\t\t{ \"username\" : \""+ rs.getString("username") + "\", \"points\" : " + rs.getInt("points") + " },\n";
				for(int i=0; i<4; i++){
					ResultSet old = rs;
					if(rs.next() && old != rs){
						json += "\t\t{ \"username\" : \""+ rs.getString("username") + "\", \"points\" : " + rs.getInt("points") + " },\n";
					} else {
						break;
					}
				}
				json = json.substring(0, json.length() - 2) + "\n\t]\n}";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	private SudokuBean getSudokuForId(int sudokuId) {
		SudokuBean sb = null;
		for(HighscoreBean hsb : tempHighscores){
			if(hsb.getSudokuID() == sudokuId) sb = hsb.getSudokuBean();
		}
		return sb;
	}
	
	private void loadSudokuForId(int sudokuId){
		try {
			PreparedStatement ps = this.db.prepareStatement("SELECT * FROM SUDOKU WHERE ID = ?");	
			ps.setInt(1, sudokuId);
			ResultSet rs = ps.executeQuery();
			int[][] field = byteToIntArr(rs.getBytes("field"));
			int[][] solved = byteToIntArr(rs.getBytes("solved"));
			int id = rs.getInt("id");
			int open = rs.getInt("open");
			sudoku.setSudokuId(id);
			sudoku.setField(field);
			sudoku.setSolved(solved);
			sudoku.setEmptyFields(open);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
