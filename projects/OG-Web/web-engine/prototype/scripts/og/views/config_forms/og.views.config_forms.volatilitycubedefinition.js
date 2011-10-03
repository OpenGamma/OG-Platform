/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.volatilitycubedefinition',
    dependencies: [
        'og.api.text',
        'og.api.rest',
        'og.common.util.ui',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest, arr = function (obj) {
            return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [];
        };
        return function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name, submit_type,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                master = config.data.template_data.configJSON,
                class_name = 'com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition',
                fields = ['swapTenors', 'optionExpiries', 'relativeStrikes'], new_item,
                form = new ui.Form({
                    module: 'og.views.forms.volatility-cube-definition',
                    extras: {name: orig_name},
                    data: fields.reduce(function (acc, val) {return acc[val] = [], acc;}, {0: master[0] || class_name}),
                    selector: selector,
                    processor: function (data) {
                        var $strips = $(form_id + ' .og-js-strips');
                        fields.forEach(function (field, idx) {
                            var to_num = idx === 2; // Relative Strikes are numbers
                            $($strips[idx]).find('.og-js-strip input').each(function (idx, input) {
                                data[field].push($(input).val());
                            });
                        });
                    }
                }),
                form_id = '#' + form.id,
                save_resource = function (data, as_new) {
                    var name = data.name;
                    if (!deleted && !is_new && as_new && (orig_name === name))
                        return window.alert('Please select a new name.');
                    delete data.name;
                    api.configs.put({
                        id: as_new ? undefined : resource_id,
                        name: name,
                        json: JSON.stringify(data),
                        loading: loading,
                        handler: as_new ? save_new_handler : save_handler
                    });
                };
            new_item = function (val, idx) {
                return new form.Block({module: 'og.views.forms.volatility-cube-definition-val', extras: {value: val}});
            };
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-toolbar"></div>\
                          <h1 class="og-js-name">' + orig_name + '</h1>\
                          <br />(Volatility Cube Definition)\
                        </header>\
                    ';
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    if (deleted || is_new)
                        $(form_id + ' .og-js-submit[value=save]').remove(), submit_type = 'save_as_new';
                    if (is_new) $(form_id + ' .og-js-submit[value=save_as_new]').text('Save');
                    load_handler();
                }},
                {type: 'click', selector: form_id + ' .og-js-submit', handler: function (e) {
                    submit_type = $(e.target).val();
                }},
                {type: 'click', selector: form_id + ' .og-js-rem', handler: function (e) {
                    $(e.target).parents('.og-js-strip:first').remove();
                }},
                {type: 'click', selector: form_id + ' .og-js-add', handler: function (e) {
                    new_item('').html(function (html) {
                        $(e.target).parents('.og-js-holder:first').find('.og-js-strips').append($(html));
                    });
                }},
                {type: 'form:submit', handler: function (result) {
                    save_resource(result.data, submit_type === 'save_as_new');
                }},
                {type: 'keyup', selector: form_id + ' input[name=name]', handler: function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                }}
            ]);
            form.children = fields.map(function (field) {
                return new form.Block({
                    wrap: '<ul class="og-awesome-list og-js-strips">{{html html}}</ul>',
                    children: (master[field] = arr(master[field])).map(new_item)
                });
            });
            form.dom();
        };
    }
});