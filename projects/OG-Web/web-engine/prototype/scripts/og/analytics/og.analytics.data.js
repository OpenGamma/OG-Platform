/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Data',
    dependencies: ['og.api.rest'],
    obj: function () {
        var module = this, counter = 1, api = og.api.rest.views;
        return function (config) {
            var data = this, events = {meta: [], data: []}, id = 'data_' + counter++, meta, cols,
                viewport = null, view_id = config.view, graph_id, viewport_id, viewport_version, subscribed = false,
                ROOT = 'rootNode', SETS = 'columnSets', ROWS = 'rowCount',
                grid_type = config.type, depgraph = !!config.depgraph,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'};
            var data_handler = function (result) {
                if (!result || result.error)
                    return og.dev.warn(result && result.message || module.name + ': reset connection');
                if (!events.data.length || !result.data) return; // if a tree falls or there's no tree, etc.
                if (result.data.version === viewport_version) return fire('data', result.data.data);
            };
            var data_setup = function () {
                if (!viewport) return;
                var viewports = (depgraph ? api[grid_type].depgraphs : api[grid_type]).viewports;
                subscribed = true;
                (viewport_id ? viewports.get({
                    view_id: view_id, graph_id: graph_id, viewport_id: viewport_id, update: data_setup
                }) : viewports
                    .put({view_id: view_id, graph_id: graph_id, rows: viewport.rows, columns: viewport.cols})
                    .pipe(function (result) {
                        if (result.error) // goes to data_setup so take care
                            return (view_id = graph_id = viewport_id = subscribed = null);
                        (viewport_id = result.meta.id), (viewport_version = result.data.version);
                        return viewports
                            .get({view_id: view_id, graph_id: graph_id, viewport_id: viewport_id, update: data_setup});
                    })
                ).pipe(data_handler);
            };
            var fire = function (type) {
                var args = Array.prototype.slice.call(arguments, 1);
                events[type].forEach(function (value) {value.handler.apply(null, value.args.concat(args));});
            };
            var grid_handler = function (result) {
                if (depgraph && !graph_id) return;
                if (!result || result.error) return (view_id = graph_id = viewport_id = subscribed = null),
                    og.dev.warn(result && result.message || module.name + ': reset connection'), initialize();
                if (!result.data[SETS].length) return;
                meta.data_rows = result.data[ROOT] ? result.data[ROOT][1] + 1 : result.data[ROWS];
                meta.structure = result.data[ROOT] || [];
                meta.columns.fixed = [{
                    name: fixed_set[grid_type], columns: result.data[SETS][0].columns
                        .map(function (col) {return (col.width = 150), col;})
                }];
                meta.columns.scroll = result.data[SETS].slice(1).map(function (set) {
                    return set.columns.forEach(function (col) {return (col.width = 175), col;}), set;
                });
                fire('meta', meta);
                if (!subscribed) return data_setup();
            };
            var grid_setup = function () {
                return depgraph ?
                    api[grid_type].grid.get({view_id: view_id, update: initialize}).pipe(function (result) {
                        if (result.error || !result.data[SETS].length) return; // goes to grid_handler so take care
                        if (graph_id) return api[grid_type].depgraphs.grid
                            .get({view_id: view_id, graph_id: graph_id, update: initialize});
                        return api[grid_type].depgraphs.put({row: config.row, col: config.col, view_id: view_id})
                            .pipe(function (result) {
                                return api[grid_type].depgraphs.grid
                                    .get({view_id: view_id, graph_id: (graph_id = result.meta.id), update: initialize});
                            })
                    })
                    : api[grid_type].grid.get({view_id: view_id, update: initialize});
            };
            var initialize = function () {
                var put_options = ['viewdefinition', 'aggregators', 'live', 'provider', 'snapshot', 'version']
                    .reduce(function (acc, val) {return (acc[val] = config[val]), acc;}, {});
                (view_id ? grid_setup() : api.put(put_options).pipe(view_handler)).pipe(grid_handler);
            };
            var view_handler = function (result) {return (view_id = result.meta.id), grid_setup();};
            data.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            data.id = id;
            data.meta = meta = {columns: {}};
            data.cols = cols = {};
            data.on = function (type, handler) {
                if (type in events)
                    events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
                return data;
            };
            data.viewport = function (new_viewport) {
                var viewports = (depgraph ? api[grid_type].depgraphs : api[grid_type]).viewports;
                viewport = new_viewport;
                if (!viewport_id) return;
                data.busy(true);
                viewports.put({
                    view_id: view_id, graph_id: graph_id, viewport_id: viewport_id,
                    rows: viewport.rows, columns: viewport.cols
                }).pipe(function (result) {
                    if (result.error) return; else (viewport_version = result.data.version), data.busy(false);
                });
            };
            initialize();
        };
    }
});