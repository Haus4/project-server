$(document).ready(function() {
	$("td[class='active']").click(tdClickHandler);
});
	
function tdClickHandler(e){
	e.stopPropagation()
	if (e.target.id != 'inputField') {
		$('#inputField').remove();
		var input = document.createElement('input');
		input.id = "inputField";
		e.target.innerHTML = "";
		$(this).append(input);
		$('#inputField').unbind('keyup');
		$('#inputField').keyup(inputKeyUpHandler);
	}
}
function inputKeyUpHandler(e){
	var key = e.which;
	if(key == 13)  // the enter key code
	  {
		var test = $('#inputField').val();
		if(parseInt(test) < 10 && parseInt(test) > 0 ){
			var id = $(this).parent().get(0).id;
			sendResult(parseInt(test), id);
			$(this).parent().html(test);
			$('#inputField').remove();
		} else {
			writeToMessageBoard(test + " is not a valid number...");
		}
	  }
}
function writeToMessageBoard(m){
	$('#messageBoard').append(document.createTextNode( "\n"+m ));
	$('#messageBoard').scrollTop($('#messageBoard')[0].scrollHeight);
}
function sendResult(v, i){
	console.log({
		id: i,
		value: v
	});
}
