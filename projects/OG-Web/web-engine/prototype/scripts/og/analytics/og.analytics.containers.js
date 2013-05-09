/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.containers',
    dependencies: ['og.common.gadgets.GadgetsContainer'],
    obj: function () {
        var GadgetsContainer = og.common.gadgets.GadgetsContainer, containers = {
            initialize: function () {
                var panels = ['south', 'dock-north', 'dock-center', 'dock-south'], move = null;
                panels.forEach(function (panel) {
                    containers[panel] = new GadgetsContainer('.OG-layout-analytics-', panel)
                        .init()
                        .on('del', function (index) {
                            if (move) move = og.analytics.url.move({panel: panel, index: index}, move), null;
                            else og.analytics.url.remove(panel, index);
                        })
                        .on('drop', function (params, source) {
                            move = ~panels.indexOf(source) && {panel: panel, params: params};
                            if (!move) og.analytics.url.add(panel, params); 
                            return false;
                        })
                        .on('swap', function (params, index) {og.analytics.url.swap(panel, params, index);})
                        .on('launch', og.analytics.url.launch);
                });
                delete containers.initialize;
            }
        };
        containers.on = og.common.events.on;
        containers.off = og.common.events.off;
        containers.fire = og.common.events.fire;
        containers.on('cellhighlight', function (source, row, col) {
            if (!og.analytics.grid) return;
            if (Object.equals(source, og.analytics.grid.source)) og.analytics.grid.highlight(row, col);
        })
        return containers;
    }
});