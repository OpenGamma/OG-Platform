/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Fx_forward',
    dependencies: [],
    obj: function () {   
        return function (config) {
            config = og.blotter.util.FAKE_FX_FORWARD;
            var constructor = this, ui = og.common.util.ui, data = config || {};
            constructor.load = function () {
                constructor.title = 'FX Forward';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_forward_tash',
                        extras: {pay: data.payAmount, recieve: data.recieveAmount},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'payCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'recieveCurrency'}})
                        ]
                    }),                    
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("forwardDate");

                    if(data.length) return;
                    og.blotter.util.set_select("recieveCurrency", data.recieveCurrency);
                    og.blotter.util.set_select("payCurrency", data.payCurrency);
                    og.blotter.util.set_datetime("forwardDate", data.forwardDate);

                });
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});