/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.default',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form, api = og.api.rest;
        return function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                master = config.data.template_data.configJSON.data, config_type = config.type,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name,
                resource_id = config.data.template_data.object_id, type_map = config.type_map || {},
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                new_name = '',
                form = new Form({
                    module: 'og.views.forms.config_default',
                    data: {name: null},
                    type_map: type_map,
                    selector: selector,
                    extras: {name: orig_name, raw: is_new ? '{}' : JSON.stringify(master, null, 2)},
                    processor: function (data) {
                        var key, parsed = JSON.parse(data.raw);
                        delete data.raw;
                        // turn data into parsed object so meta mapping can happen
                        for (key in parsed) data[key] = parsed[key];
                        new_name = data.name;
                        if (!type_map.name) delete data.name;
                    }
                }),
                form_id = '#' + form.id,
                save_resource = function (result) {
                    var data = result.data, meta = result.meta, as_new = result.extras.as_new;
                    if (!deleted && !is_new && as_new && (orig_name === new_name))
                        return window.alert('Please select a new name.');
                    api.configs.put({
                        id: as_new ? void 0 : resource_id,
                        name: new_name,
                        json: JSON.stringify({data: data, meta: meta}),
                        type: config_type,
                        loading: loading,
                        handler: as_new ? save_new_handler : save_handler
                    });
                };
            og.dev.warn('using default config template for config type:\n' + config_type);
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-toolbar"></div>\
                          <h1 class="og-js-name">' + orig_name + '</h1>\
                        </header>';
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    load_handler(form);
                }},
                {type: 'keyup', selector: form_id + ' [name=name]', handler: function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                }},
                {type: 'form:submit', handler: save_resource}
            ]);
            form.dom();
        };
    }
});