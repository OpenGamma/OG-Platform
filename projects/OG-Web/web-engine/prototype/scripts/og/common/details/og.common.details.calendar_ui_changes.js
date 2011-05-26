/*
 * @copyright 2009 - 2011 by OpenGamma Inc
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
                return '<select>' + html.join('') + '</select>';
            },
            update_pagination = function () {
                var year = parseFloat($('.ui-datepicker-year').html());
                $('.OG-holiday .og-js-prev').html(year - 1);
                $('.OG-holiday .og-js-next').html(year + 1);
                $('.OG-holiday .og-js-pagination select').val(year);
            };
        return calendar_ui_changes = function (dates) {
            var year = $('.ui-datepicker-year').html(),
                $prev = $('.ui-datepicker-prev').html(+year - 1).addClass('og-js-prev'),
                $next = $('.ui-datepicker-next').html(+year + 1).addClass('og-js-next'),
                onclick = $('.ui-datepicker-next').attr('onclick'), val = $(this).val(),
                diff = val < year ? year - val : val - year, months = diff * 12;
            $('.OG-holiday .og-js-pagination').css('display', 'inline').append($prev).append(build_selector(dates))
                .append($next).find('select').change(onclick).css('display', 'inline');
            $('.OG-holiday .og-js-pagination select').val(year);
            $('.og-js-pagination a').click(update_pagination);
        };
    }
});


