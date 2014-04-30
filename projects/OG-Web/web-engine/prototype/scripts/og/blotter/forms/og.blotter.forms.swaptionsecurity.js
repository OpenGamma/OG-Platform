/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.swaptionsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, pay_block, receive_block, pay_select,
                receive_select, pay_index = og.common.id('pay'), receive_index = og.common.id('receive'), validate,
                pay_leg = 'underlying.payLeg.', receive_leg = 'underlying.receiveLeg.', $pay_select, $receive_select,
                util =  og.blotter.util;
            if (config.details) {
                data = config.details.data;
                data.id = config.details.data.trade.uniqueId;
            } else {
                data = {underlying: {type: "SwapSecurity", externalIdBundle: "", attributes: {}},
                    trade: util.otc_trade, security: {type: "SwaptionSecurity",
                    externalIdBundle: ""}};
            }
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'Swaption';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.swaption_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.underlying.payLeg.type = $pay_select.val();
                        data.underlying.receiveLeg.type = $receive_select.val();
                        data.underlying.counterparty = data.trade.counterparty;
                        data.underlying.attributes = {};
                        data.security.attributes = {};
                        data.underlying.payLeg.notional.type = 'InterestRateNotional';
                        data.underlying.receiveLeg.notional.type = 'InterestRateNotional';
                        data.underlying.name = util.create_underlying_name(data);
                        data.security.name = util.create_name(data);
                        data.underlying.tradeDate = data.trade.tradeDate;
                        data.underlying.exchangeInitialNotional = util.get_checkbox('underlying.exchangeInitialNotional');
                        data.underlying.exchangeFinalNotional = util.get_checkbox('underlying.exchangeFinalNotional');
                        util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade, name: data.security.name}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_quick_entry_tash'
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.long_short_tash'
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swaption_details_tash',
                        extras: {notional: data.security.notional, expiry: data.security.expiry,
                            settlement: data.security.settlementDate},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.exercisetypes', index: 'security.exerciseType',
                                value: data.security.exerciseType, placeholder: 'Select Exercise Type'
                            })
                        ]
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_tash',
                        extras: {trade: data.underlying.tradeDate, maturity: data.underlying.maturityDate,
                            effective: data.underlying.effectiveDate, prefix: 'underlying.'}
                    }),
                    pay_select = new ui.Dropdown({
                        form: form, placeholder: 'Select Swap Type',
                        data_generator: function (handler) {handler(util.swap_types);}
                    }),
                    pay_block = new form.Block({content: "<div id='" + pay_index + "'></div>"}),
                    receive_select = new ui.Dropdown({
                        form: form, placeholder: 'Select Swap Type',
                        data_generator: function (handler) {handler(util.swap_types);}
                    }),
                    receive_block = new form.Block({content: "<div id='" + receive_index + "'></div>"}),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function () {
                    $pay_select = $('#' + pay_select.id);
                    $receive_select = $('#' + receive_select.id);
                    util.add_date_picker('.blotter-date');
                    util.add_time_picker('.blotter-time');
                    util.set_initial_focus();
                    util.set_select("security.currency", data.security.currency);
                    util.check_radio("security.payer", data.security.payer);
                    util.check_radio("security.cashSettled", data.security.cashSettled);
                    util.check_radio("security.longShort", data.security.longShort);
                    util.check_checkbox('underlying.exchangeInitialNotional', data.underlying.exchangeInitialNotional);
                    util.check_checkbox('underlying.exchangeFinalNotional', data.underlying.exchangeFinalNotional);
                    if (typeof data.underlying.payLeg != 'undefined') {
                        swap_leg({type: data.underlying.payLeg.type, index: pay_index, leg: pay_leg, child: 6,
                            pay_edit: true});
                        $pay_select.val(data.underlying.payLeg.type);
                    }
                    if (typeof data.underlying.receiveLeg != 'undefined'){
                        swap_leg({type: data.underlying.receiveLeg.type, index: receive_index, leg: receive_leg,
                            child: 8, receive_edit: true});
                        $receive_select.val(data.underlying.receiveLeg.type);
                    }
                });
                form.on('form:submit', function (result) {
                    $.when(config.handler(result.data)).then(validate);
                });
                form.on('change', '#' + pay_select.id, function (event) {
                    swap_leg({type: event.target.value, index: pay_index, leg: pay_leg, child: 6});
                });
                form.on('change', '#' + receive_select.id,  function (event) {
                    swap_leg({type: event.target.value, index: receive_index, leg: receive_leg, child: 8});
                });
            };
            swap_leg = function (swap) {
                var new_block;
                if(!swap.type.length) {
                    new_block = new form.Block({content: "<div id='" + swap.index + "'></div>"});
                } else if (!~swap.type.indexOf('Floating')) {
                    new_block = new og.blotter.forms.blocks.Fixedleg({form: form, data: data, leg: swap.leg,
                        index: swap.index});
                } else {
                    new_block = new og.blotter.forms.blocks.Floatingleg({form: form, data: data, leg: swap.leg,
                        type: swap.type, index: swap.index});
                }
                new_block.html(function (html) {
                    $('#' + swap.index).replaceWith(html);
                    if (swap.receive_edit) {
                        util.check_checkbox(receive_leg + 'eom', data.underlying.receiveLeg.eom);
                        util.set_select(receive_leg + "notional.currency",
                            data.underlying.receiveLeg.notional.currency);
                    } else if (swap.pay_edit) {
                        util.check_checkbox(pay_leg + 'eom', data.underlying.payLeg.eom);
                        util.set_select(pay_leg + "notional.currency",
                            data.underlying.payLeg.notional.currency);
                    }
                });
                form.children[swap.child] = new_block;
            };
            constructor.load();
            constructor.submit = function (handler) {
                validate = handler;
                form.submit();
            };
            constructor.submit_new = function (handler) {
                validate = handler;
                util.clear_save_as(data);
                util.clear_underlying_save_as(data);
                form.submit();
            };
        };
    }
});
