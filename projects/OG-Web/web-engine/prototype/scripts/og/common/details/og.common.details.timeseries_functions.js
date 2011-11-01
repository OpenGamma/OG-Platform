/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.details.timeseries_functions',
    dependencies: [],
    obj: function () {
        var timeseries_functions,
            /**
             * TODO: create generic date transformer
             * @param {String} selector
             * @param {json} data
             */
            render_identifiers = function (selector, json) {
                var html = [];
                json.forEach(function (datum) {
                    var date_text = '', start_date = datum.date.start, end_date = datum.date.end;
                    date_text = [end_date && (start_date || '?') || start_date, end_date].filter(Boolean).join(' - ');
                    html.push(
                        '<div><div>', datum.scheme.replace('_', ' '), ':</div><div><strong>', datum.value,
                        '</strong><span>', (date_text && '(' + date_text + ')'), '</span></div></div>'
                    );
                });
                $(selector).html(html.join(''));
            };
        return timeseries_functions = {
            render_identifiers: render_identifiers
        };

    }
});