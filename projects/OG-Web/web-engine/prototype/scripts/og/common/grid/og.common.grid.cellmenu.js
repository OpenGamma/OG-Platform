/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.grid.CellMenu',
    dependencies: ['og.common.gadgets.mapping'],
    obj: function () {
        var module = this, panels = ['south', 'dock-north', 'dock-center', 'dock-south'],
            mapping = og.common.gadgets.mapping, scroll_size = og.common.util.scrollbar_size;
        var hide_menu = function (grid, cell) {
            var depgraph = grid.source.depgraph, primitives = grid.source.type === 'primitives',
                portfolio = !depgraph && !primitives, blotter = !!grid.source.blotter;
            if (!!cell.value.logLevel) {
                return false; // always show if log exists
            }
            if (cell.right > grid.elements.parent.width() - scroll_size) {
                return true; // end of the cell not visible
            }
            if (blotter && cell.col) {
                return true; // all blotter cols except 1st
            }
            if (cell.type === 'NODE') {
                return true; // is node
            }
            if (!portfolio && cell.col === 0) {
                return true; // 1st column of non-portfolio
            }
            if ((depgraph && ~mapping.depgraph_blacklist.indexOf(cell.type))) {
                return true; // unsupported type on depgraph
            }
            if (portfolio && cell.col === 1 && grid.meta.columns.fixed.length === 2) {
                return true; // 2nd fixed column
            }
            return false; // OTHERWISE: show cell menu
        };
        var constructor = function (grid) {
            var cellmenu = this, primitives = grid.source.type === 'primitives', inplace_config, timer;
            cellmenu.frozen = false;
            cellmenu.setdrag(false);
            cellmenu.grid = grid;
            cellmenu.busy = (function (busy) {
                return function (value) {
                    busy = value !== 'undefined' ? value : busy;
                    return busy;
                };
            }(false));
            og.api.text({module: 'og.views.gadgets.grid.cell_options'}).pipe(function (template) {
                (cellmenu.menu = $(template)).hide();
                grid.on('cellhoverin', function (cell) {
                    if (cellmenu.frozen || cellmenu.busy()) {
                        return;
                    }
                    clearTimeout(timer);
                    cellmenu.current = cell;
                    if (hide_menu(grid, cell)) {
                        cellmenu.hide();
                    } else {
                        cellmenu.show(grid.cell_coords(cell.row, cell.col));
                    }
                }).on('cellhoverout', function () {
                    clearTimeout(timer);
                    setTimeout(function () {
                        if (!cellmenu.menu.is(':hover')) {
                            cellmenu.hide();
                        }
                    });
                }).on('scrollstart', function () {
                    cellmenu.busy(true);
                    if (cellmenu.frozen) {
                        if (!cellmenu.getdrag()) {
                            cellmenu.destroy_frozen();
                        }
                    } else {
                        cellmenu.hide();
                    }
                }).on('scrollend', function () {
                    cellmenu.busy(false);
                });
                og.api.text({module: 'og.analytics.inplace_tash'}).pipe(function (tmpl_inplace) {
                    var unique = og.common.id('inplace');
                    inplace_config = {cntr: $('.og-inplace', cellmenu.menu), tmpl: tmpl_inplace, data: {name: unique}};
                    cellmenu.inplace = new og.common.util.ui.DropMenu(inplace_config);
                    cellmenu.container = new og.common.gadgets.GadgetsContainer('.OG-layout-analytics-', unique);
                    cellmenu.inplace.$dom.toggle.on('click', function () {
                        if (cellmenu.inplace.toggle_handler()) {
                            cellmenu.create_inplace('.OG-layout-analytics-' + unique, grid);
                            cellmenu.inplace.$dom.menu.blurkill(cellmenu.destroy_frozen.bind(cellmenu));
                        } else {
                            cellmenu.destroy_frozen();
                        }
                    });
                    cellmenu.container.on('del', function () {
                        cellmenu.destroy_frozen();
                    });
                });
            });
        };
        constructor.prototype.destroy_frozen = function () {
            $('.OG-cell-options.og-frozen').remove();
            $('.og-inplace-resizer').remove();
            if (og.analytics.grid) {
                og.analytics.grid.highlight(0, 0, "");
            }
            og.common.gadgets.manager.clean();
        };
        constructor.prototype.create_inplace = function (selector, grid) {
            var cellmenu = this, panel = 'inplace', options, cell = cellmenu.current, inner_height, inner_width,
                cell_coordinates = grid.cell_coords(cellmenu.current.row, cellmenu.current.col),
                cell_width = cell_coordinates.right - cell_coordinates.left, new_menu,
                offset = cellmenu.inplace.$dom.cntr.offset(), inner = cellmenu.inplace.$dom.menu,
                input = {
                    view_id: grid.dataman.connection.view_id,
                    grid_type: grid.dataman.connection.grid_type,
                    viewport_id: grid.dataman.viewport_id,
                    row: cell.row,
                    col: cell.col
                };
            cellmenu.destroy_frozen();
            cellmenu.frozen = true;
            cellmenu.menu.addClass('og-frozen');
            /* value requirements are not needed when:
             * 1. gadgets are launched off a depgraph
             * 2. Position/Trade gadgets are launched (cell.col = 0)
             */
            if (!cell.col || cellmenu.grid.source.depgraph) {
                implement(null);
            } else {
                og.api.rest.views.grid.viewports.valuereq.get(input).pipe(implement);
            }
            function implement(result) {
                options = mapping.options(cell, cellmenu.grid, panel, result ? result.data : null);
                cellmenu.container.add([options], null, true);
                cellmenu.container.on('launch', og.analytics.url.launch);
                inner_height = $(window).height() / 2.5;
                inner_width = $(window).width() / 2.5;
                inner.height(inner_height);
                inner.width(inner_width);
                if ((offset.top + inner_height + 10) > $(window).height()) {
                    cellmenu.menu.addClass('og-pop-up');
                    inner.css({marginTop: -inner_height + 1});
                }
                if ((offset.left - cell_width + inner_width) > $(window).width()) {
                    inner.css({marginLeft: -inner_width - (offset.left - $(window).width())});
                }
                new_menu = new constructor(cellmenu.grid);
                og.analytics.resize({
                    selector: selector,
                    offset: {top: -25, left: -1},
                    tmpl: '<div class="OG-analytics-resize og-resizer og-inplace-resizer" title="Drag to resize me" />',
                    mouseup_handler: function (right, bottom) {
                        var newWidth = Math.max(480, ($(document).outerWidth() - right) - inner.offset().left),
                            newHeight = Math.max(200, ($(document).outerHeight() - bottom) - inner.offset().top);
                        inner.css({width: newWidth, height: newHeight});
                        cellmenu.container.resize();
                    }
                });
            }
        };
        constructor.prototype.hide = function () {
            var cellmenu = this;
            if (cellmenu.menu && cellmenu.menu.length && !cellmenu.frozen) {
                cellmenu.menu.hide();
            }
        };
        constructor.prototype.setdrag = function (state) {
            constructor.prototype.drag = state;
        };
        constructor.prototype.getdrag = function () {
            return constructor.prototype.drag;
        };
        constructor.prototype.show = function (coordinates) {
            var cellmenu = this, current = this.current, width = coordinates.right - coordinates.left,
                gadget_type = mapping.type(cellmenu.current, 'inplace'),
                $chevron = cellmenu.menu.find('.og-icon-down-chevron');
            if (cellmenu.menu && cellmenu.menu.length) {
                if (!mapping.is_complex(gadget_type)) {
                    $chevron.addClass('og-complex');
                } else {
                    $chevron.removeClass('og-complex');
                }
                cellmenu.menu.appendTo($('body'))
                    .css({top: current.top, left: current.right + cellmenu.grid.elements.parent.offset().left}).show()
                    .find('.OG-gadgets-container').css('margin-left', -width + 'px').end()
                    .find('.og-cell-border').css({left: -width + 'px'}).show();
            }
        };
        return constructor;
    }
});
