/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.forms.Constraints',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, id_count = 0, prefix = 'constraints_widget_';
        return function (config) {
            var data = config.data, data_index = config.index, render, classes = config.classes || '',
                ids = {
                    container: prefix + id_count++,
                    widget: prefix + id_count++,
                    row_with: prefix + id_count++,
                    row_without: prefix + id_count++
                },
                convert = function (datum) {
                    var length = 0, item, data = [], lcv;
                    if (!datum || typeof datum === 'string') return datum || '';
                    for (item in datum) if (+item + 0 === +item) length += 1;
                    for (lcv = 0; lcv < length; lcv += 1) data.push(datum[lcv]);
                    return data.map(function (str) {return str.replace(/,/g, '\\,');}).join(', ');
                },
                deconvert = function (datum, optional) {
                    var array = datum ?
                        datum.replace(/\\\,/g, '\0').split(/\,\s*/g).map(function (s) {return s.replace(/\0/g, ',');})
                            : [], result, empty = true;
                    if (!optional && !array.length) return null;
                    result = array.reduce(function (acc, val, idx) {
                        empty = false;
                        return val ? (acc[idx] = val, acc) : acc;
                    }, {});
                    if (empty && !optional) return null;
                    if (optional) result.optional = null;
                    return result;
                };
            return new config.form.Block({
                module: 'og.views.forms.constraints',
                extras: $.extend({classes: classes}, ids),
                processor: function (data) {
                    var indices = data_index.split('.'), last = indices.pop(), result = {},
                        $withs = $('#' + ids.widget + ' .og-js-with'),
                        $withouts = $('#' + ids.widget + ' .og-js-without');
                    if (!$('#' + ids.widget).length) return;
                    $withs.each(function (idx, el) {
                        var $el = $(el), optional = !!$el.find('input[type=checkbox]').filter(':checked').length,
                            key = $el.find('input.og-js-key').val();
                        if (!key) throw Error('Type in a with constraint must be defined.');
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
                },
                handlers: [
                    {type: 'form:load', handler: function () {
                        var item, $widget = $('#' + ids.widget), rows = {
                            'with': $('#' + ids.row_with).remove().removeAttr('id'),
                            without: $('#' + ids.row_without).remove().removeAttr('id')
                        };
                        render = {
                            'with': function (datum, $replace, $after) {
                                var item, add = function (item) {
                                    var $row = rows['with'].clone(), $inputs = $row.find('input'),
                                        value = convert(datum[item]);
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
                    }},
                    {type: 'change', selector: '#' + ids.widget + ' input.og-js-radio', handler: function (e) {
                        var target = e.target, value = target.value;
                        if (value === 'without' && $('#' + ids.widget + ' .og-js-without-field').length)
                            return alert('Sorry, but only one "without" constraint at a time.'), e.target.checked = '';
                        render[value]({'with': {'': null}, without: ''}[value], $(target).closest('.og-js-row'));
                    }},
                    {type: 'click', selector: '#' + ids.widget + ' .og-js-rem', handler: function (e) {
                        $(e.target).closest('.og-js-row').remove();
                    }},
                    {type: 'click', selector: '#' + ids.container + ' .og-js-add', handler: function (e) {
                        render['with']({'': null});
                    }}
                ]
            });
        };
    }
});