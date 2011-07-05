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
        return {
            init: function () {
                $('.OG-container').show();
                og.common.util.ui.resize_panes('.OG-js-search-panel', '.OG-js-details-panel', '.OG-resizeBar');
                og.common.layout.resize('defaults');
                $(window).resize(function () {setTimeout(og.common.layout.resize, 150)});
            }
        };
    }
});