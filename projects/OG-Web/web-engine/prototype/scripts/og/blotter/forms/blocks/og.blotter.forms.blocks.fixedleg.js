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
                ui = og.common.util.ui, leg_path = (leg.slice(0,leg.length-1)).split('.'), regions_block,
                data = leg_path.reduce(function (acc, val) {return acc[val];},config.data) || {notional:{}};  
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                extras: {rate: data.rate, notional: data.notional.amount, index: config.index, leg: leg},
                children : [
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: leg + "notional.currency"}
                    }),
                    regions_block = new og.blotter.forms.blocks.Regions({name: leg + 'regionId', 
                        value: data.regionId, form: form}),
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
                },
                generator: function (handler, tmpl, data) {
                    handler(tmpl(data));
                    //dom does not exist for form load so need to called again after
                    regions_block.create_autocomplete(); 
                }
            });
        };
        Fixedleg.prototype = new Block(); // inherit Block prototype
        return Fixedleg;
    }
});