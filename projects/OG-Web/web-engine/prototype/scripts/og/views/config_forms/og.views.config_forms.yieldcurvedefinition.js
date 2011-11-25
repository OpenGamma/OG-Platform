/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.yieldcurvedefinition',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest, Form = ui.Form,
            arr = function (obj) {return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [];};
        return function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                master = config.data.template_data.configJSON.data, strips,
                CONV = 'conventionName', NUMF = 'numFutures',
                CURV = 'CurveSpecificationBuilderConfiguration', INTR = 'interpolatorName',
                INDX = '<INDEX>',
                sep = '~',
                config_type = config.type,
                type_map = [
                    [['0', INDX].join('.'),                 Form.type.STR],
                    ['currency',                            Form.type.STR],
                    [INTR,                                  Form.type.STR],
                    ['name',                                Form.type.STR],
                    ['region',                              Form.type.STR],
                    [['strip', INDX, CONV].join('.'),       Form.type.STR],
                    [['strip', INDX, NUMF].join('.'),       Form.type.BYT],
                    [['strip', INDX, 'tenor'].join('.'),    Form.type.STR],
                    [['strip', INDX, 'type'].join('.'),     Form.type.STR],
                    ['uniqueId',                            Form.type.STR]
                ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {}),
                form = new Form({
                    module: 'og.views.forms.yield-curve-definition',
                    data: master,
                    type_map: type_map,
                    selector: selector,
                    extras: {
                        name: master.name, currency: master.currency || (master.currency = 'USD'),
                        interpolator: master[INTR]
                    },
                    processor: function (data) {
                        data.strip = data.strip.filter(function (v) {return v !== undefined;}); // remove undefineds
                    }
                }),
                form_id = '#' + form.id,
                new_conv_dropdown = function (row, idx, currency) {
                    return new forms.Dropdown({
                        form: form, classes: 'og-convention og-js-conv', value: row[CONV],
                        index: ['strip', idx, CONV].join('.'), placeholder: 'Please select...',
                        data_generator: function (handler) {
                            api.configs.get({
                                page: '*', name: '*_' + currency, type: CURV, cache_for: 30 * 1000,
                                handler: function (result) {
                                    handler(result.data.data.map(function (val) {
                                        var value = val.split('|')[1].match(/^([^_]+)/)[1];
                                        return {value: value, text: value};
                                    }));
                                }
                            });
                        }
                    });
                },
                new_strip = function (row, idx) {
                    return new form.Block({
                        module: 'og.views.forms.yield-curve-definition-strip',
                        extras: {idx: idx, tenor: row.tenor, future: row[NUMF]},
                        children: [new_conv_dropdown(row, idx, master.currency)],
                        handlers: [
                            {type: 'form:load', handler: function () {
                                $(form_id + ' [name="' + ['strip', idx, 'type'].join('.') + '"]').val(row.type);
                                if (row.type !== 'FUTURE')
                                    $(form_id + ' [name="' + ['strip', idx, NUMF].join('.') + '"]')
                                        .attr('disabled', 'disabled');
                            }},
                            {
                                type: 'change',
                                selector: form_id + ' [name="' + ['strip', idx, 'type'].join('.') + '"]',
                                handler: function (e) {
                                    var $el = $(form_id + ' [name="' + ['strip', idx, NUMF].join('.') + '"]'),
                                        is_future = $(e.target).val() === 'FUTURE';
                                    $el.attr('disabled', is_future ? '' : 'disabled');
                                    if (!is_future) $el.attr('value', '');
                                }
                            }
                        ]
                    });
                },
                save_resource = function (result) {
                    var data = result.data, meta = result.meta, as_new = result.extras.as_new;
                    if (as_new && (orig_name === data.name + '_' + data.currency))
                        return window.alert('Please select a new name and/or currency.');
                    api.configs.put({
                        id: as_new ? void 0 : resource_id,
                        name: data.name + '_' + data.currency,
                        json: JSON.stringify({data: data, meta: meta}),
                        type: config_type,
                        loading: loading,
                        handler: as_new ? save_new_handler : save_handler
                    });
                };
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-tools"></div>\
                          <h1>\
                            <span class="og-js-name">' + master.name + '</span>_<span class="og-js-currency">' +
                            master.currency + '</span>\
                          </h1>\
                          (Yield Curve Definition)\
                        </header>\
                    ';
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    $(form_id + ' [name=currency]').val(master.currency);
                    load_handler(form);
                }},
                {type: 'form:submit', handler: save_resource},
                {type: 'change', selector: form_id + ' [name=currency]', handler: function (e) {
                    var currency = $(e.target).val();
                    $('.ui-layout-inner-center  .og-js-currency').text(currency);
                    $(form_id + ' .og-js-conv').each(function () {
                        var $el = $(this), idx = $el.attr('name').split('.').slice(1, -1),
                            row = master.strip[idx], value = row[CONV] || (row[CONV] = $el.val());
                        new_conv_dropdown(row, idx, currency).html(function (html) {
                            $el.replaceWith($(html).val(value));
                        });
                    });
                }},
                {type: 'keyup', selector: form_id + ' [name=name]', handler: function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                }},
                {type: 'click', selector: form_id + ' .og-js-rem', handler: function (e) { // remove a strip
                    var $el = $(e.target).parents('.og-js-strip:first'),
                        data_idx = $el.find('input').attr('name').split('.').slice(1, -1);
                        master.strip[data_idx] = undefined;
                        $el.remove();
                }},
                {type: 'click', selector: form_id + ' .og-js-add', handler: function (e) { // add a strip
                    var block = new_strip({}, master.strip.push({}) - 1);
                    block.html(function (html) {$(form_id + ' .og-js-strips').append($(html)), block.load();});
                }}
            ]);
            form.children = [
                new form.Field({module: 'og.views.forms.currency', generator: function (handler, template) { // item_0
                    handler(template);
                }}),
                new forms.Dropdown({ // item_1
                    form: form, value: master.region.split(sep)[1],
                    resource: 'regions', placeholder: 'Please select...',
                    processor: function (selector, data, errors) {
                        data.region = master.region.split(sep)[0] + sep + $(selector).val();
                    },
                    data_generator: function (handler) {
                        api.regions.get({
                            page: '*',
                            handler: function (result) {
                                handler(result.data.data.map(function (region) {
                                    var split = region.split('|');
                                    if (!split[3]) return null;
                                    return {value: split[3], text: split[3] + ' - ' + split[1]}
                                }).filter(Boolean).sort(function (a, b) { // alphabetize
                                    return a.text < b.text ? -1 : a === b ? 0 : 1;
                                }));
                            }
                        });
                    }
                }),
                strips = new form.Block({wrap: '<ul class="og-awesome-list og-js-strips">{{html html}}</ul>'}) // item_2
            ];
            if ((master.strip = arr(master.strip)).length)
                Array.prototype.push.apply(strips.children, master.strip.map(new_strip));
            form.dom();
        };
    }
});