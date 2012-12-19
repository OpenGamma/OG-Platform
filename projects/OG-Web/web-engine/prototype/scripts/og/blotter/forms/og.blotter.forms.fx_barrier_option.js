/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Fx_barrier_option',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this, ui = og.common.util.ui;
            constructor.load = function () {
                constructor.title = 'FX Barrier Option';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.long_short_tash',
                        extras: {}
                    }), 
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_value_tash',
                        extras: {},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'putCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'callCurrency'}})
                        ]
                    }),                    
                    new form.Block({
                        module: 'og.blotter.forms.blocks.barrier_tash',
                        extras: {},
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.monitoringtype', index: 'monitoringType',
                                value: '', rest_options: {}, placeholder: 'Select Monitoring Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.samplingfrequencies', index: 'samplingFrequency',
                                value: '', rest_options: {}, placeholder: 'Select Sampling Frequency'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form})
                );
                form.dom();
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});