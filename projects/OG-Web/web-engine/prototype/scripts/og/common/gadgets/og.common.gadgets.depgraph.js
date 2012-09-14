/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.depgraph',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var prefix = 'og_depgraph_gadget_', counter = 1;
        return function (config) {
            var gadget = this, alive = prefix + counter++, selector = $(config.selector),
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0}, grid,
                cellmenu = new og.analytics.CellMenu();
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.load = function () {
                selector.addClass(alive).css(css_position);
                grid = new og.analytics.Grid({
                   selector: selector,
                   source: {
                       type: 'portfolio',
                       depgraph: true,
                       viewdefinition: 'DbCfg~1475588',
                       live: true,
                       provider: 'Live market data (Bloomberg, Activ, TullettPrebon, ICAP)',
                       view_id: 'DbCfg~1475588',
                       row: 5,
                       col: 5
                   }
                });
                grid.on('cellhover', function (cell) {
                    if (!cell.value || cell.type === 'PRIMITIVE') return  cellmenu.hide();
                    cellmenu.show(cell);
                });
            };
            gadget.load();
            gadget.resize = gadget.load;
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});