/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.resize',
    dependencies: ['og.views.common.layout', 'og.common.gadgets.manager'],
    obj: function () {
        return function () {
            var $main = $('.OG-layout-analytics-center'), $r = $('<div class="OG-analytics-resize og-resizer" />'),
                layout = og.views.common.layout, icon_size = 16, offset = $main.offset();
            var mousedown_handler = function (e) {
                e.preventDefault();
                var $bars = $('<div class="OG-analytics-resize og-bars"></div>'),
                    $overlay = $('<div class="OG-analytics-resize og-overlay"></div>'),
                    offset = $main.offset(), right, bottom;
                $bars.css({
                    'width': $main.width(), 'height': $main.height() + 1,
                    'top': offset.top, 'left': offset.left + 1
                });
                $overlay.on('mousemove', function (e) {
                    $bars.css({
                        'width': 'auto', 'height': 'auto',
                        'right': right = $(document).outerWidth() - e.pageX,
                        'bottom': bottom = $(document).outerHeight() - e.pageY
                    });
                });
                $overlay.on('mouseup', function () {
                    layout.inner.sizePane('south', bottom - layout.main.south.options.size);
                    layout.main.sizePane('east', right);
                    $bars.remove(), $overlay.remove();
                });
                $([$bars, $overlay]).appendTo('body');
            };
            var resize = function () {$r.css({
                'left': offset.left + $main.width() - icon_size,
                'top': offset.top + $main.height() - icon_size
            })};
            og.common.gadgets.manager.register({alive: function () {return true;}, resize: resize});
            $r
                .on('mousedown', mousedown_handler)
                .on('contextmenu', function () {return false;})
                .appendTo('body');
            resize();
        }
    }
});