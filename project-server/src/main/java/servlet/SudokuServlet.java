package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import beans.SudokuBean;
import db.SudokuDB;

/**
 * Servlet implementation class SudokuServlet
 */
@WebServlet("/SudokuServlet")
public class SudokuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SudokuBean sudoku;
	private SudokuDB db;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SudokuServlet() {
		super();
	}

	public void init() {
		this.log("starting up servlet");
		//TODO: load Sudoku from Bean here
		this.sudoku = new SudokuBean();
		this.db = new SudokuDB(this.getServletContext().getRealPath("/db/database.db"));
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

}
