/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Swap',
    dependencies: [],
    obj: function () {   
        return function (config) {
            config = og.blotter.util.FAKE_SWAP;
            config.floating = og.blotter.util.FAKE_FLOATING;
            config.fixed = og.blotter.util.FAKE_FIXED;
            var constructor = this, ui = og.common.util.ui, data = config || {}, 
            floating = "floatingspreadirleg.";
            fixed = "fixedinterestrateleg.";
            constructor.load = function () {
                constructor.title = 'Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.swap_tash',
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
                        extras: {trade: data.tradeDate, maturity: data.maturityDate, effective: data.effectiveDate}
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                        extras: {rate: data.fixed.rate, notional: data.fixed.notional},
                        children : [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', 
                                index: fixed + 'dayCount',
                                value: data.fixed.dayCount, placeholder: 'Select Day Count'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', 
                                index: fixed + 'frequency',
                                value: data.fixed.frequency, placeholder: 'Select Frequency'
                            })
                        ]
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_floating_tash',
                        extras: {type: floating, initial: data.floating.initialFloatingRate, 
                            settlement: data.floating.settlementDays, spread: data.floating.spread, 
                            gearing: data.floating.gearing, notional: data.floating.notional},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: floating + 'dayCount',
                                value: data.floating.dayCount, placeholder: 'Select Day Count'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: floating + 'frequency',
                                value: data.floating.frequency, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.businessdayconventions', 
                                index: floating + 'businessDayConvention',
                                value: data.floating.businessDayConvention, 
                                placeholder: 'Select Business Day Convention'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.floatingratetypes', 
                                index: floating + 'floatingRateTypes',
                                value: data.floating.floatingRateType, placeholder: 'Select Floating Rate Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: floating + 'offsetFixing',
                                value: data.floating.offsetFixing, placeholder: 'Select Offset Fixing'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.check_checkbox(floating + 'eom', data.floating.eom);
                    og.blotter.util.check_checkbox(fixed + 'eom', data.fixed.eom);
                }); 
            }; 
            constructor.kill = function () {
            };
            constructor.load();
        };
    }
});