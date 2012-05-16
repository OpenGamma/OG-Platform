/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Selector',
    dependencies: ['og.analytics.Grid'],
    obj: function () {
        var module = this;
        return function (grid) {
            var selector = this, $ = grid.$, grid_offset, start = null;
            var cleanup = function () {
                $(window).off('mousemove', mousemove_observer).off('mouseup', mouseup_observer);
                if (document.selection) document.selection.empty(); // IE
                if (window.getSelection) window.getSelection().removeAllRanges(); // civilization
                grid.dataman.busy(false);
                selector.busy(false);
                grid.set_viewport();
            };
            var deselect = function () {
                $(grid.id + ' .OG-g-sel').remove();
                selector.render.memo = null;
            };
            var mousemove_observer = function (event) {
                var scroll_left = grid.elements.scroll_body.scrollLeft(),
                    scroll_top = grid.elements.scroll_body.scrollTop(),
                    x = event.pageX - grid_offset.left + scroll_left,
                    y = event.pageY - grid_offset.top + scroll_top - grid.meta.header_height,
                    fixed_width = grid.meta.columns.width.fixed, scroll_width = grid.meta.columns.width.scroll,
                    areas = [], rectangle = {};
                rectangle.top_left = nearest_cell(Math.min(start.x, x), Math.min(start.y, y));
                rectangle.bottom_right = nearest_cell(Math.max(start.x, x), Math.max(start.y, y));
                rectangle.width = rectangle.bottom_right.right - rectangle.top_left.left;
                rectangle.height = rectangle.bottom_right.bottom - rectangle.top_left.top;
                if (rectangle.top_left.left < fixed_width) areas.push({ // fixed overlay
                    position: {top: rectangle.top_left.top, left: rectangle.top_left.left},
                    dimensions: {
                        height: rectangle.height,
                        width: rectangle.width + rectangle.top_left.left > fixed_width ?
                            fixed_width - rectangle.top_left.left : rectangle.width
                    },
                    fixed: true
                });
                if (rectangle.bottom_right.right > fixed_width) areas.push({ // scroll overlay
                    position: {top: rectangle.top_left.top, left: areas[0] ? 0 : rectangle.top_left.left - fixed_width},
                    dimensions: {
                        height: rectangle.height,
                        width: areas[0] ? rectangle.width - areas[0].dimensions.width : rectangle.width
                    },
                    fixed: false
                });
                areas.rectangle = rectangle;
                selector.render(areas);
            };
            var mouseup_observer = cleanup;
            var mousedown_observer = function (event) {
                var $cell, $target, offset, position, fixed, right_click = event.which === 3 || event.button === 2,
                    scroll_left = grid.elements.scroll_body.scrollLeft(),
                    scroll_top = grid.elements.scroll_body.scrollTop();
                if (right_click) return;
                cleanup();
                $cell = ($target = $(event.target)).is('.OG-g-cell') ? $target : $target.parents('.OG-g-cell:first');
                if (!$cell.length) return;
                grid_offset = grid.elements.parent.offset();
                offset = $cell.offset();
                fixed = +$cell.attr('class').match(/\sc(\d+)\s?/)[1] < grid.meta.columns.fixed.length;
                grid.dataman.busy(true);
                selector.busy(true);
                position = {
                    top: start_top = offset.top - grid.meta.header_height + scroll_top,
                    left: offset.left - (fixed ? 0 : grid.meta.columns.width.fixed) + (fixed ? 0 : scroll_left)
                };
                start = {
                    x: event.pageX - grid_offset.left + (fixed ? 0 : scroll_left),
                    y: event.pageY - grid_offset.top + scroll_top - grid.meta.header_height,
                    top: position.top, bottom: position.top + grid.meta.row_height, left: position.left,
                    right: (fixed ? grid.elements.fixed_body : grid.elements.scroll_body).width() -
                        position.left - $cell.width()
                };
                mousemove_observer(event);
                $(window).on('mousemove', mousemove_observer).on('mouseup', mouseup_observer);
            };
            var nearest_cell = function (x, y, label) {
                var top, left, bottom, right, lcv, scan = grid.meta.columns.scan.all, len = scan.length;
                for (lcv = 0; lcv < len; lcv += 1) if (scan[lcv] > x) break;
                right = scan[lcv];
                left = scan[lcv - 1] || 0;
                bottom = (Math.floor(y / grid.meta.row_height) + 1) * grid.meta.row_height;
                top = bottom - grid.meta.row_height;
                return {top: top, left: left, bottom: bottom, right: right};
            };
            var scroll_observer = function (timeout) {
                return function () { // sync scroll instantaneously and set viewport after scroll stops
                    grid.dataman.busy(true);
                    grid.elements.scroll_head.scrollLeft(grid.elements.scroll_body.scrollLeft());
                    grid.elements.fixed_body.scrollTop(grid.elements.scroll_body.scrollTop());
                    if (selector.busy()) return;
                    timeout = clearTimeout(timeout) ||
                        setTimeout(function () {grid.set_viewport(function () {grid.dataman.busy(false);})}, 200);
                }
            };
            selector.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            selector.render = function (rectangles) {
                if (!selector.render.memo && !rectangles) return;
                if (rectangles) selector.render.memo = rectangles;
                $(grid.id + ' .OG-g-sel').remove();
                selector.render.memo.forEach(function (rectangle) {
                    $('<div class="OG-g-sel" />').click(deselect).css(rectangle.position).css(rectangle.dimensions)
                        .appendTo(grid.elements[rectangle.fixed ? 'fixed_body' : 'scroll_body']);
                });
            };
            selector.render.memo = null;
            selector.selection = function () {
                if (!selector.render.memo) return null;
                var bottom_right = selector.render.memo.rectangle.bottom_right,
                    top_left = selector.render.memo.rectangle.top_left,
                    row_start = Math.floor(top_left.top / grid.meta.row_height),
                    row_end = Math.floor(bottom_right.bottom / grid.meta.row_height),
                    lcv, scan = grid.meta.columns.scan.all, rows = [], cols = [];
                for (lcv = 0; lcv < scan.length; lcv += 1)
                    if (scan[lcv] <= bottom_right.right && scan[lcv] > top_left.left) cols.push(lcv);
                    else if (scan[lcv] > bottom_right.right) break;
                for (lcv = row_start; lcv < row_end; lcv += 1) rows.push(lcv);
                return {rows: rows, cols: cols};
            };
            // initialize
            grid.elements.parent.on('mousedown', mousedown_observer);
            grid.elements.scroll_body.on('scroll', scroll_observer(null));
        };
    }
});