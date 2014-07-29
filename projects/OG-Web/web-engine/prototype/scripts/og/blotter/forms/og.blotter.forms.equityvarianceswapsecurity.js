/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.equityvarianceswapsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util;
            if (config.details) {
                data = config.details.data;
                data.id = config.details.data.trade.uniqueId;
            } else {
                data = {security: {type: "EquityVarianceSwapSecurity", externalIdBundle: "",
                    attributes: {}}, trade: util.otc_trade};
            }
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'Equity Variance Swap';
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
                        module: 'og.blotter.forms.blocks.equity_variance_swap_tash',
                        extras: {notional: data.security.notional, region: data.security.regionId,
                            settlement: data.security.settlementDate, strike: data.security.strike,
                            first: data.security.firstObservationDate, last: data.security.lastObservationDate,
                            annualization: data.security.annualizationFactor },
                        processor: function (data) { data.security.parameterizedAsVariance =
                            util.get_checkbox("security.parameterizedAsVariance"); },
                        children: [
                            new form.Block({module: 'og.views.forms.currency_tash',
                                extras: {name: "security.currency"}}),
                            new og.blotter.forms.blocks.Regions({name: 'security.regionId',
                                value: data.security.regionId, form: form}),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Spot Underlying ID", security: data.security.spotUnderlyingId,
                                index: "security.spotUnderlyingId"
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'security.observationFrequency',
                                value: data.security.observationFrequency, placeholder: 'Frequency'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function () {
                    util.add_date_picker('.blotter-date');
                    util.add_time_picker('.blotter-time');
                    util.set_initial_focus();
                    if (data.security.length) {
                        return;
                    }
                    util.set_select("security.currency", data.security.currency);
                    util.check_checkbox("security.parameterizedAsVariance",
                        data.security.parameterizedAsVariance);
                });
                form.on('form:submit', function (result) {
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