/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Depgraph',
    dependencies: ['og.analytics.Grid'],
    obj: function () {
        var Grid = og.analytics.Grid, Depgraph = function (config) {
            Grid.call(this, {
                selector: config.selector, child: config.child, cellmenu: !~config.selector.indexOf('inplace'),
                show_sets: false, show_views: false, collapse_level: 1,
                source: $.extend({depgraph: true, row: config.row, col: config.col}, config.source)
            });
        };
        Depgraph.prototype = Object.create(Grid.prototype);
        Depgraph.prototype.label = 'depgraph';
        return Depgraph;
    }
});