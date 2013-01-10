/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.nondeliverablefxoptionsecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            config = og.blotter.util.FAKE_FX_OPTION;
            var constructor = this, ui = og.common.util.ui, data = config || {};
            constructor.load = function () {
                constructor.title = 'Non-Deliverable FX Option';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.long_short_tash'
                    }), 
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_value_tash',
                        extras: {put: data.putAmount, call: data.callAmount},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'putCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'callCurrency'}})
                        ]
                    }),                
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_date_tash',
                        extras: {nondev:true, expiry: data.expiry, settlement: data.settlementDate},
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.exercisetypes', index: 'exerciseType',
                                value: data.exerciseType, placeholder: 'Select Exercise Type'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.set_select("putCurrency", data.putCurrency);
                    og.blotter.util.set_select("callCurrency", data.callCurrency);
                    og.blotter.util.check_radio("longShort", data.longShort);
                    og.blotter.util.check_checkbox("deliveryInCallCurrency", data.deliveryInCallCurrency);
                });  
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});