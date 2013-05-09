/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Depgraph',
    dependencies: ['og.common.gadgets.Grid'],
    obj: function () {
        var Grid = og.common.gadgets.Grid, Depgraph = function (config) {
            var depgraph = this, highlight;
            Grid.call(depgraph, {
                selector: config.selector, child: config.child, cellmenu: !~config.selector.indexOf('inplace'),
                show_sets: false, show_views: false, collapse_level: 1,
                source: $.extend({depgraph: true, row: config.row, col: config.col}, config.source)
            });
            og.analytics.containers.on('cellhighlight', highlight = function (parent, row, col) {
                if (!Object.equals(parent, config.source)) return;
                depgraph.highlight(row, col);
            });
            depgraph.kill = function () {
                Grid.prototype.kill.call(depgraph);
                og.analytics.containers.off('cellhighlight', highlight);
            }
        };
        Depgraph.prototype = Object.create(Grid.prototype);
        Depgraph.prototype.label = 'depgraph';
        return Depgraph;
    }
});