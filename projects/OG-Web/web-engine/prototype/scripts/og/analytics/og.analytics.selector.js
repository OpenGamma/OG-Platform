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
            var selector = this, $ = grid.$, grid_offset = grid.elements.parent.offset(),
                $overlay, start_row, start_col, start_left, start_top, start_fixed,
                start_width, start_height, start_x, start_y;
            var cleanup = function () {
                $(window).off('mousemove', mousemove_observer);
                $('body').off('mouseup', mouseup_observer);
                if (document.selection) document.selection.empty(); // IE
                if (window.getSelection) window.getSelection().removeAllRanges(); // civilization
                grid.dataman.busy(false);
                selector.busy = false;
                grid.set_viewport();
            };
            var render = function (position, dimensions) {
                if (!$overlay) $overlay = $('<div class="OG-g-sel" />').click(function () {
                    $(this).remove();
                    selection = null;
                }).appendTo(grid.elements.main);
                $overlay.removeAttr('style').css($.extend(position, dimensions));
            };
            var mousemove_observer = function (event) {
                var x = event.pageX - grid_offset.left, y = event.pageY - grid_offset.top, position = {},
                    top = 0, left = 0, width = 0, height = grid.row_height;
                if (x > start_x) { // rightward
                    width = grid.meta.columns.fixed.concat(grid.meta.columns.scroll).reduce(function (acc, col, idx) {
                        acc.scan += col.width;
                        if (idx >= start_col && (Math.abs(acc.scan - x) < col.width || acc.scan < x))
                            acc.width += col.width;
                        return acc;
                    }, {scan: 0, width: 0}).width;
                    position = {left: start_left, top: start_top};
                } else { // leftward
                    position.right = grid.elements.main.width() - (start_left + start_width);
                    width = grid.meta.columns.fixed.concat(grid.meta.columns.scroll).reduce(function (acc, col, idx) {
                        acc.scan += col.width;
                        console.log('idx', idx, 'acc.scan', acc.scan, 'x', x, 'position.right', position.right);
                        if (idx < start_col && acc.scan > x && acc.scan < position.right) acc.width += col.width;
                        return acc;
                    }, {scan: 0, width: start_width}).width;
                    position.top = start_top;
                }
                render(position, {width: width, height: height});
            };
            var mouseup_observer = function (event) {
                cleanup();
            };
            var mousedown_observer = function (event) {
                var $cell, $row, $target, offset, position,
                    right_click =  event.which === 3 || event.button === 2;
                if (right_click) return;
                cleanup();
                if ($overlay) return $overlay.remove(), $overlay = null;
                $cell = ($target = $(event.target)).is('.OG-g-cell') ? $target : $target.parents('.OG-g-cell:first');
                if (!$cell) return;
                $row = $cell.parents('.OG-g-row:first');
                offset = $cell.offset();
                start_width = $cell.width();
                start_height = grid.row_height;
                start_x = event.pageX - grid_offset.left;
                start_y = event.pageY - grid_offset.top;
                start_col = +$cell.attr('class').match(/\sc(\d+)\s?/)[1];
                start_row = +$row.attr('class').match(/\svr(\d+)\s?/)[1];
                start_fixed = start_col < grid.meta.columns.fixed.length;
                grid.dataman.busy(true);
                selector.busy = true;
                position = {top: start_top = offset.top, left: start_left = offset.left};
                render(position, {width: start_width, height: start_height}, start_fixed);
                $('body').on('mouseup', mouseup_observer);
                $(window).on('mousemove', mousemove_observer);
            };
            var scroll_observer = function (timeout) {
                return function () { // sync scroll instantaneously and set viewport after scroll stops
                    grid.dataman.busy(true);
                    grid.elements.scroll_head.scrollLeft(grid.elements.scroll_body.scrollLeft());
                    grid.elements.fixed_body.scrollTop(grid.elements.scroll_body.scrollTop());
                    if (selector.busy) return;
                    timeout = clearTimeout(timeout) || setTimeout(function () {
                        grid.set_viewport(function () {grid.dataman.busy(false);})
                    }, 200);
                }
            };
            selector.selection = function () {return selection;};
            grid.elements.parent.on('mousedown', mousedown_observer);
            grid.elements.scroll_body.on('scroll', scroll_observer(null));
        };
    }
});