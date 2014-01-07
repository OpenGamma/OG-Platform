/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.creditdefaultswapoptionsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util, cds_select,
                cds_id = 'blotter-cds-block', prefix = 'underlying';
            if (config.details) {
                data = config.details.data;
                data.id = config.details.data.trade.uniqueId;
            } else {
                data = {security: {type: 'CreditDefaultSwapOptionSecurity', externalIdBundle: "", attributes: {}},
                    trade: util.otc_trade, underlying: {attributes: {}}};
            }
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'CDS Option';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.cds_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.buy = data.underlying.buy;
                        data.security.protectionBuyer = data.underlying.protectionBuyer;
                        data.security.protectionSeller = data.underlying.protectionSeller;
                        data.security.currency = data.underlying.notional.currency;
                        data.security.notional = data.underlying.notional.amount;
                        data.underlying.type = $('#' + cds_select.id).val();
                        data.underlying.name = util.create_underlying_name(data);
                        data.security.name = util.create_cds_name(data);
                        data.security.maturityDate = data.underlying.startDate;
                        util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade, name: data.security.name}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.cds_option_tash',
                        extras: {strike: data.security.strike, startDate: data.security.startDate},
                        children: [ new ui.Dropdown({ form: form, resource: 'blotter.exercisetypes', 
                            index: 'security.exerciseType', value: data.security.exerciseType, 
                            placeholder: 'Select Exercise Type'})]
                    }),
                    cds_select = new ui.Dropdown({
                        form: form, placeholder: 'Select CDS Type',
                        data_generator: function (handler) {handler(util.cds_types);}
                    }),
                    cds_block = new form.Block({content:"<div id='" + cds_id + "'></div>"}),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    util.add_date_picker('.blotter-date');
                    util.add_time_picker('.blotter-time');
                    util.set_initial_focus();
                    if (data.underlying.type) {
                        var $cds_select = $('#' + cds_select.id), type = data.underlying.type;
                        swap_cds({type: type});
                        $cds_select.val(type);
                        $cds_select.prop("disabled", true);
                    }
                    if (data.security.length) return;
                    util.check_radio('security.payer', data.security.payer);
                    util.check_radio('security.knockOut', data.security.knockOut);
                });
                form.on('form:submit', function (result){
                    $.when(config.handler(result.data)).then(validate);
                });
                form.on('change', '#' + cds_select.id, function (event) {
                    swap_cds({type: event.target.value});
                });
            };
            swap_cds = function (cds) {
                var new_block;
                if (!cds.type.length) {
                    new_block = new form.Block({content: "<div id='" + cds_id + "'></div>"});
                } else {
                    new_block = new og.blotter.forms.blocks.cds({form: form, data: data, prefix: prefix,
                                                                    type: cds.type});
                }
                new_block.html(function (html) {
                    $('#' + cds_id).replaceWith(html);
                    util.set_cds_data(prefix, data);
                });
                form.children[2] = new_block;
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