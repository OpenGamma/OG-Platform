/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Data',
    dependencies: ['og.analytics.connection'],
    obj: function () {
        var module = this, counter = 1, api = og.api.rest;
        return function (config) {
            var data = this, events = {init: [], data: []}, id = 'data_' + counter++, meta, cols,
                viewport = null, view_id = config.view, viewport_id,
                subs = {viewport: false, cols: false},
                ROOT = 'rootNode', SETS = 'columnSets', ROWS = 'rowCount',
                grid_type = config.type, depgraph = !!config.depgraph,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'};
            var cols_handler = function (result) {
                console.log('columns', result);
                // do something with the result here
                fire('init', meta);
                if (!subs.data) return data_setup();
            };
            var cols_setup = function () {
                subs.cols = true;
                return api.views[grid_type].columns.get({id: view_id, update: cols_setup});
            };
            var data_handler = function (result) {
                if (!events.data.length) return; // if a tree falls, etc.
                if (result.error) return og.dev.warn(result.message);
                fire('data', result.data);
            };
            var data_setup = function () {
                if (!viewport) return;
                var viewports = api.views[grid_type].viewports;
                subs.data = true;
                (viewport_id ? viewports.get({
                    view_id: view_id, viewport_id: viewport_id, update: data_setup
                }) : viewports.put({id: view_id, rows: viewport.rows, columns: viewport.cols}).pipe(function (result) {
                    return viewports.get({
                        view_id: view_id, viewport_id: viewport_id = result.meta.id, update: data_setup
                    });
                })).pipe(data_handler);
            };
            var fire = function (type) {
                var args = Array.prototype.slice.call(arguments, 1);
                events[type].forEach(function (value) {value.handler.apply(null, value.args.concat(args));});
            };
            var grid_handler = function (result) {
                if (result.error) return og.dev.warn(result.message); else if (!result.data[SETS].length) return;
                meta.rows = result.data[ROOT] ? result.data[ROOT][1] + 1 : result.data[ROWS];
                meta.columns = {
                    fixed: [{
                        name: fixed_set[grid_type], columns: result.data[SETS][0].columns
                            .map(function (col) {return (col.width = 150), col;})
                    }],
                    scroll: result.data[SETS].slice(1).map(function (set) {
                        return set.columns.forEach(function (col) {return (col.width = 175), col;}), set;
                    })
                };
                if (!subs.cols) return cols_setup();
            };
            var grid_setup = function (result) {
                return api.views[grid_type].grid.get({id: view_id, update: initialize});
            };
            var initialize = function () {
                (typeof view_id === 'number' ? grid_setup() : api.views.put(config).pipe(view_handler))
                .pipe(grid_handler)
                .pipe(cols_handler);
            };
            var view_handler = function (result) {return (view_id = result.meta.id), grid_setup();};
            data.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            data.id = id;
            data.kill = function () {for (var type in events) events[type] = [];};
            data.meta = meta = {columns: null};
            data.cols = cols = {};
            data.on = function (type, handler) {
                if (type in events)
                    events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
            };
            data.viewport = function (new_viewport) {
                viewport = new_viewport;
                if (viewport.rows === 'all') viewport.rows = [0, meta.rows];
            };
            initialize();
        };
    }
});