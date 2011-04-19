/**
 * @copyright 2009 - 2010 by OpenGamma Inc
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
            render_timeseries_table = function (selector, json, handler) {
                var CHUNK = 500, length = json.data.length,$parent = $(selector).html([
                        '<table class="OG-table-style-01"><thead><tr><td>', json.fieldLabels[0], '</td><td>',
                        json.fieldLabels[1], '</td></tr></thead><tbody></tbody></table>'
                    ].join('')).find('tbody'),
                    render = function (start, end) {
                        if (start >= length) return handler();
                        var str = json.data.slice(start, end).reduce(function (acc, val) {
                            var date = new Date(val[0]);
                            acc.push(
                                '<tr><td>', date.getDay(), '/', date.getMonth(), '/', date.getFullYear(), '</td>',
                                '<td>', val[1], '</td></tr>'
                            );
                            return acc;
                        }, []).join('');
                        $parent.append(str);
                        setTimeout(render.partial(end, end + CHUNK), 0);
                    };
                render(0, CHUNK);
            },
            render_timeseries_identifiers = function (selector, json) {
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
            }

        return timeseries_functions = {
            render_timeseries_table: render_timeseries_table,
            render_timeseries_identifiers: render_timeseries_identifiers
        };

    }
});