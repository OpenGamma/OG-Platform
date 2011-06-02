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
                $('#container').show();
                og.common.util.ui.resize_panes('#OG-sr', '#OG-details', '.OG-resizeBar');
                og.common.layout.resize();
                og.common.search_results.minimize('.OG-JS-minimize-search-results', '.OG-JS-maximize-search-results');
                og.common.util.ui.toggle_text_on_focus.set_selector('#OG-masthead .og-search input');
                $(window).resize(function() {og.common.layout.resize();}); // TODO: need to reload slickgrid on resize
            }
        };
    }
});