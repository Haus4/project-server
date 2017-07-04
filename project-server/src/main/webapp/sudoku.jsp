<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href="https://fonts.googleapis.com/css?family=Open+Sans"
	rel="stylesheet">
<link href="css/style.css" rel="stylesheet">
<title>Sudoku</title>
<link rel="stylesheet" href="css/style_sudoku.css">
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/sudoku.js"></script>
</head>

<body>
	<div class="header">
		<h1>
			<a href="index.html">Sudoku</a>
		</h1>
	</div>
	<nav>
		<div class="container">
			<ul>
				<li><a href="anleitung.html">Anleitung</a></li>
				<li><a href="kontakt.html">Kontakt</a></li>
				<li>
					<div class="dropdown">
						Jetzt spielen
						<ul class="dropdown-items">
							<li><a href="sudoku?diff=easy">Einfach</a></li>
							<li><a href="sudoku?diff=medium">Mittel</a></li>
							<li><a href="sudoku?diff=hard">Schwer</a></li>
						</ul>
					</div>
				</li>
			</ul>
		</div>
	</nav>
	<div class="container">
		<jsp:useBean id="sudokuBean" scope="session" class="beans.SudokuBean"></jsp:useBean>
		<table>
			<c:set var="rows" value="${ sudokuBean.getRows() }"></c:set>
			<c:set var="collumns" value="${ sudokuBean.getCollumns() }"></c:set>
			<c:set var="fieldArr" value="${ sudokuBean.getFormattedArr() }"></c:set>
			<c:forEach begin="0" end="${rows-1}" var="i">
				<tr id="${i}">
					<c:forEach begin="0" end="${collumns-1}" var="j">
						<c:set var="active" value=""></c:set>
						<c:if test="${fieldArr[i][j] == ' '}">
							<c:set var="active" value="active"></c:set>
						</c:if>
						<td id="${sudokuBean.parseID(i,j)}" class="${active} r${i}c${j}">${ fieldArr[i][j] }</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
	<input type="hidden" id="sudokuId"
		value="${ sudokuBean.getSudokuId() }" />
</body>

<footer>
	<div class="container">
		<ul>
			<li><a href="impressum.html">Impressum</a></li>
		</ul>
	</div>
</footer>

</html>