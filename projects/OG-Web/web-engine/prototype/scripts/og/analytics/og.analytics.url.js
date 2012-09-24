/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.url',
    dependencies: ['og.api.rest'],
    obj: function () {
        var last = {};
        return {
            add: function (container, params) {
                // add a gadget to the URL
            },
            process: function (args) {
                og.api.rest.compressor.get({content: args.data})
                    .pipe(function (result) {
                        var config = result.data.data, current_main;
                        if (config.main && last.main !== (current_main = JSON.stringify(config.main))) {
                            // TODO remove global
                            grid = new og.analytics.Grid({selector: '.OG-layout-analytics-center', source: config.main});
                            grid.on('cellhover', function (cell) {
                                if (!cell.value || cell.type === 'PRIMITIVE' || cell.col < 2) return  cellmenu.hide();
                                cellmenu.show(cell);
                            });
                            grid.on('cellselect', function () {});
                            last.main = current_main;
                        }
                        if (config.containers) config.containers.forEach(function (container) {
                            if (!last[container.name]) last[container.name] = [];
                            last[container.name] = container.gadgets.map(function (gadget, index) {
                                var current_gadget = JSON.stringify(gadget);
                                if (last[container.name][index] === current_gadget) return current_gadget;
                                og.analytics.containers[container.name].add([gadget], index)
                                return current_gadget;
                            });
                        });
                    });
            },
            remove: function (container, index) {
                // remove a gadget from the URL
            }
        };
    }
});