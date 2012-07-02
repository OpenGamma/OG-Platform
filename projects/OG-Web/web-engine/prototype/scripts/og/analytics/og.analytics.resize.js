/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.resize',
    dependencies: ['og.views.common.layout', 'og.common.gadgets.manager'],
    obj: function () {
        var resizer_html = '<div class="OG-analytics-resize og-resizer" title="Drag (resize) / Right click (menu)" />';
        return function () {
            var $main = $('.OG-layout-analytics-center'), $menu, block_menu = false, $resizer = $(resizer_html),
                layout = og.views.common.layout, icon_size = 16, offset = $main.offset();
            var left_handler = function () {
                var $bars = $('<div class="OG-analytics-resize og-bars"></div>'),
                    $overlay = $('<div class="OG-analytics-resize og-overlay"></div>'),
                    offset = $main.offset(), right, bottom;
                if ($menu) $menu.remove();
                $bars.css({width: $main.width(), height: $main.height() + 1, top: offset.top, left: offset.left + 1});
                $overlay.on('mousemove', function (event) {
                    $bars.css({
                        width: 'auto', height: 'auto',
                        right: right = $(document).outerWidth() - event.pageX,
                        bottom: bottom = $(document).outerHeight() - event.pageY
                    });
                }).on('mouseup', function () {
                    if (!bottom || !right) return;
                    layout.inner.sizePane('south', bottom - layout.main.south.options.size);
                    layout.main.sizePane('east', right);
                    $bars.remove(), $overlay.remove();
                });
                $([$bars, $overlay]).appendTo('body');
                return false;
            };
            var load_preset = function (preset) {
                var inner = layout.inner.sizePane, right = layout.right.sizePane, main = layout.main.sizePane;
                switch (preset) {
                    case 1: inner('south', '50%'); main('east', '25%'); break;
                    case 2: inner('south', '50%'); main('east', '50%'); break;
                    case 3: inner('south', '15%'); main('east', '10%'); break;
                }
                right('north', '33%');
                right('south', '33%');
            };
            var mousedown_handler = function (event) {
                return event.which !== 3 || event.button !== 2 ? left_handler()
                    : event.stopPropagation(), (block_menu = true), right_handler();
            };
            var resize = function () {
                $resizer.css({
                    left: offset.left + $main.width() - icon_size, top: offset.top + $main.height() - icon_size
                });
            };
            var right_handler = function () {
                $.when(og.api.text({module: 'og.analytics.resize_menu_tash'})).then(function (template) {
                    $menu = $(template).position({my: 'left top', at: 'right bottom', of: $resizer})
                        .on('mousedown', 'div', function () {
                            // extract preset number from class and load
                            load_preset(+$(this).find('> span').attr('class').replace(/^(?:.*)([1-3])$/, '$1'));
                            $menu.remove(); // it should already be gone, but just in case you are IE8
                        }).appendTo('body').blurkill();
                });
                return false;
            };
            og.common.gadgets.manager.register({alive: function () {return true;}, resize: resize});
            resize();
            $(document).on('contextmenu.og_analytics_resize', function (event) {
                if (block_menu) return event.stopPropagation(), (block_menu = false);
            });
            $resizer.on('mousedown', mousedown_handler).appendTo('body');
        }
    }
});