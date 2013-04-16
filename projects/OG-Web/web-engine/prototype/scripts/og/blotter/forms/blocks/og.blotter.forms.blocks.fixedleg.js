/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Fixedleg',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Fixedleg = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form, leg = config.leg,
                ui = og.common.util.ui, leg_path = (leg.slice(0,leg.length-1)).split('.'),
                data = leg_path.reduce(function (acc, val) {return acc[val];},config.data) || {notional:{}};  
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                extras: {rate: data.rate, notional: data.notional.amount, index: config.index, leg: leg},
                children : [
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: leg + "notional.currency"}
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.regions', index:  leg + 'regionId',
                        value: data.regionId, placeholder: 'Select Region ID'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.daycountconventions', 
                        index: leg + 'dayCount',
                        value: data.dayCount, placeholder: 'Select Day Count'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.businessdayconventions', 
                        index:  leg + 'businessDayConvention',
                        value: data.businessDayConvention, 
                        placeholder: 'Select Business Day Convention'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', 
                        index: leg + 'frequency',
                        value: data.frequency, placeholder: 'Select Frequency'
                    })
                ],
                processor: function (data) {
                    leg_path.reduce(function (acc, val) {return acc[val];},data)['eom'] = 
                        og.blotter.util.get_checkbox(leg + 'eom');
                }
            });
        };
        Fixedleg.prototype = new Block(); // inherit Block prototype
        return Fixedleg;
    }
});