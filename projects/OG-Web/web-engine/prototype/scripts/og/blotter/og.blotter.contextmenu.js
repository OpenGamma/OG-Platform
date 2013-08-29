/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.contextmenu',
    dependencies: [],
    obj: function () {
        return function (cell, event) {
            var grid = og.analytics.grid;
            var context_items = function (cell) {
                var items = [];
                // when aggregated the portfolio node structure is not shown so edit/add is not possible
                if (og.analytics.url && og.analytics.url.last.main.aggregators.length) {
                    items.push({name: 'Unable to add/edit trades whilst aggregated', handler: $.noop});
                    return items;
                }
                // positions with no trades first need a trade created based on the position
                var position_edit = function () {
                    var id = og.blotter.util.get_unique_id(cell.row_value.positionId);
                    og.api.rest.blotter.positions.get({id: id}).pipe(function (data) {
                        new og.blotter.Dialog({
                            details: data,
                            node: {name: id, id: id},
                            handler: function (data) {return og.api.rest.blotter.positions.put(data); },
                            complete : complete_handler
                        });
                    });
                };
                // trades added to positions need to be locked in to the same trade/security type 
                var position_insert = function () {
                    var id = og.blotter.util.get_unique_id(cell.row_value.positionId), nodeId = cell.row_value.nodeId;
                    og.api.rest.blotter.positions.get({id: id}).pipe(function (data) {
                        new og.blotter.Dialog({
                            details: data,
                            node: {name: nodeId, id: nodeId},
                            handler: function (data) {return og.api.rest.blotter.trades.put(data); },
                            complete : complete_handler
                        });
                    });
                };
                // editing a trade just needs trade details
                var trade_edit = function () {
                    og.api.rest.blotter.trades.get({id: cell.row_value.tradeId}).pipe(function (data) {
                        new og.blotter.Dialog({
                            details: data,
                            node: {name: cell.row_value.nodeId, id: cell.row_value.nodeId},
                            handler: function (data) {return og.api.rest.blotter.trades.put(data); },
                            complete : complete_handler,
                            save_as: true
                        });
                    });
                };
                // addding a new trade needs a node id to append to
                var trade_insert_node = function () {
                    new og.blotter.Dialog({node: {name: cell.row_value.nodeId, id: cell.row_value.nodeId},
                        handler: function (data) {return og.api.rest.blotter.trades.put(data); },
                        complete : complete_handler
                    });
                };

                var del = function (type) {
                    var title, message, del_func;
                    if (type.trade) {
                        title = 'Delete Trade?';
                        message = 'Are you sure you want to permanently delete this trade?';
                        del_func = trade_delete;
                    } else {
                        title = 'Delete Position?';
                        message = 'Are you sure you want to permanently delete this position?';
                        del_func = position_delete;
                    }
                    return function () {
                        og.common.util.ui.dialog({
                            type: 'confirm',
                            title: title,
                            width: 400,
                            height: 190,
                            message: message,
                            buttons: {
                                'Delete': function () {
                                    $(this).dialog('close');
                                    del_func();
                                },
                                'Cancel': function () {$(this).dialog('close'); }
                            }
                        });
                    };
                };
                var trade_delete = function () {
                    og.api.rest.blotter.trades.del({id: cell.row_value.tradeId}).pipe(function (result) {
                        if (result.error) {
                            og.common.util.ui.message({css: {position: 'inherit', whiteSpace: 'normal'},
                                location: '.OG-blotter-error-block', message: result.message});
                        }
                    });
                };
                var position_delete = function (config) {
                    og.api.rest.configs.get({id: og.analytics.url.last.main.viewdefinition}).pipe(function (result) {
                        var portfolio = result.data.template_data.configJSON.data.identifier;
                        og.api.rest.portfolios.del({
                            id: portfolio,
                            node: cell.row_value.nodeId,
                            position: og.blotter.util.get_unique_id(cell.row_value.positionId)
                        }).pipe(function (result) {
                            if (result.error) {
                                og.common.util.ui.message({css: {position: 'inherit', whiteSpace: 'normal'},
                                    location: '.OG-blotter-error-block', message: result.message});
                            }
                        });
                    });
                };
                var complete_handler = function (result) {
                    var msg, id = result.meta.id;
                    if (id) {
                        msg = 'Trade ' + result.meta.id + ' successfully added';
                    } else {
                        msg = 'Trade successfully updated';
                    }
                    og.common.util.ui.message({location: '.OG-layout-analytics-center', message: msg, live_for: 6000});
                };
                var create_portfolio = function () {
                    $(this).dialog('close');
                    og.api.rest.configs.get({id: og.analytics.url.last.main.viewdefinition}).pipe(function (result) {
                        var portfolio = result.data.template_data.configJSON.data.identifier;
                        og.api.rest.portfolios.put({
                            name: og.common.util.ui.dialog({return_field_value: 'name'}),
                            id: portfolio,
                            node: cell.row_value.nodeId,
                            'new': true
                        });
                    });
                };
                var new_portfolio = function () {
                    og.common.util.ui.dialog({
                        width: 400,
                        height: 190,
                        type: 'input',
                        title: 'Add New Portfolio',
                        fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                        buttons: {
                            'OK': create_portfolio,
                            'Cancel': function () {$(this).dialog('close'); }
                        }
                    });
                };
                // if a row is a node AND the cell is a position only the position insert option is relevant
                // note that cell.type === 'POSITION' is only relevant for the first column, so can't be used
                // else if a row is a node OR the cell is a node only the add new trade option is relevant
                if (cell.row in grid.state.nodes && cell.row_value.positionId) {
                    items.push({name: 'Add Trade', handler: position_insert});
                    items.push({name: 'Delete Position', handler: del({position: true})});
                    return items;
                } else if (cell.row in grid.state.nodes || cell.type === 'NODE') {
                    items.push({name: 'Add Trade/Position', handler: trade_insert_node});
                    items.push({name: 'Add Sub Portfolio', handler: new_portfolio});
                    return items;
                }
                // if a cell has a tradeId then edit the trade otherwise it is an empty position
                if (cell.row_value.tradeId) {
                    var name = (cell.type === 'OTC_TRADE') ? 'Edit Position' : 'Edit Trade';
                    items.push({name: name, handler: trade_edit});
                } else {
                    items.push({name: 'Edit Position', handler: position_edit});
                }
                // OTC/POSITION needs a delete position action, FUNGIBLE needs delete trade
                if (cell.type === 'OTC_TRADE' || cell.type === 'POSITION') {
                    items.push({name: 'Delete Position', handler: del({position: true})});
                } else {
                    items.push({name: 'Delete Trade', handler: del({trade: true})});
                }
                return items;
            };
            return og.common.util.ui.contextmenu({
                defaults: false,
                zindex: 4,
                items: new og.common.grid.NodeMenu(grid, cell, event).items().concat({}, context_items(cell))
            }, event, cell);
        };
    }
});