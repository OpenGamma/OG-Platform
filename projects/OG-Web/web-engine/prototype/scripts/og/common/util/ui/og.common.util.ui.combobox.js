/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.combobox',
    dependencies: [],
    obj: function () {
        return function (obj) {
            var placeholder = obj.placeholder, selector = obj.selector, $input, $button, autocomplete_obj,
                list = obj.data.map(function (val) {return val.replace(/^.*\|(.*)\|.*$/, '$1');}),
                open = function () {
                    // open using the current input value or an empty string
                    $input.autocomplete('search', ($input.val() !== placeholder) ? ($input.val() || '') : '').select();
                };
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
            $input = $('<input type="text" />')
                .autocomplete(autocomplete_obj).attr('placeholder', placeholder).on('mouseup', open);
            // Enable html list items
            $input.data('autocomplete')._renderItem = function(ul, item) {
                return $('<li></li>').data('item.autocomplete', item).append('<a>' + item.label + '</a>').appendTo(ul);
            };
            $button = $('<div class="OG-icon og-icon-down"></div>').on('click', function () {
                return $input.autocomplete('widget').is(':visible') ? $input.autocomplete('close').select() : open();
            });
            return $([$input, $button]).prependTo(selector);
        }
    }
});