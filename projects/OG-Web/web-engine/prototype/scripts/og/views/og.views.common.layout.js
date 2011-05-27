/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return function (layout) {
            if (layout === 'analytics') {
                $('#OG-details, #OG-sr').hide();
                $('#OG-analytics').show();
            }
            if (layout === 'default') {
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
            }
        }
    }
});