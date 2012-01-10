/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.trades',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        var ui = og.common.util.ui, position_json, table = '\
          <table class="OG-table">\
            <thead>\
              <tr>\
                <th><span>Trades</span></th>\
                <th>Quantity</th>\
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
        get_trades = function (handler, id) {
            og.api.rest.positions.get({dependencies: ['id', 'node'], handler: handler, id: id});
        },
        /*
         * Gets the latest trade information for the current position before putting the new trade
         */
        put_trade = function (trade) {
            var template_data = position_json.data.template_data;
            get_trades(function (result) {
                if (result.error) return alert(result.message);
                var trades = result.data.trades;
                trades.push(trade);
                og.api.rest.positions.put({
                    trades: format_trades(trades), id: template_data.object_id, quantity: template_data.quantity,
                    handler: function (result) {
                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                        og.views.positions.details(og.common.routes.current().args);
                    }
                });
            }, template_data.object_id);
        },
        /*
         * Formats arrays of trade objects for submission.
         * The object that we relieve in the response cant be sent back as is because its been formatted slightly
         * differently, this also applies for the form object for the new trade to be added
         */
        format_trades = function (trades) {
            var format_date = function (str) {return str.replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$3-$2-$1')};
            trades.map(function (trade) {
                var premium, tradeDate;
                if (trade.premium) {
                    premium = trade.premium.split(' ');
                    trade.premium = +premium[0];
                    if (premium[1]) trade.premiumCurrency = premium[1];
                } else delete trade.premium;
                if (trade.premium_date_time) {
                    premium = trade.premium_date_time.split(' ');
                    trade.premiumDate = format_date(premium[0]);
                    if (premium[1]) trade.premiumTime = premium[1];
                    if (premium[2]) trade.premiumOffset = premium[2].replace(/\((.*)\)/, '$1');
                }
                if (trade.trade_date_time) {
                    tradeDate = trade.trade_date_time.split(' ');
                    trade.tradeDate = format_date(tradeDate[0]);
                    if (tradeDate[1]) trade.tradeTime = tradeDate[1];
                    if (tradeDate[2]) {
                        trade.tradeOffset = tradeDate[2].replace(/\((.*)\)/, '$1');
                        trade.tradeOffset.toString();
                    }
                }
                if (trade.counterParty) trade.counterParty =
                    trade.counterParty.split('~')[1] || trade.counterParty;
                if (trade.quantity) trade.quantity = trade.quantity.replace(',', '');
                if (trade.currency) trade.premiumCurrency = trade.currency, delete trade.currency;
                delete trade.premium_date_time,
                delete trade.trade_date_time,
                delete trade.id;
                return trade;
            });
            return trades;
        },
        /*
         * Embeds a link that enables adding trades via a form in a dialog
         */
        add_trades_link = function (selector) {
            var link = '<a href="#" class="OG-link-add" style="position: relative; left: 2px; top: 3px;">add trade</a>';
            $(selector).append(link).find('.OG-link-add').bind('click', function (e) {
                e.preventDefault();
                ui.dialog({
                    type: 'input', title: 'Add New Trade', minWidth: 400, minHeight: 400,
                    form: {
                        module: 'og.views.forms.add-trades',
                        handlers: [{type: 'form:submit', handler: function (obj) {put_trade(obj.data);}}]
                    },
                    buttons: {'OK': function () {$(this).dialog('close').find('form').submit();}}
                })
            });
        };
        return function (config) {
            get_trades(function (result) {
                if (result.error) return alert(result.message);
                position_json = result;
                var trades = result.data.trades, selector = config.selector, tbody, has_attributes = false,
                    fields = ['id', 'quantity', 'counterParty', 'trade_date_time', 'premium', 'premium_date_time'],
                    start = '<tr><td>', end = '</td></tr>';
                if (!trades)
                    return $(selector).html(table.replace('{TBODY}', '<tr><td colspan="6">No Trades</td></tr>'));
                tbody = trades.reduce(function (acc, trade) {
                    acc.push(start, fields.map(function (field, i) {
                        var expander;
                        i === 0 ? expander = '<span class="OG-icon og-icon-expand"></span>' : expander = '';
                        return expander + (trade[field].replace(/.*~/, '')).lang();
                    }).join('</td><td>'), end);
                    (function () { // display attributes if available
                        if (!trade.attributes) return;
                        var attr, attr_type, attr_obj, key, html = [];
                        for (attr_type in trade.attributes) {
                            attr_obj = trade.attributes[attr_type], attr = [];
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
                // remove all expand links if no attributes
                if (!has_attributes) $(config.selector + ' .og-icon-expand').hide();
                // remove expand links with no attributes
                $(selector + ' .OG-table > tbody > tr').each(function () {
                    var $this = $(this);
                    if ($this.next().hasClass('og-js-attribute')) {
                        $this.find('.og-icon-expand').bind('click', function () {
                            $(this).toggleClass('og-icon-collapse').parents('tr').next().toggle();
                        });
                    } else $this.find('.og-icon-expand').css('visibility', 'hidden');
                });
                // Add trades functionality
                add_trades_link(selector);
                $(selector + ' > .OG-table > tbody > tr:not(".og-js-attribute"):last td').css('padding-bottom', '10px');
                $(selector + ' .OG-table').awesometable({height: 400});
            }, config.id);
        }
    }
});