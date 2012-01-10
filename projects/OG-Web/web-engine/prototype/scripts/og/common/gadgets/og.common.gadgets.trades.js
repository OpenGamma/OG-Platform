/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.trades',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        var ui = og.common.util.ui, template_data, table = '\
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
        add_trade = function (trade) {
            get_trades(function (result) {
                if (result.error) return alert(result.message);
                var trades = result.data.trades || [];
                trades.push(trade);
                put_trades(format_trades(trades));
            }, template_data.object_id);
        },
        /*
         * Gets the latest trade information for the current position before deleting a trade
         */
        delete_trade = function (trade_id) {
            get_trades(function (result) {
                if (result.error) return alert(result.message);
                var trades = result.data.trades;
                trades.forEach(function (trade, i) {
                    if (trade_id === trade.id.split('~')[1]) {trades.splice(i, 1);}
                });
                ui.dialog({
                    type: 'confirm',
                    title: 'Delete trade?',
                    message: 'Are you sure you want to permanently delete trade ' +
                        '<strong style="white-space: nowrap">' + trade_id + '</strong>?',
                    buttons: {'Delete': function () {
                        put_trades(format_trades(trades));
                        $(this).dialog('close');
                    }}
                });
            }, template_data.object_id);
        },
        /*
         * Update trades
         */
        put_trades = function (trades) {
            og.api.rest.positions.put({
                trades: trades,
                id: template_data.object_id,
                quantity: template_data.quantity,
                handler: function (result) {
                    if (result.error) return ui.dialog({type: 'error', message: result.message});
                    og.views.positions.details(og.common.routes.current().args);
                }
            });
        },
        /*
         * Formats arrays of trade objects for submission.
         * The object that we receive in the response can't be sent back as is because it's been formatted slightly
         * differently, this also applies for the form object for the new trade to be added
         */
        format_trades = function (trades) {
            var format_date = function (str) {return str.replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$3-$2-$1')};
            trades.map(function (trade) {
                var premium, tradeDate;
                if (trade.premium) {
                    premium = trade.premium.toString().split(' ');
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
            $(selector).append('<a href="#" class="OG-link-add">add trade</a>').find('.OG-link-add').css({
                'position': 'relative', 'left': '2px', 'top': '3px', 'float': 'left'
            }).bind('click', function (e) {
                e.preventDefault();
                ui.dialog({
                    type: 'input', title: 'Add New Trade', minWidth: 400, minHeight: 400,
                    form: {
                        module: 'og.views.forms.add-trades',
                        handlers: [{type: 'form:submit', handler: function (obj) {add_trade(obj.data);}}]
                    },
                        buttons: {'OK': function () {$(this).dialog('close').find('form').submit();}}
                })
            });
        };
        return function (config) {
            get_trades(function (result) {
                if (result.error) return alert(result.message);
                template_data = result.data.template_data;
                var trades = result.data.trades, selector = config.selector, tbody, has_attributes = false,
                    fields = ['id', 'quantity', 'counterParty', 'trade_date_time', 'premium', 'premium_date_time'];
                if (!trades) return $(selector).html(table.replace('{TBODY}',
                    '<tr><td colspan="6">No Trades</td></tr>')), add_trades_link(selector);
                tbody = trades.reduce(function (acc, trade) {
                    acc.push('<tr class="og-row"><td>', fields.map(function (field, i) {
                        var expander;
                        i === 0 ? expander = '<span class="OG-icon og-icon-expand"></span>' : expander = '';
                        return expander + (trade[field].replace(/.*~/, '')).lang();
                    }).join('</td><td>'), '</td></tr>');
                    /*
                     * display trade attributes if available
                     */
                    (function () {
                        if (!trade.attributes) return;
                        var attr, attr_type, attr_obj, key, html = [];
                        for (attr_type in trade.attributes) {
                            attr_obj = trade.attributes[attr_type], attr = [];
                            for (key in attr_obj) attr.push(
                                '<tr><td>', key.replace(/.+~(.+)/, '$1').lang(),
                                ':</td><td>', attr_obj[key].lang(), '</td></tr>'
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
                /*
                 * Remove expand links when no trade attributes are available
                 */
                if (!has_attributes) $(config.selector + ' .og-icon-expand').hide();
                $(selector + ' .OG-table > tbody > tr').each(function () {
                    var $this = $(this);
                    if ($this.next().hasClass('og-js-attribute')) {
                        $this.find('.og-icon-expand').bind('click', function () {
                            $(this).toggleClass('og-icon-collapse').parents('tr').next().toggle();
                        });
                    } else $this.find('.og-icon-expand').css('visibility', 'hidden');
                });
                add_trades_link(selector);
                $(selector + ' > .OG-table > tbody > tr:not(".og-js-attribute"):last td').css('padding-bottom', '10px');
                $(selector + ' .OG-table').awesometable({height: 400});
                /*
                 * Enable delete trade
                 */
                (function () {
                    var swap_css = function (elm, css) {
                        $(elm).find('td').css(css);
                        if ($(elm).next().hasClass('og-js-attribute')) {
                            $(elm).next().find('> td').css(css);
                        }
                    };
                    $(selector + ' .og-row').hover(
                        function () {
                            swap_css(this, {'background-color': '#d7e7f2', 'cursor': 'default'});
                            $(this).find('td:last-child').append('<div class="og-del"></div>').find('.og-del')
                                .click(function () {
                                    delete_trade($(this).parents('tr').find('td:first-child').text());
                                });
                        },
                        function () {
                            swap_css(this, {'background-color': '#ecf5fa'});
                            $(this).find('.og-del').remove();
                        }
                    )
                }());
            }, config.id);
        }
    }
});