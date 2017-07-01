package servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

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
		String id = request.getParameter("id");
		String value = request.getParameter("value");
		if (id != null && value != null) {
			boolean isCorrect = this.checkSudokuField(id, value);
			response.getWriter().append(String.valueOf(isCorrect));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	private boolean checkSudokuField(String id, String value) {
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
		if (val < 0 || val >= 9){
			val = 0;
		}
		boolean result = this.sudoku.getResultField(row, col) == val;
		return result;
	}
	
	private void loadSudokuFromDB(SudokuDifficulty diff) {
		try {
			int count = this.db.executeQuery("SELECT COUNT(ID) FROM SUDOKU WHERE DIFF =" + diff.ordinal()).getInt(1);
			int index = this.rand.nextInt(++count);
			ResultSet rs = this.db.executeQuery("SELECT * FROM SUDOKU WHERE DIFF =" + diff.ordinal()); //+ " AND ID = " + index);
			int[][] field = byteToIntArr(rs.getBytes("field"));
			int[][] solved = byteToIntArr(rs.getBytes("solved"));
			this.sudoku.setField(field);
			this.sudoku.setSolved(solved);
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

}
