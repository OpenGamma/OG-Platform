/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.module.trade_table',
    dependencies: [],
    obj: function () {
        var table = '\
          <table class="OG-table">\
            <thead>\
              <tr>\
                <th><span>Trades</span></th>\
                <th>Quality</th>\
                <th>Counterparty</th>\
                <th>Date</th>\
              </tr>\
            </thead>\
            <tbody>{TBODY}</tbody>\
          </table>\
        ',
        attributes = '\
          <tr class="og-js-attribute">\
            <td colspan="4">\
              <table class="og-sub-list">{TBODY}</table>\
            </td>\
          </tr>\
        ';
        return function (config) {
            var trades = config.trades, selector = config.selector, tbody,
                fields = ['id', 'quantity', 'counterParty', 'date'], start = '<tr><td>', end = '</td></tr>';
            // if (!trades[0]) return $(selector).html('<tr><td colspan="4">No Trades</td></tr>');
            tbody = trades.reduce(function (acc, trade) {
                acc.push(start, fields.map(function (field) {return trade[field];}).join('</td><td>'), end);
                (function () { // attributes
                    var attr = [], key,
                        sub_head = '<tbody><tr><td class="og-header" colspan="2">Attributes</td></tr></tbody>';
                    if (!Object.keys(trade['attributes']).length) return;
                    for (key in trade['attributes']) attr.push(start, key, '</td><td>', trade['attributes'][key], end);
                    acc.push(attributes.replace('{TBODY}',
                        [sub_head, '<tbody class="OG-background-01">' + attr.join('') + '</tbody>'].join('')
                    ));
                }());
                return acc;
            }, []).join('');
            $(selector).html(table.replace('{TBODY}', tbody));
            $(selector + ' .OG-table').awesometable({height: 300});
        }
    }
});