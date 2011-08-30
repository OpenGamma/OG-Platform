/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs.curvespecificationbuilderconfiguration',
    dependencies: [
        'og.api.text',
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
                master = config.data.template_data.configJSON, strip, new_strip_item,
                transposed = {}, has = 'hasOwnProperty', format,
                field_names = ['BAS', 'CAS', 'FRA', 'FUT', 'OIS', 'RAT', 'SWA', 'TSW'],
                fields = {
                    BAS: 'basisSwapInstrumentProviders',
                    CAS: 'cashInstrumentProviders',
                    FRA: 'fraInstrumentProviders',
                    FUT: 'futureInstrumentProviders',
                    OIS: 'oisSwapInstrumentProviders',
                    RAT: 'rateInstrumentProviders',
                    SWA: 'swapInstrumentProviders',
                    TSW: 'tenorSwapInstrumentProviders'
                },
                MKTS = 'marketSector',
                CURR = 'ccy',
                SCHM = 'scheme',
                STTY = 'stripType',
                INST = 'instrument',
                PRFX = 'prefix',
                popup_module = 'og.views.forms.curve-specification-builder-popup',
                form = new ui.Form({
                    module: 'og.views.forms.curve-specification-builder',
                    data: field_names.reduce(function (acc, val) {
                        return acc[fields[val]] = [], acc;
                    }, {tenors: master.tenors, 0: master[0]}),
                    selector: selector,
                    extras: {name: orig_name}
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
            og.api.text({module: popup_module, handler: $.noop}); // cache popup module so there is no initial delay
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
                }},
                {type: 'keydown', selector: form_id + ' .og-js-popup input', handler: function (e) {
                    if (e.keyCode !== $.ui.keyCode.ESCAPE) return;
                    $(form_id + ' .og-js-popup').remove();
                    $(form_id + ' .og-js-cell').removeClass('og-active');
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
                {type: 'click', selector: form_id + ' .og-js-popup button', handler: function (e) {
                    var data_type = null;
                    if (data_type) ({
                        'static': function () {},
                        'future': function () {},
                        'synthetic': function () {}
                    })[data_type]();
                    $(form_id + ' .og-js-popup').remove();
                    $(form_id + ' .og-js-cell').removeClass('og-active');
                }},
                {type: 'click', selector: form_id + ' .og-js-cell', handler: function (e) {
                    var $cell = $(e.target).is('.og-js-cell') ? $(e.target) : $(e.target).parent('.og-js-cell:first'),
                        $row = $cell.parents('.og-js-row:first'),
                        $holder = $(form_id + ' .og-js-holder'),
                        cell_offset = $cell.offset(), holder_offset = $holder.offset(),
                        data = JSON.parse($cell.find('input[type=hidden]').val() || '{}'),
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
                                console.log('data', data);
                                var $popup = $(form_id + ' .og-js-popup'),
                                    last_offset = $row.find('.og-js-strip .og-js-cell').index($cell) === 7 ?
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
                                        $popup.find('.og-js-identifier').val(scheme_id[1]).focus();
                                    },
                                    'future': function () {
                                        $popup.find('.og-js-prefix').val(data[PRFX]);
                                        $popup.find('.og-js-market-sector').val(data[MKTS]).focus();
                                    },
                                    'synthetic': function () {
                                        $popup.find('.og-js-currency').val(data[CURR]).focus();
                                    }
                                })[data_type]();
                            }}
                        ]
                    });
                    block.html(function (html) {$holder.append($(html)), block.load();});
                }}
            ]);
            format = (function (dummy) {
                return function (val) {
                    var json = '', formatted = val[PRFX] ? val[PRFX] + ' * ' + val[MKTS]
                        : val[CURR] ? val[CURR] + '/' + val[SCHM] + '/' + val[STTY]
                            : val[INST];
                    json = $.outer(dummy.attr({value: JSON.stringify(val)})[0]);
                    return '<div class="og-value og-js-cell">' + json + '<span>' + formatted +
                        '</span><span></span></div>';
                }
            })($('<input />').prop({type: 'hidden'}));
            new_strip_item = function (val, idx) {
                var key, extras = {tenor: val.tenor};
                delete val.tenor;
                for (key in val) if (!val[has](key)) continue; else extras[key] = val[key] ? format(val[key])
                    : '<div class="og-configure og-js-cell"><span>configure</span></div>';
                return new form.Block({
                    module: 'og.views.forms.curve-specification-builder-strip',
                    extras: extras
                });
            };
            transposed = field_names.reduce(function (acc, val) {
                acc[val] = (master[fields[val]] || []).reduce(function (acc, val) {
                    for (var tenor in val) if (val[has](tenor)) acc[tenor] = val[tenor];
                    return acc;
                }, {});
                // fill out sparse array, populating nulls
                master.tenors.forEach(function (tenor) {if (!acc[val][has](tenor)) acc[val][tenor] = null;});
                return acc;
            }, {});
            form.children = [
                (strip = new form.Block({
                    wrap: '<ul class="og-awesome-list og-js-strip">{{html html}}</ul>',
                    children: master.tenors.map(function (tenor) {
                        return field_names.reduce(function (acc, val) {
                            return (acc[val] = transposed[val][tenor] || null), acc;
                        }, {tenor: tenor});
                    }).map(new_strip_item)
                }))
            ];
            form.dom();
        };
    }
});