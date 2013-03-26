/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.CDS',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var CDS = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form,
                ui = og.common.util.ui;
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.cds_tash',
                extras: {},
                children: [
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{}
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.regions', index:  leg + 'regionId',
                        value: data.regionId, placeholder: 'Select Region ID'
                    }),
                    security_block = new og.blotter.forms.blocks.Security({
                        form: form, label: "Short Underlying ID", security: data.floatingReferenceRateId,
                        index: leg + "floatingReferenceRateId"
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
                        index:  leg + 'floatingRateType',
                        value: data.floatingRateType, placeholder: 'Select Floating Rate Type'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', index:  leg + 'offsetFixing',
                        value: data.offsetFixing, placeholder: 'Select Offset Fixing'
                    })
                ],
                processor: function (data) {

                },
                generator: function (handler, tmpl, data) {

                }
            });
        };
        CDS.prototype = new Block(); // inherit Block prototype
        return CDS;
    }
});