/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Data',
    dependencies: ['og.api.rest'],
    obj: function () {
        var module = this, counter = 1, connections = {};
        $(window).on('unload', function () {
            Object.keys(connections).forEach(function (key) {try {connections[key].kill();} catch (error) {}});
        });
        var constructor = function (config, bypass_types) {
            var data = this, api = og.api.rest.views, id = 'data_' + counter++ + '_' + +new Date, meta,
                fire = og.common.events.fire, viewport = null, view_id = config.view, graph_id, viewport_id,
                viewport_version, subscribed = false, ROOT = 'rootNode', SETS = 'columnSets', ROWS = 'rowCount',
                grid_type = null, depgraph = !!config.depgraph, types_emitted = false, loading_viewport_id = false,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'};
            var data_handler = function (result) {
                data.busy(false);
                if (!result || result.error)
                    return og.dev.warn(module.name + ': ' + (result && result.message || 'reset connection'));
                if (!data.events.data.length || !result.data) return; // if a tree falls or there's no tree, etc.
                if (viewport && viewport.empty !== true && result.data.version === viewport_version)
                    try {fire(data.events.data, result.data.data);}
                    catch (error) {return og.dev.warn(module.name + ': killed connection due to ', error), data.kill();}
            };
            var data_setup = function () {
                if (!view_id || !viewport) return;
                var viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                subscribed = true;
                data.busy(true);
                (viewport_id ? viewports.get({
                    view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                    viewport_id: viewport_id, update: data_setup
                }) : viewports.put({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                        loading: function () {loading_viewport_id = true;},
                        rows: viewport.rows, columns: viewport.cols, expanded: viewport.expanded
                    }).pipe(function (result) {
                        loading_viewport_id = false;
                        if (result.error) // goes to data_setup so take care
                            return (view_id = graph_id = viewport_id = subscribed = null);
                        (viewport_id = result.meta.id), (viewport_version = result.data.version);
                        return viewports.get({
                            view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                            viewport_id: viewport_id, update: data_setup
                        });
                    })
                ).pipe(data_handler);
            };
            var initialize = function () {
                var put_options = ['viewdefinition', 'aggregators', 'providers']
                    .reduce(function (acc, val) {return (acc[val] = config[val]), acc;}, {});
                if (depgraph || bypass_types) grid_type = config.type; // don't bother with type_setup
                if (view_id && grid_type) return structure_setup().pipe(structure_handler);
                if (grid_type) return api.put(put_options).pipe(view_handler).pipe(structure_handler);
                api.put(put_options).pipe(view_handler);
            };
            var structure_handler = function (result) {
                var message;
                if (!grid_type || (depgraph && !graph_id)) return;
                if (result.error && server_error(result)) return (view_id = graph_id = viewport_id = subscribed = null),
                    og.dev.warn(message = module.name + ': ' + result.message), fire(data.events.fatal, message);
                if (result.error) return (view_id = graph_id = viewport_id = subscribed = null),
                    og.dev.warn(message = module.name + ': ' + result.message), initialize();
                if (!result.data[SETS].length) return;
                meta.data_rows = result.data[ROOT] ? result.data[ROOT][1] + 1 : result.data[ROWS];
                meta.structure = result.data[ROOT] || [];
                meta.columns.fixed = [{name: fixed_set[grid_type], columns: result.data[SETS][0].columns}];
                meta.columns.scroll = result.data[SETS].slice(1);
                try {fire(data.events.meta, meta);}
                catch (error) {return og.dev.warn(module.name + ': killed connection due to ', error), data.kill();}
                if (!subscribed) return data_setup();
            };
            var structure_setup = function () {
                return !view_id ? null : depgraph ? api.grid.structure
                    .get({view_id: view_id, grid_type: grid_type, update: initialize}).pipe(function (result) {
                        if (result.error || !result.data[SETS].length) return result; // goes to structure_handler
                        if (graph_id) return api.grid.depgraphs.structure
                            .get({view_id: view_id, grid_type: grid_type, graph_id: graph_id, update: initialize});
                        return api.grid.depgraphs.put({
                            view_id: view_id, grid_type: grid_type, row: config.row, col: config.col
                        }).pipe(function (result) {
                            return api.grid.depgraphs.structure.get({
                                view_id: view_id, grid_type: grid_type,
                                graph_id: (graph_id = result.meta.id), update: initialize
                            });
                        })
                    })
                    : api.grid.structure.get({view_id: view_id, grid_type: grid_type, update: initialize});
            };
            var type_setup = function (view_result) {
                var port_request, prim_request, initial_load = !!config.type && (grid_type === null);
                if (!grid_type) grid_type = config.type;
                port_request = api.grid.structure.get({view_id: view_id, update: type_setup, grid_type: 'portfolio'});
                prim_request = api.grid.structure.get({view_id: view_id, update: type_setup, grid_type: 'primitives'});
                $.when(port_request, prim_request).then(function (port_struct, prim_struct) {
                    var portfolio = !!(port_struct.data[ROOT] ? port_struct.data[ROOT][1] : port_struct.data[ROWS]),
                        primitives = !!(prim_struct.data[ROOT] ? prim_struct.data[ROOT][1] : prim_struct.data[ROWS]);
                    if (grid_type) {
                        if (initial_load) initialize();
                        if (types_emitted || !(portfolio || primitives)) return;
                        types_emitted = true;
                        return fire(data.events.types, {portfolio: portfolio, primitives: primitives});
                    }
                    if (portfolio) return (grid_type = config.type = 'portfolio'), initialize();
                    if (primitives) return (grid_type = config.type = 'primitives'), initialize();
                });
            };
            var view_handler = function (result) {
                if (result.error) return og.dev.warn(module.name + ': ' + result.message), result;
                return (view_id = result.meta.id), grid_type ? structure_setup() : type_setup();
            };
            var server_error = function (result) {return !result.message || ~result.message.indexOf('Status: 5');};
            data.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            data.events = {meta: [], data: [], fatal: [], types: []};
            data.id = id;
            data.kill = function () {
                if (view_id) api.del({view_id: view_id}).pipe(function (result) {
                    view_id = null;
                    delete connections[data.id];
                });
            };
            data.meta = meta = {columns: {}};
            data.viewport = function (new_viewport) {
                var viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                if (new_viewport === null) {
                    data.busy(true);
                    if (viewport_id) viewports.del({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id
                    }).pipe(function (result) {data.busy(false);});
                    viewport = null; viewport_id = null;
                    return data;
                }
                if (!new_viewport.rows.length || !new_viewport.cols.length)
                    return og.dev.warn(module.name + ': nonsensical viewport, ', new_viewport), data;
                viewport = new_viewport;
                if (!viewport_id) return loading_viewport_id ? data : data_setup(), data;
                data.busy(true);
                viewports.put({
                    view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id,
                    rows: viewport.rows, columns: viewport.cols, expanded: !!viewport.expanded
                }).pipe(function (result) {
                    if (result.error) return; else (viewport_version = result.data.version), data.busy(false);
                });
                return data;
            };
            connections[data.id] = data;
            initialize();
        };
        constructor.prototype.off = og.common.events.off;
        constructor.prototype.on = og.common.events.on;
        return constructor;
    }
});