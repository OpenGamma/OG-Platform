/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.details.batch_functions',
    dependencies: [],
    obj: function () {
        var batch_functions,
            rows = function (empty, columns, selector, json) {
                if (!json[0]) return $(selector).html(empty);
                $(selector).html(json.map(function (val) {
                    return '<tr><td>' + val.split('|').slice(0, columns).join('</td><td>') + '</td></tr>';
                }).join(''));
            };
        return batch_functions = {
            results: rows.partial('<tr><td colspan="5">No results</td></tr>', 5),
            errors: rows.partial('<tr><td colspan="7">No errors</td></tr>', 7)
        };
    }
});