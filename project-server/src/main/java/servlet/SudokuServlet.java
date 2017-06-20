package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.SudokuBean;

/**
 * Servlet implementation class SudokuServlet
 */
@WebServlet("/SudokuServlet")
public class SudokuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SudokuBean sudoku;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SudokuServlet() {
        super();
    }
    
    public void init() {
    	this.log("starting up servlet");
    	this.sudoku = new SudokuBean();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		String value = request.getParameter("value");
		if(id != null && value != null){
			boolean isCorrect = this.checkSudokuField(id, value);
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private boolean checkSudokuField(String id, String value) {
		int row = Integer.parseInt(id.split(".")[0]);
		int col = Integer.parseInt(id.split(".")[1]);
		int val = Integer.parseInt(value);
		boolean result = this.sudoku.getField(row, col) == val;
		return result;
	}

}
