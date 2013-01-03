/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Swaption',
    dependencies: [],
    obj: function () {   
        return function (config) {
            config = og.blotter.util.FAKE_SWAPTION;
            config.swap = og.blotter.util.FAKE_SWAP;
            config.swap.floating = og.blotter.util.FAKE_FLOATING;
            config.swap.fixed = og.blotter.util.FAKE_FIXED;
            console.log(config);
            var constructor = this, ui = og.common.util.ui, data = config || {}, 
            floating = "swapsecurity.floatingspreadirleg.";
            fixed = "swapsecurity.fixedinterestrateleg.";
            constructor.load = function () {
                constructor.title = 'Swaption';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.swaption_tash',
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_quick_entry_tash',
                        extras: {}
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_tash',
                        extras: {trade: data.swap.tradeDate, maturity: data.swap.maturityDate, 
                            effective: data.swap.effectiveDate}
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                        extras: {rate: data.swap.fixed.rate, notional: data.swap.fixed.notional},
                        children : [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', 
                                index: fixed + 'dayCount',
                                value: data.swap.fixed.dayCount, placeholder: 'Select Day Count'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', 
                                index: fixed + 'frequency',
                                value: data.swap.fixed.frequency, placeholder: 'Select Frequency'
                            })
                        ]
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_floating_tash',
                        extras: {type: floating, initial: data.swap.floating.initialFloatingRate, 
                            settlement: data.swap.floating.settlementDays, spread: data.swap.floating.spread, 
                            gearing: data.swap.floating.gearing, notional: data.swap.floating.notional},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: floating + 'dayCount',
                                value: data.swap.floating.dayCount, placeholder: 'Select Day Count'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: floating + 'frequency',
                                value: data.swap.floating.frequency, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.businessdayconventions', 
                                index: floating + 'businessDayConvention',
                                value: data.swap.floating.businessDayConvention, 
                                placeholder: 'Select Business Day Convention'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.floatingratetypes', 
                                index: floating + 'floatingRateTypes',
                                value: data.swap.floating.floatingRateType, placeholder: 'Select Floating Rate Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: floating + 'offsetFixing',
                                value: data.swap.floating.offsetFixing, placeholder: 'Select Offset Fixing'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})  
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.check_checkbox(floating + 'eom', data.swap.floating.eom);
                    og.blotter.util.check_checkbox(fixed + 'eom', data.swap.fixed.eom);
                }); 
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});