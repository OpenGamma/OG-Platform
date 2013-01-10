/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fxbarrieroptionsecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui;
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {security: {type: "FXBarrierOptionSecurity", name: "FXBarrierOptionSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};}
            constructor.load = function () {
                constructor.title = 'FX Barrier Option';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
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
                    attr = new og.common.util.ui.Attributes({
                        form: form, attributes: data.security.attributes, index : "security.attributes"
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.expiry");
                    og.blotter.util.add_datetimepicker("security.settlementDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.putCurrency", data.security.putCurrency);
                    og.blotter.util.set_select("security.callCurrency", data.security.callCurrency);
                    og.blotter.util.check_radio("security.longShort", data.security.longShort);
                });
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data);
                });
            }; 
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});