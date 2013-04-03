/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.equityvarianceswapsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "EquityVarianceSwapSecurity", externalIdBundle: "",
                attributes: {}}, trade: og.blotter.util.otc_trade};}
            data.nodeId = config.portfolio ? config.portfolio.id : null;
            constructor.load = function () {
                constructor.title = 'Equity Variance Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.name = og.blotter.util.create_name(data);
                        og.blotter.util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.equity_variance_swap_tash',
                        extras: {notional: data.security.notional, region: data.security.regionId,
                            settlement: data.security.settlementDate, strike: data.security.strike,
                            first: data.security.firstObservationDate, last: data.security.lastObservationDate,
                            annualization: data.security.annualizationFactor
                         },
                        processor: function (data) {
                            data.security.parameterizedAsVariance =
                            og.blotter.util.get_checkbox("security.parameterizedAsVariance");
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.regions', index: 'security.regionId',
                                value: data.security.regionId, placeholder: 'Select Region ID'
                            }),
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
                form.on('form:load', function (){
                    og.blotter.util.add_date_picker('.blotter-date');
                    og.blotter.util.add_time_picker('.blotter-time');
                    og.blotter.util.set_initial_focus();
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.currency", data.security.currency);
                    og.blotter.util.check_checkbox("security.parameterizedAsVariance",
                        data.security.parameterizedAsVariance);
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