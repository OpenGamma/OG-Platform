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
            var block = this, form = config.form, data = config.data, ui = og.common.util.ui, buy, sell, ref,
                prefix = config.prefix ? config.prefix : 'security', util = og.blotter.util, regions_block,
                standard = util.is_standard_cds(config.type), legacy = util.is_legacy_cds(config.type),
                recrate = util.is_recrate_cds(config.type), stdvanilla = util.is_stdvanilla_cds(config.type),
                index = util.is_index_cds(config.type),
                children = [
                    buy = new og.blotter.forms.blocks.Security({ form: form, label: 'Protection Buyer',
                        security: data[prefix].protectionBuyer, index: prefix + '.protectionBuyer'
                    }),
                    sell = new og.blotter.forms.blocks.Security({ form: form, label: 'Protection Seller',
                        security: data[prefix].protectionSeller, index: prefix + '.protectionSeller'
                    }),
                    ref = new og.blotter.forms.blocks.Security({index: prefix + '.referenceEntity',
                        form: form, label: 'Reference Entity', security: data[prefix].referenceEntity
                    }),
                    new form.Block({module: 'og.views.forms.currency_tash',
                        extras: { name: prefix + '.notional.currency'}
                    }),
                    regions_block = new og.blotter.forms.blocks.Regions({name: prefix + '.regionId',
                        value: data[prefix].regionId, form: form
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.daycountconventions', index: prefix + '.dayCount',
                        value: data[prefix].dayCount, placeholder: 'Select Day Count'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.frequencies', index:  prefix + '.couponFrequency',
                        value: data[prefix].couponFrequency, placeholder: 'Select Frequency'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.businessdayconventions',
                        index:  prefix + '.businessDayConvention', value: data[prefix].businessDayConvention,
                        placeholder: 'Select Business Day Convention'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.restructuringclause',
                        index:  prefix + '.restructuringClause', value: data[prefix].restructuringClause,
                        placeholder: 'Select Restructuring Clause'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.debtseniority',
                        index:  prefix + '.debtSeniority', value: data[prefix].debtSeniority,
                        placeholder: 'Select Debt Seniority'
                    }),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.stubtype',
                        index:  prefix + '.stubType', value: data[prefix].stubType,
                        placeholder: 'Select Stub Type'
                    }),
                    new form.Block({module: 'og.views.forms.currency_tash',
                        extras:{name: prefix + '.upfrontAmount.currency'}
                    })
                ];
            if (index) {
                children.push(new form.Block({module: 'og.views.forms.currency_tash',
                    extras: {name: prefix + '.upfrontPayment.currency'}}));
            }
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.cds_tash',
                extras: {data: data[prefix], legacy: legacy, standard: standard, recrate: recrate,
                    stdvanilla: stdvanilla, index: index, prefix: prefix},
                children: children,
                processor: function (data) {
                    var sec = data[prefix];
                    sec.includeAccruedPremium = util.get_checkbox(prefix + '.includeAccruedPremium');
                    sec.protectionStart = util.get_checkbox(prefix + '.protectionStart');
                    sec.adjustMaturityDate = util.get_checkbox(prefix + '.adjustMaturityDate');
                    sec.adjustEffectiveDate = util.get_checkbox(prefix + '.adjustEffectiveDate');
                    sec.immAdjustMaturityDate = util.get_checkbox(prefix + '.immAdjustMaturityDate');
                    sec.adjustCashSettlementDate = util.get_checkbox(prefix + '.adjustCashSettlementDate');
                    sec.adjustSettlementDate = util.get_checkbox(prefix + '.adjustSettlementDate');
                    sec.notional.type = 'InterestRateNotional';
                    if (standard) {
                        sec.upfrontAmount.type = 'InterestRateNotional';
                    }
                    if (index) {
                        sec.upfrontPayment.type = 'InterestRateNotional';
                    }
                },
                /*
                 * The generator is needed here to enable the creation of the autocomplete securities
                 * The security blocks create the autocomplete on form load which does not happen
                 * when using the cds options. As the generator exists, the default handler is needed
                 */
                generator: function (handler, tmpl, data) {
                    handler(tmpl(data));
                    if (config.prefix) {
                        buy.create_autocomplete();
                        sell.create_autocomplete();
                        ref.create_autocomplete();
                        regions_block.create_autocomplete();
                    }
                }
            });
            form.on('form:load', function () {
                util.set_cds_data(prefix, data);
            });
        };
        CDS.prototype = new Block(); // inherit Block prototype
        return CDS;
    }
});