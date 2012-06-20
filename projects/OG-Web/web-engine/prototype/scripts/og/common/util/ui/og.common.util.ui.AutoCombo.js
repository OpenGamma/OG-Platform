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
            var $wrapper, $input, $button,
                list = data.map(function (val) {return val.replace(/^.*\|(.*)\|.*$/, '$1');}), autocomplete_obj,
                open = function () {
                    // open using the current input value or an empty string
                    $input.autocomplete('search', ($input.val() !== placeholder) ? ($input.val() || '') : '').select();
                };
            placeholder = placeholder || '';
            autocomplete_obj = {
                minLength: 0, delay: 0,
                source: function (req, res) {
                    var escaped = $.ui.autocomplete.escapeRegex(req.term),
                        matcher = new RegExp(escaped, 'i'),
                        htmlize = function (str) {
                            return !req.term ? str : str.replace(
                                new RegExp('(?![^&;]+;)(?!<[^<>]*)(' + escaped + ')(?![^<>]*>)(?![^&;]+;)', 'gi'),
                                '<strong>$1</strong>'
                            );
                        };
                    res(list.reduce(function (acc, val) {
                        if (val && (!req.term || matcher.test(val))) acc.push({label: htmlize(val), value: val});
                        return acc;
                    }, []));
                }
            };
            $wrapper = $('<div />'); // wrap input in div to enable input width at 100% of parent, FF, IE
            $input = $wrapper.html('<input type="text" />').find('input')
                .autocomplete(autocomplete_obj).attr('placeholder', placeholder).on('mouseup', open)
                .on('blur', function () {if ($input.val() === placeholder) $input.val('');})
                .on('focus', function () {$input.trigger('open', $input)});
            // Enable html list items
            $input.data('autocomplete')._renderItem = function(ul, item) {
                return $('<li></li>').data('item.autocomplete', item).append('<a>' + item.label + '</a>').appendTo(ul);
            };
            $button = $('<div class="OG-icon og-icon-down"></div>').on('click', function () {
                return $input.autocomplete('widget').is(':visible') ? $input.autocomplete('close').select() : open();
            });
            $([$wrapper, $button]).prependTo(selector);
            this.select = function () {$input.select();};
        }
    }
});