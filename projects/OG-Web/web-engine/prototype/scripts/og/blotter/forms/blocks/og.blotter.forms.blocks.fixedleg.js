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
            var block = this, id = og.common.id('attributes'), form = config.form, data = config.data,
                leg = config.leg, ui = og.common.util.ui;
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                extras: {rate: data.rate, notional: data.notional, index: config.index, leg: leg},
                children : [
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: leg + "currency"}
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.daycountconventions', 
                        index: leg + 'dayCount',
                        value: data.dayCount, placeholder: 'Select Day Count'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', 
                        index: leg + 'frequency',
                        value: data.frequency, placeholder: 'Select Frequency'
                    })
                ]
            });
        };
        Fixedleg.prototype = new Block(); // inherit Block prototype
        return Fixedleg;
    }
});