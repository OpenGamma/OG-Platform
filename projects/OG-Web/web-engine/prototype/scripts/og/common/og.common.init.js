/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.init',
    dependencies: [
        'og.common.search_results',
        'og.common.util.ui.toggle_text_on_focus',
        'og.common.util.ui.resize_panes',
        'og.common.layout.resize'
    ],
    obj: function () {
        var t;
        return {
            init: function () {
                $('.OG-container').show();
                /**
                 * Sets up the vertical resize bar in the middle of the application
                 */
                og.common.util.ui.resize_panes('.OG-js-search-panel', '.OG-js-details-panel', '.OG-resizeBar');
                /**
                 * First call to extend elements to the bottom of the screen
                 */
                og.common.layout.resize('defaults');
                /**
                 * Reset the height of all default elements and others being watched on browser resize
                 */
                $(window).resize(function () {clearTimeout(t), t = setTimeout(og.common.layout.resize, 150);});
            }
        };
    }
});