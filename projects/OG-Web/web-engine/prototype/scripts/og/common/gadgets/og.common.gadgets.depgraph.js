/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Depgraph',
    dependencies: ['og.common.gadgets.Grid'],
    obj: function () {
        var Grid = og.common.gadgets.Grid, Depgraph = function (config) {
            var depgraph = this, containers;
            Grid.call(depgraph, {selector: config.selector, child: config.child,
                cellmenu: !~config.selector.indexOf('inplace'), show_sets: false, show_views: false, collapse_level: 1,
                source: $.extend({depgraph: true, req: config.req, colset: config.colset}, config.source)
                });
            if (!og.analytics.containers) { // highlighting only works in analytics view (for now)
                return;
            }
            containers = og.analytics.containers;
            var highlight = function (parent, row, col, event_type) {
                if (Object.equals(parent, depgraph.source)) {
                    depgraph.highlight(row, col, event_type);
                } else {
                    depgraph.highlight();
                }
            };
            containers.on('cellhighlight', highlight);
            containers.on('cellhighlightinplace', highlight);
            depgraph.on('kill', function () {
                containers.off('cellhighlight', highlight);
                containers.off('cellhighlightinplace', highlight);
            });
        };
        Depgraph.prototype = Object.create(Grid.prototype);
        Depgraph.prototype.label = 'depgraph';
        return Depgraph;
    }
});