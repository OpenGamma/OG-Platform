$(function() {
	$(".datepicker").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat : 'yy-mm-dd'
	});
});

$(function() {
	$('.tsResolver input[type="text"]').change(function() {
		var tsResolver = $(this).closest('.tsResolver');
		var chbox = $(tsResolver).find('input[type="checkbox"]');

		if (!$(this).val()) {
			chbox.attr('checked', false);
		} else {
			chbox.attr('checked', true);
		}
	})
});

$(function() {
	$('.tsResolver input[type="checkbox"]').change(function() {
		var tsResolver = $(this).closest('.tsResolver');
		var inputTxt = $(tsResolver).find('input[type="text"]');

		if (!$(this).attr('checked')) {
			inputTxt.val('');
		}
	})
});

$(function() {
  $('.hide').click(function(event) {
    $("#"+event.target.name).toggle();
  })
});