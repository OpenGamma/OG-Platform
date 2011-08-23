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
                {type: 'keyup', selector: form_id + ' input[name=name]', handler: function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                }},
                {type: 'click', selector: form_id + ' .og-js-rem', handler: function (e) {
                    $(e.target).parent('li').remove();
                }}
            ]);
            format = function (val) {
                var json = '', formatted = val[PRFX] ? val[PRFX] + ' * ' + val[MKTS]
                    : val[CURR] ? val[CURR] + '/' + val[SCHM] + '/' + val[STTY]
                        : val[INST];
                json = $.outer($('input').prop({type: 'hidden'}).attr({value: JSON.stringify(val)})[0]);
                return '<div class="og-value og-js-cell">' + json + '<span>' + formatted + '</span><span></span></div>';
            };
            new_strip_item = function (val, idx) {
                var key, extras = {tenor: val.tenor};
                delete val.tenor;
                for (key in val) if (!val[has](key)) continue; else extras[key] = val[key] ? format(val[key])
                    : '<div class="og-configure"><span>configure</span></div>';
                return new form.Block({
                    module: 'og.views.forms.curve-specification-builder-strip',
                    extras: extras
                });
            };
            transposed = field_names.reduce(function (acc, val) {
                acc[val] = master[fields[val]].reduce(function (acc, val) {
                    for (var tenor in val) if (val[has](tenor)) acc[tenor] = val[tenor];
                    return acc;
                }, {});
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