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
            var data = this, events = {init: [], data: []}, id = 'data_' + counter++, meta,
                viewport = null, view_id = config.view, viewport_id,
                ROOT = 'rootNode', SETS = 'columnSets', ROWS = 'rowCount',
                grid_type = config.type, depgraph = !!config.depgraph,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'};
            var data_handler = function (result) {
                console.log('data_handler:\n', result);
                if (!events.data.length) return; // if a tree falls, etc.
                // fire('data', matrix.reverse());
            };
            var data_setup = function () {
                if (!viewport) return;
                var viewports = api.views[grid_type].viewports;
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
            var meta_handler = function (result) {
                if (result.error) return og.dev.warn(result.message); else if (!result.data[SETS].length) return;
                meta.rows = result.data[ROOT] ? result.data[ROOT][1] + 1 : result.data[ROWS];
                meta.columns = {
                    fixed: [{
                        name: fixed_set[grid_type], columns: result.data[SETS][0].columns
                            .map(function (col) {return (col.width = 150), col;})
                    }],
                    scroll: result.data[SETS].slice(1).map(function (set) {
                        return set.columns.forEach(function (col) {return (col.width = 150), col;}), set;
                    })
                };
                fire('init', meta);
                data_setup();
            };
            var meta_setup = function () {
                (view_id ? api.views[grid_type].grid.get({id: view_id, update: meta_setup})
                    : api.views.put(config).pipe(function (result) {
                        return api.views[grid_type].grid.get({id: view_id = result.meta.id, update: meta_setup});
                })).pipe(meta_handler);
            };
            data.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            data.id = id;
            data.kill = function () {for (var type in events) events[type] = [];};
            data.meta = meta = {columns: null};
            data.on = function (type, handler) {
                if (type in events)
                    events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
            };
            data.viewport = function (new_viewport) {
                viewport = new_viewport;
                if (viewport.rows === 'all') viewport.rows = [0, meta.rows];
            };
            meta_setup();
        };
    }
});