/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.contextmenu',
    dependencies: [],
    obj: function () {
        return function (options, event, cell) {
            var $menu = $('<div />').addClass('OG-contextmenu OG-shadow'),
                css = {position: 'absolute', zIndex: options.zindex || 0};
            var items = options.items, html;
            if (options.defaults) items = items.concat([
                {}, // divider
                {name: 'Back', handler: function () {history.back()}},
                {name: 'Forward', handler: function () {history.forward()}},
                {name: 'Reload', handler: function () {location.reload()}}
            ]);
            html = items.reduce(function (acc, val, i) {
                return acc + (val.name ? '<div data-id='+ i + (val.disabled ? ' data-active="false"' : '')
                    + '><span></span>' + val.name + '</div>' : '<hr />');
            }, '');
            $menu.blurkill().html(html).css(css).appendTo('body')
                .position({my: 'left top', at: 'left top', of: event})
                .mousedown(function (event) {
                    var id = $(event.target).attr('data-id'),
                        handler = items[id] && !items[id].disabled && items[id].handler;
                    $(this).remove();
                    if (handler) handler();
                    return false;
                });
            return false;
        };
    }
});