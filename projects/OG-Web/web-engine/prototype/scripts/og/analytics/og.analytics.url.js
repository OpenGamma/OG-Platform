/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.url',
    dependencies: ['og.common.routes', 'og.api.rest'],
    obj: function () {
        var url, last_fingerprint = {}, last_object = {}, routes = og.common.routes,
            main_selector = '.OG-layout-analytics-center',
            panels = ['south', 'dock-north', 'dock-center', 'dock-south'];
        var go = function () {
            og.api.rest.compressor.put({content: last_object, dependencies: ['data']}).pipe(function (result) {
                routes.go(routes.hash(og.views.analytics2.rules.load_item, {data: result.data.data}));
            });
        };
        return url = {
            add: function (container, params, silent) {
                return (last_object[container] || (last_object[container] = [])).push(params), (!silent && go()), url;
            },
            last: last_object,
            launch: function (params) {
                var win = window.open(), url = './gadget.ftl' + routes.prefix() + '/gadgetscontainer/';
                og.api.rest.compressor.put({content: [params]})
                    .pipe(function (result) {win.location.href = url + result.data.data;});
            },
            main: function (params) {
                if (og.analytics.grid) og.analytics.grid.dataman.kill();
                $(main_selector).html('requesting...');
                return (last_object.main = params), go(), url;
            },
            process: function (args) {
                og.api.rest.compressor.get({content: args.data, dependencies: ['data']})
                    .pipe(function (result) {
                        var config = result.data.data, current_main, panel, cellmenu;
                        panels.forEach(function (panel) {delete last_object[panel];});
                        if (config.main && last_fingerprint.main !== (current_main = JSON.stringify(config.main))) {
                            if (og.analytics.grid) og.analytics.grid.dataman.kill();
                            og.analytics.grid = new og.analytics.Grid({
                                selector: main_selector, cellmenu: true,
                                source: last_object.main = JSON.parse(last_fingerprint.main = current_main)
                            }).on('viewchange', function (view) {
                                url.main($.extend({}, og.analytics.grid.source, {type: view}));
                            });
                        }
                        panels.forEach(function (panel) {
                            var gadgets = config[panel];
                            if (!gadgets) return (last_fingerprint[panel] = []), (last_object[panel] = []);
                            if (!last_fingerprint[panel]) last_fingerprint[panel] = [];
                            if (!last_object[panel]) last_object[panel] = [];
                            last_fingerprint[panel] = gadgets.map(function (gadget, index) {
                                var current_gadget = JSON.stringify(gadget);
                                last_object[panel][index] = JSON.parse(current_gadget);
                                if (last_fingerprint[panel][index] === current_gadget) return current_gadget;
                                og.analytics.containers[panel].add([gadget], index, current_gadget);
                                return current_gadget;
                            });
                        });
                        panels.forEach(function (panel) {
                            og.analytics.containers[panel].verify(last_fingerprint[panel]);
                        });
                    });
                return url;
            },
            remove: function (container, index, silent) {
                if (!last_fingerprint[container] || !last_fingerprint[container].length) return;
                last_fingerprint[container].splice(index, 1);
                last_object[container].splice(index, 1);
                if (!last_fingerprint[container].length) delete last_fingerprint[container];
                if (!last_object[container].length) delete last_object[container];
                return (!silent && go()), url;
            }
        };
    }
});