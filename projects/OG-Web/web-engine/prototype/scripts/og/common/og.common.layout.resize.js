/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * This function does the initial page resizing and also resizes
 * elements when the user changes the browser width or height
 */
$.register_module({
    name: 'og.common.layout.resize',
    dependencies: ['og.common.util.ui.expand_height_to_window_bottom'],
    obj: function () {
        var expand = og.common.util.ui.expand_height_to_window_bottom,
            // default selectors to watch
            defaults = [
                {element: '.OG-js-details-panel .OG-details', offsetpx: -48},
                {element: '.OG-resizeBar', offsetpx: -40},
                {element: '.OG-details', offsetpx: -59},
                {element: '.og-details-content', offsetpx: -48}
            ],
            all = [];
        return function (obj) {
            // Just load the defaults
            if (obj === 'defaults') {
                defaults.forEach(function (e) {
                    if ($(e.element).length !== 0) {
                        expand(e);
                    }
                });
                return;
            }
            // Add a new selector to the list and then call this on its own
            if (typeof obj === 'object') {
                if ($(obj.element).length !== 0) all = defaults, all.push(obj), expand(obj);
                return;
            }
            // Load all
            if (typeof obj === 'undefined') {
                all.forEach(function (e, i) {
                    if ($(e.element).length !== 0) {
                        expand(e);
                        if (e.callback) e.callback();
                    } else {
                        all.splice(i, 1); // remove item from array
                    }
                });
                return;
            }
            throw new TypeError('og.common.layout.resize: invalid argument type');
        };
    }
});