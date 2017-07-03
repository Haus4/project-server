var username = "";
$(document).ready(function () {
	$("td.active").click(tdClickHandler);
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
	if (e.which == 13)  // the enter key code
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
	console.log({
		id: i,
		value: v
	});
	$.ajax({
		url: "http://localhost:8080/project-server/sudoku?id=" + i + "&value=" + v+ "&username=" + username,
		success: function (data) {
			console.log(data);
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
