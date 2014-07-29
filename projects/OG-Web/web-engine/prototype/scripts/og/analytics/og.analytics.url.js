/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.url',
    dependencies: ['og.common.routes', 'og.api.rest'],
    obj: function () {
        var url, last = {}, routes = og.common.routes,
            main_selector = '.OG-layout-analytics-center',
            panels = ['south', 'dock-north', 'dock-center', 'dock-south'];
        var go = function (params) {
            og.api.rest.compressor.put({content: params || last, dependencies: ['data']}).pipe(function (result) {
                var current = routes.current(), hash = routes
                    .hash(og.views[og.analytics.blotter ? 'blotter' : 'analytics']
                        .rules.load_item, {data: result.data.data});
                if (current.hash === hash) return url.process(current.args);
                routes.go(hash, 'loading title...');
            });
        };
        return url = {
            add: function (container, params) {
                var new_params = $.extend({}, last);
                new_params[container] = (new_params[container] || []).concat(params);
                return go(new_params), url;
            },
            clear_main: function () {
                if (og.analytics.grid) og.analytics.grid.kill();
                last.main = null;
                return url;
            },
            last: last,
            launch: function (params) {
                var win = window.open(), url = './gadget.ftl' + routes.prefix() + '/gadgetscontainer/';
                og.api.rest.compressor.put({content: [params]})
                    .pipe(function (result) {win.location.href = url + result.data.data;});
            },
            main: function (params) {
                url.clear_main();
                $(main_selector).html('requesting...');
                return go($.extend({}, last, {main: params})), url;
            },
            move: function (from, to) {
                var new_params = $.extend({}, last);
                new_params[to.panel] = (new_params[to.panel] || []).concat(to.params);
                if (new_params[from.panel] && new_params[from.panel].length)
                    new_params[from.panel].splice(from.index, 1);
                if (!new_params[from.panel].length) delete new_params[from.panel];
                return go(new_params), url;
            },
            process: function (args, handler) {
                $.when(args.data ? og.api.rest.compressor.get({content: args.data, dependencies: ['data']}) : void 0)
                .then(function (result) {
                    var config = result ? result.data.data : {}, blotter = og.analytics.blotter;
                    if (config.main && !Object.equals(config.main, last.main)) {
                        new og.analytics.Form({callback: og.analytics.url.main, data: config.main});
                        if (og.analytics.grid) {
                            og.analytics.grid.kill();
                        }
                        og.analytics.grid = new og.common.gadgets.Grid({
                            selector: main_selector, cellmenu: true, show_save: blotter,
                            source: !!blotter ? $.extend({blotter: blotter}, config.main) : config.main
                        }).on('viewchange', function (view) {
                            url.main($.extend({}, og.analytics.grid.source, {type: view}));
                        }).on('cycle', function (cycle) {
                            og.analytics.status.cycle(cycle.duration);
                        }).on('fatal', url.clear_main);
                        if (og.analytics.blotter) og.analytics.grid.on('contextmenu', function (event, cell, col) {
                            if (cell) return og.blotter.contextmenu(cell, event);
                        });
                    } else if (!config.main) {
                        new og.analytics.Form({callback: og.analytics.url.main});
                    }
                    last.main = config.main;
                    og.common.routes.set_title('loading title...');
                    $.when(last.main ? og.api.rest.configs.get({id: last.main.viewdefinition}) : null)
                        .then(function (result) {
                            var title = result ? !result.error ? result.data.template_data.name : null : null;
                            title = title || $('.OG-analytics-form .og-view input').val() || 'Analytics';
                            routes.set_title(title);
                            if (last.main) og.common.util.history
                                .put({name: title, item: 'history.analytics.recent', value: routes.current().hash});
                        });
                    panels.forEach(function (panel) {
                        var gadgets = config[panel], new_gadgets = [];
                        if (!gadgets || !gadgets.length) return last[panel] = [];
                        if (!last[panel]) last[panel] = [];
                        last[panel] = gadgets.map(function (gadget, index) {
                            if (Object.equals(gadget, last[panel][index])) return gadget;
                            new_gadgets.push($.extend({}, gadget));
                            if (typeof new_gadgets.add_index === 'undefined') new_gadgets.add_index = index;
                            return gadget;
                        });
                        if (new_gadgets.length) og.analytics.containers[panel]
                            .add(new_gadgets, new_gadgets.length === 1 ? new_gadgets.add_index : void 0);
                    });
                    panels.forEach(function (panel) {
                        if (og.analytics.containers[panel]) og.analytics.containers[panel].verify(last[panel]);
                    });
                    if (handler) handler();
                });
                return url;
            },
            remove: function (container, index) {
                if (!last[container] || !last[container].length) return;
                last[container].splice(index, 1);
                if (!last[container].length) delete last[container];
                return go(), url;
            },
            swap: function (container, params, index) {
                return (last[container][index] = params), go(), url;
            }
        };
    }
});