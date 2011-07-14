/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return function (layout) {
            if (layout === 'analytics') {
                $('.OG-js-search-panel, .OG-js-details-panel').hide();
                $('.OG-js-analytics-panel').show();
            }
            if (layout === 'default') {
                $('.OG-js-search-panel, .OG-js-details-panel').show();
                $('.OG-js-analytics-panel').hide().find('.OG-analytics').empty();
            }
        }
    }
});