package beans;

public class HighscoreBean {
	
	private String username;
	private int sudokuID;
	
	//temporary score
	private int fieldsCorrect = 0;
	private int fieldsToSolve;
	
	public HighscoreBean(String username, int sudokuId, int fieldsToSolve) {
		this.setUsername(username);
		this.setSudokuID(sudokuId);
		this.fieldsToSolve = fieldsToSolve;
	}
	
	public boolean checkHighscore(boolean correctField){
		boolean saveHighscore = false;
		if (correctField) fieldsCorrect++;
		if (fieldsCorrect >= fieldsToSolve) {
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

}
