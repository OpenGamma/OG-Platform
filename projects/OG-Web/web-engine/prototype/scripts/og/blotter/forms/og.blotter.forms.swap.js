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
            var constructor = this, ui = og.common.util.ui, data = config || {};
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
                        extras: {}
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                        extras: {}
                    }) ,
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_floating_tash',
                        extras: {initial: data.initialFloatingRate, settlement: data.settlementDays, 
                            spread: data.spread, gearing: data.gearing},
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: 'dayCount',
                                value: data.dayCount, placeholder: 'Select Day Count'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'frequency',
                                value: data.frequency, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.businessdayconventions', index: 'businessDayConvention',
                                value: data.businessDayConvention, placeholder: 'Select Business Day Convention'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.floatingratetypes', index: 'floatingRateTypes',
                                value: data.floatingRateType, placeholder: 'Select Floating Rate Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'offsetFixing',
                                value: data.offsetFixing, placeholder: 'Select Offset Fixing'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    if(data.length) return;
                    og.blotter.util.check_checkbox("eom", data.eom);
                }); 
            }; 
            constructor.kill = function () {
            };
            constructor.load();
        };
    }
});