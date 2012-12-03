/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Depgraph',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this, menu;
        return function (config) {
            var gadget = this, alive = og.common.id('gadget_depgraph'),
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0}, grid;
            gadget.alive = function () {return grid.alive();};
            gadget.load = function () {
                var source = $.extend({depgraph: true, row: config.row, col: config.col}, config.source);
                $(config.selector).addClass(alive).css(css_position);
                menu = (config.selector.indexOf('inplace') >= 0) ? false : true;
                grid = new og.analytics.Grid({
                    selector: config.selector, source: source, cellmenu: menu, child: config.child
                });
            };
            gadget.load();
            gadget.resize = function () {
                if (grid) grid.resize(); else throw new Error(module.name + ': no grid to resize');
            };
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});