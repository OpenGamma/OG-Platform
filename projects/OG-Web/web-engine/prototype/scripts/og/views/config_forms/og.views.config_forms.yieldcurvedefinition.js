/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_forms.yieldcurvedefinition',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest, Form = ui.Form,
            CONV = 'conventionName', NUMF = 'numFutures',
            CURV = 'CurveSpecificationBuilderConfiguration', INTR = 'interpolatorName',
            INTY = 'interpolateYields', LTXN = 'leftExtrapolatorName', RTXN = 'rightExtrapolatorName',
            INDX = '<INDEX>',
            type_map = [
                [['0', INDX].join('.'),                 Form.type.STR],
                ['currency',                            Form.type.STR],
                [INTR,                                  Form.type.STR],
                [INTY,                                  Form.type.BOO],
                [LTXN,                                  Form.type.STR],
                [RTXN,                                  Form.type.STR],
                ['name',                                Form.type.STR],
                ['region',                              Form.type.STR],
                [['strip', INDX, CONV].join('.'),       Form.type.STR],
                [['strip', INDX, NUMF].join('.'),       Form.type.BYT],
                [['strip', INDX, 'tenor'].join('.'),    Form.type.STR],
                [['strip', INDX, 'type'].join('.'),     Form.type.STR],
                ['uniqueId',                            Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {});
        var arr = function (obj) {return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [];};
        var constructor = function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                master = config.data.template_data.configJSON.data, strips, sep = '~', config_type = config.type,
                form = new Form({
                    module: 'og.views.forms.yield-curve-definition_tash',
                    data: master, type_map: type_map, selector: selector,
                    extras: {
                        name: master.name, currency: master.currency || (master.currency = 'USD'),
                        interpolator: master[INTR], interpolate: master[INTY] ? 'checked="checked"' : '',
                        left: master[LTXN], right: master[RTXN]
                    },
                    processor: function (data) {
                        data[INTY] = $(form_id + ' input[name=' + INTY + ']').is(':checked');
                        data.strip = data.strip.filter(function (v) {return v !== void 0;}); // remove undefineds
                    }
                }),
                form_id = '#' + form.id,
                new_conv_dropdown = function (row, idx, currency) {
                    return new ui.Dropdown({
                        form: form, classes: 'og-convention og-js-conv', value: row[CONV],
                        index: ['strip', idx, CONV].join('.'), placeholder: 'Please select...',
                        data_generator: function (handler) {
                            var rest_options = {page: '*', name: '*_' + currency, type: CURV, cache_for: 30 * 1000};
                            api.configs.get(rest_options).pipe(function (result) {
                                var options = result.data.data
                                    .map(function (val) {return val.split('|')[1].split('_').slice(0, -1).join('_');});
                                handler(options);
                            });
                        }
                    });
                },
                new_strip = function (row, idx) {
                    return new form.Block({
                        module: 'og.views.forms.yield-curve-definition-strip_tash',
                        extras: {idx: idx, tenor: row.tenor, future: row[NUMF]},
                        children: [new_conv_dropdown(row, idx, master.currency)]
                    }).on('form:load', function () {
                        $(form_id + ' [name="' + ['strip', idx, 'type'].join('.') + '"]').val(row.type);
                        if (row.type !== 'FUTURE') $(form_id + ' [name="' + ['strip', idx, NUMF].join('.') + '"]')
                            .attr('disabled', 'disabled');
                    }).on('change', form_id + ' [name="' + ['strip', idx, 'type'].join('.') + '"]', function (event) {
                        var $el = $(form_id + ' [name="' + ['strip', idx, NUMF].join('.') + '"]'),
                            is_future = $(event.target).val() === 'FUTURE';
                        if (is_future) $el.removeAttr('disabled'); else $el.attr('disabled', 'disabled');
                        if (!is_future) $el.attr('value', '');
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
            form.on('form:submit', save_resource).on('form:load', function () {
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
                $('.OG-layout-admin-details-center .ui-layout-header').html(header);
                $(form_id + ' [name=currency]').val(master.currency);
                setTimeout(load_handler.partial(form));
            }).on('change', form_id + ' [name=currency]', function (event) {
                var currency = $(event.target).val();
                master.currency = $(event.target).val(); // do now in case a new row is added, not just on submit
                $('.OG-layout-admin-details-center .og-js-currency').text(currency);
                $(form_id + ' .og-js-conv').each(function () {
                    var $el = $(this), idx = $el.attr('name').split('.').slice(1, -1),
                        row = master.strip[idx], value = row[CONV] || (row[CONV] = $el.val());
                    new_conv_dropdown(row, idx, currency).html(function (html) {$el.replaceWith($(html).val(value));});
                });
            }).on('keyup', form_id + ' [name=name]', function (event) {
                $('.OG-layout-admin-details-center .og-js-name').text($(event.target).val());
            }).on('click', form_id + ' .og-js-rem', function (event) { // remove a strip
                var $el = $(event.target).parents('.og-js-strip:first');
                master.strip[$el.find('input').attr('name').split('.').slice(1, -1)] = void 0;
                $el.remove();
            }).on('click', form_id + ' .og-js-add', function (event) { // add a strip
                var block = new_strip({}, master.strip.push({}) - 1);
                block.html(function (html) {$(form_id + ' .og-js-strips').append($(html)), block.load();});
            });
            form.children = [
                new form.Block({module: 'og.views.forms.currency_tash'}), // item_0
                new ui.Dropdown({ // item_1
                    form: form, value: master.region.split(sep)[1], placeholder: 'Please select...',
                    processor: function (selector, data, errors) {
                        data.region = master.region.split(sep)[0] + sep + $(selector).val();
                    },
                    data_generator: function (handler) {
                        api.regions.get({page: '*'}).pipe(function (result) {
                            handler(result.data.data.map(function (region) {
                                var split = region.split('|');
                                return !split[3] ? null : {value: split[3], text: split[3] + ' - ' + split[1]}
                            }).filter(Boolean).sort(function (a, b) { // alphabetize
                                return a.text < b.text ? -1 : a === b ? 0 : 1;
                            }));
                        });
                    }
                }),
                strips = new form // item_2
                    .Block({template: '<ul class="og-awesome-list og-js-strips">{{{children}}}</ul>'})
            ];
            if ((master.strip = arr(master.strip)).length)
                Array.prototype.push.apply(strips.children, master.strip.map(new_strip));
            form.dom();
        };
        constructor.type_map = type_map;
        return constructor;
    }
});