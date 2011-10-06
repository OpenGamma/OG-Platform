/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.default',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form, api = og.api;
        return function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                master = config.data.template_data.configJSON.data, config_type = config.type,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name, submit_type,
                resource_id = config.data.template_data.object_id, meta_map = config.meta || {},
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                new_name = '',
                form = new Form({
                    module: 'og.views.forms.config_default',
                    data: {name: null},
                    meta: meta_map,
                    selector: selector,
                    extras: {name: orig_name, raw: is_new ? '{}' : JSON.stringify(master, null, 2)},
                    processor: function (data) {
                        var key, parsed = JSON.parse(data.raw);
                        delete data.raw;
                        // turn data into parsed object so meta mapping can happen
                        for (key in parsed) data[key] = parsed[key];
                        new_name = data.name;
                        if (!meta_map.name) delete data.name;
                    }
                }),
                form_id = '#' + form.id,
                save_resource = function (result, as_new) {
                    var data = result.data, meta = result.meta;
                    if (!deleted && !is_new && as_new && (orig_name === new_name))
                        return window.alert('Please select a new name.');
                    console.log('data:\n', data, 'meta:\n', meta);
                    return;
                    api.configs.put({
                        id: as_new ? undefined : resource_id,
                        name: new_name,
                        json: JSON.stringify({data: data, meta: meta}),
                        loading: loading,
                        handler: as_new ? save_new_handler : save_handler
                    });
                };
            console.log('config:\n', config);
            og.dev.warn('using default config template for config type: ' + config_type);
            console.log('meta:\n', config.data.template_data.configJSON.meta);
            console.log('meta_map:\n', config.meta);
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-toolbar"></div>\
                          <h1 class="og-js-name">' + orig_name + '</h1>\
                        </header>';
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    if (deleted || is_new)
                        $(form_id + ' .og-js-submit[value=save]').remove(), submit_type = 'save_as_new';
                    if (is_new) $(form_id + ' .og-js-submit[value=save_as_new]').text('Save');
                    load_handler();
                }},
                {type: 'keyup', selector: form_id + ' [name=name]', handler: function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                }},
                {type: 'form:submit', handler: function (result) {
                    save_resource(result, submit_type === 'save_as_new');
                }}
            ]);
            form.dom();
        };
    }
});