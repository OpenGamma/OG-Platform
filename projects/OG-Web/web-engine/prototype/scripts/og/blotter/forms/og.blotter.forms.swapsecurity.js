/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.swapsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, pay_block, receive_block, pay_select, receive_select,
                pay_index = og.common.id('pay'), receive_index = og.common.id('receive'), pay_leg = 'security.payLeg.',
                receive_leg = 'security.receiveLeg.', $pay_select, $receive_select;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "SwapSecurity", regionId: 'ABC~123', externalIdBundle: "", attributes: {}},
                trade: og.blotter.util.otc_trade};}
            data.nodeId = config.portfolio.id;
            constructor.load = function () {
                constructor.title = 'Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.swap_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.payLeg.type = $pay_select.val();
                        data.security.receiveLeg.type = $receive_select.val();
                        data.security.counterparty = data.trade.counterparty;
                        data.security.attributes = {};
                        data.security.payLeg.regionId = 'ABC~123';
                        data.security.receiveLeg.regionId = 'ABC~123';
                        data.security.payLeg.notional.type = 'InterestRateNotional';
                        data.security.receiveLeg.notional.type = 'InterestRateNotional';
                        data.security.name = og.blotter.util.create_name(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_quick_entry_tash'
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_tash',
                        extras: {trade: data.security.tradeDate, maturity: data.security.maturityDate,
                            effective: data.security.effectiveDate, prefix: 'security.'}
                    }),
                    pay_select = new ui.Dropdown({
                        form: form, placeholder: 'Select Swap Type',
                        data_generator: function (handler) {handler(og.blotter.util.swap_types);}
                    }),
                    pay_block = new form.Block({content:"<div id='" + pay_index + "'></div>"}),
                    receive_select = new ui.Dropdown({
                        form: form, placeholder: 'Select Swap Type',
                        data_generator: function (handler) {handler(og.blotter.util.swap_types);}
                    }),
                    receive_block = new form.Block({content:"<div id='" + receive_index + "'></div>"}),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    $pay_select = $('#' + pay_select.id);
                    $receive_select = $('#' + receive_select.id);
                    og.blotter.util.add_datetimepicker("security.tradeDate");
                    og.blotter.util.add_datetimepicker("security.effectiveDate");
                    og.blotter.util.add_datetimepicker("security.maturityDate");
                    og.blotter.util.add_datetimepicker("trade.tradeDate");
                    og.blotter.util.add_time_picker("trade.tradeTime");
                    if(typeof data.security.payLeg != 'undefined') {
                        swap_leg({type: data.security.payLeg.type, index: pay_index, leg: pay_leg, child: 4,
                            pay_edit: true});
                        $pay_select.val(data.security.payLeg.type);
                        og.blotter.util.toggle_fixed($receive_select, data.security.payLeg.type);
                    }
                    if(typeof data.security.receiveLeg != 'undefined'){
                        swap_leg({type: data.security.receiveLeg.type, index: receive_index,leg: receive_leg,
                            child: 6, receive_edit: true});
                        $receive_select.val(data.security.receiveLeg.type);
                        og.blotter.util.toggle_fixed($pay_select, data.security.receiveLeg.type);
                    }
                });
                form.on('form:submit', function (result){
                    config.handler(result.data);
                });
                form.on('change', '#' + pay_select.id, function (event) {
                    og.blotter.util.toggle_fixed($receive_select, event.target.value);
                    swap_leg({type: event.target.value, index: pay_index, leg: pay_leg, child: 4});
                });
                form.on('change', '#' + receive_select.id,  function (event) {
                    og.blotter.util.toggle_fixed($pay_select, event.target.value);
                    swap_leg({type: event.target.value, index: receive_index, leg: receive_leg, child: 6});
                });
            };
            swap_leg = function (swap) {
                var new_block;
                if(!swap.type.length) {new_block = new form.Block({content:"<div id='" + swap.index + "'></div>"});}
                else if(!~swap.type.indexOf('Floating')){
                    new_block = new og.blotter.forms.blocks.Fixedleg({form: form, data: data, leg: swap.leg,
                        index: swap.index});
                } else {
                    new_block = new og.blotter.forms.blocks.Floatingleg({form: form, data: data, leg: swap.leg,
                        type: swap.type, index: swap.index});
                }
                new_block.html(function (html) {
                    $('#' + swap.index).replaceWith(html);
                    if(swap.receive_edit) {
                        og.blotter.util.check_checkbox(receive_leg + 'eom', data.security.receiveLeg.eom);
                        og.blotter.util.set_select(receive_leg + "notional.currency",
                            data.security.receiveLeg.notional.currency);
                    }
                    else if(swap.pay_edit) {
                        og.blotter.util.check_checkbox(pay_leg + 'eom', data.security.payLeg.eom);
                        og.blotter.util.set_select(pay_leg + "notional.currency",
                            data.security.payLeg.notional.currency);
                    }
                });
                form.children[swap.child] = new_block;
            };
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.submit_new = function () {
                delete data.id;
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});