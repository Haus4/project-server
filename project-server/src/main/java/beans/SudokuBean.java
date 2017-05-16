package beans;

import enums.SudokuDifficulty;

public class SudokuBean {
	
	private final int rows = 9;
	private final int collumns = 9;
	private String[][] formattedArr = new String[9][9];
	private int[][] field = {{0,0,9,0,2,0,0,1,0},
							 {2,0,5,3,0,0,0,0,4},
							 {0,0,0,8,0,9,5,0,2},
							 {0,1,8,4,6,3,2,5,0},
							 {0,0,3,7,9,5,4,0,0},
							 {5,7,4,0,1,0,6,9,3},
							 {0,0,2,9,8,0,0,7,0},
							 {8,0,6,1,5,0,3,2,0},
							 {0,9,7,6,3,0,0,4,0}};
	private SudokuDifficulty diff = SudokuDifficulty.EASY;
	private String diffString = "easy";
	
	public void updateFormattedArr() {
		for(int i=0; i<field.length; i++){
			for(int j=0; j<field[i].length; j++){
				formattedArr[i][j] = field[i][j] == 0 ? " " : field[i][j]+"";
			}
		}
	}
	
	public String[][] getFormattedArr() {
		return formattedArr;
	}
	public void setFormattedArr(String[][] formattedArr) {
		this.formattedArr = formattedArr;
	}
	public int[][] getField() {
		return field;
	}
	public void setField(int[][] field) {
		this.field = field;
	}
	public SudokuDifficulty getDiff() {
		return diff;
	}
	public void setDiff(SudokuDifficulty diff) {
		this.diff = diff;
	}
	public String getDiffString() {
		return diffString;
	}
	public void setDiffString(String diffString) {
		this.diffString = diffString;
	}

	public int getRows() {
		return rows;
	}
	
	public String parseID(int a, int b){
		return a + "." + b;
	}

	public int getCollumns() {
		return collumns;
	}

}
