/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.resize',
    dependencies: ['og.views.common.layout', 'og.common.gadgets.manager'],
    obj: function () {
        return function () {
            var $main = $('.OG-layout-analytics-center'), $menu,
                $r = $('<div class="OG-analytics-resize og-resizer" title="Drag (resize) / Right click (menu)" />'),
                layout = og.views.common.layout, icon_size = 16, offset = $main.offset();
            var left_click_handler = function (e) {
                e.preventDefault();
                if (e.which !== 1) return; // if not left click return
                var $bars = $('<div class="OG-analytics-resize og-bars"></div>'),
                    $overlay = $('<div class="OG-analytics-resize og-overlay"></div>'),
                    offset = $main.offset(), right, bottom;
                if ($menu) $menu.remove();
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
                    if (!bottom || !right) return;
                    console.log(bottom, layout.main.south.options.size);
                    layout.inner.sizePane('south', bottom - layout.main.south.options.size);
                    layout.main.sizePane('east', right);
                    $bars.remove(), $overlay.remove();
                });
                $([$bars, $overlay]).appendTo('body');
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
            var resize = function () {$r.css({
                'left': offset.left + $main.width() - icon_size,
                'top': offset.top + $main.height() - icon_size
            })};
            var right_click_handler = function () {
                $.when(og.api.text({module: 'og.analytics.resize_menu_tash'})).then(function (template) {
                    $menu = $(template)
                        .position({my: 'left top', at: 'right bottom', of: $r})
                        .on('click', 'div', function () {
                            // extract preset number from class and load
                            load_preset(+$(this).find('> span').attr('class').replace(/^(?:.*)([1-3])$/, '$1'));
                        })
                        .appendTo('body');
                    $(document).on('click.analytics.resize.menu', function () {$menu.remove();});
                });
                return false;
            };
            og.common.gadgets.manager.register({alive: function () {return true;}, resize: resize});
            resize();
            $r
                .on('mousedown', left_click_handler)
                .on('contextmenu', right_click_handler)
                .appendTo('body');
        }
    }
});