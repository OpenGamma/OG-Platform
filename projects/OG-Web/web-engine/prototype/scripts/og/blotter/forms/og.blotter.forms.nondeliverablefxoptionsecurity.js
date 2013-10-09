/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.nondeliverablefxoptionsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util;
            if (config.details) {
                data = config.details.data;
                data.id = config.details.data.trade.uniqueId;
            } else {
                data = {security: {type: "NonDeliverableFXOptionSecurity", externalIdBundle: "", attributes: {}},
                    trade: util.otc_trade};
            }
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'Non-Deliverable FX Option';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.name = util.create_name(data);
                        util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade, name: data.security.name}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.long_short_tash'
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_value_tash',
                        extras: {put: data.security.putAmount, call: data.security.callAmount},
                        children: [
                            new form.Block({module: 'og.views.forms.currency_tash',
                                extras: {name: 'security.putCurrency'}}),
                            new form.Block({module: 'og.views.forms.currency_tash',
                                extras: {name: 'security.callCurrency'}})
                        ]
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_date_tash',
                        extras: {nondev:true, expiry: data.security.expiry, settlement: data.security.settlementDate},
                        processor: function (data) {
                            data.security.deliveryInCallCurrency =
                            util.get_checkbox("security.deliveryInCallCurrency");
                        },
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.exercisetypes', index: 'security.exerciseType',
                                value: data.security.exerciseType, placeholder: 'Select Exercise Type'
                            })
                        ]
                    }),
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
                    util.set_select("security.putCurrency", data.security.putCurrency);
                    util.set_select("security.callCurrency", data.security.callCurrency);
                    util.check_radio("security.longShort", data.security.longShort);
                    util.check_checkbox("security.deliveryInCallCurrency",
                        data.security.deliveryInCallCurrency);
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
                util.clear_save_as(data);
                form.submit();
            };
        };
    }
});