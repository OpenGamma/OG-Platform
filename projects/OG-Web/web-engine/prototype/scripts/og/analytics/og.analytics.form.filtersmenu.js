/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.FiltersMenu',
    dependencies: [],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var FiltersMenu = function (config) {
            var block = this, form = config.form, id = og.common.id('filters'),
                menu, filters = config.filters || [{key:'', value:''}];
            var new_row = function (val, idx) {
                return new form.Block({
                    module: 'og.analytics.form_filters_row_tash',
                    extras: {
                        name: og.common.id('filters_radio'), show_key: 'key' in val, show_exp: !('key' in val),
                        key: val.key, value: val.value, idx: idx + 1
                    }
                });
            };
            form.Block.call(block, { // assign a Block instance to this (block)
                module: 'og.analytics.form_filters_tash', children: filters.map(new_row), extras: {id: id},
                processor: function (data) {
                    var indices = config.index.split('.'), last = indices.pop(), result = [];
                    Object.keys(data).forEach(function (key) {if (~key.indexOf('filters_radio')) delete data[key];});
                    $('#' + id + ' tr.row').each(function () {
                        var $row = $(this), key = $row.find('input.key').val(), value = $row.find('input.value').val();
                        result.push(key ? {key: key, value: value} : {value: value});
                    });
                    indices.reduce(function (acc, level) {
                        return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                    }, data)[last] = result;
                }
            });
            block.on('click', '#' + id + ' input[type=radio]', function (event) {
                var $target = $(event.target), $parent = $target.parents('tr.row:first'),
                    value = $target.val(), idx = $parent.find('td.number span').html();
                new_row(value === 'keyval' ? {key: '', value: ''} : {value: ''}, +idx - 1)
                    .html(function (html) {$parent.replaceWith(html);});
            }).on('click', '#' + id + ' .og-js-add', function (event) {
                var $rows = $('#' + id + ' tr.row');
                event.preventDefault();
                new_row({key: '', value: ''}, $rows.length)
                    .html(function (html) {$('#' + id + ' tr.og-js-buttons').before(html);});
            }).on('click', '#' + id + ' .og-js-rem', function (event) {
                $(event.target).parents('tr.row:first').remove();
                $('#' + id + ' td.number span').each(function (idx) {$(this).html(idx + 1);});
            }).on('click', '#' + id + ' .og-menu-actions button', function (event) {
                return menu.button_handler($(event.target).text()), menu.stop(event), false;
            });

            form.on('form:load', function () {
                menu = new og.common.util.ui.DropMenu({cntr: $('.og-filters', '.OG-analytics-form')});
                og.common.events.on('filters:dropmenu:open', function() {menu.fire('dropmenu:open', this);});
                og.common.events.on('filters:dropmenu:close', function() {menu.fire('dropmenu:close', this);});
                og.common.events.on('filters:dropmenu:focus', function() {menu.fire('dropmenu:focus', this);});
            });
        };
        FiltersMenu.prototype = new Block; // inherit Block prototype
        return FiltersMenu;
    }
});