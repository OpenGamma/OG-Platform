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
        var constructor = function (config, bypass_types, label) {
            var data = this, api = og.api.rest.views, id = 'data_' + counter++ + '_' + +new Date, meta,
                viewport = null, view_id, graph_id, viewport_id, viewport_cache, prefix,
                viewport_version, subscribed = false, ROOT = 'rootNode', SETS = 'columnSets', ROWS = 'rowCount',
                grid_type = null, depgraph = !!config.depgraph, types_emitted = false, loading_viewport_id = false,
                fixed_set = {portfolio: 'Portfolio', primitives: 'Primitives'};
            label = label ? label + '-' : '';
            var data_handler = (function () {
                var timeout = null, rate = 500, last = +new Date, current, delta, handler = function (result) {
                    if (!result || result.error) // do not kill connection even if there is an error, just warn
                        return og.dev.warn(data.prefix + (result && result.message || 'reset connection'));
                    if (!data.events.data.length || !result.data) return; // if a tree falls or there's no tree, etc.
                    if (viewport && viewport.empty !== true && result.data.version === viewport_version)
                        fire('data', result.data.data);
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
                var viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                subscribed = true;
                (viewport_id ? viewports.get({
                    view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                    viewport_id: viewport_id, update: data_setup
                }) : viewports.put({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                        loading: function () {loading_viewport_id = true;},
                        rows: viewport.rows, columns: viewport.cols, format: viewport.format
                    }).pipe(function (result) {
                        loading_viewport_id = false;
                        if (result.error) return (data.prefix = module.name + ' (' + label + view_id + '-dead):\n'),
                            (view_id = graph_id = viewport_id = subscribed = null), result; // goes to data_setup
                        (viewport_id = result.meta.id), (viewport_version = result.data.version);
                        return viewports.get({
                            view_id: view_id, grid_type: grid_type, graph_id: graph_id,
                            viewport_id: viewport_id, update: data_setup
                        });
                    })
                ).pipe(data_handler);
            };
            var disconnect_handler = function () {data.disconnect(data.prefix + 'disconnected');};
            var fire = (function () {
                var fatal_fired = false, types;
                return function (type) {
                    var args = Array.prototype.slice.call(arguments);
                    try {
                        if (type !== 'fatal') return og.common.events.fire.apply(data, args);
                        if (!fatal_fired) return (fatal_fired = true), og.common.events.fire.apply(data, args);
                    } catch (error) {og.dev.warn(data.prefix + 'a ' + type + ' handler threw ', error);}
                }
            })();
            var initialize = function () {
                var message, put_options = ['viewdefinition', 'aggregators', 'providers']
                    .reduce(function (acc, val) {return (acc[val] = config[val]), acc;}, {});
                if (depgraph || bypass_types) grid_type = config.type; // don't bother with type_setup
                if (view_id && grid_type) return structure_setup().pipe(structure_handler);
                if (grid_type) return api.put(put_options).pipe(view_handler).pipe(structure_handler);
                try {api.put(put_options).pipe(view_handler);} // initial request params come from outside so try/catch
                catch (error) {fire('fatal', data.prefix + error.message);}
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
                fire('meta', meta);
                if (!subscribed) return data_setup();
            };
            var same_viewport = function (one, two) {
                if ((!one || !two) && one !== two) return false; // if either viewport is null
                return one.rows.join('|') === two.rows.join('|') && one.cols.join('|') === two.cols.join('|') &&
                    one.format === two.format;
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
                            if (result.error) return fire('fatal', data.prefix + result.message);
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
                        return fire('types', {portfolio: portfolio, primitives: primitives});
                    }
                    grid_type = config.type = portfolio ? 'portfolio' : primitives ? 'primitives' : grid_type;
                    if (portfolio && primitives && !types_emitted) // if both come back immediately, fire types event
                        (types_emitted = true), fire('types', {portfolio: portfolio, primitives: primitives});
                    if (portfolio || primitives) initialize();
                });
            };
            var view_handler = function (result) {
                if (result.error) return fire('fatal', data.prefix + result.message);
                data.prefix = module.name + ' (' + label + (view_id = result.meta.id) + '):\n';
                return grid_type ? structure_setup() : type_setup();
            };
            og.common.events.register.call(data, 'data', 'fatal', 'meta', 'types');
            data.disconnect = function () {
                if (arguments.length) og.dev.warn.apply(null, Array.prototype.slice.call(arguments));
                if (view_id) api.del({view_id: view_id});
                data.prefix = module.name + ' (' + label + view_id + '-dead' + '):\n';
                view_id = graph_id = viewport_id = subscribed = null;
            };
            data.id = id;
            data.kill = function () {
                data.disconnect.apply(data, Array.prototype.slice.call(arguments));
                delete connections[data.id];
                og.api.rest.off('disconnect', disconnect_handler).off('reconnect', reconnect_handler);
            };
            data.meta = meta = {columns: {}};
            data.prefix = prefix = module.name + ' (' + label + 'undefined' + '):\n';
            data.viewport = function (new_viewport) {
                var viewports = (depgraph ? api.grid.depgraphs : api.grid).viewports;
                if (new_viewport === null) {
                    if (viewport_id) viewports
                        .del({view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id});
                    viewport = viewport_cache = viewport_id = null;
                    if (meta.viewport) (meta.viewport.cols = []), (meta.viewport.rows = []);
                    return data;
                }
                if (!new_viewport.rows.length || !new_viewport.cols.length)
                    return og.dev.warn(data.prefix + 'nonsensical viewport, ', new_viewport), data;
                if (same_viewport(viewport_cache, new_viewport)) return data; // duplicate viewport, do nothing
                viewport_cache = JSON.parse(JSON.stringify(data.meta.viewport = viewport = new_viewport));
                if (!viewport_id) return loading_viewport_id ? data : data_setup(), data;
                try { // viewport definitions come from outside, so try/catch
                    viewports.put({
                        view_id: view_id, grid_type: grid_type, graph_id: graph_id, viewport_id: viewport_id,
                        rows: viewport.rows, columns: viewport.cols, format: viewport.format
                    }).pipe(function (result) {if (result.error) return; else viewport_version = result.data.version;});
                } catch (error) {fire('fatal', data.prefix + error.message);}
                return data;
            };
            connections[data.id] = data;
            og.api.rest.on('disconnect', disconnect_handler).on('reconnect', reconnect_handler);
            data.on('fatal', function (message) {data.kill(message);});
            setTimeout(initialize); // allow events to be attached
        };
        constructor.prototype.off = og.common.events.off;
        constructor.prototype.on = og.common.events.on;
        return constructor;
    }
});