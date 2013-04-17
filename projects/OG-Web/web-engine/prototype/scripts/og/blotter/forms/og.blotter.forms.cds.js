/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.cds',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util, cds_select;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: config.type, externalIdBundle: "", attributes: {}}, 
                trade: util.otc_trade};}
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = config.title;
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.name = util.create_name(data);
                        util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade}),
                    new og.blotter.forms.blocks.cds({
                        form: form, data: data, standard: config.standard, stdvanilla: config.stdvanilla, 
                        legacy: config.legacy, index: config.index}),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    util.add_date_picker('.blotter-date');
                    util.add_time_picker('.blotter-time');
                    util.set_initial_focus();
                    if(data.security.length) return;
                    util.check_radio("security.buy", data.security.buy);
                    util.check_checkbox("security.protectionStart", data.security.protectionStart);
                    util.check_checkbox("security.includeAccruedPremium", data.security.includeAccruedPremium);
                    util.check_checkbox("security.adjustEffectiveDate", data.security.adjustEffectiveDate);
                    util.check_checkbox("security.adjustCashSettlementDate", data.security.adjustCashSettlementDate);
                    util.check_checkbox("security.adjustMaturityDate", data.security.adjustMaturityDate);
                    util.check_checkbox("security.immAdjustMaturityDate", data.security.immAdjustMaturityDate);
                    util.check_checkbox("security.adjustSettlementDate", data.security.adjustSettlementDate);
                    if(data.security.notional)
                        util.set_select("security.notional.currency", data.security.notional.currency);
                    if(data.security.upfrontPayment)
                        util.set_select("security.upfrontPayment.currency", data.security.upfrontPayment.currency);
                    if(data.security.upfrontAmount)
                        util.set_select("security.upfrontAmount.currency", data.security.upfrontAmount.currency);
                });
                form.on('form:submit', function (result){
                    $.when(config.handler(result.data)).then(validate);
                });
            };
            constructor.load();
            constructor.submit = function (handler) {
                validate = handler;
                form.submit();
            };
            constructor.submit_new = function (handler) {
                validate = handler;
                delete data.id;
                form.submit();
            };
        };
    }
});