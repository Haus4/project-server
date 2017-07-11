package beans;

public class HighscoreBean {

	private String username;
	private SudokuBean sudokuBean;

	// temporary score
	private int fieldsCorrect = 0;

	public HighscoreBean(String username, SudokuBean sudokuBean) {
		this.setUsername(username);
		this.setSudokuBean(sudokuBean);
	}

	public boolean checkHighscore(boolean correctField) {
		boolean saveHighscore = false;
		if (correctField)
			fieldsCorrect++;
		if (fieldsCorrect >= sudokuBean.getEmptyFields()) {
			// TODO: remove this line..
			System.out.println(username + " finished sudoku " + sudokuBean.getSudokuId() + " with " + fieldsCorrect + "/"
					+ sudokuBean.getEmptyFields() + " correct fields");
			saveHighscore = true;
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
		return sudokuBean.getSudokuId();
	}

	public SudokuBean getSudokuBean() {
		return sudokuBean;
	}

	public void setSudokuBean(SudokuBean sudokuBean) {
		this.sudokuBean = sudokuBean;
	}

}
