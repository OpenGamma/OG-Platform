/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fungibletrade',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, data,
            details = {}, ids = {}, util = og.blotter.util, 
            dropdown = '.og-blotter-security-select';
            details.selector = '.og-blocks-fungible-details';
            ids.selector = '.og-blocks-fungible-security-ids';
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {trade: og.blotter.util.fungible_trade};}
            constructor.load = function () {
                constructor.title = 'Fungible Trade';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fungible_tash',
                    data: data,
                    selector: '.OG-blotter-form-block'
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty}),
                    new form.Block({module: 'og.blotter.forms.blocks.fungible_tash', 
                        extras: {quantity: data.trade.quantity},
                        children: [new og.blotter.forms.blocks.Security({
                            form: form, label: "Underlying ID", security: data.trade.securityIdBundle, 
                            index: "trade.securityIdBundle"})
                        ]
                    }),
                    details.block = new form.Block({
                        module: 'og.blotter.forms.blocks.fungible_details_tash'
                    }),                    
                    ids.block = new form.Block({
                        module: 'og.blotter.forms.blocks.fungible_security_ids_tash'
                    }),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){

                });
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data);
                });
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