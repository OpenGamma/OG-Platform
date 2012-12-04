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
                var panels = ['south', 'dock-north', 'dock-center', 'dock-south'];
                panels.forEach(function (panel) {
                    containers[panel] = new GadgetsContainer('.OG-layout-analytics-', panel)
                        .init()
                        .on('del', function (index) {og.analytics.url.remove(panel, index);})
                        .on('drop', function (params, source) {
                            og.analytics.url.add(panel, params, ~panels.indexOf(source)); 
                            return false;
                        })
                        .on('swap', function (params, index) {
                            og.analytics.url.swap(panel, params, index);
                            return false;
                        })
                        .on('launch', og.analytics.url.launch);
                });
                delete containers.initialize;
            }
        };
        return containers;
    }
});