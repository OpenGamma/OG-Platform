/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Depgraph',
    dependencies: ['og.common.gadgets.manager', 'og.analytics.Grid'],
    obj: function () {
        var Depgraph = function (config) {
            og.analytics.Grid.call(this, {
                selector: config.selector, child: config.child, cellmenu: !~config.selector.indexOf('inplace'),
                show_sets: false, show_views: false, start_expanded: false,
                source: $.extend({depgraph: true, row: config.row, col: config.col}, config.source)
            });
        };
        Depgraph.prototype = new og.analytics.Grid;
        Depgraph.prototype.label = 'depgraph';
        return Depgraph;
    }
});