/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Equity_variance_swap',
    dependencies: [],
    obj: function () {   
        return function (config) {
            config = og.blotter.util.FAKE_VARIANCE_SWAP;
            var constructor = this, ui = og.common.util.ui, data = config || {};
            console.log(data);
            constructor.load = function () {
                constructor.title = 'Equity Varience Swap';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.equity_variance_swap_tash',
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.equity_variance_swap_tash',
                        extras: {notional: data.notional, strike: data.strike, region: data.regionId,
                            underlying: data.spotUnderlyingId, settlement: data.settlementDate, 
                            first: data.firstObservationDate, last: data.lastObservationDate,
                            annualization: data.annualizationFactor
                         },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'observationFrequency',
                                value: data.observationFrequency, placeholder: 'Frequency'
                            })                               
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.set_select("currency", data.currency);
                    og.blotter.util.check_checkbox("parameterizedAsVariance", data.parameterizedAsVariance);
                });
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});