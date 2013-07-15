/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Dropdown',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Dropdown = function (config) {
            var block = this, resource = config.resource, form = config.form, value = config.value,
                fields = config.fields || [0], values = fields[0], id = og.common.id('dropdown'),
                rest_options = $.extend({page: '*', cache_for: 30 * 1000}, config.rest_options), has = 'hasOwnProperty',
                data_generator = config.data_generator, texts = typeof fields[1] !== 'undefined' ? fields[1] : values,
                processor = config.processor, meta = rest_options.meta, disabled = !!config.disabled;
            var generator = function (handler, template, template_data) {
                var rest_handler = function (result) {
                    if (result.error) {
                        return handler('an error occurred');
                    }
                    if (meta) {
                        template_data.options = result.data.types // meta results
                            .map(function (datum) {return {value: datum, text: datum, selected: value === datum};});
                    } else if ($.isArray(result.data)) {
                        template_data.options = result.data // simple results
                            .map(function (datum) {
                                var option, text;
                                if (typeof datum === 'string') {
                                    option = text = datum;
                                } else {
                                    option = datum[values];
                                    text = datum[texts];
                                }
                                return {value: option, text: text, selected: value === option};
                            });
                    } else {
                        template_data.options = result.data.data.map(function (datum) { // search results
                            var fields = datum.split('|');
                            return {value: fields[values], text: fields[texts], selected: value === fields[values]};
                        });
                    }
                    template_data.disabled = !template_data.options.length || disabled;
                    handler(template(template_data));
                };
                if (meta && !config.rest_options[has]('page')) {
                    // remove default paging option for meta requests
                    delete rest_options.page;
                }
                if (!data_generator) {
                    return resource.split('.')
                        .reduce(function (api, key) {
                            return api[key];
                        }, og.api.rest).get(rest_options).pipe(rest_handler);
                }
                data_generator(function (data) {
                    template_data.options = data.map(function (datum) {
                        return typeof datum === 'string' ? {value: datum, text: datum, selected: value === datum}
                            : {value: datum.value, text: datum.text, selected: value === datum.value};
                    });
                    template_data.disabled = !template_data.options.length || disabled;
                    return handler(template(template_data));
                });
            };
            form.Block.call(block, {
                generator: generator, processor: processor ? processor.partial('#' + id) : null,
                extras: {
                    id: id, name: config.index, classes: config.classes,
                    style: config.style, placeholder: config.placeholder
                }
            });
            block.id = id;
        };
        Dropdown.prototype = new Block(); // inherit Block prototype
        Dropdown.prototype.off = function () {
            var block = this, args = Array.prototype.slice.call(arguments, 1), type = arguments[0];
            return Block.prototype.off.apply(block, [type, '#' + block.id].concat(args));
        };
        Dropdown.prototype.on = function () {
            var block = this, args = Array.prototype.slice.call(arguments, 1), type = arguments[0];
            return Block.prototype.on.apply(block, [type, '#' + block.id].concat(args));
        };
        Dropdown.prototype.template = Handlebars.compile('\
            <select id="{{id}}" {{#classes}}class="{{../classes}}"{{/classes}} {{#name}}name="{{../name}}"{{/name}}\
                {{#style}}style="{{../style}}"{{/style}}\
                {{#if disabled}} disabled="disabled"{{/if}}>\
                {{#placeholder}}<option value="">{{{../placeholder}}}</option>{{/placeholder}}\
                {{#each options}}\
                    <option value="{{value}}"{{#selected}}selected="selected"{{/selected}}>{{{text}}}</option>\
                {{/each}}\
            </select>\
        ');
        return Dropdown;
    }
});