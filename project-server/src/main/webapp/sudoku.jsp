<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Sudoku</title>
	<link rel="stylesheet" href="css/style_sudoku.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<script type="text/javascript" src ="js/sudoku.js"></script>
</head>
<body>
	<h2>Sudoku</h2><br/><br/><br/>
	<jsp:useBean id="sudokuBean" class="beans.SudokuBean"></jsp:useBean>
	<table>
		<c:set var="rows" value="${ sudokuBean.getRows() }"></c:set>
		<c:set var="collumns" value="${ sudokuBean.getCollumns() }"></c:set>
		<c:set var="fieldArr" value="${ sudokuBean.getFormattedArr() }"></c:set>
		<c:forEach begin="0" end="${rows-1}" var="i">
		<tr id="${i}">
			<c:forEach begin="0" end="${collumns-1}" var="j">
			<td id="${sudokuBean.parseID(i,j)}">${ fieldArr[i][j] }</td>
			</c:forEach>
		</tr>
		</c:forEach>
	</table>
</body>
</html>
