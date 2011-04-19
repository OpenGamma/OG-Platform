/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.init',
    dependencies: ['og.common.search_results', 'og.common.util.ui.toggle_text_on_focus'],
    obj: function () {
        return {
            init: function () {
                og.common.search_results.minimize('.OG-JS-minimize-search-results', '.OG-JS-maximize-search-results');
                // TODO: move to masthead
                og.common.util.ui.toggle_text_on_focus.set_selector('#OG-masthead .og-search input');
            }
        };
    }
});