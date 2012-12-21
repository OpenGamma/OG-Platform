/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Forward_rate_agreement',
    dependencies: [],
    obj: function (config) {   
        return function () {
            config = og.blotter.util.FAKE_FRA;
            var constructor = this, data = config || {};
            constructor.load = function () {
                constructor.title = 'Forward Rate Agreement';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.forward_rate_agreement_tash',
                        extras: {start: data.startDate, end: data.endDate, fixing:data.fixingDate, 
                            underlyingId: data.underlyingId, region: data.regionId, amount: data.amount, rate: data.rate
                        },
                        children: [new form.Block({module:'og.views.forms.currency_tash'})]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.set_select("currency", data.currency);
                });
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});