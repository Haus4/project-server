package servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

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

	@Override
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
	 *      Possible Request: - Reload Sudoku & return : diff != null forwards
	 *      to jsp
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String diff = request.getParameter("diff");

		if (diff != null) {
			diff = diff.toUpperCase();
			this.loadSudokuFromDB(SudokuDifficulty.valueOf(diff));
			request.getSession().setAttribute("sudokuBean", sudoku);
			request.getServletContext().getRequestDispatcher("/sudoku.jsp").forward(request, response);
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
	 *      - Check Field & return : id != null && value != null (+ username is
	 *      empty: New Username) returns json with username + check
	 * 
	 *      - Get Highscore & return : hsQuery != null returns json with
	 *      highscore/(s) (username + points)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String sudokuId = request.getParameter("sudokuId");
		String username = request.getParameter("username");
		username = username != null ? StringEscapeUtils.unescapeEcmaScript(username) : null;

		if (username != null && !username.equals("null") && !hasUserHighscore(username, Integer.parseInt(sudokuId))) {
			int currentId = Integer.parseInt(sudokuId);
			if (sudoku.getSudokuId() != currentId) {
				loadSudokuForId(currentId);
			}

			if(username.isEmpty()){
				username = "guest" + UUID.randomUUID().toString();
			}
			request.setAttribute("username", username);
			SudokuBean bean = copyBean(sudoku);
			tempHighscores.add(new HighscoreBean(username, bean));
			createHighscoreRow(sudoku.getSudokuId(), username);
			response.setContentType("application/json");
			response.getWriter().append(
					"{ \"username\" : \"" + StringEscapeUtils.escapeJson(username) + "\" }");
			response.getWriter().flush();
			return;
		}

		if (sudokuId != null && username != null) {
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
			response.getWriter().append(
					"{ \"username\" : \"" + StringEscapeUtils.escapeJson(username) + "\", \"check\" : \"" + String.valueOf(isCorrect) + "\" }");
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
		}
	}

	private boolean checkSudokuField(String id, String value, String username) {
		String[] split = StringUtils.split(id, ".");
		if (split.length < 2)
			return false;

		int row = Integer.parseInt(split[0]);
		int col = Integer.parseInt(split[1]);
		int val = Integer.parseInt(value);

		if (row < 0 || row >= 9) {
			row = 0;
		}
		if (col < 0 || col >= 9) {
			col = 0;
		}
		if (val < 0 || val > 9) {
			val = 0;
		}

		boolean result = this.sudoku.getResultField(row, col) == val;
		int index = getHighscoreIndex(username);

		if (tempHighscores.get(index).checkHighscore(result)) {
			insertHighscore(username);
		}
		return result;
	}

	private void loadSudokuFromDB(SudokuDifficulty diff) {
		try {
			int count = this.db.executeQuery("SELECT COUNT(ID) FROM SUDOKU WHERE DIFF =" + diff.ordinal()).getInt(1);
			int index = this.rand.nextInt(++count);
			ResultSet rs = this.db.executeQuery("SELECT * FROM SUDOKU WHERE DIFF =" + diff.ordinal());

			while (index > 1) {
				rs.next();
				index--;
			}

			int[][] field = byteToIntArr(rs.getBytes("field"));
			int[][] solved = byteToIntArr(rs.getBytes("solved"));
			int id = rs.getInt("id");
			int open = rs.getInt("open");
			int diffOrd = rs.getInt("diff");

			sudoku.setDiff(SudokuDifficulty.values()[diffOrd]);
			sudoku.setSudokuId(id);
			sudoku.setField(field);
			sudoku.setSolved(solved);
			sudoku.setEmptyFields(open);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int[][] byteToIntArr(byte[] b) {
		int[][] intArr = new int[b.length / 36][b.length / 36];

		IntBuffer intBuf = ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] arr = new int[intBuf.remaining()];
		intBuf.get(arr);

		for (int i = 0; i < intArr.length; i++) {
			for (int j = 0; j < intArr[i].length; j++) {
				intArr[i][j] = arr[i * intArr[i].length + j];
			}
		}

		return intArr;
	}

	private void createHighscoreRow(int sudokuId, String username) {
		try {
			PreparedStatement ps1 = this.db.prepareStatement("SELECT COUNT(ID) FROM HIGHSCORE WHERE SUDOKUID = ? AND USERNAME = ?");
			ps1.setInt(1, sudokuId);
			ps1.setString(2, username);
			int count = ps1.executeQuery().getInt(1);
			if (count == 0){
				PreparedStatement ps = this.db
						.prepareStatement("insert into highscore(sudokuid,username,points,timestamp) values(?,?,?,?)");
				ps.setInt(1, sudokuId);
				ps.setString(2, username);
				ps.setInt(3, 0);
				ps.setString(4, new Date().toString());
				db.execute(ps);	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertHighscore(String username) {
		int points = calculatePoints(username);
		try {
			PreparedStatement ps = this.db.prepareStatement("update highscore set points = ? where username = ? and sudokuid = ?");
			ps.setInt(1, points);
			ps.setString(2, username);
			ps.setInt(3, sudoku.getSudokuId());
			db.execute(ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private int calculatePoints(String username) {
		int points = 0;
		double lossFac = 1;
		try {
			Date start = getTimeStamp(username);
			Date current = new Date();
			long seconds = Math.abs(start.getTime()-current.getTime())/1000;
			switch (sudoku.getDiff()){
				case EASY:
					if (seconds > 300) lossFac = 1 / (seconds/300);
					points = (int) Math.floor(1000 * lossFac);
					break;
				case MEDIUM:
					if (seconds > 600) lossFac = 1 / (seconds/600);
					points = (int) Math.floor(2500 * lossFac);
					break;
				case HARD:
					if (seconds > 1200) lossFac = 1 / (seconds/1200);
					points = (int) Math.floor(10000 * lossFac);
					break;
				default:
					throw new Exception("No valid Difficulty for sudoku "+sudoku.getSudokuId()+" found");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return points;
	}
	
	private Date getTimeStamp(String username) throws ParseException, SQLException {
		Date timeStamp = null;
		
		PreparedStatement ps = this.db.prepareStatement("SELECT * FROM HIGHSCORE WHERE USERNAME = ? AND SUDOKUID = ?");
		ps.setString(1, username);
		ps.setInt(2, sudoku.getSudokuId());
		ResultSet rs = ps.executeQuery();
		
		String time = rs.getString("timestamp").replace(" CEST", "");
		DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
		if (time!=null) timeStamp = format.parse(time);
		return timeStamp;
	}

	private int getHighscoreIndex(String username) {
		for (HighscoreBean hsb : tempHighscores) {
			if (hsb.getUsername() == username && hsb.getSudokuID() == sudoku.getSudokuId())
				return tempHighscores.indexOf(hsb);
		}

		return 0;
	}

	private String queryHighscoresFromDB(String query, String username) {
		String json = "";
		try {
			if (query.equalsIgnoreCase("player")) {
				PreparedStatement ps = this.db.prepareStatement("SELECT * FROM HIGHSCORE WHERE USERNAME = ? AND SUDOKUID = ?");
				ps.setString(1, username);
				ps.setInt(2, sudoku.getSudokuId());
				ResultSet rs = ps.executeQuery();
				if(!rs.isClosed()){
					json = "{ \"username\" : \"" + username + "\", \"points\" : " + rs.getInt("points") + " }";
				} else {
					json = "{\n\t\"scores\" : []\n}";
				}
			} else if (query.equalsIgnoreCase("sudoku")) {
				PreparedStatement ps = this.db
						.prepareStatement("SELECT * FROM HIGHSCORE WHERE SUDOKUID = ? ORDER BY points DESC");
				ps.setInt(1, sudoku.getSudokuId());
				ResultSet rs = ps.executeQuery();
				if(!rs.isClosed()){
					json = "{\n\t\"scores\" : [\n\t\t{ \"username\" : \"" + StringEscapeUtils.escapeJson(rs.getString("username")) + "\", \"points\" : "
							+ rs.getInt("points") + " },\n";
					for (int i = 0; i < 5; i++) {
						String oldUser = rs.getString("username");
						if (rs.next()) {
							if(oldUser.equals(rs.getString("username"))) continue;
							json += "\t\t{ \"username\" : \"" + StringEscapeUtils.escapeJson(rs.getString("username")) + "\", \"points\" : "
									+ rs.getInt("points") + " },\n";
						} else {
							break;
						}
					}
					json = json.substring(0, json.length() - 2) + "\n\t]\n}";
				} else {
					json = "{\n\t\"scores\" : []\n}";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	private boolean hasUserHighscore(String username, int sudokuId) {
		for (HighscoreBean hsb : tempHighscores) {
			if(username.equals(hsb.getUsername()) && sudokuId == hsb.getSudokuID()) {
				return true;
			}
		}
		return false;
	}

	private SudokuBean getSudokuForId(int sudokuId) {
		for (HighscoreBean hsb : tempHighscores) {
			if (hsb.getSudokuID() == sudokuId) {
				return copyBean(hsb.getSudokuBean());
			}
		}

		return null;
	}

	private void loadSudokuForId(int sudokuId) {
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
	
	private SudokuBean copyBean(SudokuBean bean){
		SudokuBean copyBean = new SudokuBean();
		copyBean.setDiff(bean.getDiff());
		copyBean.setEmptyFields(bean.getEmptyFields());
		copyBean.setField(deepCopy(bean.getField()));
		copyBean.setFormattedArr(deepCopy(bean.getFormattedArr()));
		copyBean.setSolved(deepCopy(bean.getSolved()));
		copyBean.setSudokuId(bean.getSudokuId());
		return copyBean;
	}
	
	public String[][] deepCopy(String[][] original) {
		if(original == null) return null;
		final String[][] result = new String[original.length][];
		for(int i=0; i<original.length; i++){
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}
	
	public int[][] deepCopy(int[][] original) {
		if(original == null) return null;
		final int[][] result = new int[original.length][];
		for(int i=0; i<original.length; i++){
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}

}
