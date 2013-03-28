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
                extras: {data: data.security, legacy: config.legacy, standard: config.standard, stdvanilla: config.stdvanilla},
                children: [
                    new og.blotter.forms.blocks.Security({
                        form: form, label: 'Protection Buyer', security: data.security.protectionBuyer,
                        index: 'security.protectionBuyer'
                    }),
                    new og.blotter.forms.blocks.Security({
                        form: form, label: 'Protection Seller', security: data.security.protectionSeller,
                        index: 'security.protectionSeller'
                    }),
                    new og.blotter.forms.blocks.Security({
                        form: form, label: 'Reference Entity', security: data.security.referenceEntity,
                        index: 'security.referenceEntity'
                    }),
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: 'security.notional.currency'}
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.regions', index: 'security.regionId',
                        value: data.security.regionId, placeholder: 'Select Region ID'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.daycountconventions', index: 'security.dayCount',
                        value: data.security.dayCount, placeholder: 'Select Day Count'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', index:  'security.couponFrequency',
                        value: data.security.couponFrequency, placeholder: 'Select Frequency'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.businessdayconventions', 
                        index:  'security.businessDayConvention', value: data.security.businessDayConvention, 
                        placeholder: 'Select Business Day Convention'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.restructuringclause', 
                        index:  'security.restructuringClause', value: data.security.restructuringClause, 
                        placeholder: 'Select Restructuring Clause'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.debtseniority', 
                        index:  'security.debtSeniority', value: data.security.debtSeniority, 
                        placeholder: 'Select Debt Seniority'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.stubtype', 
                        index:  'security.stubType', value: data.security.stubType, 
                        placeholder: 'Select Stub Type'
                    }),
                    new form.Block({module:'og.views.forms.currency_tash', 
                        extras:{name: 'security.upfrontAmount.currency'}
                    })
                ],
                processor: function (data) {
                    var sec = data.security;
                    sec.includeAccruedPremium = og.blotter.util.get_checkbox('security.includeAccruedPremium');
                    sec.protectionStart = og.blotter.util.get_checkbox('security.protectionStart');
                    sec.adjustMaturityDate = og.blotter.util.get_checkbox('security.adjustMaturityDate');
                    sec.adjustEffectiveDate = og.blotter.util.get_checkbox('security.adjustEffectiveDate');
                    sec.immAdjustMaturityDate = og.blotter.util.get_checkbox('security.immAdjustMaturityDate');
                    sec.adjustCashSettlementDate = og.blotter.util.get_checkbox('security.adjustCashSettlementDate');
                    sec.notional.type = 'InterestRateNotional';
                    if (config.standard)
                        sec.upfrontAmount.type = 'InterestRateNotional';   
                }
            });
        };
        CDS.prototype = new Block(); // inherit Block prototype
        return CDS;
    }
});