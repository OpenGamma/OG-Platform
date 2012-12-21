/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Cap_floor',
    dependencies: [],
    obj: function () {   
        return function (config) {
            config = og.blotter.util.FAKE_CAP_FLOOR;
            var constructor = this, ui = og.common.util.ui, data = config || {};
            constructor.load = function () {
                constructor.title = 'Cap/Floor';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.cap_floor_tash',
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.cap_floor_tash',
                        extras: {start: data.startDate, maturity: data.maturityDate, notional: data.notional,
                            strike: data.strike, longId: data.longId, shortId: data.shortId, 
                            underlyingId: data.underlyingId
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'frequency',
                                value: data.frequency, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: 'dayCount',
                                value: data.dayCount, placeholder: 'Select Day Count'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.set_select("currency", data.currency);
                    og.blotter.util.check_radio("cap", data.cap);
                    og.blotter.util.check_radio("payer", data.payer);
                }); 
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});