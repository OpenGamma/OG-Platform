/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.contextmenu',
    dependencies: [],
    obj: function () {
        return function (cell, event) {
            var module = this;
            module.context_items = function (cell) {
                var items = [];
                // when aggregated the portfolio node structure is not shown so edit/add is not possible
                if (og.analytics.url && og.analytics.url.last.main.aggregators.length) {
                     items.push({name: 'Unable to add/edit trades whilst aggregated', handler: $.noop});
                     return items;
                }
                // positions with no trades first need a trade created based on the position
                var position_edit = function () {
                    var pos_arr = cell.row_value.positionId.split('~'), id = pos_arr[0] + '~' + pos_arr[1];
                    og.api.rest.blotter.positions.get({id: id}).pipe(function (data) {
                        new og.blotter.Dialog({
                            details: data, portfolio:{name: id, id: id}, 
                            handler: function (data) {return og.api.rest.blotter.positions.put(data);},
                            complete : complete_handler
                        });
                    });
                };
                // trades added to positions need to be locked in to the same trade/security type 
                var position_insert = function () {
                    var pos_arr = cell.row_value.positionId.split('~'), id = pos_arr[0] + '~' + pos_arr[1],
                        nodeId= cell.row_value.nodeId;
                    og.api.rest.blotter.positions.get({id: id}).pipe(function (data) {
                        new og.blotter.Dialog({
                            details: data, portfolio:{name: nodeId, id: nodeId}, 
                            handler: function (data) {return og.api.rest.blotter.trades.put(data);},
                            complete : complete_handler
                        });
                    });
                };
                // editing a trade just needs trade details
                var trade_edit = function () {
                    og.api.rest.blotter.trades.get({id: cell.row_value.tradeId}).pipe(function (data) {
                        new og.blotter.Dialog({
                            details: data, portfolio:{name: cell.row_value.nodeId, id: cell.row_value.nodeId},
                            handler: function (data) {return og.api.rest.blotter.trades.put(data);},
                            complete : complete_handler, save_as: true
                        });
                    });
                };
                // addding a new trade needs a node id to append to
                var trade_insert_node = function () {
                    new og.blotter.Dialog({portfolio:{name: cell.row_value.nodeId, id: cell.row_value.nodeId}, 
                        handler: function (data) {return og.api.rest.blotter.trades.put(data);},
                        complete : complete_handler
                    });
                };
                var trade_delete = function () {
                    og.common.util.ui.dialog({
                        type: 'confirm',
                        title: 'Delete Trade / Position?',
                        width: 400, height: 190,
                        message: 'Are you sure you want to permanently delete this trade / position?',
                        buttons: {
                            'Delete': function () {/*do something*/},
                            'Cancel': function () {$(this).dialog('close');}
                        }
                    });
                };
                var complete_handler = function (result) {
                    var msg, id = result.meta.id;
                    if (id) msg = "Trade " + result.meta.id + " successfully added";
                    else msg = "Trade successfully updated";
                    og.common.util.ui.message({location: '.OG-layout-analytics-center', message: msg, live_for: 6000});
                };
                // if a row is a node AND the cell is a position only the position insert option is relevant
                // if a row is a node OR the cell is a node only the add new trade option is relevant
                if (cell.row in og.analytics.grid.state.nodes && cell.type === 'POSITION') {
                    items.push({name: 'Add Trade', handler: position_insert});
                    return items;
                }
                else if (cell.row in og.analytics.grid.state.nodes || cell.type === 'NODE') {  
                    items.push({name: 'Add Trade', handler: trade_insert_node});
                    return items;  
                }
                // if a cell has a tradeId then edit the trade otherwise it is an empty position
                if (cell.row_value.tradeId) {
                    items.push({name: 'Edit Trade', handler: trade_edit}); 
                } 
                else {
                    items.push({name: 'Edit Trade', handler: position_edit}); 
                }
                //items.push({name: 'Delete', handler: trade_delete});
                return items;
            }; 
            return og.common.util.ui.contextmenu({
                defaults: true, zindex: 4, items: module.context_items(cell)}, event, cell);
        };
    }
});