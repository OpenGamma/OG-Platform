/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fxbarrieroptionsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "FXBarrierOptionSecurity", externalIdBundle: "", attributes: {}}, 
                trade: util.otc_trade};}
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'FX Barrier Option';
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
                        extras: {call: data.security.callAmount, put: data.security.putAmount,
                            strike: data.security.strike},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: 'security.putCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: 'security.callCurrency'}})
                        ]
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.barrier_tash',
                        extras: {date: data.security.settlementDate, level: data.security.barrierLevel,
                            expiry: data.security.expiry},
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.barrierdirections', index: 'security.barrierDirection',
                                value: data.security.barrierDirection, placeholder: 'Select Direction'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.barriertypes', index: 'security.barrierType',
                                value: data.security.barrierType, placeholder: 'Select Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.monitoringtype', index: 'security.monitoringType',
                                value: data.security.monitoringType, placeholder: 'Select Monitoring Type'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.samplingfrequencies',
                                index: 'security.samplingFrequency', value: data.security.samplingFrequency,
                                placeholder: 'Select Sampling Frequency'
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