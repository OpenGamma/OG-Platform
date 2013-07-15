/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.trades',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, position_id = config.id, position_version = config.version, trades,
            selector = config.selector, $selector, dependencies = ['id', 'node', 'version'], template_data,
            height = config.height || 400, editable = 'editable' in config ? config.editable : true;
            constructor.load = function () {
                og.api.rest.positions.get({dependencies: dependencies, id: position_id, handler: display_trades, 
                    cache_for: 500, version: position_version
                });
            };
            display_trades = function (result) {
                var title, tash;
                trades = (result.data.trades || []).sort(sort_trades);
                template_data = result.data.template_data;
                title = config.child ? '' : '<span><em>Trades</em></span>';
                og.api.text({module: 'og.views.gadgets.trades_tash'}).pipe(function (template){
                    tash = Handlebars.compile(template);
                    $(selector).html(tash({title: title, trades: trades}));
                    row_sortability();
                    row_editability();
                    new_trade_link();
                });
            };
            new_trade_link = function () {
                if (editable && !position_version && !trades.length) {
                    $(selector).append('<a href="#" class="OG-link-add">add trade</a>').find('.OG-link-add').css({
                        'position': 'relative', 'left': '2px', 'top': '3px', 'float': 'left'
                    }).unbind('click').bind('click', function (e) {
                        e.preventDefault();
                        og.api.rest.blotter.positions.get({id: position_id}).pipe(function (data) {
                            new og.blotter.Dialog({
                                details: data, node:{name: position_id, id: position_id},  
                                handler: function (data){return og.api.rest.blotter.positions.put(data);}
                            });
                        });
                    });
                }
            };
            sort_trades = function (a, b) {
                return a['trade_date_time'] > b['trade_date_time'] ? -1
                        : a['trade_date_time'] < b['trade_date_time'] ? 1
                            : 0;
            };
            row_editability = function () {
                if (position_version) return;
                var swap_css = function (elm, css) {
                    $(elm).find('td').css(css);
                    if ($(elm).next().is('.og-js-attribute')) $(elm).next().find('> td').css(css);
                };
                if (editable) {
                    $(selector + ' .og-row').hover(
                    function () {
                        swap_css(this, {'background-color': '#d7e7f2', 'cursor': 'default'});
                        if (trades.length === 1 ) $(this).find('td:last-child').append('<div class="og-del"></div>');
                    },
                    function () {
                        swap_css(this, {'background-color': '#ecf5fa'});
                        $(this).find('.og-del').remove();
                    }).click(function (e) {
                        var trade_id = $(this).find('td:first-child').text();
                        if ($(e.target).is('.og-del')) return e.stopPropagation(), delete_trade(trade_id);
                        edit_trade(trade_id);
                    });
                }
            };
            row_sortability = function () {
                var $table = $(selector + ' .OG-table');
                $table.tablesorter({headers: {1: {sorter:'numeric_string'}, 4: {sorter: 'currency_string'}}});
                if (!config.child) {
                    $table.awesometable({resize: function (resize) {
                        og.common.gadgets.manager.register({
                            alive: function () {return !!$(selector).length;}, resize: resize
                        });
                    }, height: height});
                 }
            };
            edit_trade = function (trade_id) {
                og.api.rest.blotter.trades.get({id: trade_id}).pipe(function (data) {
                    new og.blotter.Dialog({
                        details: data, handler: function (data) {return og.api.rest.blotter.trades.put(data);}
                    });
                });
            };
            delete_trade = function (trade_id) {
                og.common.util.ui.dialog({
                    type: 'confirm', title: 'Delete trade?', width: 400, height: 190,
                    message: 'Are you sure you want to permanently delete trade ' +
                        '<strong style="white-space: nowrap">' + trade_id + '</strong>?',
                    buttons: {
                        'Delete': function () {temp_delete(); $(this).dialog('close');},
                        'Cancel': function () {$(this).dialog('close');}
                    }
                });
            };
            //remove temp_delete from here when delete endpoint is provided
            temp_delete = function () {
                og.api.rest.positions.put({
                    trades: [], id: template_data.object_id, quantity: template_data.quantity,
                    handler: function (result) {
                        if (result.error) return og.common.util.ui.dialog({type: 'error', message: result.message});
                    }
                });
            };
            constructor.load();
        }; 
    }
});