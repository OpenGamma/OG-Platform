/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.frasecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui;
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {security: {type: "FRASecurity", name: "FRASecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};}
            constructor.load = function () {
                constructor.title = 'Forward Rate Agreement';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.forward_rate_agreement_tash',
                        extras: {start: data.security.startDate, end: data.security.endDate, 
                            fixing:data.security.fixingDate, underlyingId: data.security.underlyingId, 
                            region: data.security.regionId, amount: data.security.amount, rate: data.security.rate
                        },
                        children: [new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}})
                        ],
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.security.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.fixingDate");
                    og.blotter.util.add_datetimepicker("security.endDate");
                    og.blotter.util.add_datetimepicker("security.startDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.currency", data.security.currency);
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