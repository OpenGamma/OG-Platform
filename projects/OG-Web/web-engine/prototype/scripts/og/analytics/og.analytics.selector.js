/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Selector',
    dependencies: ['og.analytics.Grid'],
    obj: function () {
        var module = this, namespace = '.og_analytics_selector', overlay = '.OG-g-sel', cell = '.OG-g-cell';
        return function (grid) {
            var selector = this, $ = grid.$, grid_offset, grid_width, grid_height, fixed_width;
            var auto_scroll = function (event, scroll_top, scroll_left, start) {
                var x = event.pageX - grid_offset.left, y = event.pageY - grid_offset.top, increment = 35,
                    interval = 100, scroll_body = grid.elements.scroll_body, over_fixed = x < fixed_width;
                clearTimeout(auto_scroll.timeout);
                if (x > grid_width) scroll_body.scrollLeft(scroll_left + increment);
                else if (over_fixed && start.x > fixed_width) scroll_body.scrollLeft(scroll_left - increment);
                else if (over_fixed && (auto_scroll.scroll = auto_scroll.scroll || scroll_body.find(overlay).length))
                    scroll_body.scrollLeft(scroll_left - increment);
                if (y < grid.meta.header_height) scroll_body.scrollTop(scroll_top - increment);
                else if (y > grid_height) scroll_body.scrollTop(scroll_top + increment);
                auto_scroll.timeout = setTimeout(function () {
                    auto_scroll(event, scroll_body.scrollTop(), scroll_body.scrollLeft(), start);
                }, interval);
            };
            (auto_scroll.timeout = null), (auto_scroll.scroll = false);
            var clean_up = function () {
                $(document).off(namespace);
                (auto_scroll.timeout = clearTimeout(auto_scroll.timeout)), (auto_scroll.scroll = false);
            };
            var initialize = function () {
                clean_up();
                grid_offset = grid.elements.parent.offset();
                grid_width = grid.elements.parent.width();
                grid_height = grid.elements.parent.height();
                fixed_width = grid.meta.columns.width.fixed;
                $(grid.id + ' ' + overlay).remove();
            };
            var mousedown_observer = function (event) {
                if (event.which === 3 || event.button === 2) return; else initialize(); // ignore right clicks
                var $target, x = event.pageX - grid_offset.left + (event.pageX > fixed_width ?
                        grid.elements.scroll_body.scrollLeft() : 0),
                    y = event.pageY - grid_offset.top + grid.elements.scroll_body.scrollTop() - grid.meta.header_height;
                if (!(($target = $(event.target)).is(cell) ? $target : $target.parents(cell + ':first'))
                    .length && !$target.is(overlay)) return; // if the cursor is not over a cell, bail
                $(document).on('mouseup' + namespace, clean_up)
                    .on('mousemove' + namespace, (function (x, y, handler) { // run it manually once and return it
                        return (handler = function (event) {mousemove({x: x, y: y}, event);})(event), handler;
                    })(x, y));
            };
            var mousemove = function (start, event) {
                event.preventDefault();
                var scroll_left = grid.elements.scroll_body.scrollLeft(),
                    scroll_top = grid.elements.scroll_body.scrollTop(),
                    x = event.pageX - grid_offset.left + (event.pageX > fixed_width ? scroll_left : 0),
                    y = event.pageY - grid_offset.top + scroll_top - grid.meta.header_height,
                    regions = [], rectangle = {};
                auto_scroll(event, scroll_top, scroll_left, start);
                rectangle.top_left = nearest_cell(Math.min(start.x, x), Math.min(start.y, y));
                rectangle.bottom_right = nearest_cell(Math.max(start.x, x), Math.max(start.y, y));
                rectangle.width = rectangle.bottom_right.right - rectangle.top_left.left;
                rectangle.height = rectangle.bottom_right.bottom - rectangle.top_left.top;
                if (rectangle.top_left.left < fixed_width) regions.push({ // fixed overlay
                    position: {top: rectangle.top_left.top, left: rectangle.top_left.left - 1},
                    dimensions: {
                        height: rectangle.height + 1,
                        width: (rectangle.width + rectangle.top_left.left > fixed_width ?
                            fixed_width - rectangle.top_left.left : rectangle.width) + 1
                    },
                    fixed: true
                });
                if (rectangle.bottom_right.right > fixed_width) regions.push({ // scroll overlay
                    position: {
                        top: rectangle.top_left.top,
                        left: (regions[0] ? 0 : rectangle.top_left.left - fixed_width) - 1
                    },
                    dimensions: {
                        height: rectangle.height + 1,
                        width: (regions[0] ? rectangle.width - regions[0].dimensions.width : rectangle.width) + 1
                    },
                    fixed: false
                });
                selector.render(regions, rectangle);
            };
            var nearest_cell = function (x, y) {
                var top, bottom, lcv, scan = grid.meta.columns.scan.all, len = scan.length;
                for (lcv = 0; lcv < len; lcv += 1) if (scan[lcv] > x) break;
                bottom = (Math.floor(y / grid.meta.row_height) + 1) * grid.meta.row_height;
                top = bottom - grid.meta.row_height;
                return {top: top, bottom: bottom, left: scan[lcv - 1] || 0, right: scan[lcv]};
            };
            selector.render = function (regions, rectangle) {
                if (!selector.render.regions && !regions) return;
                if (regions) (selector.render.regions = regions), (selector.render.rectangle = rectangle);
                $(grid.id + ' ' + overlay).remove();
                selector.render.regions.forEach(function (region) {
                    $('<div class="' + overlay.substring(1) + '" />').css(region.position).css(region.dimensions)
                        .appendTo(grid.elements[region.fixed ? 'fixed_body' : 'scroll_body']);
                });
            };
            (selector.render.regions = null), (selector.render.rectangle = null);
            selector.selection = function () {
                if (!selector.render.rectangle) return null;
                var bottom_right = selector.render.rectangle.bottom_right,
                    top_left = selector.render.rectangle.top_left,
                    row_start = Math.floor(top_left.top / grid.meta.row_height),
                    row_end = Math.floor(bottom_right.bottom / grid.meta.row_height),
                    lcv, scan = grid.meta.columns.scan.all, rows = [], cols = [];
                for (lcv = 0; lcv < scan.length; lcv += 1)
                    if (scan[lcv] <= bottom_right.right && scan[lcv] > top_left.left) cols.push(lcv);
                    else if (scan[lcv] > bottom_right.right) break;
                for (lcv = row_start; lcv < row_end; lcv += 1) rows.push(lcv);
                return {rows: rows, cols: cols};
            };
            grid.on('mousedown', mousedown_observer); // initialize
        };
    }
});