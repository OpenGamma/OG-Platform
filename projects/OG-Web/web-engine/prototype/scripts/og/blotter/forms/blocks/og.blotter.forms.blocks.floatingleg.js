/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Floatingleg',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Floatingleg = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form, data = config.data,
                leg = config.leg, ui = og.common.util.ui;
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.swap_details_floating_tash',
                extras: {leg: leg, initial: data.initialFloatingRate, 
                    settlement: data.settlementDays, spread: data.spread, 
                    gearing: data.gearing, notional: data.notional, index: config.index},
                children: [
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: leg + "currency"}
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.daycountconventions', index: leg + 'dayCount',
                        value: data.dayCount, placeholder: 'Select Day Count'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', index:  leg + 'frequency',
                        value: data.frequency, placeholder: 'Select Frequency'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.businessdayconventions', 
                        index:  leg + 'businessDayConvention',
                        value: data.businessDayConvention, 
                        placeholder: 'Select Business Day Convention'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.floatingratetypes', 
                        index:  leg + 'floatingRateTypes',
                        value: data.floatingRateType, placeholder: 'Select Floating Rate Type'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', index:  leg + 'offsetFixing',
                        value: data.offsetFixing, placeholder: 'Select Offset Fixing'
                    })
                ],
                processor: function (data) {
                    var path = leg.split('.');
                    path.pop();
                    path.reduce(function (acc, val) {return acc[val];},data)['eom'] = 
                        og.blotter.util.get_checkbox(leg + 'eom');
                }
            });
        };
        Floatingleg.prototype = new Block(); // inherit Block prototype
        return Floatingleg;
    }
});