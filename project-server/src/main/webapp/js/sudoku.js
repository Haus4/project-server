var username = "";
var sudokuId = 0;
var startTime = new Date().getTime();
var diff = window.location.href.split("diff=")[1].toLowerCase();
var clock = null;
var lossTime = 0;
var lossSet = false;
$(document).ready(function () {
	getLossTime();
	sudokuId = $('#sudokuId').val();
	getUserName();
	$("td.active").click(tdClickHandler);
	clock = setInterval(updateTimer, 1000);
	setTimeout(function () { setHighscore("sudoku") }, 250);
});

function sudokuDoneHandler() {
	clearInterval(clock);

	var container = $("#container");
	container.css("min-height", container.height());
	container.find(".inner-container").slideUp(400, function () {
		var $this = $(this);
		var countdown = $this.find(".countdown");

		countdown.css({
			"top": $("#Sudoku").css("margin-top"),
			"position": "relative"
		});

		$this.empty();
		countdown.appendTo($this);

		countdown.children().eq(0).html("Sudoku gel&ouml;st in ");
		setTimeout($this.slideDown.bind($this, 400), 100);
	});
}

function getLossTime() {
	switch (diff) {
		case "easy":
			lossTime = 300;
			break;
		case "medium":
			lossTime = 600;
			break;
		case "hard":
			lossTime = 1200;
			break;
	}
}

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
			$(this).parent().removeClass("correct");
			var id = $(this).parent().get(0).id;
			sendResult(parseInt(test), id);

			$(this).parent().html(test);
			$('#inputField').remove();
		}
	}
}

function getUserName() {
	username = escape(window.prompt("Username:"));
	if (username == "null") {
		alert("Please enter a username!");
		getUserName();
	}
	startTime = new Date().getTime();
	$.ajax({
		url: window.location.href.split("/project-server")[0]
		+ "/project-server/sudoku?sudokuId=" + sudokuId
		+ "&username=" + username,
		type: 'POST',
		success: function (data) {
			username = data.username;
		}
	});
}

function sendResult(v, i) {
	$.ajax({
		url: window.location.href.split("/project-server")[0]
		+ "/project-server/sudoku?id=" + i + "&value=" + v
		+ "&username=" + username + "&sudokuId=" + sudokuId,
		type: 'POST',
		success: function (data) {
			if (data.check == "true") {
				var id = "#" + i[0] + "\\." + i[2];
				if (diff == "easy" || diff == "medium") {
					$(id).removeClass('active');
					$(id).unbind('click');
				}
				$(id).addClass("correct");

				if ($("#Sudoku .active").not(".correct").length == 0) {
					sudokuDoneHandler();
				}
			}
			username = data.username;
		}
	});
}

function setHighscore(q) {
	$.ajax({
		url: window.location.href.split("/project-server")[0]
		+ "/project-server/sudoku?sudokuId=" + sudokuId
		+ "&username=" + username
		+ "&getHS=" + q,
		type: 'POST',
		success: function (data) {
			if (q == "sudoku") {
				var array = data.scores;
				if (array.length == 0) {
					console.err("No highscore found trying again in 1 sec ...")
					setTimeout(function () { setHighscore("sudoku") }, 1000);
				}
				for (var j = 0; j < array.length; j++) {
					var username = array[j].username;
					var highscorePoints = array[j].points;
					$("#platz" + (j + 1)).find('td#score').text(highscorePoints);
					$("#platz" + (j + 1)).find('td#player').text(username);
				}
				setTimeout(function () { setHighscore("sudoku") }, 10000);
			}
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

	if (!lossSet && Math.floor(distance / 1000) > lossTime) {
		lossSet = true;
		$('.countdown').css("color", "red");
	}

	$('#timer').html(days + hours + minutes + seconds);
}
