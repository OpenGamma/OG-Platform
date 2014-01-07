/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Inflationindexleg',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Inflationindexleg = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form,leg = config.leg,
                ui = og.common.util.ui, type = config.type, security_block,
                leg_path = (leg.slice(0,leg.length-1)).split('.'),
                data = leg_path.reduce(function (acc, val) {return acc[val];},config.data) || {notional:{}};
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.swap_details_inflation_index_tash',
                extras: {leg: leg, notional: data.notional.amount, index: config.index, 
                    quotationIndexationLag: data.quotationIndexationLag, conventionalIndexationLag: data.conventionalIndexationLag
                },
                children: [
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: leg + "notional.currency"}
                    }),
                    regions_block = new og.blotter.forms.blocks.Regions({name: leg + 'regionId', 
                        value: data.regionId, form: form}),
                    security_block = new og.blotter.forms.blocks.Security({
                        form: form, label: "Inflation Index ID", security: data.indexId,
                        index: leg + "indexId"
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
                        form: form, resource: 'blotter.interpolationmethods', index:  leg + 'interpolationMethod',
                        value: data.interpolationMethod, placeholder: 'Select Interpolation Method'
                    })
                ],
                processor: function (data) {
                    leg_path.reduce(function (acc, val) {return acc[val];},data)['eom'] = 
                        og.blotter.util.get_checkbox(leg + 'eom');
                },
                generator: function (handler, tmpl, data) {
                    handler(tmpl(data));
                    //dom does not exist for form load so need to called again after
                    security_block.create_autocomplete(); 
                    regions_block.create_autocomplete();
                }
            });
        };
        Inflationindexleg.prototype = new Block(); // inherit Block prototype
        return Inflationindexleg;
    }
});
