/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Data',
    dependencies: ['og.api.rest'],
    obj: function () {
        return function (config) {
            var data = this, data_handler, initialize, fire, events = {init: [], data: []}, meta, viewport = null;
            fire = function (type) {
                var args = Array.prototype.slice.call(arguments, 1);
                events[type].forEach(function (value) {
                    value.handler.apply(null, value.args.concat(args));
                });
            };
            data_handler = function (result) {
                if (!events.data.length) return; // if a tree falls, etc.
                var matrix = [], rows = data.meta.rows,
                    row_start = viewport.rows[0] || 1, row_end = viewport.rows[1],
                    fixed_length = data.meta.columns.fixed.length,
                    requested_cols = viewport.cols.reduce(function (acc, val) {return (acc[val] = null), acc;}, {});
                while (rows--) if (rows >= row_start && rows <= row_end) matrix.push(function (cols, row) {
                    var lcv = 0;
                    while (lcv++ < cols)
                        row.push(lcv < fixed_length || lcv in requested_cols ? Math.random() * 1000 : null);
                    return row;
                }(fixed_length + data.meta.columns.scroll.length - 1, [rows]));
                fire('data', matrix.reverse());
                setTimeout(data_handler, 1000);
            };
            initialize = function () {
                meta.rows = 10000;
                meta.columns = {
                    fixed: [
                        {name: 'Fixed 1', width: 50},
                        {name: 'Fixed 2', width: 150}
                    ],
                    scroll: [
                        {name: 'Column 3', width: 100},
                        {name: 'Column 4', width: 100},
                        {name: 'Column 5', width: 100},
                        {name: 'Column 6', width: 100},
                        {name: 'Column 7', width: 100},
                        {name: 'Column 8', width: 100},
                        {name: 'Column 9', width: 100},
                        {name: 'Column 10', width: 100},
                        {name: 'Column 11', width: 100},
                        {name: 'Column 12', width: 100},
                        {name: 'Column 13', width: 100},
                        {name: 'Column 14', width: 100},
                        {name: 'Column 15', width: 100},
                        {name: 'Column 16', width: 100},
                        {name: 'Column 17', width: 100},
                        {name: 'Column 18', width: 100},
                        {name: 'Column 19', width: 100},
                        {name: 'Column 20', width: 100},
                        {name: 'Column 21', width: 100},
                        {name: 'Column 22', width: 100},
                        {name: 'Column 23', width: 100},
                        {name: 'Column 24', width: 100},
                        {name: 'Column 25', width: 100},
                        {name: 'Column 26', width: 100},
                        {name: 'Column 27', width: 100},
                        {name: 'Column 28', width: 100},
                        {name: 'Column 29', width: 100},
                        {name: 'Column 30', width: 100},
                        {name: 'Column 31', width: 100},
                        {name: 'Column 32', width: 100},
                        {name: 'Column 33', width: 100},
                        {name: 'Column 34', width: 100},
                        {name: 'Column 35', width: 100},
                        {name: 'Column 36', width: 100},
                        {name: 'Column 37', width: 100},
                        {name: 'Column 38', width: 100},
                        {name: 'Column 39', width: 100}
                    ]
                };
                fire('init', meta);
                data_handler();
            };
            data.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            data.meta = meta = {columns: null};
            data.on = function (type, handler) {
                if (type in events)
                    events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
            };
            data.viewport = function (new_viewport) {
                viewport = new_viewport;
                if (viewport.rows === 'all') viewport.rows = [0, meta.rows];
            };
            setTimeout(initialize, 50);
        };
    }
});