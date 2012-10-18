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
                ['south', 'dock-north', 'dock-center', 'dock-south'].forEach(function (panel) {
                    containers[panel] = new GadgetsContainer('.OG-layout-analytics-', panel)
                        .init()
                        .on('del', function (index) {og.analytics.url.remove(panel, index);})
                        .on('drop', function (params) {return og.analytics.url.add(panel, params, true), false;})
                        .on('launch', og.analytics.url.launch);
                });
                delete containers.initialize;
            }
        };
        return containers;
    }
});