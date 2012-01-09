/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Delete old next and prev buttons and add in custom ones
 * TODO: make select box work
 */
$.register_module({
    name: 'og.common.details.calendar_ui_changes',
    dependencies: [],
    obj: function () {
        var calendar_ui_changes,
            build_selector = function (dates) {
                var html = [], i;
                for (i in dates) if (dates.hasOwnProperty(i)) html.push('<option value="', i, '">', i, '</option>');
                return '<div class="og-pagination og-js-pagination"><select>' + html.join('') + '</select></div>';
            },
            update_pagination = function () {
                var year = +$('.ui-datepicker-year').html();
                if ($(this).is('.ui-datepicker-next')) ++year;
                if ($(this).is('.ui-datepicker-prev')) --year;
                $('.OG-holiday .og-js-pagination select').val(year);
            };
        return calendar_ui_changes = function (dates) {
            var year = +$('.ui-datepicker-year').html();
            $('.ui-datepicker-next, .ui-datepicker-prev').live('mouseup', update_pagination);
            $('.OG-holiday .og-js-calendar').prepend(build_selector(dates))
                .find('select').change(function () {
                    var year = +$('.ui-datepicker-year').html(), val = +$(this).val();
                    (new Function($('.ui-datepicker-next').attr('onclick').replace('+12', (val - year) * 12)))();
                    update_pagination();
                });
            $('.OG-holiday .og-js-pagination select').val(year);
        };
    }
});


