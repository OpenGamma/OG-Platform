/**
 * window.og.common.util.ui.toggleTextOnFocus
 *
 * This function will remove the default text in an input
 * field (or other element) on focus and put it back on blur
 * unless new input has been added. Attach it to an element
 * by supplying a selector matching the element.
 *
 * @param {String} CSS selector
 *
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.toggle_text_on_focus',
    dependencies: [],
    obj: function () {
        function set_selector(element) {
            var arr = [], txt_color = $(element).css('color'),
                css_on = {'color': '#000', 'border': '1px solid #ccc'},
                css_off = {'color': txt_color, 'border': '1px solid #ccc'};
            $(element).focus(function () {
                var value = $(this).attr('value');
                $(this).select().css(css_on);
                // Store the original value
                if (!arr[element]) arr[element] = value;
                // If the current value is the same as the original then clear the field
                if (value === arr[element]) $(this).attr('value', '');
            });
            $(element).focusout(function () {
                // If the field is left empty then repopulate it with the original value
                if ($(element).attr('value') === '') $(this).attr('value', arr[element]).css(css_off);
            });
        }
        return {set_selector: set_selector}
    }
});