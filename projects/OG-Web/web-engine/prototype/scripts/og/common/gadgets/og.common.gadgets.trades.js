/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.trades',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        var ui = og.common.util.ui, api = og.api,
            template_data, original_config_object,
            dependencies = ['id', 'node'],
            html = {}, action = {},
            load, reload, attach_calendar, attach_trades_link, format_trades,
            form_create, form_save, generate_form_function, populate_form_fields;
        /*
         * Helper functions
         */
        generate_form_function = function (load_handler) {
            return function (css_class) {
                $(css_class).html('Loading form...');
                var form = new og.common.util.ui.Form({
                    selector: css_class, data: {}, module: 'og.views.forms.add-trades',
                    handlers: [{type: 'form:load', handler: function () {load_handler()}}]
                });
                form.children = [new form.Field({
                    module: 'og.views.forms.currency',
                    generator: function (handler, template) {handler(template);}
                })];
                form.dom();
            }
        };
        form_create = function () {
            var obj = {};
            $(this).find('[name]').each(function (i, elm) {obj[$(elm).attr('name')] = $(elm).val();});
            action.add(obj);
            $(this).dialog('close');
        };
        form_save = function (trade_id) {
            var obj = {};
            $(this).find('[name]').each(function (i, elm) {obj[$(elm).attr('name')] = $(elm).val();});
            action.replace(obj, trade_id);
            $(this).dialog('close');
        };
        attach_calendar = function () {
            $('.OG-js-datetimepicker').datetimepicker({
                firstDay: 1, showTimezone: true, dateFormat: 'yy-mm-dd',timeFormat: 'hh:mm ttz'
            });
            $('.OG-js-add-trades .og-inline-form').click(function (e) {
                e.preventDefault();
                $(this).prev().find('input').datetimepicker('setDate', new Date());
            });
        };
        attach_trades_link = function (selector) {
            $(selector).append('<a href="#" class="OG-link-add">add trade</a>').find('.OG-link-add').css({
                'position': 'relative', 'left': '2px', 'top': '3px', 'float': 'left'
            }).bind('click', function (e) {
                e.preventDefault();
                ui.dialog({
                    type: 'input', title: 'Add New Trade', minWidth: 400, minHeight: 400,
                    form: generate_form_function(function () {attach_calendar()}),
                    buttons: {
                        'Create': form_create,
                        'Cancel': function () {$(this).dialog('close');}
                    }
                })
            });
        };
        populate_form_fields = function (trade_obj) {
            $('.OG-js-add-trades [name]').each(function (i, val) {
                // special case 'premium' as there are two fields for the one value
                var attribute = $(val).attr('name'), value = trade_obj['premium'].split(' ');
                if (attribute === 'premium') {
                    trade_obj.premium = value[0];
                    trade_obj.currency = value[1];
                }
                $(val).val(trade_obj[attribute]);
            });
        };
        /*
         * Formats arrays of trade objects for submission.
         * The object that we receive in the response can't be sent back as is because it's been formatted slightly
         * differently, this also applies for the form object for the new trade to be added
         */
        format_trades = function (trades) {
            trades.map(function (trade) {
                var premium, tradeDate;
                if (trade.premium) {
                    premium = trade.premium.toString().split(' ');
                    trade.premium = premium[0].replace(/[,.]/g, '');
                    if (premium[1]) trade.premiumCurrency = premium[1];
                } else delete trade.premium;
                if (trade.premium_date_time) {
                    premium = trade.premium_date_time.split(' ');
                    trade.premiumDate = premium[0];
                    if (premium[1]) trade.premiumTime = premium[1];
                    if (premium[2]) trade.premiumOffset = premium[2].replace(/\((.*)\)/, '$1');
                }
                if (trade.trade_date_time) {
                    tradeDate = trade.trade_date_time.split(' ');
                    trade.tradeDate = tradeDate[0];
                    if (tradeDate[1]) trade.tradeTime = tradeDate[1];
                    if (tradeDate[2]) {
                        trade.tradeOffset = tradeDate[2].replace(/\((.*)\)/, '$1');
                        trade.tradeOffset.toString();
                    }
                }
                if (trade.counterParty) trade.counterParty =
                    trade.counterParty.split('~')[1] || trade.counterParty;
                if (trade.quantity) trade.quantity = trade.quantity.replace(/[,.]/g, '');
                if (trade.currency) trade.premiumCurrency = trade.currency, delete trade.currency;
                delete trade.premium_date_time,
                delete trade.trade_date_time,
                delete trade.id;
                return trade;
            });
            return trades;
        };
        /*
         * Templates for rendering trades table
         */
        html.og_table = '\
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
          </table>';
        html.attributes = '\
          <tr class="og-js-attribute" style="display: none">\
            <td colspan="6" style="padding-left: 15px">\
              <table class="og-sub-list">{TBODY}</table>\
            </td>\
          </tr>\
        ';
        html.sub_header = '<tbody><tr><td class="og-header" colspan="2">{ATTRIBUTES}</td></tr></tbody>';
        /*
         * CRUD operations
         */
        action.add = function (trade) {
            var handler = function (result) {
                if (result.error) return alert(result.message);
                var trades = result.data.trades || [];
                trades.push(trade);
                action.put(format_trades(trades));
            };
            api.rest.positions.get({
                dependencies: dependencies,
                id: template_data.object_id,
                handler: handler
            });
        };
        action.replace = function (trade, trade_id) {
            var handler = function (result) {
                if (result.error) return alert(result.message);
                var trades = result.data.trades || [];
                trades.forEach(function (trade, i) {
                    if (trade.id.split('~')[1] === trade_id) {trades.splice(i, 1);}
                });
                trades.push(trade);
                action.put(format_trades(trades));
            };
            api.rest.positions.get({
                dependencies: dependencies,
                id: template_data.object_id,
                handler: handler
            });
        };
        action.del = function (trade_id) {
            var handler = function (result) {
                if (result.error) return alert(result.message);
                var trades = result.data.trades;
                trades.forEach(function (trade, i) {
                    if (trade_id === trade.id.split('~')[1]) trades.splice(i, 1);
                });
                ui.dialog({
                    type: 'confirm',
                    title: 'Delete trade?',
                    message: 'Are you sure you want to permanently delete trade ' +
                        '<strong style="white-space: nowrap">' + trade_id + '</strong>?',
                    buttons: {
                        'Delete': function () {
                            action.put(format_trades(trades));
                            $(this).dialog('close');
                        },
                        'Cancel': function () {$(this).dialog('close');}
                    }
                });
            };
            api.rest.positions.get({
                dependencies: dependencies,
                id: template_data.object_id,
                handler: handler
            });
        };
        action.edit = function (trade_id) {
            var handler = function (result) {
                var trade_obj;
                if (result.error) return alert(result.message);
                // get the trade object that you want to edit
                result.data.trades.forEach(function (trade) {
                    if (trade_id === trade.id.split('~')[1]) {trade_obj = trade}
                });
                ui.dialog({
                    type: 'input', title: 'Edit Trade: ' + trade_id, minWidth: 400, minHeight: 400,
                    form: generate_form_function(function () {populate_form_fields(trade_obj), attach_calendar()}),
                    buttons: {
                        'Save': form_save.partial(trade_id),
                        'Save new': form_create,
                        'Cancel': function () {$(this).dialog('close');}
                    }
                });
            };
            api.rest.positions.get({dependencies: dependencies, id: template_data.object_id, handler: handler});
        };
        action.put = function (trades) {
            api.rest.positions.put({
                trades: trades, id: template_data.object_id, quantity: template_data.quantity,
                handler: function (result) {
                    if (result.error) return ui.dialog({type: 'error', message: result.message});
                    reload();
                }
            });
        };
        load = function (config) {
            var handler = function (result) {
                if (result.error) return alert(result.message);
                original_config_object = config;
                template_data = result.data.template_data;
                var trades = result.data.trades, selector = config.selector, tbody, has_attributes = false,
                    fields = ['id', 'quantity', 'counterParty', 'trade_date_time', 'premium', 'premium_date_time'];
                if (!trades) return $(selector).html(html.og_table.replace('{TBODY}',
                    '<tr><td colspan="6">No Trades</td></tr>')), attach_trades_link(selector);
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
                                html.sub_header.replace('{ATTRIBUTES}', attr_type.lang()) +
                                '<tbody class="OG-background-01">' + attr.join('') + '</tbody>'
                            );
                        }
                        acc.push(html.attributes.replace('{TBODY}', html.join('')));
                        if (html.length) has_attributes = true;
                    }());
                    return acc;
                }, []).join('');
                $(selector).html(html.og_table.replace('{TBODY}', tbody)).hide().fadeIn();
                /*
                 * Remove expand links when no trade attributes are available
                 */
                if (!has_attributes) $(config.selector + ' .og-icon-expand').hide();
                $(selector + ' .OG-table > tbody > tr').each(function () {
                    var $this = $(this);
                    if ($this.next().hasClass('og-js-attribute')) {
                        $this.find('.og-icon-expand').bind('click', function (e) {
                            e.stopPropagation();
                            $(this).toggleClass('og-icon-collapse').parents('tr').next().toggle();
                        });
                    } else $this.find('.og-icon-expand').css('visibility', 'hidden');
                });
                attach_trades_link(selector);
                $(selector + ' > .OG-table > tbody > tr:not(".og-js-attribute"):last td').css('padding-bottom', '10px');
                $(selector + ' .OG-table').awesometable({height: 400});
                /*
                 * Enable edit/delete trade
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
                            var trade_id = $(this).find('td:first-child').text();
                            swap_css(this, {'background-color': '#d7e7f2', 'cursor': 'default'});
                            $(this).click(function () {action.edit(trade_id);});
                            $(this).find('td:last-child').append('<div class="og-del"></div>').find('.og-del')
                                .click(function (e) {e.stopPropagation(), action.del(trade_id);});
                        },
                        function () {
                            swap_css(this, {'background-color': '#ecf5fa'});
                            $(this).find('.og-del').remove();
                        }
                    )
                }());
            };
            api.rest.positions.get({dependencies: dependencies, id: config.id, handler: handler, cache_for: 500});
        };
        reload = function () {load(original_config_object);};
        return {render: load, reload: reload}
    }
});