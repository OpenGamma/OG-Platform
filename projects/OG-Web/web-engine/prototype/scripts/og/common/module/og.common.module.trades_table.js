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
          <tr class="og-js-attribute" style="display: none">\
            <td colspan="4" style="padding-left: 15px">\
              <table class="og-sub-list">{TBODY}</table>\
            </td>\
          </tr>\
        ',
        sub_head = '<tbody><tr><td class="og-header" colspan="2">{ATTRIBUTES}</td></tr></tbody>';
        return function (config) {
            var trades = config.trades, selector = config.selector, tbody,
                fields = ['id', 'quantity', 'counterParty', 'date'], start = '<tr><td>', end = '</td></tr>';
            if (!trades[0]) return $(selector).html(table.replace('{TBODY}', '<tr><td colspan="4">No Trades</td></tr>'));
            tbody = trades.reduce(function (acc, trade) {
                acc.push(start, fields.map(function (field) {return trade[field];}).join('</td><td>'), end);
                (function () { // attributes
                    var attr = [], attr_type, attr_obj, key;
                    if (!Object.keys(trade['attributes']).length) return;
                    for (attr_type in trade['attributes']) {
                        attr_obj = trade['attributes'][attr_type];
                        if (!Object.keys(attr_obj).length) continue;
                        for (key in attr_obj)
                            attr.push(start, key.replace(/.+~(.+)/, '$1'), ':</td><td>', attr_obj[key], end);
                        acc.push(attributes.replace('{TBODY}',
                            [sub_head.replace('{ATTRIBUTES}', attr_type.replace(/(.+)(Attributes)/, '$1 $2')),
                                '<tbody class="OG-background-01">' + attr.join('') + '</tbody>'
                            ].join('')
                        ));
                    }
                }());
                return acc;
            }, []).join('');
            $(selector).html(table.replace('{TBODY}', tbody));
            $(selector + ' .OG-table').awesometable({height: 300});
        }
    }
});