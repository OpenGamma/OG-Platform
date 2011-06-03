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
            var form = config.form, data = config.data, block, rows, $widget, index = config.index, render,
                ids = {widget: prefix + id_count++, row_with: prefix + id_count++, row_without: prefix + id_count++},
                convert = function (datum) {
                    var length = 0, item;
                    if (!datum || typeof datum === 'string') return datum || '';
                    for (item in datum) if (+item + 0 === +item) length += 1;
                    datum.length = length;
                    return Array.prototype.join.call(datum, ', ');
                },
                deconvert = function (datum, optional) {
                    var array = datum.split(/,\s*/g), result;
                    if (!optional && array.length === 1) return datum ? datum : null;
                    result = array.reduce(function (acc, val, idx) {return val ? (acc[idx] = val, acc) : acc;}, {});
                    if (optional) result.optional = null;
                    return result;
                };
            block = new form.Block({
                module: 'og.views.forms.constraints',
                extras: ids,
                processor: function (data) {
                    var indices = index.split('.'), last = indices.pop(), result = {};
                    $('#' + ids.widget + ' tr.og-js-with').each(function (idx, el) {
                        var $el = $(el), optional = $el.find('input[type=checkbox]').filter(':checked').length,
                            key = $el.find('input.og-js-key').val(),
                            value = deconvert($el.find('input.og-js-value').val(), optional);
                        if (!key) throw Error('Type in a with constraint must be defined.');
                        if (!result['with']) result['with'] = {};
                        result['with'][key] = value;
                    });
                    $('#' + ids.widget + ' tr.og-js-without').each(function (idx, el) {
                        var $el = $(el), key = $el.find('input.og-js-key').val();
                        result.without = key || {};
                    });
                    indices.reduce(function (acc, level) {
                        return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                    }, data)[last] = result;
                },
                handlers: [
                    {type: 'form:load', handler: function () {
                        var item;
                        $widget = $('#' + ids.widget);
                        rows = {
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
                                var $row = rows.without.clone(), $inputs = $row.find('input'), value = datum || '';
                                $inputs[1].checked = 'checked', $inputs[2].value = value;
                                if (!$replace) return $widget.append($row);
                                $replace.replaceWith($row);
                            }
                        };
                        for (item in data) render[item](data[item]);
                    }},
                    {type: 'change', selector: '#' + ids.widget + ' input.og-js-radio', handler: function (e) {
                        var target = e.target, value = target.value;
                        if (value === 'without' && $('#' + ids.widget + ' td.og-without').length)
                            return alert('Sorry, but only one without at a time.'), e.target.checked = '';
                        render[value]({'with': {'': null}, without: ''}[value], $(target).closest('tr'));
                    }},
                    {type: 'click', selector: '#' + ids.widget + ' .og-icon-remove', handler: function (e) {
                        $(e.target).closest('tr').remove();
                    }},
                    {type: 'click', selector: '#' + ids.widget + ' .og-icon-add', handler: function (e) {
                        render['with']({'': null}, null, $(e.target).closest('tr'));
                    }}
                ]
            });
            return block;
        };
    }
});