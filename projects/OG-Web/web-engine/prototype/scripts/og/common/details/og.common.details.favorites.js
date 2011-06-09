/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.details.favorites',
    dependencies: [],
    obj: function () {
        return function () {
            $('.OG-icon.og-favorites').click(function () {$(this).toggleClass('og-favorites-active');});
        };
    }
});


