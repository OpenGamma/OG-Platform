/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * autosuggest combobox
 *
 */
$.register_module({
    name: 'og.common.util.ui.AutoCombo',
    dependencies: [],
    obj: function () {
        /**
         * @param {Object} obj configuration object,
         * selector (String) and data (Array) are required, placeholder (String) is optional
         */
        return function (config) {
            var combo = this, d, replace_val, input_tag, no_arrow = config.no_arrow || false;

            if (!config.selector || typeof config.selector !== 'string')
                return og.dev.warn('og.common.util.ui.AutoCombo: Missing or invalid param [selector]');

            if (!config.placeholder || typeof config.placeholder !== 'string')
                return og.dev.warn('og.common.util.ui.AutoCombo: Missing or invalid param [placeholder]');

            var replace_placeholder = function (event) {
                if (event.type === 'keydown' && (event.which !== 40 && event.which !== 38)) return;
                var val = combo.$input.val().replace(/<(|\/)strong>/gi, "");
                combo.$input.val(val);
            };

            combo.state = 'blurred';
            combo.init_blurkill = false;

            combo.open = function () {
                if ('$input' in combo && combo.$input) combo.$input.autocomplete('search', '');
            };

            combo.placeholder = config.placeholder || '';
            combo.autocomplete_obj = {
                minLength: 0,
                delay: 0,
                open: function(event) {},
                source: config.source || $.noop,
                close: replace_placeholder,
                focus: replace_placeholder,
                select: config.select || $.noop
            };
            input_tag = config.id ? '<input type="text" id="'+ config.id +'"' : '<input type="text"';
            input_tag += config.name ? ' name="'+ config.name + '" ' : '';
            input_tag += config.value ? ' value="'+ config.value + '" ' : '';
            input_tag += config.disabled ? ' disabled >' : '>';
            // wrap input in div, enable input width 100% of parent, FF, IE
            combo.$wrapper = $('<div class="autocomplete-cntr">').html(input_tag);
            combo.$input = combo.$wrapper.find('input');
            combo.$button = $('<div class="OG-icon og-icon-down"></div>');
            if (combo.$input && combo.$button) {
                combo.$input
                    .autocomplete(combo.autocomplete_obj)
                    .attr('placeholder', config.placeholder)
                    .on('mouseup', combo.open)
                    .on('mousedown', function (event) {
                        var ac = this;
                        setTimeout(function () {
                            if (!combo.init_blurkill) {
                                combo.init_blurkill = true;
                                $(ac).autocomplete('widget').blurkill(function () {
                                    combo.init_blurkill = false;
                                     if ('$input' in combo && combo.$input) combo.$input.autocomplete('close');
                                });
                            }
                        });
                    })
                    .on('blur', function () {
                        combo.state = 'blurred';
                        if (combo.$input && combo.$input.val() === config.placeholder) combo.$input.val('');
                    })
                    .on('keydown', replace_placeholder)
                    .on('focus', function () {
                        combo.state = 'focused';
                        if (combo.$input) combo.$input.trigger('open', combo.$input);
                    });
                combo.$input.data('autocomplete')._renderItem = function(ul, item) { // Enable html list items
                    if (!ul || !item) return;
                    return $('<li></li>').data('item.autocomplete', item).append('<a>' + item.label + '</a>')
                        .appendTo(ul);
                };
                combo.$button.on('click', function () {
                    if (!combo.$input) return;
                    return combo.$input.autocomplete('widget').is(':visible') ?
                        combo.$input.autocomplete('close').select() : combo.open();
                });
                $(combo.$wrapper).appendTo(config.selector);
                if (!no_arrow) $(combo.$button).appendTo(config.selector);
                if (config.input_val) combo.$input.val(config.input_val);
                og.common.events.fire(config.selector+':autocombo:initialized');
            }
            return combo;
        };
    }
});