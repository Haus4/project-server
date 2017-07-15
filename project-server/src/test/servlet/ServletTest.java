package servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import enums.SudokuDifficulty;

public class ServletTest extends Mockito{
	
	public HttpServletRequest request;
	public HttpServletResponse response;
	public HttpSession session;
	
	public SudokuServlet servlet;
	public ServletContext context;
	public RequestDispatcher reqDisp;
	
	@Before
	public void precondition() {
		request = mock(HttpServletRequest.class);       
        response = mock(HttpServletResponse.class); 
        session = mock(HttpSession.class);
        context = mock(ServletContext.class);
        reqDisp = mock(RequestDispatcher.class);
        
        servlet = new SudokuServlet(){
        	public ServletContext getServletContext() {
                return context;
            }
        	
        	public void log(String msg) {
        		System.out.println(msg);
        	}
        };
	}

    @Test
    public void testSudokuLoading() throws Exception {
        testLoadSudoku("easy");
        testLoadSudoku("medium");
        testLoadSudoku("hard");
    }
    
    public void testLoadSudoku(String diff) throws Exception {
    	
        PrintWriter writer = new PrintWriter("C:\\Projects\\Haus4\\projects\\project-server\\tests\\"+diff+"Sudoku.txt");

        when(request.getParameter("diff")).thenReturn(diff);
        when(response.getWriter()).thenReturn(writer);
        when(servlet.getServletContext().getRealPath("/db/database.db")).thenReturn("C:\\Projects\\Haus4\\projects\\project-server\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\project-server\\db\\database.db");
        when(request.getServletContext()).thenReturn(context);
        when(request.getSession()).thenReturn(session);
        when(context.getRequestDispatcher("/sudoku.jsp")).thenReturn(reqDisp);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(reqDisp, atLeast(1)).forward(request, response);
        verify(request, atLeast(1)).getParameter("diff"); // verify diff was called
        
        writer.flush();
        servlet.destroy();
        
    }
    
    @Test
    public void testValidUsername() throws IOException, ServletException {
    	
    	PrintWriter writer = new PrintWriter("C:\\Projects\\Haus4\\projects\\project-server\\tests\\testUsername.txt");
    	when(servlet.getServletContext().getRealPath("/db/database.db")).thenReturn("C:\\Projects\\Haus4\\projects\\project-server\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\project-server\\db\\database.db");
        when(request.getParameter("diff")).thenReturn("easy");
    	when(request.getParameter("sudokuId")).thenReturn("1");
    	when(request.getParameter("username")).thenReturn("testUser");
    	when(response.getWriter()).thenReturn(writer);
    	
    	servlet.init();
        servlet.doPost(request, response);
        
        verify(request, atLeast(1)).setAttribute("username", "testUser");
        
        writer.flush();
        servlet.destroy();
    }
    
    @Test
    public void testEmptyUsername() throws IOException, ServletException {
    	
    	PrintWriter writer = new PrintWriter("C:\\Projects\\Haus4\\projects\\project-server\\tests\\emptyUsername.txt");
    	when(servlet.getServletContext().getRealPath("/db/database.db")).thenReturn("C:\\Projects\\Haus4\\projects\\project-server\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\project-server\\db\\database.db");
        when(request.getParameter("diff")).thenReturn("easy");
    	when(request.getParameter("username")).thenReturn(null);
    	when(request.getParameter("sudokuId")).thenReturn("1");
    	when(response.getWriter()).thenReturn(writer);
    	
    	servlet.init();
        servlet.doPost(request, response);
        
        verify(response, atLeast(1)).sendError(500, "No sudokuID given ...");
    }
    
    @Test
    public void testHighscore() throws IOException, ServletException {
    	
    	String file = "C:\\Projects\\Haus4\\projects\\project-server\\tests\\testHighscore.txt";
    	PrintWriter writer = new PrintWriter(file);
    	
    	when(servlet.getServletContext().getRealPath("/db/database.db")).thenReturn("C:\\Projects\\Haus4\\projects\\project-server\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\project-server\\db\\database.db");
        when(request.getParameter("diff")).thenReturn("easy");
    	when(request.getParameter("username")).thenReturn("testUser");
    	when(request.getParameter("sudokuId")).thenReturn("1");
    	when(request.getParameter("getHS")).thenReturn("sudoku");
    	when(response.getWriter()).thenReturn(writer);
    	
    	servlet.init();
        servlet.doPost(request, response);
        writer.append("\n");
        
        when(request.getParameter("getHS")).thenReturn("player");
        servlet.doPost(request, response);
        
        verify(request, atLeast(1)).setAttribute("username", "testUser");
    	
        writer.flush();
        servlet.destroy();
    }
    
    @Test
    public void testFieldCheck() throws IOException, ServletException {

    	int value = 7;
    	String file = "C:\\Projects\\Haus4\\projects\\project-server\\tests\\testFieldCheck.txt";
    	PrintWriter writer = new PrintWriter(file);
    	
    	when(servlet.getServletContext().getRealPath("/db/database.db")).thenReturn("C:\\Projects\\Haus4\\projects\\project-server\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\project-server\\db\\database.db");
        when(request.getParameter("diff")).thenReturn("easy");
    	when(request.getParameter("username")).thenReturn("testUser");
    	when(request.getParameter("sudokuId")).thenReturn("1");
    	when(request.getParameter("id")).thenReturn("0.0");
    	when(request.getParameter("value")).thenReturn(value+"");
    	when(response.getWriter()).thenReturn(writer);
    	
    	servlet.init();
        servlet.doPost(request, response);
        
    	writer.flush();
        servlet.destroy();
    }
}