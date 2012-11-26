/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_blocks.Constraints',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, prefix = 'constraints', Block = og.common.util.ui.Block;
        var Constraints = function (config) {
            var block = this, render, classes = config.classes || '', ids = {
                container: og.common.id(prefix), widget: og.common.id(prefix),
                row_with: og.common.id(prefix), row_without: og.common.id(prefix)
            }, data = config.data, data_index = config.index;
            var convert = function (datum) {
                var length = 0, item, data = [], lcv;
                if (!datum || typeof datum === 'string') return datum || '';
                for (item in datum) if (+item + 0 === +item) length += 1;
                for (lcv = 0; lcv < length; lcv += 1) data.push(datum[lcv]);
                return data.length > 1 ?
                    '[' + data.map(function (str) {return str.replace(/\,/g, '\\,');}).join(', ') + ']' : data.join('');
            };
            var deconvert = function (datum, optional) {
                var empty = true, result, array = to_array(datum);
                if (!optional && !array.length) return null;
                result = array
                    .reduce(function (acc, val, idx) {return (empty = 0), val ? (acc[idx] = val, acc) : acc;}, {});
                if (empty && !optional) return null;
                if (optional) result.optional = null;
                return result;
            };
            var to_array = function (datum) {
                return datum ?
                    /^\[(.*)\]$/.test(datum) ? RegExp.$1.replace(/\\\,/g, '\0').split(/,\s*/)
                        .map(function (str) {return str.replace(/\0/g, ',');}) : [datum]
                    : [];
            };
            config.form.Block.call(block, { // assign a Block instance to this (block)
                extras: $.extend({classes: classes}, ids),
                processor: function (data) {
                    var indices = data_index.split('.'), last = indices.pop(), result = {},
                        $withs = $('#' + ids.widget + ' .og-js-with'),
                        $withouts = $('#' + ids.widget + ' .og-js-without');
                    if (!$('#' + ids.widget).length) return;
                    $withs.each(function (idx, el) {
                        var $el = $(el), optional = !!$el.find('input[type=checkbox]').filter(':checked').length,
                            key = $el.find('input.og-js-key').val();
                        if (!key) throw new Error(module.name + ': in a with constraint, type must be defined');
                        if (!result['with']) result['with'] = {};
                        result['with'][key] = deconvert($el.find('input.og-js-value').val(), optional);
                    });
                    $withouts.each(function (idx, el) {
                        var value = $(el).find('input.og-js-key').val();
                        if (value) result.without = deconvert(value, false);
                    });
                    indices.reduce(function (acc, level) {
                        return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                    }, data)[last] = result;
                }
            });
            block.on('form:load', function () {
                var item, $widget = $('#' + ids.widget), rows = {
                    'with': $('#' + ids.row_with).remove().removeAttr('id'),
                    without: $('#' + ids.row_without).remove().removeAttr('id')
                };
                render = {
                    'with': function (datum, $replace, $after) {
                        var item, add = function (item) {
                            var $row = rows['with'].clone(), $inputs = $row.find('input'), value = convert(datum[item]);
                            $inputs[0].checked = 'checked', $inputs[2].value = item, $inputs[3].value = value;
                            if (datum[item] && typeof datum[item] === 'object' && ('optional' in datum[item]))
                                $inputs[4].checked = 'checked';
                            if (!$replace && !$after) return $widget.append($row);
                            if ($replace) return $replace.replaceWith($row);
                            if ($after) return $after.after($row);
                        };
                        for (item in datum) add(item);
                    },
                    without: function (datum, $replace) {
                        var $row = rows.without.clone(), $inputs = $row.find('input'), value = convert(datum);
                        $inputs[1].checked = 'checked', $inputs[2].value = value;
                        if (!$replace) return $widget.append($row);
                        $replace.replaceWith($row);
                    }
                };
                for (item in data) render[item](data[item]);
            }).on('change', '#' + ids.widget + ' input.og-js-radio', function (event) {
                var target = event.target, value = target.value;
                if (value === 'without' && $('#' + ids.widget + ' .og-js-without-field').length)
                    return alert('Sorry, but only one "without" constraint at a time.'), event.target.checked = '';
                render[value]({'with': {'': null}, without: ''}[value], $(target).closest('.og-js-row'));
            }).on('click', '#' + ids.widget + ' .og-js-rem', function (event) {
                $(event.target).closest('.og-js-row').remove();
            }).on('click', '#' + ids.container + ' .og-js-add', function (event) {render['with']({'': null});});
        };
        Constraints.prototype = new Block(null, {module: 'og.views.forms.constraints_tash'}); // inherit Block prototype
        return Constraints;
    }
});