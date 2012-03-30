/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.trades',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        var ui = og.common.util.ui, api = og.api,
            template_data,
            dependencies = ['id', 'node', 'version'],
            html = {}, action = {}, $add_trades,
            load, reload, attach_trades_link, format_trades,
            form_save, generate_form_function, form_handler;
        /*
         * Helper functions
         */
        generate_form_function = function (load_handler) {
            return function (css_class) {
                $(css_class).html('Loading form...');
                var form = new og.common.util.ui.Form({
                    selector: css_class, data: {}, module: 'og.views.forms.add-trades',
                    handlers: [{type: 'form:load', handler: function () {load_handler();}}]
                });
                form.children = [new form.Field({
                    module: 'og.views.forms.currency',
                    generator: function (handler, template) {handler(template);}
                })];
                form.dom();
            };
        };
        form_save = function (trade_id) {
            var obj = {}, userAttributes = {}, dealAttributes = {}, has_user_attr, has_deal_attr;
            $(this).find('[name]').each(function (i, elm) {obj[$(elm).attr('name')] = $(elm).val();});
            delete obj.attr_key;
            delete obj.attr_val;
            $(this).find('.og-js-user-attributes .og-awesome-list li').each(function (i, elm) {
                var arr = $(elm).text().split(' = ');
                userAttributes[arr[0]] = arr[1];
            });
            $(this).find('.og-js-deal-attributes .og-awesome-list li').each(function (i, elm) {
                var arr = $(elm).text().split(' = ');
                dealAttributes[arr[0]] = arr[1];
            });
            // add attributes
            has_user_attr = Object.keys(userAttributes)[0];
            has_deal_attr = Object.keys(dealAttributes)[0];
            if (has_user_attr || dealAttributes) obj.attributes = {};
            if (has_user_attr) obj.attributes.userAttributes = userAttributes;
            if (has_deal_attr) obj.attributes.dealAttributes = dealAttributes;
            if (!trade_id) action.add(obj);
            else action.replace(obj, trade_id);
            $(this).dialog('close');
        };
        attach_trades_link = function (selector, editable) {
            if (!editable) return;
            $(selector).append('<a href="#" class="OG-link-add">add trade</a>').find('.OG-link-add').css({
                'position': 'relative', 'left': '2px', 'top': '3px', 'float': 'left'
            }).unbind('click').bind('click', function (e) {
                e.preventDefault();
                ui.dialog({
                    type: 'input', title: 'Add New Trade', width: 650, height: 420,
                    form: generate_form_function(form_handler),
                    buttons: {
                        'Create': form_save,
                        'Cancel': function () {$(this).dialog('close');}
                    }
                });
            });
        };
        form_handler = function (trade_obj) {
            var populate_form_fields, attach_calendar, activate_attributes_link, activate_attributes_delete,
                attr_type, has = 'hasOwnProperty';
            $add_trades = $('.OG-js-add-trades');
            populate_form_fields = function (trade_obj) {
                var attributes_list = {}, trd_attr = trade_obj.attributes, key, has = 'hasOwnProperty';
                $add_trades.find('[name]').each(function (i, val) {
                    // special case 'premium' as there are two fields for the one value
                    var attribute = $(val).attr('name'), value = trade_obj.premium.split(' ');
                    if (attribute === 'premium') trade_obj.premium = value[0], trade_obj.currency = value[1];
                    if (attribute === 'counterParty') trade_obj.counterParty = trade_obj.counterParty.split('~')[1];
                    $(val).val(trade_obj[attribute]);
                });
                if (!trd_attr) return;
                for (attr_type in trd_attr) {
                    if (trd_attr[has](attr_type)) {
                        attributes_list[attr_type] = [];
                        for (key in trd_attr[attr_type]) {
                            if (trd_attr[attr_type][has](key)) {
                                attributes_list[attr_type].push(html.attribute
                                    .replace('{KEY}', key.replace(/Deal~/, ''))
                                    .replace('{VALUE}', trd_attr[attr_type][key])
                                );
                            }
                        }
                    }
                }
                for (var t in trd_attr) {
                    $add_trades.find('.og-js-' + t.replace('A', '-a') + ' .og-awesome-list')
                            .html(attributes_list[t].join(''));
                }
            };
            attach_calendar = function () {
                $('.OG-js-datetimepicker').datetimepicker({
                    firstDay: 1, showTimezone: true, dateFormat: 'yy-mm-dd',timeFormat: 'hh:mm ttz'
                });
                $add_trades.find('.og-inline-form').click(function (e) {
                    e.preventDefault();
                    $(this).prev().find('input').datetimepicker('setDate', new Date());
                });
            };
            activate_attributes_link = function () {
                $add_trades.find('.og-js-add-attribute').click(function (e) {
                    e.preventDefault();
                    var $group = $(e.target).parent();
                    if (!$group.find('[name=attr_key]').val() || !$group.find('[name=attr_val]').val()) return;
                    $group.find('.og-awesome-list').prepend(html.attribute
                        .replace('{KEY}', $group.find('[name=attr_key]').val())
                        .replace('{VALUE}', $group.find('[name=attr_val]').val())
                    );
                    $group.find('[name^=attr]').val('');
                    activate_attributes_delete();
                });
            };
            activate_attributes_delete = function () {
                $add_trades.find('.og-js-rem').unbind('click').click(function (e) {$(e.target).parent().remove();});
            };
            if (trade_obj) populate_form_fields(trade_obj);
            attach_calendar(), activate_attributes_link(), activate_attributes_delete();
        };
        /*
         * Formats arrays of trade objects for submission.
         * The object that we receive in the response can't be sent back as is because it's been formatted slightly
         * differently, this also applies for the form object for the new trade to be added
         */
        format_trades = function (trades) {
            return (trades || []).map(function (trade) {
                var premium, tradeDate, deal;
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
                if (trade.attributes && trade.attributes.dealAttributes) {
                    for (deal in trade.attributes.dealAttributes) {
                        if (!~deal.indexOf('Deal~')) {
                            trade.attributes.dealAttributes['Deal~' + deal] = trade.attributes.dealAttributes[deal];
                            delete trade.attributes.dealAttributes[deal];
                        }
                    }
                }
                delete trade.premium_date_time;
                delete trade.trade_date_time;
                delete trade.id;
                return trade;
            });
        };
        /*
         * Templates for rendering trades table
         */
        html.og_table = '\
          <table class="OG-table og-tablesorter">\
            <thead>\
              <tr>\
                <th colspan="6"><span>Trades</span></th>\
              </tr>\
              <tr>\
                <th>ID</th>\
                <th>Quantity</th>\
                <th>Counterparty</th>\
                <th>Trade Date / Time</th>\
                <th>Premium</th>\
                <th>Premium Date / Time</th>\
              </tr>\
            </thead>\
            <tbody>{TBODY}</tbody>\
          </table>';
        // expand-child class is for tablesorter
        html.attributes = '\
          <tr class="og-js-attribute expand-child" style="display: none">\
            <td colspan="6" style="padding: 0 10px 10px 24px; position: relative">\
              <table class="og-sub-list">{TBODY}</table>\
            </td>\
          </tr>\
        ';
        html.sub_header = '<tbody><tr><td class="og-header" colspan="2">{ATTRIBUTES}</td></tr></tbody>';
        html.attribute = '<li><div class="og-del og-js-rem"></div>{KEY} = {VALUE}</li>';
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
                    width: 400, height: 190,
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
                    type: 'input', title: 'Edit Trade: ' + trade_id, width: 650, height: 420,
                    form: generate_form_function(form_handler.partial(trade_obj)),
                    buttons: {
                        'Save': form_save.partial(trade_id),
                        'Save new': form_save,
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
                }
            });
        };
        load = function (config) {
            var handler, editable = 'editable' in config ? config.editable : true,
                version = config.version !== '*' ? config.version : void 0,
                height = config.height || 400;
            handler = function (result) {
                if (result.error) return alert(result.message);
                template_data = result.data.template_data;
                var trades, selector = config.selector, tbody, has_attributes = false,
                    fields = ['id', 'quantity', 'counterParty', 'trade_date_time', 'premium', 'premium_date_time'];
                trades = (result.data.trades || []).sort(function (a, b) {
                    return a['trade_date_time'] > b['trade_date_time'] ? -1
                        : a['trade_date_time'] < b['trade_date_time'] ? 1
                            : 0;
                });
                if (!trades.length) return $(selector).html(html.og_table.replace('{TBODY}',
                    '<tr><td colspan="6">No Trades</td></tr>')), attach_trades_link(selector, editable);
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
                        var attr, attr_type, attr_obj, key, html_arr = [];
                        for (attr_type in trade.attributes) {
                            attr_obj = trade.attributes[attr_type], attr = [];
                            for (key in attr_obj) attr.push(
                                '<tr><td>', key.replace(/.+~(.+)/, '$1').lang(),
                                ':</td><td>', attr_obj[key].lang(), '</td></tr>'
                            );
                            html_arr.push(
                                html.sub_header.replace('{ATTRIBUTES}', attr_type.lang()) +
                                '<tbody class="OG-background-01">' + attr.join('') + '</tbody>'
                            );
                        }
                        acc.push(html.attributes.replace('{TBODY}', html_arr.join('')));
                        if (html_arr.length) has_attributes = true;
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
                        $this.find('.og-icon-expand').unbind('click').bind('click', function (e) {
                            e.stopPropagation();
                            $(this).toggleClass('og-icon-collapse').parents('tr').next().toggle();
                        });
                    } else $this.find('.og-icon-expand').css('visibility', 'hidden');
                });
                if (!version && editable) attach_trades_link(selector);
                $(selector + ' .OG-table').tablesorter({
                    headers: {1: {sorter:'numeric_string'}, 4: {sorter: 'currency_string'}}
                }).awesometable({height: height});
                /*
                 * Enable edit/delete trade
                 */
                (function () {
                    if (version) return;
                    var swap_css = function (elm, css) {
                        $(elm).find('td').css(css);
                        if ($(elm).next().is('.og-js-attribute')) $(elm).next().find('> td').css(css);
                    };
                    if (editable) $(selector + ' .og-row').hover(
                        function () {
                            swap_css(this, {'background-color': '#d7e7f2', 'cursor': 'default'});
                            $(this).find('td:last-child').append('<div class="og-del"></div>');
                        },
                        function () {
                            swap_css(this, {'background-color': '#ecf5fa'});
                            $(this).find('.og-del').remove();
                        }
                    ).click(function (e) {
                        var trade_id = $(this).find('td:first-child').text();
                        if ($(e.target).is('.og-del')) return e.stopPropagation(), action.del(trade_id);
                        action.edit(trade_id);
                    })
                }());
            };
            api.rest.positions.get({
                dependencies: dependencies, id: config.id, handler: handler, cache_for: 500, version: version
            });
        };
        load.format = format_trades;
        return load;
    }
});