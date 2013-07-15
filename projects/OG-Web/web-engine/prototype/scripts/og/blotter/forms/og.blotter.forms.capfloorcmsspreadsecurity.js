/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.capfloorcmsspreadsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util =  og.blotter.util;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "CapFloorCMSSpreadSecurity", externalIdBundle: "", attributes: {}}, 
                trade: util.otc_trade};}
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'Cap/Floor CMS Spread';
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
                        portfolio: data.nodeId, trade: data.trade, name: data.security.name}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.cap_floor_cms_tash',
                        extras: {start: data.security.startDate, maturity: data.security.maturityDate,
                            notional: data.security.notional,strike: data.security.strike
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Long Underlying ID", security: data.security.longId,
                                index: "security.longId"
                            }),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Short Underlying ID", security: data.security.shortId,
                                index: "security.shortId"
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'security.frequency',
                                value: data.security.frequency, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: 'security.dayCount',
                                value: data.security.dayCount, placeholder: 'Select Day Count'
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
                    util.set_select("security.currency", data.security.currency);
                    util.check_radio("security.cap", data.security.cap);
                    util.check_radio("security.payer", data.security.payer);
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