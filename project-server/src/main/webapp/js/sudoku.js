var username = "";
var sudokuId = 0;
var startTime = new Date().getTime();
$(document).ready(function() {
	sudokuId = $('#sudokuId').val();
	$("td.active").click(tdClickHandler);
	setInterval(updateTimer, 1000);
	setHighscore("sudoku");
});

function tdClickHandler(e) {
	e.stopPropagation();

	var child = $(this).children('#inputField');
	if (child.length > 0) {
		child[0].focus();
	} else if (e.target.id != 'inputField') {
		$('#inputField').remove();

		var input = document.createElement('input');
		input.id = "inputField";
		input.maxLength = 1;
		e.target.innerHTML = "";
		$(this).append(input);
		$('#inputField').unbind('keyup');
		$('#inputField').keyup(inputKeyUpHandler);

		input.focus();
	}
}

function inputKeyUpHandler(e) {
	if (e.which == 13) // the enter key code
	{
		var test = $('#inputField').val();
		if (parseInt(test) < 10 && parseInt(test) > 0) {
			var id = $(this).parent().get(0).id;
			sendResult(parseInt(test), id);

			$(this).parent().html(test);
			$('#inputField').remove();
		}
	}
}

function sendResult(v, i) {
	$.ajax({
		url : window.location.href.split("/project-server")[0]
				+ "/project-server/sudoku?id=" + i + "&value=" + v
				+ "&username=" + username + "&sudokuId=" + sudokuId,
		type : 'POST',
		success : function(data) {
			var diff = window.location.href.split("diff=")[1].toLowerCase();
			if (data.check == "true" && (diff == "easy" || diff == "medium")) {
				var id = "#" + i[0] + "\\." + i[2];
				$(id).removeClass('active');
				$(id).unbind('click');
			}
			username = data.username;
		}
	});
}

function setHighscore(q) {
	$.ajax({
		url : window.location.href.split("/project-server")[0]
				+ "/project-server/sudoku?id=" + i + "&sudokuId=" + sudokuId
				+ "&getHS=" + q,
		type : 'POST',
		success : function(data) {
			if (q == "sudoku") {
				var array = data.scores;
				if (array.length == 0) {
					end;
				} else {
					for (var j = 1; j <= array.length; j++) {
						var username = array[j].username;
						var highscorePoints = array[j].points;
						$("#platz" + j).children().find('<td id="score"></td>').text(highscorePoints);
						$("#platz" + j).children().find('<td id="player"></td>').text(username);
					}
				}
			}

			var diff = window.location.href.split("diff=")[1].toLowerCase();
			if (data.check == "true" && (diff == "easy" || diff == "medium")) {
				var id = "#" + i[0] + "\\." + i[2];
				$(id).removeClass('active');
				$(id).unbind('click');
			}
			username = data.username;
		}
	});
}

function updateTimer() {
	var now = new Date().getTime();
	var distance = now - startTime;

	var days = Math.floor(distance / (1000 * 60 * 60 * 24));
	days = days > 0 ? days + "d " : "";
	var hours = Math.floor((distance % (1000 * 60 * 60 * 24))
			/ (1000 * 60 * 60));
	hours = hours > 0 ? hours + "h " : "";
	var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
	minutes = minutes > 0 ? minutes + "m " : "";
	var seconds = Math.floor((distance % (1000 * 60)) / 1000);
	seconds = seconds > 0 ? seconds + "s" : "";

	$('#timer').html(days + hours + minutes + seconds);
}
