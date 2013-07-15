/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.resize',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var $main = $(config.selector), $menu, block_menu = false, $resizer = $(config.tmpl),
                offset_top, offset_left,
                icon_size = 16, offset = $main.offset(), right_handler = config.right_handler || $.noop,
                $bars = $('<div class="OG-analytics-resize og-bars"></div>'),
                $overlay = $('<div class="OG-analytics-resize og-overlay"></div>');
                if (config.offset) offset_top = config.offset.top, offset_left = config.offset.left;
            var left_handler = function () {
                var offset = $main.offset(), right, bottom;
                if ($menu) $menu.remove();
                $bars.css({width: $main.width(), height: $main.outerHeight(true) + 1,
                    top: offset.top, left: offset.left + 1});
                $overlay.on('mousemove', function (event) {
                    $bars.css({
                        width: 'auto', height: 'auto',
                        right: right = $(document).outerWidth() - event.pageX,
                        bottom: bottom = $(document).outerHeight() - event.pageY
                    });
                }).on('mouseup', function () {
                    if (!bottom || !right) return;
                    config.mouseup_handler(right, bottom);
                    $bars.remove(), $overlay.remove();
                    resize();
                     $(window).off('mouseout');
                });
                $([$bars, $overlay]).appendTo('body');
                return false;
            };
            var mousedown_handler = function (event) {
                var handler;
                if (event.which !== 3 || event.button !== 2){
                    $(window).on('mouseout',  function(event) {
                        event = event ? event : window.event;
                        var from = event.relatedTarget || event.toElement;
                        if (!from || from.nodeName == "HTML") reset();
                    });
                    handler = left_handler();
                }else{
                    event.stopPropagation();
                    block_menu = true;
                    handler =  right_handler($resizer);
                }
                return handler;
            };
            var resize = function () {
                $resizer.css({
                    left: offset.left + $main.outerWidth() - icon_size - 1 + (offset_left || 0),
                    top: offset.top + $main.outerHeight() - icon_size - 1 + (offset_top || 0)
                });
            };
            var reset = function () {
                $bars.remove();
                $overlay.remove();
                $(window).off('mouseout');
            };
            og.common.gadgets.manager.register({alive: function () {return true;}, resize: resize});
            resize();
            $(document).on('contextmenu.og_analytics_resize', function (event) {
                if (block_menu) return event.stopPropagation(), (block_menu = false);
            });
            $resizer.on('mousedown', mousedown_handler).appendTo('body');
        };
    }
});