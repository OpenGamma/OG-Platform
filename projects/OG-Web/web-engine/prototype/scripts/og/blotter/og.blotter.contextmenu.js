/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.contextmenu',
    dependencies: [],
    obj: function () {
        var menu = og.common.util.ui.contextmenu({defaults: true, zindex: 4, items: context_items(cell)}, event, cell);
        var context_items = function (cell) {
            //console.log(cell);
            var items = [];
            if (url.last.main.aggregators.length) {
                 items.push({name: 'Unable to add/edit trades whilst aggregated', handler: $.noop});
                 return items;
            }
            var position_edit = function () {
                //console.log("position edit");
                var arr = cell.row_value.positionId.split('~'), id = arr[0] + '~' + arr[1];
                og.api.rest.blotter.positions.get({id: id}).pipe(function (data) {
                    // console.log(data);
                    new og.blotter.Dialog({
                        details: data, portfolio:{name: id, id: id}, 
                        handler: function (data) {return og.api.rest.blotter.positions.put(data);}
                    });
                });
            };
            var trade_edit = function () {
                //console.log("trade edit");
                og.api.rest.blotter.trades.get({id: cell.row_value.tradeId}).pipe(function (data) {
                    new og.blotter.Dialog({
                        details: data, portfolio:{name: cell.row_value.nodeId, id: cell.row_value.nodeId},
                        handler: function (data) {return og.api.rest.blotter.trades.put(data);}
                    });
                });
            };
            var trade_insert = function () {
                new og.blotter.Dialog({portfolio:{name: cell.row_value.nodeId, id: cell.row_value.nodeId}, 
                    handler: function (data) {return og.api.rest.blotter.trades.put(data);}
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
            items.push({name: 'Add Trade', handler: trade_insert});
            if (cell.row in og.analytics.grid.state.nodes || cell.type === "NODE") {
                return items;
            }
            if (cell.row_value.tradeId) {
                items.push({name: 'Edit Trade', handler: trade_edit}); 
            } 
            else {
                items.push({name: 'Edit Trade', handler: position_edit}); 
            }
            items.push({name: 'Delete', handler: trade_delete});
            return items;
        }; 
        return menu;   
    }
});