/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.forms.Dropdown',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, id_count = 0, prefix = 'dropdown_widget_';
        return function (config) {
            var name = config.index, resource = config.resource, form = config.form, placeholder = config.placeholder,
                fields = config.fields || [0], values = fields[0], rest_options = config.rest_options || null,
                texts = typeof fields[1] !== 'undefined' ? fields[1] : values, value = config.value,
                id = prefix + id_count++, meta = rest_options && rest_options.meta, test,
                field_options = {
                    generator: function (handler) {
                        var options = $.extend({}, rest_options, {
                            cache_for: 30 * 1000,
                            handler: function (result) {
                                if (result.error) return handler('an error occurred');
                                var $html = $('<p><select/></p>'), $select = $html.find('select');
                                if (name) $select.attr('name', name);
                                $select.attr('id', id);
                                if (placeholder) $select.append($('<option/>').html(placeholder));
                                if (meta) {
                                    result.data.types.forEach(function (datum) {
                                        var $option = $('<option/>').attr('value', datum).html(datum);
                                        if (value === datum) $option[0].setAttribute('selected', 'selected');
                                        $select.append($option);
                                    });
                                } else {
                                    result.data.data.forEach(function (datum) {
                                        var fields = datum.split('|'),
                                            $option = $('<option/>').attr('value', fields[values]).html(fields[texts]);
                                        if (value === fields[values]) $option[0].setAttribute('selected', 'selected');
                                        $select.append($option);
                                    });
                                }
                                handler($html.html());
                            }
                        });
                        test = og.api.rest[resource].get(options);
                    }
                };
            if (config.processor) field_options.processor = config.processor.partial('select#' + id);
            return new form.Field(field_options);
        };
    }
});