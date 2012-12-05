/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.CellMenu',
    dependencies: ['og.common.gadgets.mapping'],
    obj: function () {
        var module = this,
            icons = '.og-num, .og-icon-new-window-2',
            open_icon = '.og-small',
            expand_class = 'og-expanded',
            panels = ['south', 'dock-north', 'dock-center', 'dock-south'],
            width = 34,
            mapping = og.common.gadgets.mapping;
        var constructor = function (grid) {
            var cellmenu = this, depgraph = grid.source.depgraph, primitives = grid.source.type === 'primitives',
                parent = grid.elements.parent, inplace_config, timer;
            cellmenu.frozen = false;
            cellmenu.grid = grid;
            if (og.analytics.containers.initialize) throw new Error(module.name + ': there are no panels');
            cellmenu.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            og.api.text({module: 'og.analytics.cell_options'}).pipe(function (template) {
                (cellmenu.menu = $(template)).hide()
                .on('mouseleave', function () {
                    clearTimeout(timer), cellmenu.menu.removeClass(expand_class), cellmenu.hide();
                })
                .on('mouseenter', open_icon, function () {
                    clearTimeout(timer), timer = setTimeout(function () {cellmenu.menu.addClass(expand_class);}, 500);
                })
                .on('mouseenter', function () {$.data(cellmenu, 'hover', true);})
                .on('click', open_icon, function () {
                    cellmenu.destroy_frozen();
                    cellmenu.menu.addClass(expand_class);
                })
                .on('mouseenter', icons, function () {
                    var panel = panels[$(this).text() - 1];
                    panels.forEach(function (val) {og.analytics.containers[val].highlight(true, val === panel);});
                })
                .on('mouseleave', icons, function () {
                    panels.forEach(function (val) {og.analytics.containers[val].highlight(false);});
                })
                .on('click', icons, function () {
                    var panel = panels[+$(this).text() - 1], cell = cellmenu.current,
                        options = mapping.options(cell, grid, panel);
                    cellmenu.destroy_frozen();
                    cellmenu.hide();
                    if (!panel) og.analytics.url.launch(options); else og.analytics.url.add(panel, options);
                });
                grid.on('cellhoverin', function (cell) {
                    if (cellmenu.frozen || cellmenu.busy()) return;
                    cellmenu.menu.removeClass(expand_class);
                    clearTimeout(timer);
                    var type = cell.type, hide =
                        // Hide the cell menu if...
                        !(cellmenu.current = cell).value                    // There is no cell Value
                        || (cell.col === ((!primitives && !depgraph) && 1)) // Second column of portfolio
                        || ((depgraph || primitives) && cell.col < 1)       // First column of depgraph or primitives
                        || (cell.value.nodeId)                              // Is node
                        || (cell.right > parent.width())                             // End of the cell not visible
                        || (depgraph && ~mapping.depgraph_blacklist.indexOf(type));  // Unsupported type on a depgraph
                    if (hide) cellmenu.hide(); else cellmenu.show();
                })
                .on('cellhoverout', function () {
                    clearTimeout(timer);
                    setTimeout(function () {if (!cellmenu.menu.is(':hover')) {cellmenu.hide();}});
                })
                .on('scrollstart', function () {
                    cellmenu.busy(true);
                    if (cellmenu.frozen) cellmenu.destroy_frozen(); else cellmenu.hide();
                })
                .on('scrollend', function () {cellmenu.busy(false);});
                og.api.text({module: 'og.analytics.inplace_tash'}).pipe(function (tmpl_inplace) {
                    var unique = og.common.id('inplace');
                    inplace_config = {$cntr: $('.og-inplace', cellmenu.menu), tmpl: tmpl_inplace, data: {name: unique}};
                    cellmenu.inplace = new og.common.util.ui.DropMenu(inplace_config);
                    cellmenu.container = new og.common.gadgets.GadgetsContainer('.OG-layout-analytics-', unique);
                    cellmenu.inplace.$dom.toggle.on('click', function () {
                        if (cellmenu.inplace.toggle_handler()) {
                            cellmenu.create_inplace();
                            cellmenu.inplace.$dom.menu.blurkill(cellmenu.destroy_frozen.bind(cellmenu));
                        }
                        else cellmenu.destroy_frozen();
                    });
                    cellmenu.container.on('del', function () {cellmenu.destroy_frozen();});
                });
            });
        };
        constructor.prototype.destroy_frozen = function () {
           $('.OG-cell-options.og-frozen').remove();
           og.common.gadgets.manager.clean();
        };
        constructor.prototype.create_inplace = function () {
            var cellmenu = this, panel = 'inplace', options, cell = cellmenu.current, fingerprint,
                offset = cellmenu.inplace.$dom.cntr.offset(), inner = cellmenu.inplace.$dom.menu;
            cellmenu.destroy_frozen();
            cellmenu.frozen = true;
            cellmenu.menu.addClass('og-frozen');
            options = mapping.options(cell, cellmenu.grid, panel);
            fingerprint = JSON.stringify(options);
            options.fingerprint = fingerprint;
            cellmenu.container.add([options]);
            if ((offset.top + inner.height())> $(window).height())
                inner.css({marginTop: -inner.height()});
            if ((offset.left + inner.width())> $(window).width())
                inner.css({marginLeft: -inner.width() + width} );
            new constructor(cellmenu.grid);
        };
        constructor.prototype.hide = function () {
           var cellmenu = this;
            if (cellmenu.menu && cellmenu.menu.length && !cellmenu.frozen) {
                cellmenu.menu.hide();
            }
        };
        constructor.prototype.show = function () {
            var cellmenu = this, current = this.current;
            if (cellmenu.menu && cellmenu.menu.length) cellmenu.menu.appendTo($('body'))
                .css({top: current.top, left: current.right - width + cellmenu.grid.offset.left}).show();
        };
        return constructor;
    }
});
