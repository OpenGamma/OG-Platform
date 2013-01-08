/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fxforwardsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data = config || {};
            if(config) {data = config;  data.id = config.trade.uniqueId;}
            else {data = {security: {type: "FXForwardSecurity", name: "FXForwardSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.manageable_trade};}
            constructor.load = function () {
                constructor.title = 'FX Forward';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_forward_tash',
                        extras: {pay: data.security.payAmount, receive: data.security.receiveAmount},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', 
                                extras:{name: 'security.payCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', 
                                extras:{name: 'security.receiveCurrency'}})
                        ]
                    }),                    
                    new og.common.util.ui.Attributes({form: form, attributes: data.security.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.forwardDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.receiveCurrency", data.security.receiveCurrency);
                    og.blotter.util.set_select("security.payCurrency", data.security.payCurrency);
                    og.blotter.util.set_datetime("security.forwardDate", data.security.forwardDate);
                });
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data).pipe(/*console.log*/);
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