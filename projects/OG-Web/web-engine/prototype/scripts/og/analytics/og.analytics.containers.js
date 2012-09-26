/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.containers',
    dependencies: ['og.common.gadgets.GadgetsContainer'],
    obj: function () {
        var GadgetsContainer = og.common.gadgets.GadgetsContainer, containers = {
            initialize: function (args) {
                ['south', 'dock-north', 'dock-center', 'dock-south'].forEach(function (val) {
                    containers[val] = new GadgetsContainer('.OG-layout-analytics-', val)
                        .add(args[val])
                        .on('del', function (index) {og.analytics.url.remove(val, index);});
                });
                delete containers.initialize;
            }
        };
        return containers;
    }
});