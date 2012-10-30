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
        return function (selector, placeholder, d) {
            var combo = this, data;
            data = d.sort((function(i){ // sort by name
                return function (a, b) {return (a[i] === b[i] ? 0 : (a[i] < b[i] ? -1 : 1));};
            })('name'));
            combo.state = 'blurred';
            combo.open = function () {
                combo.$input.autocomplete('search', '').select();
            };
            combo.placeholder = placeholder || '';
            combo.autocomplete_obj = {
                minLength: 0, delay: 0,
                open: function() {
                    $(this).autocomplete('widget').blurkill(function () {
                        combo.$input.autocomplete('close');
                    });
                },
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
            };
            // wrap input in div, enable input width 100% of parent, FF, IE
            combo.$wrapper = $('<div>').html('<input type="text">');
            combo.$input = combo.$wrapper.find('input');
            combo.$input
                .autocomplete(combo.autocomplete_obj)
                .attr('placeholder', placeholder)
                .on('mouseup', combo.open)
                .on('blur', function () {
                    combo.state = 'blurred';
                    if (combo.$input.val() === placeholder) combo.$input.val('');
                })
                .on('focus', function () {
                    combo.state = 'focused';
                    combo.$input.trigger('open', combo.$input);
                });
            combo.$input.data('autocomplete')._renderItem = function(ul, item) { // Enable html list items
                return $('<li></li>').data('item.autocomplete', item).append('<a>' + item.label + '</a>')
                    .appendTo(ul);
            };
            combo.$button = $('<div class="OG-icon og-icon-down"></div>').on('click', function () {
                return combo.$input.autocomplete('widget').is(':visible') ? 
                    combo.$input.autocomplete('close').select() : combo.open();
            });
            $([combo.$wrapper, combo.$button]).prependTo(selector);
            return combo;
        };
    }
});