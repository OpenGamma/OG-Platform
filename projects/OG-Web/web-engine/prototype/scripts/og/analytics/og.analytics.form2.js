/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [
        'og.common.util.ui.Form',
        'og.common.util.ui.AutoCombo',
        'og.views.common.layout'
    ],
    obj: function () { 
        return function (config) {
            var Form = og.common.util.ui.Form, ds_response = { // dummy 
                    type: ['Live', 'Snapsot', 'Historical', 'Data Type'],
                    live: ['Bloomberg', 'Reuters'],
                    snapshot: ['Alan', 'Alan 2'],
                    historical: ['01 June 2012', '02 June 2012', '03 June 2012'],
                    datatype: ['type 1', 'type 2'],
                    row: [
                        {num: 1, type: 'Live', value: 'Bloomberg'},
                        {num: 2, type: 'Snapshot', value: 'Alan'},
                        {num: 3, type: 'Historical', value: '02 June 2012'},
                        {num: 4, type: 'Data Type', value: 'type 2', last: true}
                    ]
                },
                load_handler = config.handler || $.noop, selector = config.selector, type_map = config.type_map,
                loading = config.loading || $.noop, id_count = 0, prefix = 'analytics_form_',
                master = config.data.template_data.configJSON.data, config_type = config.type,
                form = new Form({
                    module: 'og.analytics.form2',
                    data: master,
                    type_map: type_map,
                    selector: selector,
                    extras: {name: master.name},
                    processor: function () {}
                }),
                form_id = '#' + form.id,
                construct_form = function (template, view, aggregators) {},
                handle_error = function (err) {};
            form.attach([
                {type: 'form:load', handler: function () { setTimeout(load_handler.partial(form)); }},
                {type: 'form:submit', handler: function () {}},
                {type: 'click', selector: 'tbc', handler:function () {}}
            ]);
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.rest.viewdefinitions.get(),
                og.api.rest.aggregators.get()
            ).then(construct_form);
        };
    }
});