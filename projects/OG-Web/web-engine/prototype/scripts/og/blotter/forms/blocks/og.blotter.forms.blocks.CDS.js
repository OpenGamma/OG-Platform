/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.cds',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var Block = og.common.util.ui.Block;
        var CDS = function (config) {
            var block = this, form = config.form, data = config.data, ui = og.common.util.ui;
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.cds_tash',
                extras: {legacy: config.legacy, standard: config.standard, stdvanilla: config.stdvanilla},
                children: [
                    new og.blotter.forms.blocks.Security({
                        form: form, label: "Protection Buyer", security: data.security.protectionBuyer,
                        index: "security.protectionBuyer"
                    }),
                    new og.blotter.forms.blocks.Security({
                        form: form, label: "Protection Seller", security: data.security.protectionSeller,
                        index: "security.protectionSeller"
                    }),
                    new og.blotter.forms.blocks.Security({
                        form: form, label: "Reference Entity", security: data.security.referenceEntity,
                        index: "security.referenceEntity"
                    }),
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{}
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.regions', index:  'regionId',
                        value: data.regionId, placeholder: 'Select Region ID'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.daycountconventions', index: 'dayCount',
                        value: data.dayCount, placeholder: 'Select Day Count'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', index:  'couponFrequency',
                        value: data.couponFrequency, placeholder: 'Select Frequency'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.businessdayconventions', 
                        index:  'businessDayConvention', value: data.businessDayConvention, 
                        placeholder: 'Select Business Day Convention'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.restructuringclause', 
                        index:  'restructuringClause', value: data.restructuringClause, 
                        placeholder: 'Select Restructuring Clause'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.debtseniority', 
                        index:  'debtSeniority', value: data.debtSeniority, 
                        placeholder: 'Select Debt Seniority'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.stubtype', 
                        index:  'stubType', value: data.stubType, 
                        placeholder: 'Select Stub Type'
                    })
                ]
            });
        };
        CDS.prototype = new Block(); // inherit Block prototype
        return CDS;
    }
});