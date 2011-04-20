/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.details.position_functions',
    dependencies: [],
    obj: function () {
        var position_functions,
            render_main = function (selector, json) {
                $(selector).html([
                    '<td class="og-security"><a href=#/securities/', json.security.uniqueId, '/type=',
                    json.security.security_type, '>', json.security.name, '</a></td>', '<td>',
                    json.security.security_type, '</td><td><strong class="og-quantity" data-og-editable="quantity">',
                    json.templateData.QUANTITY, '</strong></td>'
                ].join(''));
            },
            render_identifiers = function (selector, json) {
                $(selector).html(json.reduce(function (acc, val) {
                    acc.push(val.scheme, ': ', val.value, '<br />');
                    return acc
                }, []).join(''));
            },
            render_trade_rows = function (selector, json) {
                if (!json[0]) return $(selector).html('<tr><td colspan="4">No Trades</td></tr>');
                var fields = ['id', 'quantity', 'counterParty', 'date'], start = '<tr><td>', end = '</td></tr>';
                $(selector).html(json.reduce(function (acc, trade) {
                    acc.push(start, fields.map(function (field) {return trade[field];}).join('</td><td>'), end);
                    return acc;
                }, []).join(''));
            };
        return position_functions = {
            render_main: render_main,
            render_identifiers: render_identifiers,
            render_trade_rows: render_trade_rows
        };

    }
});