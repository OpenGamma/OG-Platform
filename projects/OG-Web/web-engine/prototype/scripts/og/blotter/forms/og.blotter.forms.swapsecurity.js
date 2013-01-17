/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.swapsecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, floating = "floatingspreadirleg." ,
            fixed = "fixedinterestrateleg.";
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {security: {type: "SwapSecurity", name: "SwapSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};} 
            constructor.load = function () {
                constructor.title = 'Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.swap_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_quick_entry_tash'
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_tash',
                        extras: {trade: data.security.tradeDate, maturity: data.security.maturityDate, 
                            effective: data.security.effectiveDate}
                    }),
                    new og.blotter.forms.blocks.Fixedleg({form: form, data: data, leg: 'recieveLeg.'}),
                    new og.blotter.forms.blocks.Floatingleg({form: form, data: data, leg: 'pagLeg.'}),
                    new og.common.util.ui.Attributes({form: form, attributes: data.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.tradeDate");
                    og.blotter.util.add_datetimepicker("security.effectiveDate");
                    og.blotter.util.add_datetimepicker("security.maturityDate");
                    if(data.length) return;
                    og.blotter.util.check_checkbox(floating + 'eom', data.eom);
                    og.blotter.util.check_checkbox(fixed + 'eom', data.eom);
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