/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs.curvespecificationbuilderconfiguration',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest;
        return function (config) {
            og.dev.log('config.data!', config.data.template_data.configJSON);
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name, submit_type,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                master = config.data.template_data.configJSON,
                form = new ui.Form({
                    module: 'og.views.forms.curve-specification-builder',
                    data: master,
                    selector: selector,
                    extras: {name: master.name}
                }),
                form_id = '#' + form.id,
                save_resource = function (data, as_new) {
                    var name = data.name;
                    if (as_new && (orig_name === name)) return window.alert('Please select a new name.');
                    delete data.name;
                    api.configs.put({
                        id: as_new ? undefined : resource_id,
                        name: name,
                        json: JSON.stringify(data),
                        loading: loading,
                        handler: as_new ? save_new_handler : save_handler
                    });
                };
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-toolbar"></div>\
                          <h1 class="og-js-name">' + orig_name + '</h1>\
                          <br />(Curve Specification Builder Configuration)\
                        </header>\
                    ';
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    load_handler();
                }},
                {type: 'form:submit', handler: function (result) {
                    save_resource(result.data, submit_type === 'save_as_new');
                }}
            ]);
            form.dom();
        };
    }
});