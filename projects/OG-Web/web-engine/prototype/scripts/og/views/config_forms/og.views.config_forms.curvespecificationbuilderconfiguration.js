/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.curvespecificationbuilderconfiguration',
    dependencies: [
        'og.api.text',
        'og.api.rest',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, ui = og.common.util.ui, Form = ui.Form, api = og.api.rest,
            MKTS = 'marketSector',
            CURR = 'ccy',
            SCHM = 'scheme',
            STTY = 'stripType',
            INST = 'instrument',
            PRFX = 'prefix',
            INDX = '<INDEX>',
            INSP = /InstrumentProviders$/,
            CURV = 'curveSpecificationBuilderConfiguration',
            field_names = [],
            data_types = {
                'future': 'com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider',
                'static': 'com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider',
                'synthetic': 'com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider'
            },
            type_map = [
                ['0',                           Form.type.STR],
                [['0', INDX].join('.'),         Form.type.STR],
                [['*', '*', '0'].join('.'),     Form.type.STR],
                [['*', '*', CURR].join('.'),    Form.type.STR],
                [['*', '*', INST].join('.'),    Form.type.STR],
                [['*', '*', MKTS].join('.'),    Form.type.STR],
                [['*', '*', PRFX].join('.'),    Form.type.STR],
                [['*', '*', SCHM].join('.'),    Form.type.STR],
                [['*', '*', STTY].join('.'),    Form.type.STR],
                [['*', '*', 'type'].join('.'),  Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {}),
            arr = function (obj) {return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [];},
            form_builder, constructor;
        form_builder = function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name, config_type = config.type,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                master = config.data.template_data.configJSON.data || {}, new_strip_item,
                tenors = config.data.template_data.configJSON.tenors || [],
                transposed = {}, has = 'hasOwnProperty', format, new_name = '',
                popup_module = 'og.views.forms.curve-specification-builder-popup', update_cell,
                form = new Form({
                    module: 'og.views.forms.curve-specification-builder',
                    data: {0: master[0] || config_type},
                    selector: selector,
                    extras: {name: orig_name},
                    type_map: type_map,
                    processor: function (data) {
                        (new_name = data.name), delete data.name;
                        $(form_id + ' .og-js-row').each(function (idx, row) {
                            var $row = $(row), tenor = $(row).find('.og-js-tenor').val();
                            if (!tenor) return true; // i.e. continue;
                            $row.find('input[type=hidden]').each(function (idx, val) {
                                var value = JSON.parse($(val).val());
                                if (!value) return true; // i.e. continue;
                                value[0] = data_types[value.type.toLowerCase()];
                                (data[field_names[idx]] || (data[field_names[idx]] = {}))[tenor] = value;
                            });
                        });
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
            og.api.text({module: popup_module, handler: $.noop}); // cache popup module so there is no initial delay
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-tools"></div>\
                          <h1 class="og-js-name">' + orig_name + '</h1>\
                          (Curve Specification Builder Configuration)\
                        </header>',
                        section_width = $(form_id + ' .og-js-cell:first').outerWidth() * (field_names.length + 1);
                    section_width += 40; // padding + awesome list delete icon
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    $(form_id + ' .og-js-section').width(section_width);
                    load_handler(form);
                }},
                {type: 'form:submit', handler: save_resource},
                {type: 'keydown', selector: form_id + ' .og-js-popup input', handler: function (e) {
                    if ((e.keyCode !== $.ui.keyCode.ESCAPE) &&
                        (e.keyCode !== $.ui.keyCode.ENTER) &&
                        (e.keyCode !== $.ui.keyCode.NUMPAD_ENTER)) return;
                    if ((e.keyCode === $.ui.keyCode.ESCAPE)) {
                        $(form_id + ' .og-js-popup').remove();
                        $(form_id + ' .og-js-cell').removeClass('og-active');
                        return;
                    }
                    update_cell();
                }},
                {type: 'keyup', selector: form_id + ' input[name=name]', handler: function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                }},
                {type: 'click', selector: form_id + ' .og-js-rem', handler: function (e) {
                    $(e.target).parent('li').remove();
                }},
                {type: 'click', selector: form_id + ' .og-js-popup .og-js-cancel', handler: function (e) {
                    $(form_id + ' .og-js-popup').remove();
                    $(form_id + ' .og-js-cell').removeClass('og-active');
                }},
                {type: 'click', selector: form_id + ' .og-js-popup .og-js-clear', handler: function (e) {
                    var $cell = $(form_id + ' .og-js-cell.og-active');
                    $(form_id + ' .og-js-popup').remove();
                    $(form_id + ' .og-js-cell').removeClass('og-active');
                    $cell.replaceWith(format(null));
                }},
                {type: 'click', selector: form_id + ' .og-js-popup .og-js-update', handler: update_cell = function (e) {
                    var $cell = $(form_id + ' .og-js-cell.og-active'),
                        $row = $cell.parents('.og-js-row:first'),
                        $popup = $(form_id + ' .og-js-popup'),
                        data_type = $popup.find('.og-js-type').val().toLowerCase();
                    $(form_id + ' .og-js-cell').removeClass('og-active');
                    if (data_type) ({
                        'static': function () {
                            var result = {type: 'Static'};
                            result[INST] = $popup.find('.og-js-scheme').val() + '~' +
                                $popup.find('.og-js-identifier').val();
                            $cell.replaceWith(format(result));
                        },
                        'future': function () {
                            var result = {type: 'Future'};
                            result[PRFX] = $popup.find('.og-js-prefix').val();
                            result[MKTS] = $popup.find('.og-js-market-sector').val();
                            $cell.replaceWith(format(result));
                        },
                        'synthetic': function () {
                            var cell_index = $row.find('.og-js-cell').index($cell),
                                result = {type: 'Synthetic', scheme: 'OG_SYNTHETIC_TICKER'};
                            result[CURR] = $popup.find('.og-js-currency').val();
                            result[STTY] = field_names[cell_index].replace(INSP, '') // unCamel + underscore + caps
                                .replace(/([\d]+[A-Z]|[\d]+|[A-Z])/g, '_$1').toUpperCase();
                            $cell.replaceWith(format(result));
                        }
                    })[data_type]();
                    $(form_id + ' .og-js-popup').remove();
                }},
                {type: 'click', selector: form_id + ' .og-js-cell', handler: function (e) {
                    var $cell = $(e.target).is('.og-js-cell') ? $(e.target) : $(e.target).parent('.og-js-cell:first'),
                        $row = $cell.parents('.og-js-row:first'), $holder = $(form_id + ' .og-js-holder'),
                        cell_offset = $cell.offset(), holder_offset = $holder.offset(),
                        data = JSON.parse($cell.find('input[type=hidden]').val()) || {},
                        block;
                    $(form_id + ' .og-js-popup').remove();
                    if ($cell.is('.og-active')) return $cell.removeClass('og-active');
                    $(form_id + ' .og-js-cell').removeClass('og-active');
                    $cell.addClass('og-active');
                    block = new form.Block({
                        module: popup_module,
                        handlers: [
                            {type: 'change', selector: form_id + ' .og-js-type', handler: function (e) {
                                var $popup = $(form_id + ' .og-js-popup'), data_type = $(e.target).val().toLowerCase();
                                ['static', 'future', 'synthetic'].forEach(function (type) {
                                    $popup.find('.og-js-' + type)[data_type === type ? 'show' : 'hide']();
                                });
                            }},
                            {type: 'form:load', handler: function () {
                                var $popup = $(form_id + ' .og-js-popup'),
                                    last_offset = $row.find('.og-js-cell').index($cell) === 7 ?
                                        $popup.width() - $cell.width() + 3 : 0,
                                    top = Math.round(cell_offset.top - holder_offset.top) + $row.height(),
                                    left = Math.round(cell_offset.left - holder_offset.left - last_offset),
                                    data_type = data.type ? data.type.toLowerCase() : '';
                                $popup.css({top: top + 'px', left: left + 'px'});
                                $popup.find('.og-js-type').val(data.type);
                                ['static', 'future', 'synthetic'].forEach(function (type) {
                                    $popup.find('.og-js-' + type)[data_type === type ? 'show' : 'hide']();
                                });
                                if (data_type) ({
                                    'static': function () {
                                        if (!data.instrument) return;
                                        var scheme_id = data[INST].split('~');
                                        $popup.find('.og-js-scheme').val(scheme_id[0]);
                                        $popup.find('.og-js-identifier').val(scheme_id[1]).focus().select();
                                    },
                                    'future': function () {
                                        $popup.find('.og-js-prefix').val(data[PRFX]);
                                        $popup.find('.og-js-market-sector').val(data[MKTS]).focus().select();
                                    },
                                    'synthetic': function () {
                                        $popup.find('.og-js-currency').val(data[CURR]).focus().select();
                                    }
                                })[data_type]();
                            }}
                        ]
                    });
                    block.html(function (html) {$holder.append($(html)), block.load();});
                }},
                {type: 'click', selector: form_id + ' .og-js-add', handler: function (e) {
                    var strip_item = new_strip_item(field_names.reduce(function (acc, val) {
                        return (acc[val] = null), acc;
                    }, {tenor: ''}));
                    strip_item.html(function (html) {
                        $(form_id + ' .og-js-strip').append($(html)), strip_item.load();
                    });
                }}
            ]);
            format = (function (dummy) {
                return function (val) {
                    var json, formatted, title;
                    json = $.outer(dummy.attr({value: JSON.stringify(val)})[0]);
                    if (!val) return '<div class="og-configure og-js-cell">' + json + '<span>configure</span></div>';
                    title = val[PRFX] ? val[PRFX] + ' * ' + val[MKTS] : val[CURR] ? 'Synthetic~' + val[CURR]
                        : val[INST];
                    formatted = val[INST] ? title.split('~')[1] : title;
                    return '<div class="og-value og-js-cell" title="' + title + '">' + json + '<span>' + formatted +
                        '</span><span></span></div>';
                }
            })($('<input type="hidden" />'));
            new_strip_item = function (val, idx) {
                return new form.Block({
                    wrap: '\
                        <li class="og-js-row">\
                          <div class="og-del og-js-rem"></div>\
                          <div class="og-strip">\
                            <div class="og-input">\
                                <input value="' + val.tenor + '" class="og-js-tenor" type="text" />\
                            </div>\
                            {{html html}}\
                          </div>\
                        </li>',
                    children: field_names.map(function (field) {return format(val[field]);})
                });
            };
            transposed = field_names.reduce(function (acc, val) {
                acc[val] = arr(master[val]).reduce(function (acc, val) {
                    for (var tenor in val) if (val[has](tenor)) acc[tenor] = val[tenor];
                    return acc;
                }, {});
                // fill out sparse array, populating nulls
                tenors.forEach(function (tenor) {if (!acc[val][has](tenor)) acc[val][tenor] = null;});
                return acc;
            }, {});
            form.children = [
                new form.Block({ // headers
                    wrap: '<div class="og-a-list-header og-strip"><div>Tenor</div>{{html html}}</div>',
                    children: field_names.map(function (val) {return '<div>' + val.replace(INSP, '') + '</div>';})
                }),
                new form.Block({ // content
                    wrap: '<ul class="og-awesome-list og-js-strip">{{html html}}</ul>',
                    children: tenors.map(function (tenor) {
                        return field_names.reduce(function (acc, val) {
                            return (acc[val] = transposed[val][tenor] || null), acc;
                        }, {tenor: tenor});
                    }).map(new_strip_item)
                })
            ];
            form.dom();
        };
        constructor = function (config) {
            api.configs.get({meta: true, handler: function (result) {
                if (result.error) return ui.dialog({type: 'error', message: result.message});
                field_names = result.data[CURV];
                form_builder(config);
            }});
        };
        constructor.type_map = type_map;
        return constructor;
    }
});