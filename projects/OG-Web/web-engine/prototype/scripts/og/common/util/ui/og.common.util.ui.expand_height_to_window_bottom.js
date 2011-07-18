/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * This function sets the height of an element to exactly the amount of px
 * needed for it to expand down to the bottom of the browser window.
 *
 * @param {object} obj This object should supply a selector and an optional offset
 * e.g. {element:'#OG-sr .og-js-results', offsetpx: -5}
 */
$.register_module({
    name: 'og.common.util.ui.expand_height_to_window_bottom',
    dependencies: [],
    obj: function () {
        return function (obj) {
            var element_offset = ($(obj.element) && $(obj.element).offset().top) || 0,
                manual_offset = obj.offsetpx || 0,
                height = $(window).height() + manual_offset,
                new_height = height - element_offset;
            $(obj.element).css('height', new_height);
            if ($.isFunction(obj.callback)) obj.callback();
        }
    }
});