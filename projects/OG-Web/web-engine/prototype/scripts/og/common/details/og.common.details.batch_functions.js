/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.details.batch_functions',
    dependencies: [],
    obj: function () {
        var batch_functions,
            rows = function (empty, columns, selector, json) {
                if (!json[0]) return $(selector).html(empty);
                $(selector).html(json.map(function (val, i) {
                    val = i+1 + '|' + val;
                    return '<tr><td>' + val.split('|').slice(0, columns).join('</td><td>') + '</td></tr>';
                }).join(''));
            };
        return batch_functions = {
            results: rows.partial('<tr><td colspan="6">No results</td></tr>', 6),
            errors: rows.partial('<tr><td colspan="8">No errors</td></tr>', 8)
        };
    }
});