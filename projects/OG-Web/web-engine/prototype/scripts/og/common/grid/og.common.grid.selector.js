/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.grid.Selector',
    dependencies: ['og.common.events'],
    obj: function () {
        var module = this, namespace = '.og_grid_selector', overlay = '.OG-g-sel', cell = '.OG-g-cell';
        var Selector = function (grid) {
            var selector = this, grid_width, grid_height, fixed_width, max_scroll_top;
            var auto_scroll = function (event, scroll_top, scroll_left, start) {
                var grid_offset = grid.elements.parent.offset(),
                    x = event.pageX - grid_offset.left, y = event.pageY - grid_offset.top, increment = 35,
                    interval = 100, scroll_body = grid.elements.scroll_body, over_fixed = x < fixed_width;
                clearTimeout(auto_scroll.timeout);
                if (x > grid_width) scroll_body.scrollLeft(scroll_left + increment);
                else if (over_fixed && start.x > fixed_width) scroll_body.scrollLeft(scroll_left - increment);
                else if (over_fixed && (auto_scroll.scroll = auto_scroll.scroll || scroll_body.find(overlay).length))
                    scroll_body.scrollLeft(scroll_left - increment);
                if (y < grid.meta.header_height) scroll_body.scrollTop(scroll_top - increment);
                else if (y > grid_height) scroll_body.scrollTop(Math.min(scroll_top + increment, max_scroll_top));
                auto_scroll.timeout = setTimeout(function () {
                    auto_scroll(event, scroll_body.scrollTop(), scroll_body.scrollLeft(), start);
                }, interval);
            };
            auto_scroll.timeout = auto_scroll.scroll = null;
            var clean_up = function () {
                var selection = selector.selection(), sheet, css;
                $(document).off(namespace);
                auto_scroll.timeout = auto_scroll.scroll = clearTimeout(auto_scroll.timeout), null;
                if (selection) {
                    selector.fire('select', selection);
                    grid.elements.selector_style.empty();
                    css = selection.rows.map(function (row) {return grid.id + ' .OG-g-' + row + ' .c0';})
                        .join(', ') + ' {color:#000000;}';
                    if ((sheet = grid.elements.selector_style[0]).styleSheet) sheet.styleSheet.cssText = css; // IE
                    else sheet.appendChild(document.createTextNode(css));
                }
                selector.render(); // render again just to give clipboard the chance to make a selection
                selector.busy(false);
            };
            var initialize = function () {
                var meta = grid.meta, inner = grid.meta.inner;
                grid_width = meta.columns.width.fixed + inner.width;
                grid_height = inner.scroll_height + meta.header_height;
                fixed_width = meta.columns.width.fixed;
                max_scroll_top = inner.height - inner.scroll_height + meta.scrollbar;
                $(grid.id + ' ' + overlay).remove();
            };
            var mousedown = function (event) {
                initialize();
                var $target = $(event.target), scroll_body = grid.elements.scroll_body,
                    grid_offset = grid.elements.parent.offset(),
                    is_cell = ($target.is(cell) ? $target : $target.parents(cell + ':first')).length,
                    x = event.pageX - grid_offset.left + (event.pageX > fixed_width ? scroll_body.scrollLeft() : 0),
                    y = event.pageY - grid_offset.top + scroll_body.scrollTop() - grid.meta.header_height;
                selector.clear();
                clean_up();
                if (!(event.which === 3 || event.button === 2) && (!is_cell || $target.is(overlay)))
                    return selector.fire('deselect');
                selector.busy(true);
                setTimeout(function () { // do this after the event has finished (and its parents have gotten it)
                    if (!selector.registered) selector.registered = !!grid.elements.parent
                        .blurkill(function () {selector.registered = false; selector.clear();});
                });
                $(document)
                    .on('mouseup' + namespace, clean_up)
                    .on('mousemove' + namespace, (function (x, y, handler) { // run it manually once and return it
                        handler = function (event, reset) {mousemove({x: x, y: y}, event, reset);};
                        return handler(event, true), handler;
                    })(x, y));
            };
            var mousemove = (function () {
                var resolution = 6, counter = 0; // only accept 1/resolution of the mouse moves, we have too many
                return function (start, event, reset) {
                    if (reset) counter = 0;
                    if (counter++ % resolution) return;
                    if (counter > resolution) counter = 1;
                    var scroll_body = grid.elements.scroll_body, rectangle = {},
                        scroll_left = scroll_body.scrollLeft(), scroll_top = scroll_body.scrollTop(),
                        grid_offset = grid.elements.parent.offset(),
                        x = event.pageX - grid_offset.left + (event.pageX > fixed_width ? scroll_left : 0),
                        y = event.pageY - grid_offset.top + scroll_top - grid.meta.header_height;
                    auto_scroll(event, scroll_top, scroll_left, start);
                    rectangle.top_left = grid.nearest_cell(Math.min(start.x, x), Math.min(start.y, y));
                    rectangle.bottom_right = grid.nearest_cell(Math.max(start.x, x), Math.max(start.y, y));
                    rectangle.width = rectangle.bottom_right.right - rectangle.top_left.left;
                    rectangle.height = rectangle.bottom_right.bottom - rectangle.top_left.top;
                    selector.render(rectangle);
                }
            })();
            selector.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            selector.grid = grid;
            selector.rectangle = null;
            grid.elements.selector_style = $('<style type="text/css" />').appendTo('head');
            grid.on('mousedown', mousedown) // initialize
                .on('render', selector.render, selector)
                .on('resize', selector.resize, selector)
                .on('kill', selector.kill, selector);
        };
        Selector.prototype.clear = function () {
            var selector = this, grid = selector.grid;
            grid.elements.selector_style.empty();
            $(selector.grid.id + ' ' + overlay).remove();
            selector.rectangle = null;
        };
        Selector.prototype.fire = og.common.events.fire;
        Selector.prototype.kill = function () {
            var selector = this, grid = selector.grid;
            try {grid.elements.selector_style.remove();} catch (error) {}
        };
        Selector.prototype.off = og.common.events.off;
        Selector.prototype.on = og.common.events.on;
        Selector.prototype.render = function (rectangle) {
            var selector = this, grid = selector.grid, data, copyable,
                fixed_width = grid.meta.columns.width.fixed,
                regions = [], rectangle = rectangle || selector.rectangle;
            if (!rectangle) return grid.clipboard.clear();
            if (rectangle.top_left.left < fixed_width) regions.push({ // fixed overlay
                fixed: true, position: {top: rectangle.top_left.top, left: rectangle.top_left.left - 1},
                dimensions: {
                    height: rectangle.height + 1,
                    width: (rectangle.width + rectangle.top_left.left > fixed_width ?
                        fixed_width - rectangle.top_left.left : rectangle.width) + 1
                }
            });
            if (rectangle.bottom_right.right > fixed_width) regions.push({ // scroll overlay
                fixed: false, position: {
                    top: rectangle.top_left.top,
                    left: (regions[0] ? 0 : rectangle.top_left.left - fixed_width) - 1
                },
                dimensions: {
                    height: rectangle.height + 1,
                    width: (regions[0] ? rectangle.width - regions[0].dimensions.width : rectangle.width) + 1
                }
            });
            if (!regions) return grid.clipboard.clear();
            if (rectangle) selector.rectangle = rectangle;
            $(grid.id + ' ' + overlay).remove();
            copyable =  (selector.copyable = grid.clipboard.has(selector.selection())) ? ' OG-g-cop' : '';
            regions.forEach(function (region) {
                $('<div class="' + overlay.substring(1) + copyable + '" />')
                    .css(region.position).css(region.dimensions)
                    .appendTo(grid.elements[region.fixed ? 'fixed_body' : 'scroll_body']);
            });
            if (selector.copyable) grid.clipboard.select();
        };
        Selector.prototype.resize = function () {
            var selector = this, grid = selector.grid, selection = grid.clipboard.selection, meta = grid.meta,
                rectangle = {};
            if (!selection) return;
            rectangle.top_left = grid.cell_coords(selection.rows[0], selection.cols[0]);
            rectangle.bottom_right = grid
                .cell_coords(selection.rows[selection.rows.length -1], selection.cols[selection.cols.length - 1]);
            rectangle.width = rectangle.bottom_right.right - rectangle.top_left.left;
            rectangle.height = rectangle.bottom_right.bottom - rectangle.top_left.top;
            selector.render(rectangle);
        };
        Selector.prototype.selection = function (rectangle) {
            if (!this.rectangle && !rectangle) return null;
            var selector = this, grid = selector.grid, meta = grid.meta, state = grid.state,
                bottom_right = (rectangle = rectangle || selector.rectangle).bottom_right,
                top_left = rectangle.top_left, grid = selector.grid,
                row_start = Math.floor(top_left.top / meta.row_height),
                row_end = Math.floor(bottom_right.bottom / meta.row_height),
                lcv, scan = meta.columns.scan.all, rows = [], cols = [], types;
            if (row_start < 0) return null; // bad input
            for (lcv = 0; lcv < scan.length; lcv += 1)
                if (scan[lcv] <= bottom_right.right && scan[lcv] > top_left.left)
                    cols.push(state.col_reorder.length ? state.col_reorder[lcv] : lcv);
                else if (scan[lcv] > bottom_right.right) break;
            for (lcv = row_start; lcv < row_end; lcv += 1) rows.push(state.available[lcv]);
            types = cols.map(function (col) {
                return meta.columns.types[state.col_reorder.length ? state.col_reorder.indexOf(col) : col];
            });
            return {cols: cols, rows: rows, type: types};
        };
        return Selector;
    }
});