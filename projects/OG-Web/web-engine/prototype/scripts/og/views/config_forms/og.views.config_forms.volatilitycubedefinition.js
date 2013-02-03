/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_forms.volatilitycubedefinition',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form, forms = og.views.forms, api = og.api.rest,
            INDX = '<INDEX>', EMPT = '<EMPTY>', SWAP = 'swapTenors', OPEX = 'optionExpiries', RLST = 'relativeStrikes',
            fields = [SWAP, OPEX, RLST],
            type_map = [
                [['0', INDX].join('.'),         Form.type.STR],
                [[OPEX, EMPT, INDX].join('.'),  Form.type.STR],
                [[RLST, EMPT, INDX].join('.'),  Form.type.DBL],
                [[SWAP, EMPT, INDX].join('.'),  Form.type.STR],
                ['uniqueId',                    Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {});
        var arr = function (obj) {return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [];};
        var constructor = function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector, config_type = config.type,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                master = config.data.template_data.configJSON.data, new_name = '',
                form = new Form({
                    module: 'og.views.forms.volatility-cube-definition_tash',
                    extras: {name: orig_name}, type_map: type_map, selector: selector,
                    data: fields
                        .reduce(function (acc, val) {return acc[val] = {'': []}, acc;}, {0: master[0] || config_type}),
                    processor: function (data) {
                        var $strips = $(form_id + ' .og-js-strips');
                        (new_name = data.name), delete data.name;
                        fields.forEach(function (field, idx) {
                            var to_num = idx === 2; // Relative Strikes are numbers
                            $($strips[idx]).find('.og-js-strip input')
                                .each(function (idx, input) {data[field][''].push($(input).val());});
                        });
                    }
                }),
                form_id = '#' + form.id;
            var new_item = function (val, idx) {
                return new form
                    .Block({module: 'og.views.forms.volatility-cube-definition-val_tash', extras: {value: val}});
            };
            var save_resource = function (result) {
                var data = result.data, meta = result.meta, as_new = result.extras.as_new;
                if (!deleted && !is_new && as_new && (orig_name === new_name))
                    return window.alert('Please select a new name.');
                delete data.name;
                api.configs.put({
                    id: as_new ? void 0 : resource_id,
                    name: new_name, json: JSON.stringify({data: data, meta: meta}), type: config_type, loading: loading
                }).pipe(as_new ? save_new_handler : save_handler);
            };
            form.children = fields.map(function (field) {
                return new form.Block({
                    template: '<ul class="og-awesome-list og-js-strips">{{{children}}}</ul>',
                    children: (master[field][''] = arr(master[field][''])).map(new_item)
                });
            });
            form.on('form:load', function () {
                var header = '\
                    <header class="OG-header-generic">\
                      <div class="OG-tools"></div>\
                      <h1 class="og-js-name">' + orig_name + '</h1>(Volatility Cube Definition)\
                    </header>';
                $('.OG-layout-admin-details-center .ui-layout-header').html(header);
                setTimeout(load_handler.partial(form));
            }).on('click', form_id + ' .og-js-rem', function (event) {
                $(event.target).parents('.og-js-strip:first').remove();
            }).on('click', form_id + ' .og-js-add', function (event) {
                new_item('').html(function (html) {
                    $(event.target).parents('.og-js-holder:first').find('.og-js-strips').append($(html));
                });
            }).on('keyup', form_id + ' input[name=name]', function (event) {
                $('.OG-layout-admin-details-center .og-js-name').text($(event.target).val());
            }).on('form:submit', save_resource).dom();
        };
        constructor.type_map = type_map;
        return constructor;
    }
});