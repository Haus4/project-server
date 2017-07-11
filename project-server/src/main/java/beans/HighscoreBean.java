package beans;

public class HighscoreBean {

	private String username;
	private int sudokuID;
	private SudokuBean sudokuBean;

	// temporary score
	private int fieldsCorrect = 0;
	private int fieldsToSolve;

	public HighscoreBean(String username, SudokuBean sudokuBean) {
		this.setUsername(username);
		this.setSudokuBean(sudokuBean);
		this.setSudokuID(sudokuBean.getSudokuId());
		this.fieldsToSolve = sudokuBean.getEmptyFields();
	}

	public boolean checkHighscore(boolean correctField) {
		boolean saveHighscore = false;
		if (correctField)
			fieldsCorrect++;
		if (fieldsCorrect >= fieldsToSolve) {
			// TODO: remove this line..
			System.out.println(username + " finished sudoku " + sudokuID + " with " + fieldsCorrect + "/"
					+ fieldsToSolve + " correct fields");
			saveHighscore = true;
			fieldsCorrect = 0;
			fieldsToSolve = 0;
		}
		return saveHighscore;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getSudokuID() {
		return sudokuID;
	}

	public void setSudokuID(int sudokuID) {
		this.sudokuID = sudokuID;
	}

	public SudokuBean getSudokuBean() {
		return sudokuBean;
	}

	public void setSudokuBean(SudokuBean sudokuBean) {
		this.sudokuBean = sudokuBean;
	}

}
