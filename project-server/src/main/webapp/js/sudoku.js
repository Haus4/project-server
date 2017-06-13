$(document).ready(function() {
	$("td").click(tdClickHandler);
});
	
function tdClickHandler(e){
	var input = document.createElement('input');
	input.id = "inputField";
	e.target.innerHTML = "";
	e.target.append(input);
	$('#inputField').keyup(inputKeyUpHandler);
}
function inputKeyUpHandler(e){
	var key = e.which;
	if(key == 13)  // the enter key code
	  {
		var test = $('#inputField').val();
		$(this).parent().html(test);
		$('#inputField').remove();
	  }
}