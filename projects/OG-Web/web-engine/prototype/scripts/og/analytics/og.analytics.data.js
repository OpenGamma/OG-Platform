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
                viewport = null, view_id = config.view, graph_id, viewport_id, connection_subscribed = false,
                viewport_version, subscribed = false, ROOT = 'rootNode', SETS = 'columnSets', ROWS = 'rowCount',
                grid_type = null, depgraph = !!config.depgraph, types_emitted = false, loading_viewport_id = false,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'};
            var data_handler = function (result) {
                data.busy(false);
                if (!result || result.error) // do not kill connection even if there is an error, just warn
                    return og.dev.warn(module.name + ': ' + (result && result.message || 'reset connection'));
                if (!data.events.data.length || !result.data) return; // if a tree falls or there's no tree, etc.
                if (viewport && viewport.empty !== true && result.data.version === viewport_version)
                    fire(data.events.data, result.data.data);
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
            var disconnect_handler = function () {data.disconnect(module.name + ': disconnected');};
            var fire = (function () {
                var fatal_fired = false, types;
                return function () {
                    var events = arguments[0], args = Array.prototype.slice.call(arguments),
                        type = (types || (types = Object.keys(data.events)))
                            .filter(function (type) {return data.events[type] === events;})[0];
                    try {
                        if (events !== data.events.fatal) return og.common.events.fire.apply(data, args);
                        if (!fatal_fired) return (fatal_fired = true), og.common.events.fire.apply(data, args);
                    } catch (error) {
                        og.dev.warn(module.name + ': a ' + type + ' handler threw ', error);
                    }
                }
            })();
            var initialize = function () {
                var message, put_options = ['viewdefinition', 'aggregators', 'providers']
                    .reduce(function (acc, val) {return (acc[val] = config[val]), acc;}, {});
                if (depgraph || bypass_types) grid_type = config.type; // don't bother with type_setup
                if (view_id && grid_type) return structure_setup().pipe(structure_handler);
                if (grid_type) return api.put(put_options).pipe(view_handler).pipe(structure_handler);
                try {api.put(put_options).pipe(view_handler);} // initial request params come from outside so try/catch
                catch (error) {
                    data.kill(message = module.name + ': ' + error.message), fire(data.events.fatal, message);
                }
            };
            var reconnect_handler = function () {initialize();};
            var structure_handler = function (result) {
                var message = module.name + ': ' + result.message;
                if (!grid_type || (depgraph && !graph_id)) return;
                if (result.error) return data.kill(message), fire(data.events.fatal, message);
                if (!result.data[SETS].length) return;
                meta.data_rows = result.data[ROOT] ? result.data[ROOT][1] + 1 : result.data[ROWS];
                meta.structure = result.data[ROOT] || [];
                meta.columns.fixed = [{name: fixed_set[grid_type], columns: result.data[SETS][0].columns}];
                meta.columns.scroll = result.data[SETS].slice(1);
                fire(data.events.meta, meta);
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
                            var message = module.name + ': ' + result.message;
                            if (result.error) return data.kill(message), fire(data.events.fatal, message);
                            return api.grid.depgraphs.structure.get({
                                view_id: view_id, grid_type: grid_type,
                                graph_id: (graph_id = result.meta.id), update: initialize
                            });
                        })
                    })
                    : api.grid.structure.get({view_id: view_id, grid_type: grid_type, update: initialize});
            };
            var type_setup = function () {
                var port_request, prim_request, initial_load = !!config.type && (grid_type === null);
                if (!grid_type) grid_type = config.type;
                port_request = api.grid.structure.get({view_id: view_id, update: type_setup, grid_type: 'portfolio'});
                prim_request = api.grid.structure.get({view_id: view_id, update: type_setup, grid_type: 'primitives'});
                $.when(port_request, prim_request).then(function (port_struct, prim_struct) {
                    var portfolio = port_struct.data &&
                            !!(port_struct.data[ROOT] ? port_struct.data[ROOT][1] : port_struct.data[ROWS]),
                        primitives = prim_struct.data &&
                            !!(prim_struct.data[ROOT] ? prim_struct.data[ROOT][1] : prim_struct.data[ROWS]);
                    if (grid_type) {
                        if (initial_load) initialize();
                        if (types_emitted || !(portfolio || primitives)) return;
                        types_emitted = true;
                        return fire(data.events.types, {portfolio: portfolio, primitives: primitives});
                    }
                    grid_type = config.type = portfolio ? 'portfolio' : primitives ? 'primitives' : grid_type;
                    if (portfolio && primitives && !types_emitted) // if both come back immediately, fire types event
                        (types_emitted = true), fire(data.events.types, {portfolio: portfolio, primitives: primitives});
                    if (portfolio || primitives) initialize();
                });
            };
            var view_handler = function (result) {
                var message = module.name + ': ' + result.message;
                if (result.error) return data.kill(message), fire(data.events.fatal, message);
                return (view_id = result.meta.id), grid_type ? structure_setup() : type_setup();
            };
            data.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            data.disconnect = function () {
                if (arguments.length) og.dev.warn.apply(null, Array.prototype.slice.call(arguments));
                if (view_id) api.del({view_id: view_id})
                    .pipe(function (result) {view_id = graph_id = viewport_id = subscribed = null;});
                else view_id = graph_id = viewport_id = subscribed = null;
            };
            data.events = {meta: [], data: [], fatal: [], types: []};
            data.id = id;
            data.kill = function () {
                data.disconnect.apply(data, Array.prototype.slice.call(arguments));
                delete connections[data.id];
                og.api.rest.off('disconnect', disconnect_handler).off('reconnect', reconnect_handler);
            };
            data.meta = meta = {columns: {}};
            data.viewport = function (new_viewport) {
                var message, viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                if (new_viewport === null) {
                    if (viewport_id && data.busy(true)) viewports.del({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id
                    }).pipe(function (result) {data.busy(false);});
                    viewport = null; viewport_id = null;
                    if (meta.viewport) (meta.viewport.cols = []), (meta.viewport.rows = []);
                    return data;
                }
                if (!new_viewport.rows.length || !new_viewport.cols.length)
                    return og.dev.warn(module.name + ': nonsensical viewport, ', new_viewport), data;
                data.meta.viewport = viewport = new_viewport;
                if (!viewport_id) return loading_viewport_id ? data : data_setup(), data;
                data.busy(true);
                try { // viewport definitions come from outside, so try/catch
                    viewports.put({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id,
                        rows: viewport.rows, columns: viewport.cols, expanded: !!viewport.expanded
                    }).pipe(function (result) {
                        if (result.error) return; else (viewport_version = result.data.version), data.busy(false);
                    });
                } catch (error) {
                    data.kill(message = module.name + ': ' + error.message), fire(data.events.fatal, message);
                }
                return data;
            };
            connections[data.id] = data;
            og.api.rest.on('disconnect', disconnect_handler).on('reconnect', reconnect_handler);
            setTimeout(initialize); // allow events to be attached
        };
        constructor.prototype.off = og.common.events.off;
        constructor.prototype.on = og.common.events.on;
        return constructor;
    }
});