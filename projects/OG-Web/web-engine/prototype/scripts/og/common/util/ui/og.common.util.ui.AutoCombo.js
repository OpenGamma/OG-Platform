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
        return function (selector, placeholder, data) {
            var AutoCombo = this, Combo;
            return Combo = function () {
                var combo = this;
                return combo.state = 'blurred',
                    combo.open = function () { // open using the current input value or an empty string
                        combo.$input.autocomplete('search', (combo.$input.val() !== placeholder) ? 
                        (combo.$input.val() || '') : '').select();
                    },
                    combo.placeholder = placeholder || '', 
                    combo.autocomplete_obj = {
                        minLength: 0, delay: 0,
                        open: function() {$(this).autocomplete('widget')
                            .blurkill(function () {combo.$input.autocomplete('close')});},
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
                            res(data.reduce(function (acc, val) {
                                if (!req.term || matcher.test(val.name))
                                    acc.push({label: htmlize(val.name), value: val.id});
                                return acc;
                            }, []));
                        }
                    },
                    combo.$wrapper = $('<div />'), // wrap input in div, enable input width 100% of parent, FF, IE
                    combo.$input = combo.$wrapper.html('<input type="text" />').find('input')
                        .autocomplete(combo.autocomplete_obj).attr('placeholder', placeholder).on('mouseup', open)
                        .on('blur', function () {
                            combo.state = 'blurred';
                            if (combo.$input.val() === placeholder) combo.$input.val('');
                        })
                        .on('focus', function () {
                            combo.state = 'focused';
                            combo.$input.trigger('open', combo.$input)
                        }),
                    // Enable html list items
                    combo.$input.data('autocomplete')._renderItem = function(ul, item) {
                        return $('<li></li>').data('item.autocomplete', item).append('<a>' + item.label + '</a>')
                            .appendTo(ul);
                    },
                    combo.$button = $('<div class="OG-icon og-icon-down"></div>').on('click', function () {
                        return combo.$input.autocomplete('widget').is(':visible') ? 
                            combo.$input.autocomplete('close').select() : open();
                    }), $([combo.$wrapper, combo.$button]).prependTo(selector), combo;
            }, Combo.prototype = EventEmitter.prototype, AutoCombo = new Combo();
        }
    }
});