/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.trades',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        var ui = og.common.util.ui, table = '\
          <table class="OG-table">\
            <thead>\
              <tr>\
                <th><span>Trades</span></th>\
                <th>Quality</th>\
                <th>Counterparty</th>\
                <th>Trade Date / Time</th>\
                <th>Premium</th>\
                <th>Premium Date / Time</th>\
              </tr>\
            </thead>\
            <tbody>{TBODY}</tbody>\
          </table>\
        ',
        attributes = '\
          <tr class="og-js-attribute" style="display: none">\
            <td colspan="6" style="padding-left: 15px">\
              <table class="og-sub-list">{TBODY}</table>\
            </td>\
          </tr>\
        ',
        sub_head = '<tbody><tr><td class="og-header" colspan="2">{ATTRIBUTES}</td></tr></tbody>',
        disable_expand = function (config) {$(config.selector + ' .og-icon-expand').hide()};
        return function (config) {
            var handler = function (result) {
                var trades = result.data.trades, selector = config.selector, tbody, has_attributes = false,
                    fields = ['id', 'quantity', 'counterParty', 'date_time', 'premium', 'premium_date_time'],
                    start = '<tr><td>', end = '</td></tr>';
                if (!trades[0])
                    return $(selector).html(table.replace('{TBODY}', '<tr><td colspan="6">No Trades</td></tr>'));
                tbody = trades.reduce(function (acc, trade) {
                    acc.push(start, fields.map(function (field, i) {
                        var expander;
                        i === 0 ? expander = '<span class="OG-icon og-icon-expand"></span>' : expander = '';
                        return expander + (trade[field].replace(/.*~/, '')).lang();
                    }).join('</td><td>'), end);
                    (function () { // display attributes if available
                        var attr, attr_type, attr_obj, key, html = [], keys = Object.keys, trd_attr = trade.attributes;
                        if (!keys(trd_attr['dealAttributes']).length && !keys(trd_attr['userAttributes']).length) return;
                        for (attr_type in trade.attributes) {
                            attr_obj = trade.attributes[attr_type], attr = [];
                            if (!Object.keys(attr_obj).length) continue;
                            for (key in attr_obj) attr.push(
                                start, key.replace(/.+~(.+)/, '$1').lang(), ':</td><td>', attr_obj[key].lang(), end
                            );
                            html.push(
                                sub_head.replace('{ATTRIBUTES}', attr_type.lang()) +
                                '<tbody class="OG-background-01">' + attr.join('') + '</tbody>'
                            );
                        }
                        acc.push(attributes.replace('{TBODY}', html.join('')));
                        if (html.length) has_attributes = true;
                    }());
                    return acc;
                }, []).join('');
                $(selector).html(table.replace('{TBODY}', tbody)).hide().fadeIn();
                if (!has_attributes) disable_expand(config); // remove all expand links if no attributes
                $(selector + ' .OG-table > tbody > tr').each(function () { // remove expand links with no attributes
                    var $this = $(this);
                    if ($this.next().hasClass('og-js-attribute')) {
                        $this.find('.og-icon-expand').bind('click', function () {
                            $(this).toggleClass('og-icon-collapse').parents('tr').next().toggle();
                        });
                    } else $this.find('.og-icon-expand').css('visibility', 'hidden');
                });
//                $(selector).append(
//                    '<a href="#" class="OG-link-add" style="position: relative; left: 2px; top: 3px;">add new trade</a>'
//                ).bind('click', function (e) {
//                    e.preventDefault();
//                });
                $(selector + ' > .OG-table > tbody > tr:not(".og-js-attribute"):last td').css('padding-bottom', '10px');
                $(selector + ' .OG-table').awesometable({height: 400});
            };
            og.api.rest.positions.get({
                dependencies: ['id', 'node'],
                handler: function (result) {
                    if (result.error) return alert(result.message);
                    handler(result);
                },
                id: config.id,
                cache_for: 10000,
                loading: function () {}
            });
        }
    }
});