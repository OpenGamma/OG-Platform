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
        return function (selector, placeholder, data, input_val) {
            var combo = this, d, replace_val;

            if (!selector || typeof selector !== 'string')
                return og.dev.warn('og.common.util.ui.AutoCombo: Missing or invalid param [selector]');

            if (!placeholder || typeof placeholder !== 'string')
                return og.dev.warn('og.common.util.ui.AutoCombo: Missing or invalid param [placeholder]');

            if (!data) return og.dev.warn('og.common.util.ui.AutoCombo: Missing param [data]');

            if (!$.isArray(data))
                return og.dev.warn('og.common.util.ui.AutoCombo: Invalid type param [data]; expected object');

            if (!data.length) og.dev.warn('og.common.util.ui.AutoCombo: Empty array param [data]');

            d = data.sort((function(){
                return function (a, b) {return (a === b ? 0 : (a < b ? -1 : 1));};
            })());
            var replace_placeholder = function (event) {
                if (event.type === 'keydown' && (event.which !== 40 && event.which !== 38)) return;
                var val = combo.$input.val().replace(/<(|\/)strong>/gi, "");
                combo.$input.val(val);
            };
            combo.state = 'blurred';
            combo.init_blurkill = false;
            combo.open = function () {
                if ('$input' in combo && combo.$input) combo.$input.autocomplete('search', '').select();
            };
            combo.placeholder = placeholder || '';
            combo.autocomplete_obj = { // TODO AG: move into og.analytics.form
                minLength: 0, delay: 0,
                open: function(event) {},
                source: function (req, res) {
                    var escaped = $.ui.autocomplete.escapeRegex(req.term),
                        matcher = new RegExp(escaped, 'i'),
                        htmlize = function (str) {
                            return !req.term ? str : str.replace(
                                new RegExp(
                                    '(?![^&;]+;)(?!<[^<>]*)(' + escaped + ')(?![^<>]*>)(?![^&;]+;)', 'gi'
                                ), '<strong>$1</strong>'
                            );
                        };
                    if (d && d.length) {
                        res(d.reduce(function (acc, val) {
                            if (!req.term || val && matcher.test(val)) acc.push({label: htmlize(val)});
                            return acc;
                        }, []));
                    }
                },
                close: replace_placeholder,
                focus: replace_placeholder
            };
            // wrap input in div, enable input width 100% of parent, FF, IE
            combo.$wrapper = $('<div>').html('<input type="text">');
            combo.$input = combo.$wrapper.find('input');
            combo.$button = $('<div class="OG-icon og-icon-down"></div>');
            if (combo.$input && combo.$button) {
                combo.$input
                    .autocomplete(combo.autocomplete_obj)
                    .attr('placeholder', placeholder)
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
                        if (combo.$input && combo.$input.val() === placeholder) combo.$input.val('');
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
                $([combo.$wrapper, combo.$button]).prependTo(selector);
                if (input_val) combo.$input.val(input_val);
            }
            return combo;
        };
    }
});