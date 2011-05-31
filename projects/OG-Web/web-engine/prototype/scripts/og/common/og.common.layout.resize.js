/**
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 *
 * This function does the initial page resizing and also resizes
 * elements when the user changes the browser width or height
 */
$.register_module({
    name: 'og.common.layout.resize',
    dependencies: ['og.common.util.ui.expand_height_to_window_bottom'],
    obj: function () {
        var expand = og.common.util.ui.expand_height_to_window_bottom;
        return function () {
            expand({element: '#OG-sr .og-js-results-slick', offsetpx: -3});
            expand({element: '.OG-resizeBar', offsetpx: -3});
            expand({element: '#OG-details .og-main', offsetpx: -3});
            expand({element: '#OG-analytics .og-main', offsetpx: -3});
        };
    }
});