/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Data',
    dependencies: ['og.api.rest'],
    obj: function () {
        var module = this, connections = {};
        $(window).on('unload', function () {
            Object.keys(connections).forEach(function (key) {try {connections[key].kill();} catch (error) {}});
        });
        var Data = function (source, config, label) {
            var data = this, api = og.api.rest.views, meta, label = config.label ? config.label + '-' : '',
                viewport = null, viewport_id, viewport_cache, prefix, view_id = config.view_id, viewport_version,
                graph_id = config.graph_id, subscribed = false, ROOT = 'rootNode', SETS = 'columnSets',
                ROWS = 'rowCount', grid_type = null, depgraph = !!source.depgraph, loading_viewport_id = false,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'}, bypass_types = config.bypass;
            var data_handler = (function () {
                var timeout = null, rate = 500, last = +new Date, current, delta;
                var handler = function (result) {
                    if (!result || result.error) // do not kill connection even if there is an error, just warn
                        return og.dev.warn(data.prefix + (result && result.message || 'reset connection'));
                    if (result.data && result.data.version === viewport_version) fire('data', result.data.data);
                };
                return function (result) {
                    clearTimeout(timeout);
                    if (!view_id) return; // connection is dead
                    if ((delta = (current = +new Date) - last) >= rate) return (last = current), handler(result);
                    timeout = setTimeout(data_handler.partial(result), rate - delta);
                };
            })();
            var data_setup = function () {
                if (!view_id || !viewport) return;
                var promise, viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                subscribed = true;
                (viewport_id ? viewports.get({
                    view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                    viewport_id: viewport_id, update: data_setup
                }) : (promise = viewports.put({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                        loading: function () {loading_viewport_id = true;},
                        rows: viewport.rows, cols: viewport.cols, cells: viewport.cells,
                        format: viewport.format, log: viewport.log
                    })).pipe(function (result) {
                        loading_viewport_id = false;
                        if (result.error) return (data.prefix = module.name + ' (' + label + view_id + '-dead):\n'),
                            (view_id = graph_id = viewport_id = subscribed = null), result; // goes to data_setup
                        viewport_id = result.meta.id; viewport_version = promise.id;
                        return viewports.get({
                            view_id: view_id, grid_type: grid_type, graph_id: graph_id, dry: true,
                            viewport_id: viewport_id, update: data_setup
                        });
                    })
                ).pipe(data_handler);
            };
            var disconnect_handler = function () {fire('disconnect'), data.disconnect(data.prefix + 'disconnected');};
            var fire = (function () {
                var fatal_fired = false;
                return function (type) {
                    var args = Array.prototype.slice.call(arguments);
                    try {
                        if (type === 'fatal' && !fatal_fired) // fire only once ever
                            return (fatal_fired = true), og.common.events.fire.apply(data, args);
                        og.common.events.fire.apply(data, args);
                    } catch (error) {og.dev.warn(data.prefix + 'a ' + type + ' handler threw ', error);}
                }
            })();
            var initialize = function () {
                var message, put_options = ['viewdefinition', 'aggregators', 'providers']
                    .reduce(function (acc, val) {return (acc[val] = source[val]), acc;}, {});
                if (depgraph || bypass_types) grid_type = source.type; // don't bother with type_setup
                if (view_id && grid_type) return structure_setup();
                if (grid_type) return api.put(put_options).pipe(view_handler).pipe(structure_handler);
                try {api.put(put_options).pipe(view_handler);} // initial request params come from outside so try/catch
                catch (error) {fire('fatal', data.prefix + error.message);}
            };
            var nonsensical_viewport = function (viewport) {
                return !(viewport.cells && viewport.cells.length) &&
                    (!viewport.rows || !viewport.rows.length || !viewport.cols || !viewport.cols.length);
            };
            var reconnect_handler = function () {initialize();};
            var structure_handler = function (result) {
                if (!grid_type || (depgraph && !graph_id)) return;
                if (result.error) return fire('fatal', data.prefix + result.message);
                if (!result.data[SETS].length) return;
                meta.data_rows = result.data[ROOT] ? result.data[ROOT][1] + 1 : result.data[ROWS];
                meta.structure = result.data[ROOT] || [];
                meta.columns.fixed = [{name: fixed_set[grid_type], columns: result.data[SETS][0].columns}];
                meta.columns.scroll = result.data[SETS].slice(1);
                fire('meta', meta, {grid_type: grid_type, view_id: view_id, graph_id: graph_id, meta: result});
                if (!subscribed) return data_setup();
            };
            var same_viewport = function (one, two) {
                if ((!one || !two) && one !== two) return false; // if either viewport is null
                return one.rows.join('|') === two.rows.join('|') && one.cols.join('|') === two.cols.join('|') &&
                    one.format === two.format;
            };
            var structure_setup = function (update) {
                var initial = !update;
                if (initial) return api.grid.structure
                    .get({view_id: view_id, grid_type: grid_type, update: structure_setup, initial: initial});
                api.grid.structure.get({view_id: view_id, grid_type: grid_type, update: structure_handler})
                    .pipe(function (result) {
                        if (result.error) return fire('fatal', data.prefix + result.message);
                        return !depgraph ? structure_handler(result) : api.grid.depgraphs.put({
                            view_id: view_id, grid_type: grid_type, row: source.row, col: source.col
                        }).pipe(function (result) {
                            if (result.error) return fire('fatal', data.prefix + result.message);
                            return api.grid.depgraphs.structure
                                .get({view_id: view_id, grid_type: grid_type, graph_id: (graph_id = result.meta.id)})
                                .pipe(structure_handler);
                        })
                    });
            };
            var type_setup = function (update) {
                var port_request, prim_request, initial = grid_type === null;
                grid_type = source.type;
                port_request = api.grid.structure
                    .get({dry: initial, view_id: view_id, update: initial ? type_setup : null, grid_type: 'portfolio'});
                if (initial) return /* just register interest and bail*/; else prim_request = api.grid.structure
                    .get({view_id: view_id, grid_type: 'primitives'});
                $.when(port_request, prim_request).then(function (port_struct, prim_struct) {
                    var portfolio = port_struct.data &&
                            !!(port_struct.data[ROOT] ? port_struct.data[ROOT][1] : port_struct.data[ROWS]),
                        primitives = prim_struct.data &&
                            !!(prim_struct.data[ROOT] ? prim_struct.data[ROOT][1] : prim_struct.data[ROWS]);
                    if (!grid_type)
                        grid_type = source.type = portfolio ? 'portfolio' : primitives ? 'primitives' : grid_type;
                    structure_handler(grid_type === 'portfolio' ? port_struct : prim_struct);
                    fire('types', {portfolio: portfolio, primitives: primitives});
                });
            };
            var view_handler = function (result) {
                if (result.error) return fire('fatal', data.prefix + result.message);
                data.prefix = module.name + ' (' + label + (view_id = result.meta.id) + '):\n';
                return grid_type ? structure_setup() : type_setup();
            };
            data.disconnect = function () {
                if (arguments.length) og.dev.warn.apply(null, Array.prototype.slice.call(arguments));
                if (view_id && !data.parent) api.del({view_id: view_id});
                data.prefix = module.name + ' (' + label + view_id + '-dead' + '):\n';
                view_id = graph_id = viewport_id = subscribed = null;
            };
            data.id = og.common.id('data');
            data.kill = function () {
                data.disconnect.apply(data, Array.prototype.slice.call(arguments));
                delete connections[data.id];
                if (!data.parent)
                    og.api.rest.off('disconnect', disconnect_handler).off('reconnect', reconnect_handler);
            };
            data.meta = meta = {columns: {}};
            data.parent = config.parent;
            data.prefix = prefix = module.name + ' (' + label + 'undefined' + '):\n';
            data.reconnect = function (connection) {
                (view_id = connection.view_id), (graph_id = connection.graph_id), initialize();
            };
            data.viewport = function (new_viewport) {
                var promise, viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                if (new_viewport === null) {
                    if (viewport_id) viewports
                        .del({view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id});
                    viewport = viewport_cache = viewport_id = null;
                    if (meta.viewport) (meta.viewport.cols = []), (meta.viewport.rows = []);
                    return data;
                }
                if (nonsensical_viewport(new_viewport))
                    return og.dev.warn(data.prefix + 'nonsensical viewport, ', new_viewport), data;
                if (same_viewport(viewport_cache, new_viewport)) return data; // duplicate viewport, do nothing
                viewport_cache = JSON.parse(JSON.stringify(data.meta.viewport = viewport = new_viewport));
                if (!viewport_id) return loading_viewport_id ? data : data_setup(), data;
                try { // viewport definitions come from outside, so try/catch
                    (promise = viewports.put({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id,
                        rows: viewport.rows, cols: viewport.cols, cells: viewport.cells,
                        format: viewport.format, log: viewport.log
                    })).pipe(function (result) {if (result.error) return;});
                    viewport_version = promise.id;
                } catch (error) {fire('fatal', data.prefix + error.message);}
                return data;
            };
            connections[data.id] = data;
            data.on('fatal', function (message) {data.kill(message);});
            if (data.parent) { // use parent's connection information
                bypass_types = true; // child data connections don't need to know about grid types
                data.parent.on('meta', function (meta, raw) {
                    data.disconnect();
                    view_id = raw.view_id; graph_id = raw.graph_id; grid_type = raw.grid_type;
                    structure_handler(raw.meta);
                });
            } else {
                og.api.rest.on('disconnect', disconnect_handler).on('reconnect', reconnect_handler);
                setTimeout(initialize); // allow events to be attached
            }
        };
        Data.prototype.off = og.common.events.off;
        Data.prototype.on = og.common.events.on;
        return Data;
    }
});