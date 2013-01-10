/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fxbarrieroptionsecurity',
    dependencies: [],
    obj: function (config) {   
        return function () {
            config = og.blotter.util.FAKE_FX_BARRIER;
            var constructor = this, ui = og.common.util.ui, data = config || {}, attr, form;
            constructor.load = function () {
                constructor.title = 'FX Barrier Option';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: {},
                    processor: function (data) {    

                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.long_short_tash'
                    }), 
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_value_tash',
                        extras: {call: data.callAmount, put: data.putAmount, strike: data.strike},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'putCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'callCurrency'}})
                        ]
                    }),                    
                    new form.Block({
                        module: 'og.blotter.forms.blocks.barrier_tash',
                        extras: {date: data.settlementDate, level: data.barrierLevel, expiry: data.expiry},
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.barrierdirections', index: 'barrierDirection',
                                value: data.barrierDirection, placeholder: 'Select Direction'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.barriertypes', index: 'barrierType',
                                value: data.barrierType, placeholder: 'Select Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.monitoringtype', index: 'monitoringType',
                                value: data.monitoringType, placeholder: 'Select Monitoring Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.samplingfrequencies', index: 'samplingFrequency',
                                value: data.samplingFrequency, placeholder: 'Select Sampling Frequency'
                            })
                        ]
                    }),
                    attr = new og.common.util.ui.Attributes({
                        form: form, attributes: data.attributes, index : "security.attributes"
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.set_select("putCurrency", data.putCurrency);
                    og.blotter.util.set_select("callCurrency", data.callCurrency);
                    og.blotter.util.check_radio("longShort", data.longShort);
                });
                form.on('form:submit', function (result){
                });
            }; 
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});