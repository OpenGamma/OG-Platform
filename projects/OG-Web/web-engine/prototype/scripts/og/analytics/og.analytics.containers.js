/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.containers',
    dependencies: ['og.common.gadgets.GadgetsContainer'],
    obj: function () {
        var GadgetsContainer = og.common.gadgets.GadgetsContainer, analytics = og.analytics, containers = {
            initialize: function () {
                var panels = ['south', 'dock-north', 'dock-center', 'dock-south'], move = null;
                panels.forEach(function (panel) {
                    containers[panel] = new GadgetsContainer('.OG-layout-analytics-', panel)
                        .init()
                        .on('del', function (index) {
                            if (move) {
                                move = analytics.url.move({panel: panel, index: index}, move), null;
                            } else {
                                analytics.url.remove(panel, index);
                            }
                        })
                        .on('drop', function (params, source) {
                            move = ~panels.indexOf(source) && {panel: panel, params: params};
                            if (!move) analytics.url.add(panel, params);
                            return false;
                        })
                        .on('swap', function (params, index) {
                            analytics.url.swap(panel, params, index);
                        })
                        .on('launch', analytics.url.launch);
                });
                delete containers.initialize;
            }
        };
        var highlight_handler = function (source, row, col, event_type) {
            if (!analytics.grid) {
                return;
            }
            if (Object.equals(source, analytics.grid.source)) {
                return analytics.grid.highlight(row, col, event_type);
            }
            analytics.grid.highlight();
        }
        containers.on = og.common.events.on;
        containers.off = og.common.events.off;
        containers.fire = og.common.events.fire;
        containers.on('cellhighlight', highlight_handler);
        containers.on('cellhighlightinplace', highlight_handler);
        return containers;
    }
});