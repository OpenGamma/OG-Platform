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
            var selector = this, $ = grid.$, $overlay, start_offset = null,
                start_width, start_height, selection = null, $start, $end;
            grid.elements.parent[0].onselectstart = function () {return false;}; // IE
            var cleanup = function () {
                start_offset = null;
                start_width = 0;
                start_height = 0;
                $start = null;
                $('body').off('hover', mousemove_observer);
                $('body').off('mouseup', mouseup_observer);
            };
            var render = function (top, left, width, height) {
                if (!$overlay) $overlay = $('<div class="OG-g-sel" />').click(function () {
                    $(this).remove();
                    selection = null;
                }).appendTo('body');
                $overlay.css({top: top, left: left, width: width, height: height}).show();
            };
            var mousemove_observer = function (event) {
                if (!start_offset) return cleanup();
                var end_offset, top, left, width, height, end_width, end_height;
                $end = ($target = $(event.target)).is('.OG-g-cell') ? $target
                    : $target.parents('.OG-g-cell:first');
                if (!$end || !$end.length) return;
                end_offset = $end.offset();
                top = Math.min(start_offset.top, end_offset.top);
                left = Math.min(start_offset.left, end_offset.left);
                if ($start[0] === $end[0]) {
                    width = start_width;
                    height = start_height;
                } else {
                    end_width = $end.width();
                    end_height = $end.height();
                    width = left === start_offset.left ? end_offset.left + end_width - left
                        : start_offset.left + start_width - left;
                    height = top === start_offset.top ? end_offset.top + end_height - top
                        : start_offset.top + start_height - top;
                }
                render(top, left, width, height);
            };
            var mouseup_observer = function (event) {
                cleanup();
                grid.dataman.busy(false);
            };
            var mousedown_observer = function (event) {
                var $target, right_click =  event.which === 3 || event.button === 2;
                $start = null;
                if (right_click) return;
                cleanup();
                if ($overlay) $overlay.remove();
                $overlay = null;
                $start = ($target = $(event.target)).is('.OG-g-cell') ? $target
                    : $target.parents('.OG-g-cell:first');
                if (!$start) return;
                start_width = $start.width();
                start_height = $start.height();
                start_offset = $start.offset();
                render(start_offset.top, start_offset.left, start_width, start_height);
                $('body').on('mouseup', mouseup_observer).on('mousemove', mousemove_observer);
                grid.dataman.busy(true);
            };
            selector.selection = function () {return selection;};
            grid.elements.parent.on('mousedown', mousedown_observer);
        };
    }
});